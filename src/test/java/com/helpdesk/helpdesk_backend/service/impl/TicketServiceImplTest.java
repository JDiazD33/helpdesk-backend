package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.model.*;
import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;
import com.helpdesk.helpdesk_backend.model.enums.PrioridadTicket;
import com.helpdesk.helpdesk_backend.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private CategoriaTicketRepository categoriaRepository;
    @Mock private ProblemaTicketRepository problemaRepository;
    @Mock private TicketComentarioRepository comentarioRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private Ticket ticket;
    private Empresa empresa;
    private Usuario cliente;
    private Usuario agente;
    private CategoriaTicket categoria;
    private ProblemaTicket problema;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().id(1L).nombre("Tech Solutions").build();
        cliente = Usuario.builder().id(1L).nombres("Juan").apellidos("Perez").email("juan@test.com").build();
        agente = Usuario.builder().id(2L).nombres("Maria").apellidos("Lopez").email("maria@test.com").build();
        categoria = CategoriaTicket.builder().id(1L).nombre("Soporte").empresa(empresa).build();
        problema = ProblemaTicket.builder().id(1L).nombre("Falla de red").categoria(categoria).build();

        ticket = Ticket.builder()
                .id(1L)
                .codigo("TCK-1001")
                .titulo("Problema de red")
                .descripcion("Sin conexión")
                .estado(EstadoTicket.ABIERTO)
                .prioridad(PrioridadTicket.ALTA)
                .cliente(cliente)
                .empresa(empresa)
                .build();
    }

    @Test
    void listarTodos_debeRetornarLista() {
        when(ticketRepository.findAll()).thenReturn(List.of(ticket));

        List<Ticket> resultado = ticketService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("TCK-1001", resultado.get(0).getCodigo());
    }

    @Test
    void buscarPorId_debeRetornarTicket() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        Optional<Ticket> resultado = ticketService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("TCK-1001", resultado.get().getCodigo());
    }

    @Test
    void buscarPorId_noExiste_debeRetornarVacio() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Ticket> resultado = ticketService.buscarPorId(99L);

        assertFalse(resultado.isPresent());
    }

    @Test
    void guardar_debeAsignarCodigoYGuardar() {
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket nuevo = Ticket.builder()
                .titulo("Nuevo ticket")
                .descripcion("Descripción")
                .cliente(cliente)
                .empresa(empresa)
                .build();

        Ticket resultado = ticketService.guardar(nuevo);

        assertNotNull(resultado);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void guardar_sinEstadoNiPrioridad_debeAsignarDefecto() {
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket nuevo = Ticket.builder()
                .titulo("Ticket sin estado")
                .descripcion("desc")
                .cliente(cliente)
                .empresa(empresa)
                .build();

        ticketService.guardar(nuevo);

        assertEquals(EstadoTicket.ABIERTO, nuevo.getEstado());
        assertEquals(PrioridadTicket.MEDIA, nuevo.getPrioridad());
    }

    @Test
    void guardar_conEstadoYPrioridad_noSobreescribe() {
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket nuevo = Ticket.builder()
                .titulo("Ticket con estado")
                .descripcion("desc")
                .estado(EstadoTicket.EN_PROGRESO)
                .prioridad(PrioridadTicket.CRITICA)
                .cliente(cliente)
                .empresa(empresa)
                .build();

        ticketService.guardar(nuevo);

        assertEquals(EstadoTicket.EN_PROGRESO, nuevo.getEstado());
        assertEquals(PrioridadTicket.CRITICA, nuevo.getPrioridad());
    }

    @Test
    void guardar_conReferencias_debeResolverlas() {
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(usuarioRepository.getReferenceById(1L)).thenReturn(cliente);
        when(empresaRepository.getReferenceById(1L)).thenReturn(empresa);
        when(categoriaRepository.getReferenceById(1L)).thenReturn(categoria);
        when(problemaRepository.getReferenceById(1L)).thenReturn(problema);
        when(usuarioRepository.getReferenceById(2L)).thenReturn(agente);

        Ticket nuevo = Ticket.builder()
                .titulo("Ticket con refs")
                .descripcion("desc")
                .cliente(cliente)
                .empresa(empresa)
                .categoria(categoria)
                .problema(problema)
                .agenteAsignado(agente)
                .build();

        ticketService.guardar(nuevo);

        verify(usuarioRepository, atLeastOnce()).getReferenceById(anyLong());
        verify(empresaRepository).getReferenceById(1L);
        verify(categoriaRepository).getReferenceById(1L);
        verify(problemaRepository).getReferenceById(1L);
    }

    @Test
    void guardarConComentarioInicial_conMensaje_debeGuardarComentario() {
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(comentarioRepository.save(any(TicketComentario.class))).thenReturn(null);

        Ticket resultado = ticketService.guardarConComentarioInicial(ticket, "Primer comentario", 1L);

        assertNotNull(resultado);
        verify(comentarioRepository).save(any(TicketComentario.class));
    }

    @Test
    void guardarConComentarioInicial_sinMensaje_noGuardaComentario() {
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        ticketService.guardarConComentarioInicial(ticket, "", 1L);

        verify(comentarioRepository, never()).save(any());
    }

    @Test
    void guardarConComentarioInicial_mensajeNull_noGuardaComentario() {
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        ticketService.guardarConComentarioInicial(ticket, null, 1L);

        verify(comentarioRepository, never()).save(any());
    }

    @Test
    void guardarConComentarioInicial_usuarioNoExiste_lanzaExcepcion() {
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                ticketService.guardarConComentarioInicial(ticket, "mensaje", 99L));
    }

    @Test
    void actualizar_debeActualizarYRetornarTicket() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket actualizado = Ticket.builder()
                .titulo("Título actualizado")
                .descripcion("Nueva descripción")
                .estado(EstadoTicket.EN_PROGRESO)
                .prioridad(PrioridadTicket.ALTA)
                .build();

        Ticket resultado = ticketService.actualizar(1L, actualizado);

        assertNotNull(resultado);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void actualizar_noExiste_lanzaExcepcion() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                ticketService.actualizar(99L, ticket));
    }

    @Test
    void actualizar_sinEstadoNiPrioridad_noSobreescribe() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket actualizado = Ticket.builder()
                .titulo("Título")
                .descripcion("desc")
                .build(); // sin estado ni prioridad

        ticketService.actualizar(1L, actualizado);

        assertEquals(EstadoTicket.ABIERTO, ticket.getEstado());
        assertEquals(PrioridadTicket.ALTA, ticket.getPrioridad());
    }

    @Test
    void actualizar_conReferencias_debeResolverlas() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(usuarioRepository.getReferenceById(1L)).thenReturn(cliente);
        when(usuarioRepository.getReferenceById(2L)).thenReturn(agente);
        when(empresaRepository.getReferenceById(1L)).thenReturn(empresa);
        when(categoriaRepository.getReferenceById(1L)).thenReturn(categoria);
        when(problemaRepository.getReferenceById(1L)).thenReturn(problema);

        Ticket actualizado = Ticket.builder()
                .titulo("Título")
                .descripcion("desc")
                .cliente(cliente)
                .agenteAsignado(agente)
                .empresa(empresa)
                .categoria(categoria)
                .problema(problema)
                .build();

        ticketService.actualizar(1L, actualizado);

        verify(usuarioRepository, atLeastOnce()).getReferenceById(anyLong());
        verify(empresaRepository).getReferenceById(1L);
        verify(categoriaRepository).getReferenceById(1L);
        verify(problemaRepository).getReferenceById(1L);
    }

    @Test
    void eliminar_debeEliminarTicket() {
        when(ticketRepository.existsById(1L)).thenReturn(true);

        ticketService.eliminar(1L);

        verify(comentarioRepository).deleteByTicketId(1L);
        verify(ticketRepository).deleteById(1L);
    }

    @Test
    void eliminar_noExiste_lanzaExcepcion() {
        when(ticketRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                ticketService.eliminar(99L));
    }

    @Test
    void buscarPorCodigo_debeRetornarTicket() {
        when(ticketRepository.findByCodigo("TCK-1001")).thenReturn(Optional.of(ticket));

        Optional<Ticket> resultado = ticketService.buscarPorCodigo("TCK-1001");

        assertTrue(resultado.isPresent());
    }

    @Test
    void listarPorEmpresaId_debeRetornarLista() {
        when(ticketRepository.findByEmpresaId(1L)).thenReturn(List.of(ticket));

        List<Ticket> resultado = ticketService.listarPorEmpresaId(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void listarPorClienteId_debeRetornarLista() {
        when(ticketRepository.findByClienteId(1L)).thenReturn(List.of(ticket));

        assertEquals(1, ticketService.listarPorClienteId(1L).size());
    }

    @Test
    void listarPorAgenteAsignadoId_debeRetornarLista() {
        when(ticketRepository.findByAgenteAsignadoId(1L)).thenReturn(List.of(ticket));

        assertEquals(1, ticketService.listarPorAgenteAsignadoId(1L).size());
    }

    @Test
    void listarPorCategoriaId_debeRetornarLista() {
        when(ticketRepository.findByCategoriaId(1L)).thenReturn(List.of(ticket));

        assertEquals(1, ticketService.listarPorCategoriaId(1L).size());
    }

    @Test
    void listarPorEstado_debeRetornarLista() {
        when(ticketRepository.findByEstado(EstadoTicket.ABIERTO)).thenReturn(List.of(ticket));

        assertEquals(1, ticketService.listarPorEstado(EstadoTicket.ABIERTO).size());
    }

    @Test
    void listarPorPrioridad_debeRetornarLista() {
        when(ticketRepository.findByPrioridad(PrioridadTicket.ALTA)).thenReturn(List.of(ticket));

        assertEquals(1, ticketService.listarPorPrioridad(PrioridadTicket.ALTA).size());
    }

    @Test
    void listarPorEmpresaIdYEstado_debeRetornarLista() {
        when(ticketRepository.findByEmpresaIdAndEstado(1L, EstadoTicket.ABIERTO))
                .thenReturn(List.of(ticket));

        assertEquals(1, ticketService.listarPorEmpresaIdYEstado(1L, EstadoTicket.ABIERTO).size());
    }

    @Test
    void listarPorEmpresaIdYPrioridad_debeRetornarLista() {
        when(ticketRepository.findByEmpresaIdAndPrioridad(1L, PrioridadTicket.ALTA))
                .thenReturn(List.of(ticket));

        assertEquals(1, ticketService.listarPorEmpresaIdYPrioridad(1L, PrioridadTicket.ALTA).size());
    }

    @Test
    void existePorCodigo_debeRetornarTrue() {
        when(ticketRepository.existsByCodigo("TCK-1001")).thenReturn(true);

        assertTrue(ticketService.existePorCodigo("TCK-1001"));
    }

    @Test
    void listarPorEmpresaConDetalles_debeRetornarLista() {
        when(ticketRepository.findByEmpresaConDetallesOrdenado(1L)).thenReturn(List.of(ticket));

        assertEquals(1, ticketService.listarPorEmpresaConDetalles(1L).size());
    }

    @Test
    void filtrarTickets_debeRetornarLista() {
        when(ticketRepository.filtrarTickets(1L, EstadoTicket.ABIERTO, PrioridadTicket.ALTA))
                .thenReturn(List.of(ticket));

        assertEquals(1, ticketService.filtrarTickets(1L, EstadoTicket.ABIERTO, PrioridadTicket.ALTA).size());
    }

    @Test
    void contarPorEstado_debeRetornarLista() {
        when(ticketRepository.contarPorEstado(1L)).thenReturn(List.of());

        assertNotNull(ticketService.contarPorEstado(1L));
    }

    @Test
    void listarPorAgenteYEstado_debeRetornarLista() {
        when(ticketRepository.findByAgenteYEstado(1L, EstadoTicket.ABIERTO))
                .thenReturn(List.of(ticket));

        assertEquals(1, ticketService.listarPorAgenteYEstado(1L, EstadoTicket.ABIERTO).size());
    }

    @Test
    void listarPorEmpresaYPeriodo_debeRetornarLista() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(7);
        LocalDateTime fin = LocalDateTime.now();

        when(ticketRepository.findByEmpresaYPeriodo(1L, inicio, fin)).thenReturn(List.of(ticket));

        assertEquals(1, ticketService.listarPorEmpresaYPeriodo(1L, inicio, fin).size());
    }

    @Test
    void listarPrioridadAltaPorEmpresa_debeRetornarLista() {
        when(ticketRepository.findPrioridadAltaPorEmpresa(1L)).thenReturn(List.of(ticket));

        assertEquals(1, ticketService.listarPrioridadAltaPorEmpresa(1L).size());
    }
}