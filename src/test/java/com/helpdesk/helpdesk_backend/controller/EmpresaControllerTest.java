package com.helpdesk.helpdesk_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpdesk.helpdesk_backend.dto.EmpresaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.EmpresaResponseDTO;
import com.helpdesk.helpdesk_backend.security.CustomUserDetailsService;
import com.helpdesk.helpdesk_backend.security.JwtUtil;
import com.helpdesk.helpdesk_backend.service.EmpresaService;

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

@WebMvcTest(EmpresaController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmpresaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmpresaService empresaService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private EmpresaResponseDTO empresaResponse;
    private EmpresaRequestDTO empresaRequest;

    @BeforeEach
    void setUp() {
        empresaResponse = EmpresaResponseDTO.builder()
                .id(1L)
                .nombre("Tech Solutions")
                .ruc("12345678901")
                .correoContacto("contacto@tech.com")
                .telefonoContacto("999999999")
                .activo(true)
                .fechaCreacion(LocalDateTime.now())
                .build();

        empresaRequest = EmpresaRequestDTO.builder()
                .ruc("12345678901")
                .nombre("Tech Solutions")
                .telefonoContacto("999999999")
                .correoContacto("contacto@tech.com")
                .build();
    }

    @Test
    void listarTodos_debeRetornarLista() throws Exception {
        Mockito.when(empresaService.listarEmpresasActivas())
                .thenReturn(List.of(empresaResponse));

        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Tech Solutions"));
    }

    @Test
    void buscarPorId_debeRetornarEmpresa() throws Exception {
        Mockito.when(empresaService.obtenerEmpresaPorId(1L))
                .thenReturn(empresaResponse);

        mockMvc.perform(get("/api/empresas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruc").value("12345678901"));
    }

    @Test
    void guardar_debeCrearEmpresa() throws Exception {
        Mockito.when(empresaService.crearEmpresa(any()))
                .thenReturn(empresaResponse);

        mockMvc.perform(post("/api/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(empresaRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Tech Solutions"));
    }

    @Test
    void guardar_camposVacios_debeRetornar400() throws Exception {
        EmpresaRequestDTO requestInvalido = EmpresaRequestDTO.builder().build();

        mockMvc.perform(post("/api/empresas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_debeRetornarEmpresaActualizada() throws Exception {
        Mockito.when(empresaService.actualizarEmpresa(eq(1L), any()))
                .thenReturn(empresaResponse);

        mockMvc.perform(put("/api/empresas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(empresaRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Tech Solutions"));
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        Mockito.doNothing().when(empresaService).eliminarEmpresa(1L);

        mockMvc.perform(delete("/api/empresas/1"))
                .andExpect(status().isNoContent());
    }
}