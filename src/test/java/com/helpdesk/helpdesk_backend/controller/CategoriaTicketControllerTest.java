package com.helpdesk.helpdesk_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpdesk.helpdesk_backend.dto.CategoriaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.CategoriaResponseDTO;
import com.helpdesk.helpdesk_backend.service.CategoriaTicketService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoriaTicketController.class)
class CategoriaTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoriaTicketService categoriaService;

    private CategoriaRequestDTO requestDTO;
    private CategoriaResponseDTO responseDTO;

    private final Long EMPRESA_ID = 1L;
    private final Long CATEGORIA_ID = 10L;

    @BeforeEach
    void setUp() {
        requestDTO = new CategoriaRequestDTO();
        requestDTO.setNombre("Hardware");
        requestDTO.setDescripcion("Problemas físicos");

        responseDTO = new CategoriaResponseDTO();
        responseDTO.setId(CATEGORIA_ID);
        responseDTO.setNombre("Hardware");
        responseDTO.setDescripcion("Problemas físicos");
        responseDTO.setActiva(true);
    }

    @Test
    void listarCategoriasActivas_DebeRetornarListaYEstado200() throws Exception {
        when(categoriaService.listarCategoriasActivas(EMPRESA_ID)).thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/categorias/activas")
                        .header("X-Empresa-Id", EMPRESA_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Hardware"));
    }

    @Test
    void crearCategoria_DebeRetornarCategoriaCreadaYEstado201() throws Exception {
        when(categoriaService.crearCategoria(any(CategoriaRequestDTO.class), eq(EMPRESA_ID)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/categorias")
                        .header("X-Empresa-Id", EMPRESA_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Hardware"));
    }

    @Test
    void crearCategoria_DebeRetornar400SiFaltaHeaderEmpresaId() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        // Omitimos el header deliberadamente para probar la seguridad Multi-tenant
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminarCategoria_DebeRetornarEstado204() throws Exception {
        mockMvc.perform(delete("/api/categorias/{id}", CATEGORIA_ID)
                        .header("X-Empresa-Id", EMPRESA_ID))
                .andExpect(status().isNoContent());

        verify(categoriaService).eliminarCategoria(CATEGORIA_ID, EMPRESA_ID);
    }
}