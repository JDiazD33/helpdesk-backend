package com.helpdesk.helpdesk_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpdesk.helpdesk_backend.dto.ProblemaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ProblemaResponseDTO;
import com.helpdesk.helpdesk_backend.service.ProblemaTicketService;

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

@WebMvcTest(ProblemaTicketController.class)
class ProblemaTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProblemaTicketService problemaService;

    private ProblemaRequestDTO requestDTO;
    private ProblemaResponseDTO responseDTO;

    private final Long EMPRESA_ID = 1L;
    private final Long CATEGORIA_ID = 10L;
    private final Long PROBLEMA_ID = 100L;

    @BeforeEach
    void setUp() {
        requestDTO = new ProblemaRequestDTO();
        requestDTO.setNombre("Pantalla Rota");
        requestDTO.setDescripcion("El monitor no enciende o está estrellado");
        requestDTO.setCategoriaId(CATEGORIA_ID);

        responseDTO = new ProblemaResponseDTO();
        responseDTO.setId(PROBLEMA_ID);
        responseDTO.setNombre("Pantalla Rota");
        responseDTO.setDescripcion("El monitor no enciende o está estrellado");
        responseDTO.setActivo(true);
        responseDTO.setCategoriaId(CATEGORIA_ID);
        responseDTO.setCategoriaNombre("Hardware");
    }

    @Test
    void listarProblemasPorCategoria_DebeRetornarListaYEstado200() throws Exception {
        when(problemaService.listarProblemasPorCategoria(CATEGORIA_ID, EMPRESA_ID))
                .thenReturn(List.of(responseDTO));

        mockMvc.perform(get("/api/problemas/categoria/{categoriaId}", CATEGORIA_ID)
                        .header("X-Empresa-Id", EMPRESA_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Pantalla Rota"))
                .andExpect(jsonPath("$[0].categoriaNombre").value("Hardware"));
    }

    @Test
    void crearProblema_DebeRetornarProblemaCreadoYEstado201() throws Exception {
        when(problemaService.crearProblema(any(ProblemaRequestDTO.class), eq(EMPRESA_ID)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/problemas")
                        .header("X-Empresa-Id", EMPRESA_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Pantalla Rota"));
    }

    @Test
    void crearProblema_DebeRetornar400SiDatosSonInvalidos() throws Exception {
        // Rompemos la validación de Jakarta @NotBlank
        requestDTO.setNombre("");

        mockMvc.perform(post("/api/problemas")
                        .header("X-Empresa-Id", EMPRESA_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void eliminarProblema_DebeRetornarEstado204() throws Exception {
        mockMvc.perform(delete("/api/problemas/{id}", PROBLEMA_ID)
                        .header("X-Empresa-Id", EMPRESA_ID))
                .andExpect(status().isNoContent());

        verify(problemaService).eliminarProblema(PROBLEMA_ID, EMPRESA_ID);
    }
}