package com.helpdesk.helpdesk_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpdesk.helpdesk_backend.dto.CategoriaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.CategoriaResponseDTO;
import com.helpdesk.helpdesk_backend.security.CustomUserDetailsService;
import com.helpdesk.helpdesk_backend.security.JwtUtil;
import com.helpdesk.helpdesk_backend.service.CategoriaTicketService;

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

@WebMvcTest(CategoriaTicketController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoriaTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoriaTicketService categoriaTicketService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoriaResponseDTO categoriaResponse;
    private CategoriaRequestDTO categoriaRequest;

    @BeforeEach
    void setUp() {
        categoriaResponse = new CategoriaResponseDTO();
        categoriaResponse.setId(1L);
        categoriaResponse.setNombre("Soporte Técnico");
        categoriaResponse.setDescripcion("Problemas técnicos");
        categoriaResponse.setActiva(true);

        categoriaRequest = new CategoriaRequestDTO();
        categoriaRequest.setNombre("Soporte Técnico");
        categoriaRequest.setDescripcion("Problemas técnicos");
    }

    @Test
    void listarTodos_debeRetornarLista() throws Exception {
        Mockito.when(categoriaTicketService.listarTodas(1L))
                .thenReturn(List.of(categoriaResponse));

        mockMvc.perform(get("/api/categorias")
                .param("empresaId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Soporte Técnico"));
    }

    @Test
    void listarPorEmpresa_debeRetornarLista() throws Exception {
        Mockito.when(categoriaTicketService.listarTodas(1L))
                .thenReturn(List.of(categoriaResponse));

        mockMvc.perform(get("/api/categorias/empresa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void listarActivas_debeRetornarLista() throws Exception {
        Mockito.when(categoriaTicketService.listarCategoriasActivas(1L))
                .thenReturn(List.of(categoriaResponse));

        mockMvc.perform(get("/api/categorias/activas")
                .param("empresaId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activa").value(true));
    }

    @Test
    void obtenerPorId_debeRetornarCategoria() throws Exception {
        Mockito.when(categoriaTicketService.obtenerPorId(1L, 1L))
                .thenReturn(categoriaResponse);

        mockMvc.perform(get("/api/categorias/1")
                .param("empresaId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Soporte Técnico"));
    }

    @Test
    void guardar_debeCrearCategoria() throws Exception {
        Mockito.when(categoriaTicketService.crearCategoria(any(), eq(1L)))
                .thenReturn(categoriaResponse);

        mockMvc.perform(post("/api/categorias")
                .param("empresaId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoriaRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void actualizar_debeRetornarCategoriaActualizada() throws Exception {
        Mockito.when(categoriaTicketService.actualizarCategoria(eq(1L), any(), eq(1L)))
                .thenReturn(categoriaResponse);

        mockMvc.perform(put("/api/categorias/1")
                .param("empresaId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoriaRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Soporte Técnico"));
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        Mockito.doNothing().when(categoriaTicketService).eliminarCategoria(1L, 1L);

        mockMvc.perform(delete("/api/categorias/1")
                .param("empresaId", "1"))
                .andExpect(status().isNoContent());
    }
}