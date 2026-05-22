package com.helpdesk.helpdesk_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpdesk.helpdesk_backend.dto.ComentarioRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ComentarioResponseDTO;
import com.helpdesk.helpdesk_backend.security.CustomUserDetailsService;
import com.helpdesk.helpdesk_backend.security.JwtUtil;
import com.helpdesk.helpdesk_backend.service.TicketComentarioService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketComentarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class TicketComentarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketComentarioService comentarioService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private ComentarioResponseDTO comentarioResponse;
    private ComentarioRequestDTO comentarioRequest;

    @BeforeEach
    void setUp() {
        comentarioResponse = new ComentarioResponseDTO();
        comentarioResponse.setId(1L);
        comentarioResponse.setMensaje("Hola, necesito ayuda");
        comentarioResponse.setFechaEnvio(LocalDateTime.now());
        comentarioResponse.setTicketId(1L);
        comentarioResponse.setUsuarioId(1L);
        comentarioResponse.setUsuarioNombre("Juan Perez");

        comentarioRequest = new ComentarioRequestDTO();
        comentarioRequest.setMensaje("Hola, necesito ayuda");
        comentarioRequest.setTicketId(1L);
    }

    @Test
    void agregarComentario_debeRetornarCreated() throws Exception {
        Mockito.when(comentarioService.agregarComentario(any(), eq(1L), eq(1L)))
                .thenReturn(comentarioResponse);

        mockMvc.perform(post("/api/comentarios")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Empresa-Id", "1")
                .header("x-Usuario-Id", "1")
                .content(objectMapper.writeValueAsString(comentarioRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.mensaje").value("Hola, necesito ayuda"));
    }

    @Test
    void agregarComentario_mensajeVacio_debeRetornar400() throws Exception {
        ComentarioRequestDTO invalido = new ComentarioRequestDTO();
        invalido.setTicketId(1L);

        mockMvc.perform(post("/api/comentarios")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Empresa-Id", "1")
                .header("x-Usuario-Id", "1")
                .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listarComentariosPorTicket_debeRetornarLista() throws Exception {
        Mockito.when(comentarioService.listarComentariosPorTicket(1L, 1L))
                .thenReturn(List.of(comentarioResponse));

        mockMvc.perform(get("/api/comentarios/ticket/1")
                .header("X-Empresa-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mensaje").value("Hola, necesito ayuda"))
                .andExpect(jsonPath("$[0].usuarioNombre").value("Juan Perez"));
    }
}