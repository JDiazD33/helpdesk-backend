package com.helpdesk.helpdesk_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpdesk.helpdesk_backend.dto.EmpresaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.EmpresaResponseDTO;
import com.helpdesk.helpdesk_backend.service.EmpresaService;

@WebMvcTest(EmpresaController.class)
class EmpresaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Usamos @MockitoBean para Spring Boot 3.4+
    @MockitoBean
    private EmpresaService empresaService;

    private EmpresaRequestDTO requestDTO;
    private EmpresaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = EmpresaRequestDTO.builder()
                .nombre("Empresa Alpha")
                .ruc("10000000001")
                .correoContacto("alpha@empresa.com")
                .telefonoContacto("111111111")
                .build();

        responseDTO = EmpresaResponseDTO.builder()
                .id(1L)
                .nombre("Empresa Alpha")
                .ruc("10000000001")
                .correoContacto("alpha@empresa.com")
                .telefonoContacto("111111111")
                .activo(true)
                .build();
    }

    @Test
    void crearEmpresa_ConDatosValidos_RetornaStatus201() throws Exception {
        // Arrange
        when(empresaService.crearEmpresa(any(EmpresaRequestDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/empresas")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Empresa Alpha"))
                .andExpect(jsonPath("$.ruc").value("10000000001"));
    }

    @Test
    void crearEmpresa_ConDatosInvalidos_RetornaStatus400() throws Exception {
        // Arrange - Simulamos un request incompleto (sin RUC ni nombre) para forzar el fallo de @Valid
        EmpresaRequestDTO requestInvalido = EmpresaRequestDTO.builder()
                .correoContacto("correo_invalido") // No es un email válido
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/empresas")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest()); // Esperamos un error 400 por fallar las validaciones
    }

    @Test
    void obtenerEmpresa_CuandoExiste_RetornaStatus200() throws Exception {
        // Arrange
        when(empresaService.obtenerEmpresaPorId(1L)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/empresas/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Empresa Alpha"));
    }

    @Test
    void listarEmpresasActivas_RetornaListaYStatus200() throws Exception {
        // Arrange
        List<EmpresaResponseDTO> listaEmpresas = Arrays.asList(responseDTO);
        when(empresaService.listarEmpresasActivas()).thenReturn(listaEmpresas);

        // Act & Assert
        mockMvc.perform(get("/api/empresas")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].ruc").value("10000000001"));
    }

    @Test
    void actualizarEmpresa_ConDatosValidos_RetornaStatus200() throws Exception {
        // Arrange
        when(empresaService.actualizarEmpresa(eq(1L), any(EmpresaRequestDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/empresas/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Empresa Alpha"));
    }

    @Test
    void eliminarEmpresa_RetornaStatus204() throws Exception {
        // Arrange
        doNothing().when(empresaService).eliminarEmpresa(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/empresas/{id}", 1L))
                .andExpect(status().isNoContent()); // Esperamos 204 No Content para la eliminación exitosa
    }
}