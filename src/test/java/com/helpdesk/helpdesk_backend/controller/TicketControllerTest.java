package com.helpdesk.helpdesk_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.helpdesk.helpdesk_backend.dto.TicketConComentarioRequestDTO;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.model.Ticket;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;
import com.helpdesk.helpdesk_backend.model.enums.PrioridadTicket;
import com.helpdesk.helpdesk_backend.security.CustomUserDetailsService;
import com.helpdesk.helpdesk_backend.security.JwtUtil;
import com.helpdesk.helpdesk_backend.service.TicketService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Ticket ticket;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        Empresa empresa = Empresa.builder().id(1L).nombre("Tech Solutions").build();
        Usuario cliente = Usuario.builder().id(1L).nombres("Juan").apellidos("Perez").email("juan@test.com").build();

        ticket = Ticket.builder()
                .id(1L)
                .codigo("TCK-1001")
                .titulo("Falla de red")
                .descripcion("No hay conexión a internet")
                .estado(EstadoTicket.ABIERTO)
                .prioridad(PrioridadTicket.ALTA)
                .cliente(cliente)
                .empresa(empresa)
                .build();
    }

    @Test
    void listarPorEmpresaId_debeRetornarLista() throws Exception {
        Mockito.when(ticketService.listarPorEmpresaId(1L)).thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Falla de red"));
    }

    @Test
    void buscarPorId_debeRetornarTicket() throws Exception {
        Mockito.when(ticketService.buscarPorId(1L)).thenReturn(Optional.of(ticket));

        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("TCK-1001"));
    }

    @Test
    void buscarPorId_noExiste_debeRetornar404() throws Exception {
        Mockito.when(ticketService.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tickets/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarPorCodigo_debeRetornarTicket() throws Exception {
        Mockito.when(ticketService.buscarPorCodigo("TCK-1001")).thenReturn(Optional.of(ticket));

        mockMvc.perform(get("/api/tickets/codigo/TCK-1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Falla de red"));
    }

    @Test
    void buscarPorCodigo_noExiste_debeRetornar404() throws Exception {
        Mockito.when(ticketService.buscarPorCodigo("TCK-9999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tickets/codigo/TCK-9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarPorEmpresa_debeRetornarLista() throws Exception {
        Mockito.when(ticketService.listarPorEmpresaId(1L)).thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets/empresa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Falla de red"));
    }

    @Test
    void listarPorCliente_debeRetornarLista() throws Exception {
        Mockito.when(ticketService.listarPorClienteId(1L)).thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets/cliente/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listarPorAgente_debeRetornarLista() throws Exception {
        Mockito.when(ticketService.listarPorAgenteAsignadoId(1L)).thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets/agente/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listarPorEstado_debeRetornarLista() throws Exception {
        Mockito.when(ticketService.listarPorEstado(EstadoTicket.ABIERTO)).thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets/estado/ABIERTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("ABIERTO"));
    }

    @Test
    void listarPorPrioridad_debeRetornarLista() throws Exception {
        Mockito.when(ticketService.listarPorPrioridad(PrioridadTicket.ALTA)).thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets/prioridad/ALTA"))
                .andExpect(status().isOk());
    }

    @Test
    void listarPorCategoria_debeRetornarLista() throws Exception {
        Mockito.when(ticketService.listarPorCategoriaId(1L)).thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets/categoria/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listarPorEmpresaConDetalles_debeRetornarLista() throws Exception {
        Mockito.when(ticketService.listarPorEmpresaConDetalles(1L)).thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets/empresa/1/detalle"))
                .andExpect(status().isOk());
    }

    @Test
    void filtrarTickets_debeRetornarLista() throws Exception {
        Mockito.when(ticketService.filtrarTickets(1L, EstadoTicket.ABIERTO, PrioridadTicket.ALTA))
                .thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets/empresa/1/filtrar")
                .param("estado", "ABIERTO")
                .param("prioridad", "ALTA"))
                .andExpect(status().isOk());
    }

    @Test
    void filtrarTickets_sinFiltros_debeRetornarLista() throws Exception {
        Mockito.when(ticketService.filtrarTickets(1L, null, null)).thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets/empresa/1/filtrar"))
                .andExpect(status().isOk());
    }

    @Test
    void contarPorEstado_debeRetornarLista() throws Exception {
        Mockito.when(ticketService.contarPorEstado(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/tickets/empresa/1/conteo"))
                .andExpect(status().isOk());
    }

    @Test
    void listarPorAgenteYEstado_debeRetornarLista() throws Exception {
        Mockito.when(ticketService.listarPorAgenteYEstado(1L, EstadoTicket.ABIERTO))
                .thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets/agente/1/estado/ABIERTO"))
                .andExpect(status().isOk());
    }

    @Test
    void listarPorPeriodo_debeRetornarLista() throws Exception {
        Mockito.when(ticketService.listarPorEmpresaYPeriodo(eq(1L), any(), any()))
                .thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets/empresa/1/periodo")
                .param("inicio", "2025-01-01")
                .param("fin", "2025-12-31"))
                .andExpect(status().isOk());
    }

    @Test
    void listarPrioridadAlta_debeRetornarLista() throws Exception {
        Mockito.when(ticketService.listarPrioridadAltaPorEmpresa(1L)).thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets/empresa/1/prioridad-alta"))
                .andExpect(status().isOk());
    }

    @Test
    void guardar_debeCrearTicket() throws Exception {
        Mockito.when(ticketService.guardar(any())).thenReturn(ticket);

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticket)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Falla de red"));
    }

    @Test
    void guardarConComentario_debeCrearTicket() throws Exception {
        TicketConComentarioRequestDTO request = new TicketConComentarioRequestDTO();
        request.setTicket(ticket);
        request.setMensajeInicial("Primer comentario");
        request.setUsuarioComentarioId(1L);

        Mockito.when(ticketService.guardarConComentarioInicial(any(), anyString(), anyLong()))
                .thenReturn(ticket);

        mockMvc.perform(post("/api/tickets/completo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void actualizar_debeRetornarTicketActualizado() throws Exception {
        Mockito.when(ticketService.actualizar(eq(1L), any())).thenReturn(ticket);

        mockMvc.perform(put("/api/tickets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ticket)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Falla de red"));
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        Mockito.doNothing().when(ticketService).eliminar(1L);

        mockMvc.perform(delete("/api/tickets/1"))
                .andExpect(status().isNoContent());
    }
}