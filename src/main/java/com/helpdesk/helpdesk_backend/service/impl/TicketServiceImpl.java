package com.helpdesk.helpdesk_backend.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.dto.CambiarEstadoRequestDTO;
import com.helpdesk.helpdesk_backend.dto.CalificacionRequestDTO;
import com.helpdesk.helpdesk_backend.dto.CierreRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketAnonimoRequestDTO;
import com.helpdesk.helpdesk_backend.model.CategoriaTicket;
import com.helpdesk.helpdesk_backend.model.ProblemaTicket;
import com.helpdesk.helpdesk_backend.model.Ticket;
import com.helpdesk.helpdesk_backend.model.TicketComentario;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;
import com.helpdesk.helpdesk_backend.model.enums.PrioridadTicket;
import com.helpdesk.helpdesk_backend.repository.CategoriaTicketRepository;
import com.helpdesk.helpdesk_backend.repository.EmpresaRepository;
import com.helpdesk.helpdesk_backend.repository.ProblemaTicketRepository;
import com.helpdesk.helpdesk_backend.repository.TicketComentarioRepository;
import com.helpdesk.helpdesk_backend.repository.TicketRepository;
import com.helpdesk.helpdesk_backend.repository.UsuarioRepository;
import com.helpdesk.helpdesk_backend.security.SecurityUtils;
import com.helpdesk.helpdesk_backend.service.TicketService;

@Service
@Transactional
public class TicketServiceImpl implements TicketService{

    /**
     * Máquina de estados del ticket. Define qué transiciones son válidas.
     * CERRADO es terminal: no se puede sacar un ticket de CERRADO.
     * Las transiciones al mismo estado se tratan como no-op (no se generan
     * comentarios del sistema) y nunca lanzan excepción.
     */
    private static final Map<EstadoTicket, Set<EstadoTicket>> TRANSICIONES_PERMITIDAS;

    static {
        Map<EstadoTicket, Set<EstadoTicket>> map = new EnumMap<>(EstadoTicket.class);
        map.put(EstadoTicket.ABIERTO, EnumSet.of(
                EstadoTicket.EN_PROGRESO,
                EstadoTicket.RESUELTO,
                EstadoTicket.CERRADO));
        map.put(EstadoTicket.EN_PROGRESO, EnumSet.of(
                EstadoTicket.ABIERTO,
                EstadoTicket.RESUELTO,
                EstadoTicket.CERRADO));
        map.put(EstadoTicket.RESUELTO, EnumSet.of(
                EstadoTicket.EN_PROGRESO,
                EstadoTicket.CERRADO));
        map.put(EstadoTicket.CERRADO, EnumSet.noneOf(EstadoTicket.class));
        TRANSICIONES_PERMITIDAS = Collections.unmodifiableMap(map);
    }

    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final CategoriaTicketRepository categoriaRepository;
    private final ProblemaTicketRepository problemaRepository;
    private final TicketComentarioRepository comentarioRepository;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             UsuarioRepository usuarioRepository,
                             EmpresaRepository empresaRepository,
                             CategoriaTicketRepository categoriaRepository,
                             ProblemaTicketRepository problemaRepository,
                             TicketComentarioRepository comentarioRepository) {
        this.ticketRepository = ticketRepository;
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.categoriaRepository = categoriaRepository;
        this.problemaRepository = problemaRepository;
        this.comentarioRepository = comentarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarTodos() {
        return ticketRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarTodos(Boolean asignado) {
        if (asignado == null) return ticketRepository.findAll();
        return asignado ? ticketRepository.findAllConAgente() : ticketRepository.findAllSinAgente();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ticket> buscarPorId(Long id) {
        return ticketRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ticket> buscarPorIdAndEmpresa(Long id, Long empresaId) {
        // El ADMIN_OWNER puede consultar tickets de cualquier empresa.
        if (SecurityUtils.isAdminOwner()) {
            return ticketRepository.findById(id);
        }
        return ticketRepository.findByIdAndEmpresaId(id, empresaId);
    }

    /**
     * Crea un nuevo ticket:
     * 1. Genera código único centralizado en el service
     * 2. Asigna estado ABIERTO y prioridad MEDIA por defecto
     * 3. Resuelve las entidades anidadas con getReferenceById
     */
    @Override
    public Ticket guardar(Ticket ticket) {
        // Generar código único en el service (formato: TCK-XXXX)
        String codigo = "TCK-" + (1000 + new java.util.Random().nextInt(9000));
        ticket.setCodigo(codigo);

        // Valores por defecto
        if (ticket.getEstado() == null) {
            ticket.setEstado(EstadoTicket.ABIERTO);
        }
        if (ticket.getPrioridad() == null) {
            ticket.setPrioridad(PrioridadTicket.MEDIA);
        }

        // Resolver entidades anidadas (evita errores de entidades desconectadas)
        resolverReferencias(ticket);

        return ticketRepository.save(ticket);
    }

    @Override
    public Ticket guardarConComentarioInicial(Ticket ticket, String mensajeInicial, Long usuarioComentarioId) {
        Ticket guardado = guardar(ticket);
        // No insertamos la descripción como comentario automático: la descripción
        // del ticket ya se muestra en el detalle, y duplicarla ensucia la
        // conversación (que debe quedar reservada para el agente). Solo creamos
        // comentario si el mensaje inicial es distinto a la descripción del ticket.
        if (StringUtils.hasText(mensajeInicial)
                && !mensajeInicial.trim().equalsIgnoreCase(
                        guardado.getDescripcion() == null ? "" : guardado.getDescripcion().trim())) {
            Usuario usuario = usuarioRepository.findById(usuarioComentarioId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Usuario no encontrado con id: " + usuarioComentarioId));
            TicketComentario comentario = TicketComentario.builder()
                    .mensaje(mensajeInicial)
                    .ticket(guardado)
                    .usuario(usuario)
                    .build();
            comentarioRepository.save(comentario);
        }
        return guardado;
    }

    /**
     * Crea un ticket reportado sin iniciar sesión.
     *
     * El reportante NO está autenticado, pero su correo debe corresponder a un
     * usuario CLIENTE registrado. A partir de ese correo se identifica la empresa
     * (no se pide manualmente). Si el correo no existe, lanza 404.
     *
     * La categoría debe pertenecer a la empresa del usuario; el problema (opcional)
     * debe pertenecer a la categoría elegida. El título se auto-genera.
     */
    @Override
    public Ticket guardarAnonimo(TicketAnonimoRequestDTO request) {
        // 1. Identificar la empresa a partir del correo del usuario registrado.
        Usuario reportante = usuarioRepository.findByEmail(request.getCorreo())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No encontramos una cuenta asociada al correo: " + request.getCorreo()
                        + ". Regístrate o contacta a soporte."));

        // Solo los clientes pueden reportar por esta vía. Se rechaza a administradores
        // y agentes para evitar reportes fuera de su flujo (ellos usan el panel autenticado).
        String nombreRol = reportante.getRol() != null ? reportante.getRol().getNombre() : null;
        if (!"CLIENTE".equals(nombreRol)) {
            throw new IllegalArgumentException(
                    "El reporte público de tickets es exclusivo para clientes. "
                    + "Inicia sesión con tu cuenta para gestionar tickets.");
        }

        Long empresaId = reportante.getEmpresa().getId();

        // 2. Validar que la categoría exista, esté activa y pertenezca a esa empresa.
        CategoriaTicket categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoría no encontrada con id: " + request.getCategoriaId()));
        if (!categoria.isActiva()) {
            throw new IllegalArgumentException("La categoría seleccionada ya no está disponible.");
        }
        if (!categoria.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException(
                    "La categoría seleccionada no pertenece a la empresa del correo ingresado.");
        }

        // 3. Validar el problema (opcional): debe estar activo y pertenecer a la categoría.
        ProblemaTicket problema = null;
        if (request.getProblemaId() != null) {
            problema = problemaRepository.findById(request.getProblemaId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Problema no encontrado con id: " + request.getProblemaId()));
            if (!problema.isActivo()) {
                throw new IllegalArgumentException("El problema seleccionado ya no está disponible.");
            }
            if (!problema.getCategoria().getId().equals(categoria.getId())) {
                throw new IllegalArgumentException(
                        "El problema seleccionado no pertenece a la categoría elegida.");
            }
        }

        // 4. El título lo escribe la persona en el formulario; la prioridad también
        //    la elige (con MEDIA por defecto si llegara nula, aunque el DTO la exige).
        PrioridadTicket prioridad = request.getPrioridad() != null
                ? request.getPrioridad()
                : PrioridadTicket.MEDIA;

        // 5. Construir el ticket. Se vincula al reportante como cliente (identificado
        //    por su correo) para que el ticket aparezca en su dashboard/listado.
        Ticket ticket = Ticket.builder()
                .titulo(request.getTitulo().trim())
                .descripcion(request.getDescripcion())
                .telefonoReportante(request.getTelefono())
                .correoReportante(request.getCorreo())
                .estado(EstadoTicket.ABIERTO)
                .prioridad(prioridad)
                .empresa(empresaRepository.getReferenceById(empresaId))
                .categoria(categoria)
                .problema(problema)
                .cliente(reportante)
                .build();

        // Generar código único (mismo formato que guardar()).
        ticket.setCodigo("TCK-" + (1000 + new java.util.Random().nextInt(9000)));

        return ticketRepository.save(ticket);
    }

    /**
     * Actualiza un ticket existente, restringido al tenant del JWT.
     * - NO permite reasignar empresa ni cliente (se conservan los del registro original).
     * - NO permite cambiar el estado: para eso se debe usar el endpoint
     *   {@code PUT /api/tickets/{id}/estado}, que valida la máquina de transiciones.
     * - El resto de campos editables (titulo, descripcion, prioridad, agenteAsignado,
     *   categoria, problema, contacto) se toman del body.
     */
    @Override
    public Ticket actualizar(Long id, Long empresaId, Ticket ticket) {
        Ticket ticketExistente = obtenerTicketDeTenant(id, empresaId);

        ticketExistente.setTitulo(ticket.getTitulo());
        ticketExistente.setDescripcion(ticket.getDescripcion());
        ticketExistente.setTelefonoReportante(ticket.getTelefonoReportante());
        ticketExistente.setCorreoReportante(ticket.getCorreoReportante());

        if (ticket.getPrioridad() != null) {
            ticketExistente.setPrioridad(ticket.getPrioridad());
        }

        ticketExistente.setJustificacionCierre(ticket.getJustificacionCierre());
        ticketExistente.setImagenCierre(ticket.getImagenCierre());

        // empresa y cliente son inmutables tras la creación: se conservan los del registro.
        // Solo se reasignan agenteAsignado, categoria y problema si vienen en el body.
        if (ticket.getAgenteAsignado() != null && ticket.getAgenteAsignado().getId() != null) {
            ticketExistente.setAgenteAsignado(usuarioRepository.getReferenceById(ticket.getAgenteAsignado().getId()));
        }
        if (ticket.getCategoria() != null && ticket.getCategoria().getId() != null) {
            ticketExistente.setCategoria(categoriaRepository.getReferenceById(ticket.getCategoria().getId()));
        }
        if (ticket.getProblema() != null && ticket.getProblema().getId() != null) {
            ticketExistente.setProblema(problemaRepository.getReferenceById(ticket.getProblema().getId()));
        }

        // Si el cliente intenta enviar estado, se ignora silenciosamente.
        // El estado SOLO se modifica a través de cambiarEstado(), que valida transiciones.
        // NOTA: no se delega al endpoint /estado para no acoplar el PUT genérico
        // con la lógica de transiciones; se prefiere ignorar el campo si viene mal.

        return ticketRepository.save(ticketExistente);
    }

    @Override
    public void eliminar(Long id, Long empresaId) {
        Ticket ticket = obtenerTicketDeTenant(id, empresaId);
        comentarioRepository.deleteByTicketId(ticket.getId());
        ticketRepository.deleteById(ticket.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ticket> buscarPorCodigo(String codigo) {
        return ticketRepository.findByCodigo(codigo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarPorEmpresaId(Long empresaId) {
        return ticketRepository.findByEmpresaId(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarPorEmpresaId(Long empresaId, Boolean asignado) {
        if (asignado == null) return ticketRepository.findByEmpresaId(empresaId);
        return asignado ? ticketRepository.findByEmpresaConAgente(empresaId) : ticketRepository.findByEmpresaSinAgente(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarPorClienteId(Long clienteId) {
        return ticketRepository.findByClienteId(clienteId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarPorAgenteAsignadoId(Long agenteAsignadoId) {
        return ticketRepository.findByAgenteAsignadoId(agenteAsignadoId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarPorCategoriaId(Long categoriaId) {
        return ticketRepository.findByCategoriaId(categoriaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarPorEstado(EstadoTicket estado) {
        return ticketRepository.findByEstado(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarPorPrioridad(PrioridadTicket prioridad) {
        return ticketRepository.findByPrioridad(prioridad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarPorEmpresaIdYEstado(Long empresaId, EstadoTicket estado) {
        return ticketRepository.findByEmpresaIdAndEstado(empresaId, estado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarPorEmpresaIdYPrioridad(Long empresaId, PrioridadTicket prioridad) {
        return ticketRepository.findByEmpresaIdAndPrioridad(empresaId, prioridad);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCodigo(String codigo) {
        return ticketRepository.existsByCodigo(codigo);
    }

    // ─── Métodos con queries JPQL personalizadas ───

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarPorEmpresaConDetalles(Long empresaId) {
        return ticketRepository.findByEmpresaConDetallesOrdenado(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> filtrarTickets(Long empresaId, EstadoTicket estado, PrioridadTicket prioridad) {
        return ticketRepository.filtrarTickets(empresaId, estado, prioridad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> contarPorEstado(Long empresaId) {
        return ticketRepository.contarPorEstado(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarPorAgenteYEstado(Long agenteId, EstadoTicket estado) {
        return ticketRepository.findByAgenteYEstado(agenteId, estado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarPorEmpresaYPeriodo(Long empresaId, LocalDateTime inicio, LocalDateTime fin) {
        return ticketRepository.findByEmpresaYPeriodo(empresaId, inicio, fin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarPrioridadAltaPorEmpresa(Long empresaId) {
        return ticketRepository.findPrioridadAltaPorEmpresa(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarPrioridadAltaGlobal() {
        return ticketRepository.findPrioridadAltaGlobal();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> buscarPorTexto(Long empresaId, String texto) {
        return ticketRepository.buscarPorTexto(empresaId, texto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarSinAsignar(Long empresaId) {
        return ticketRepository.findSinAsignar(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarSinAsignarGlobal() {
        return ticketRepository.findSinAsignarGlobal();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarPorPeriodoGlobal(LocalDateTime inicio, LocalDateTime fin) {
        return ticketRepository.findByPeriodoGlobal(inicio, fin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarPorClienteConDetalles(Long clienteId, Long empresaId) {
        return ticketRepository.findByClienteConDetalles(clienteId, empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> listarActualizadosRecientemente(Long empresaId, int dias) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(dias);
        return ticketRepository.findActualizadosRecientemente(empresaId, fechaLimite);
    }

    @Override
    public Ticket cambiarEstado(Long id, Long empresaId, CambiarEstadoRequestDTO request) {
        Ticket ticket = obtenerTicketDeTenant(id, empresaId);
        // Los tickets RESUELTO o CERRADO son de solo lectura: ya no se puede
        // cambiar su estado (evita reapertura accidental o maliciosa).
        if (ticket.getEstado() == EstadoTicket.RESUELTO || ticket.getEstado() == EstadoTicket.CERRADO) {
            throw new IllegalArgumentException(
                    "No se puede cambiar el estado de un ticket " + ticket.getEstado().name()
                    + ". El ticket ya está cerrado y es de solo lectura.");
        }
        if (request.getEstado() == EstadoTicket.CERRADO
                && (request.getJustificacionCierre() == null || request.getJustificacionCierre().isBlank())) {
            throw new IllegalArgumentException("La justificacion de cierre es obligatoria para estado CERRADO");
        }
        EstadoTicket estadoAnterior = ticket.getEstado();
        validarTransicion(estadoAnterior, request.getEstado());
        ticket.setEstado(request.getEstado());
        ticket.setJustificacionCierre(request.getJustificacionCierre());
        Ticket guardado = ticketRepository.save(ticket);
        if (estadoAnterior != request.getEstado()) {
            Long usuarioId = SecurityUtils.getCurrentUserId();
            if (usuarioId != null) {
                String texto = "Estado cambiado de " + estadoAnterior.name() + " a " + request.getEstado().name();
                Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
                TicketComentario comentario = TicketComentario.builder()
                        .mensaje(texto)
                        .ticket(guardado)
                        .usuario(usuario)
                        .esSistema(true)
                        .build();
                comentarioRepository.save(comentario);
            }
        }
        return guardado;
    }

    @Override
    public Ticket asignarAgente(Long id, Long empresaId, Long agenteId) {
        Ticket ticket = obtenerTicketDeTenant(id, empresaId);
        // Los tickets RESUELTO o CERRADO son de solo lectura: no se reasigna agente.
        if (ticket.getEstado() == EstadoTicket.RESUELTO || ticket.getEstado() == EstadoTicket.CERRADO) {
            throw new IllegalArgumentException(
                    "No se puede asignar agente a un ticket " + ticket.getEstado().name()
                    + ". El ticket ya está cerrado y es de solo lectura.");
        }
        // El agente debe pertenecer a la MISMA empresa del ticket (no a la del JWT).
        // Así el owner puede asignar agentes a tickets de cualquier empresa,
        // y siempre respeta la consistencia tenant del propio ticket.
        Long empresaDelTicket = ticket.getEmpresa().getId();
        Usuario agente = usuarioRepository.findByIdAndEmpresaId(agenteId, empresaDelTicket)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Agente no encontrado o no pertenece a la empresa del ticket"));
        ticket.setAgenteAsignado(agente);
        Ticket guardado = ticketRepository.save(ticket);
        String texto = "Ticket asignado a " + agente.getNombres() + " " + agente.getApellidos();
        TicketComentario comentario = TicketComentario.builder()
                .mensaje(texto)
                .ticket(guardado)
                .usuario(agente)
                .esSistema(true)
                .build();
        comentarioRepository.save(comentario);
        return guardado;
    }

    @Override
    public Ticket guardarCierre(Long id, Long empresaId, CierreRequestDTO request) {
        Ticket ticket = obtenerTicketDeTenant(id, empresaId);
        validarTransicion(ticket.getEstado(), EstadoTicket.RESUELTO);
        ticket.setEstado(EstadoTicket.RESUELTO);
        ticket.setJustificacionCierre(request.getJustificacionCierre());
        ticket.setImagenCierre(request.getImagenCierre());
        Ticket guardado = ticketRepository.save(ticket);
        Long usuarioId = SecurityUtils.getCurrentUserId();
        if (usuarioId != null) {
            String texto = "Ticket resuelto: " + request.getJustificacionCierre();
            Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
            TicketComentario comentario = TicketComentario.builder()
                    .mensaje(texto)
                    .ticket(guardado)
                    .usuario(usuario)
                    .esSistema(true)
                    .build();
            comentarioRepository.save(comentario);
        }
        return guardado;
    }

    /**
     * El CLIENTE dueño califica (1-5 estrellas) la atención de un ticket RESUELTO.
     *
     * Validaciones:
     * - El usuario autenticado debe ser el cliente dueño del ticket.
     * - El ticket debe estar RESUELTO y tener un agente asignado.
     * - No se permite recalificar (si ya tiene calificación, se rechaza).
     */
    @Override
    public Ticket calificarTicket(Long id, Long empresaId, CalificacionRequestDTO request) {
        Ticket ticket = obtenerTicketDeTenant(id, empresaId);

        // Solo el cliente dueño puede calificar su propio ticket.
        Long usuarioActual = SecurityUtils.getCurrentUserId();
        if (usuarioActual == null || ticket.getCliente() == null
                || !usuarioActual.equals(ticket.getCliente().getId())) {
            throw new IllegalArgumentException(
                    "Solo el cliente dueño del ticket puede calificar la atención.");
        }

        if (ticket.getEstado() != EstadoTicket.RESUELTO) {
            throw new IllegalArgumentException(
                    "Solo se puede calificar un ticket que esté resuelto.");
        }

        if (ticket.getAgenteAsignado() == null) {
            throw new IllegalArgumentException(
                    "Este ticket no tiene un agente asignado que calificar.");
        }

        if (ticket.getCalificacionAgente() != null) {
            throw new IllegalArgumentException(
                    "Este ticket ya fue calificado. No es posible cambiar la calificación.");
        }

        ticket.setCalificacionAgente(request.getCalificacion());
        Ticket guardado = ticketRepository.save(ticket);

        // Comentario de sistema dejando constancia de la calificación.
        String texto = "El cliente calificó la atención con " + request.getCalificacion()
                + (request.getCalificacion() == 1 ? " estrella." : " estrellas.");
        Usuario usuario = usuarioRepository.getReferenceById(usuarioActual);
        TicketComentario comentario = TicketComentario.builder()
                .mensaje(texto)
                .ticket(guardado)
                .usuario(usuario)
                .esSistema(true)
                .build();
        comentarioRepository.save(comentario);

        return guardado;
    }

    /**
     * Ranking de mejores agentes por promedio de calificación.
     * empresaId NULL → vista global del ADMIN_OWNER.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Object[]> rankingMejoresAgentes(Long empresaId) {
        // El ADMIN_OWNER ve todas las empresas (empresaId = null). Los demás roles
        // filtran por su empresa.
        if (SecurityUtils.isAdminOwner()) {
            return ticketRepository.rankingMejoresAgentes(null);
        }
        return ticketRepository.rankingMejoresAgentes(empresaId);
    }

    /**
     * Método privado para resolver las entidades anidadas que llegan del JSON
     * con solo su ID, obteniendo las referencias reales de la BD.
     */
    private void resolverReferencias(Ticket ticket) {
        if (ticket.getCliente() != null && ticket.getCliente().getId() != null) {
            ticket.setCliente(usuarioRepository.getReferenceById(ticket.getCliente().getId()));
        }
        if (ticket.getEmpresa() != null && ticket.getEmpresa().getId() != null) {
            ticket.setEmpresa(empresaRepository.getReferenceById(ticket.getEmpresa().getId()));
        }
        if (ticket.getCategoria() != null && ticket.getCategoria().getId() != null) {
            ticket.setCategoria(categoriaRepository.getReferenceById(ticket.getCategoria().getId()));
        }
        if (ticket.getProblema() != null && ticket.getProblema().getId() != null) {
            ticket.setProblema(problemaRepository.getReferenceById(ticket.getProblema().getId()));
        }
        if (ticket.getAgenteAsignado() != null && ticket.getAgenteAsignado().getId() != null) {
            ticket.setAgenteAsignado(usuarioRepository.getReferenceById(ticket.getAgenteAsignado().getId()));
        }
    }

    /**
     * Carga un ticket validando que pertenezca a la empresa del tenant.
     * El ADMIN_OWNER (super-admin del SaaS) puede acceder a tickets de cualquier empresa.
     * Devuelve 404 (no 403) para no filtrar la existencia del recurso entre tenants.
     */
    private Ticket obtenerTicketDeTenant(Long id, Long empresaId) {
        if (SecurityUtils.isAdminOwner()) {
            return ticketRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con id: " + id));
        }
        return ticketRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con id: " + id));
    }

    /**
     * Valida que la transición de estado sea permitida por la máquina de estados.
     * Las transiciones al mismo estado se tratan como no-op (permitidas).
     * Lanza {@link IllegalArgumentException} con las alternativas válidas si no lo es.
     */
    private void validarTransicion(EstadoTicket origen, EstadoTicket destino) {
        if (origen == destino) {
            return;
        }
        Set<EstadoTicket> destinosValidos = TRANSICIONES_PERMITIDAS.getOrDefault(origen, EnumSet.noneOf(EstadoTicket.class));
        if (!destinosValidos.contains(destino)) {
            throw new IllegalArgumentException(
                    "Transicion de estado no permitida: " + origen + " -> " + destino
                            + ". Transiciones validas desde " + origen + ": " + destinosValidos);
        }
    }
}
