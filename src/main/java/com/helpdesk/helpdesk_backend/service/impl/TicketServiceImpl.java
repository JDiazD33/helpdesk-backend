package com.helpdesk.helpdesk_backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
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
import com.helpdesk.helpdesk_backend.service.TicketService;

@Service
@Transactional
public class TicketServiceImpl implements TicketService{
    
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
    public Optional<Ticket> buscarPorId(Long id) {
        return ticketRepository.findById(id);
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
     * Actualiza un ticket existente.
     * También resuelve las referencias de entidades anidadas.
     */
    @Override
    public Ticket actualizar(Long id, Ticket ticket) {
        Ticket ticketExistente = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con id: " + id));

        ticketExistente.setTitulo(ticket.getTitulo());
        ticketExistente.setDescripcion(ticket.getDescripcion());
        ticketExistente.setTelefonoReportante(ticket.getTelefonoReportante());
        ticketExistente.setCorreoReportante(ticket.getCorreoReportante());

        if (ticket.getEstado() != null) {
            ticketExistente.setEstado(ticket.getEstado());
        }
        if (ticket.getPrioridad() != null) {
            ticketExistente.setPrioridad(ticket.getPrioridad());
        }

        ticketExistente.setJustificacionCierre(ticket.getJustificacionCierre());
        ticketExistente.setImagenCierre(ticket.getImagenCierre());

        // Resolver referencias de entidades anidadas
        if (ticket.getCliente() != null && ticket.getCliente().getId() != null) {
            ticketExistente.setCliente(usuarioRepository.getReferenceById(ticket.getCliente().getId()));
        }
        if (ticket.getAgenteAsignado() != null && ticket.getAgenteAsignado().getId() != null) {
            ticketExistente.setAgenteAsignado(usuarioRepository.getReferenceById(ticket.getAgenteAsignado().getId()));
        }
        if (ticket.getCategoria() != null && ticket.getCategoria().getId() != null) {
            ticketExistente.setCategoria(categoriaRepository.getReferenceById(ticket.getCategoria().getId()));
        }
        if (ticket.getProblema() != null && ticket.getProblema().getId() != null) {
            ticketExistente.setProblema(problemaRepository.getReferenceById(ticket.getProblema().getId()));
        }
        if (ticket.getEmpresa() != null && ticket.getEmpresa().getId() != null) {
            ticketExistente.setEmpresa(empresaRepository.getReferenceById(ticket.getEmpresa().getId()));
        }

        return ticketRepository.save(ticketExistente);
    }

    @Override
    public void eliminar(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ticket no encontrado con id: " + id);
        }
        comentarioRepository.deleteByTicketId(id);
        ticketRepository.deleteById(id);
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
}
