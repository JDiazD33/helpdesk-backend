package com.helpdesk.helpdesk_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.helpdesk.helpdesk_backend.dto.UsuarioRequestDTO;
import com.helpdesk.helpdesk_backend.dto.UsuarioResponseDTO;
import com.helpdesk.helpdesk_backend.service.UsuarioService;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper nos permite convertir nuestros DTOs (Objetos Java) a Strings JSON para enviarlos en las peticiones
    @Autowired
    private ObjectMapper objectMapper;

    // Usamos @MockitoBean como acordamos para evitar el warning de deprecación en Spring Boot 3.4+
    @MockitoBean
    private UsuarioService usuarioService;

    private UsuarioRequestDTO requestDTO;
    private UsuarioResponseDTO responseDTO;
    private final Long EMPRESA_CONTEXTO_ID = 1L;

    @BeforeEach
    void setUp() {
        // Preparamos los DTOs de prueba
        requestDTO = UsuarioRequestDTO.builder()
                .nombres("Juan")
                .apellidos("Pérez")
                .email("juan@alpha.com")
                .password("secreta")
                .rolId(1L)
                .build();

        responseDTO = UsuarioResponseDTO.builder()
                .id(100L)
                .nombres("Juan")
                .apellidos("Pérez")
                .email("juan@alpha.com")
                .empresaId(EMPRESA_CONTEXTO_ID)
                .rolNombre("ADMIN")
                .build();
    }

    @Test
    void crearUsuario_ConDatosValidosYHeader_RetornaStatus201() throws Exception {
        // Arrange
        when(usuarioService.crearUsuario(any(UsuarioRequestDTO.class), eq(EMPRESA_CONTEXTO_ID)))
                .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/usuarios")
                .header("X-Empresa-Id", EMPRESA_CONTEXTO_ID)
                .contentType(MediaType.APPLICATION_JSON_VALUE) // Evita el warning "Null type safety"
                .content(objectMapper.writeValueAsString(requestDTO))) // Convertimos el objeto a JSON
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.email").value("juan@alpha.com"));
    }

    @Test
    void crearUsuario_SinHeaderXEmpresaId_RetornaStatus400() throws Exception {
        // Arrange - Si olvidamos enviar el header, el controlador debe fallar antes de tocar el Service
        
        // Act & Assert
        mockMvc.perform(post("/api/usuarios")
                // Omitimos deliberadamente el .header("X-Empresa-Id")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest()); // 400 Bad Request por la falta de @RequestHeader
    }

    @Test
    void obtenerUsuario_ConHeader_RetornaStatus200() throws Exception {
        // Arrange
        when(usuarioService.obtenerUsuarioPorId(100L, EMPRESA_CONTEXTO_ID)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/usuarios/{id}", 100L)
                .header("X-Empresa-Id", EMPRESA_CONTEXTO_ID)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Juan"))
                .andExpect(jsonPath("$.rolNombre").value("ADMIN"));
    }

    @Test
    void listarUsuariosPorEmpresa_ConHeader_RetornaListaYStatus200() throws Exception {
        // Arrange
        List<UsuarioResponseDTO> lista = Arrays.asList(responseDTO);
        when(usuarioService.listarUsuariosPorEmpresa(EMPRESA_CONTEXTO_ID)).thenReturn(lista);

        // Act & Assert
        mockMvc.perform(get("/api/usuarios")
                .header("X-Empresa-Id", EMPRESA_CONTEXTO_ID)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].email").value("juan@alpha.com"));
    }

    @Test
    void eliminarUsuario_ConHeader_RetornaStatus204() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/usuarios/{id}", 100L)
                .header("X-Empresa-Id", EMPRESA_CONTEXTO_ID))
                .andExpect(status().isNoContent()); // 204 No Content para borrados exitosos
    }
    

}
