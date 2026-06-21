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
import com.helpdesk.helpdesk_backend.dto.CierreRequestDTO;
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
        if (StringUtils.hasText(mensajeInicial)) {
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
        Usuario agente = usuarioRepository.findByIdAndEmpresaId(agenteId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Agente no encontrado o no pertenece a la empresa con id: " + agenteId));
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
     * Devuelve 404 (no 403) para no filtrar la existencia del recurso entre tenants.
     */
    private Ticket obtenerTicketDeTenant(Long id, Long empresaId) {
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
