package com.helpdesk.helpdesk_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpdesk.helpdesk_backend.dto.ProblemaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ProblemaResponseDTO;
import com.helpdesk.helpdesk_backend.security.CustomUserDetailsService;
import com.helpdesk.helpdesk_backend.security.JwtUtil;
import com.helpdesk.helpdesk_backend.service.ProblemaTicketService;

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

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProblemaTicketController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProblemaTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProblemaTicketService problemaTicketService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private ProblemaResponseDTO problemaResponse;
    private ProblemaRequestDTO problemaRequest;

    @BeforeEach
    void setUp() {
        problemaResponse = new ProblemaResponseDTO();
        problemaResponse.setId(1L);
        problemaResponse.setNombre("Falla de red");
        problemaResponse.setDescripcion("Problemas de conectividad");
        problemaResponse.setActivo(true);
        problemaResponse.setCategoriaId(1L);
        problemaResponse.setCategoriaNombre("Soporte Técnico");

        problemaRequest = new ProblemaRequestDTO();
        problemaRequest.setNombre("Falla de red");
        problemaRequest.setDescripcion("Problemas de conectividad");
        problemaRequest.setCategoriaId(1L);
    }

    @Test
    void listarPorCategoria_debeRetornarLista() throws Exception {
        Mockito.when(problemaTicketService.listarProblemasPorCategoria(1L, 1L))
                .thenReturn(List.of(problemaResponse));

        mockMvc.perform(get("/api/problemas/categoria/1")
                .param("empresaId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Falla de red"));
    }

    @Test
    void listarActivosPorEmpresa_debeRetornarLista() throws Exception {
        Mockito.when(problemaTicketService.listarActivosPorEmpresa(1L))
                .thenReturn(List.of(problemaResponse));

        mockMvc.perform(get("/api/problemas/empresa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void obtenerPorId_debeRetornarProblema() throws Exception {
        Mockito.when(problemaTicketService.obtenerPorId(1L, 1L))
                .thenReturn(problemaResponse);

        mockMvc.perform(get("/api/problemas/1")
                .param("empresaId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Falla de red"));
    }

    @Test
    void guardar_debeCrearProblema() throws Exception {
        Mockito.when(problemaTicketService.crearProblema(any(), eq(1L)))
                .thenReturn(problemaResponse);

        mockMvc.perform(post("/api/problemas")
                .param("empresaId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(problemaRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Falla de red"));
    }

    @Test
    void guardar_camposVacios_debeRetornar400() throws Exception {
        ProblemaRequestDTO invalido = new ProblemaRequestDTO();

        mockMvc.perform(post("/api/problemas")
                .param("empresaId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_debeRetornarProblemaActualizado() throws Exception {
        Mockito.when(problemaTicketService.actualizarProblema(eq(1L), any(), eq(1L)))
                .thenReturn(problemaResponse);

        mockMvc.perform(put("/api/problemas/1")
                .param("empresaId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(problemaRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Falla de red"));
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        Mockito.doNothing().when(problemaTicketService).eliminarProblema(1L, 1L);

        mockMvc.perform(delete("/api/problemas/1")
                .param("empresaId", "1"))
                .andExpect(status().isNoContent());
    }
}