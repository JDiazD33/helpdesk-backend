package com.helpdesk.helpdesk_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpdesk.helpdesk_backend.model.Permiso;
import com.helpdesk.helpdesk_backend.security.CustomUserDetailsService;
import com.helpdesk.helpdesk_backend.security.JwtUtil;
import com.helpdesk.helpdesk_backend.service.PermisoService;

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

@WebMvcTest(PermisoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PermisoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PermisoService permisoService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Permiso permiso;

    @BeforeEach
    void setUp() {
        permiso = Permiso.builder()
                .id(1L)
                .nombre("CREAR_TICKET")
                .descripcion("Permite crear tickets")
                .activo(true)
                .build();
    }

    @Test
    void listarTodos_debeRetornarLista() throws Exception {
        Mockito.when(permisoService.listarTodos())
                .thenReturn(List.of(permiso));

        mockMvc.perform(get("/api/permisos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("CREAR_TICKET"));
    }

    @Test
    void listarActivos_debeRetornarLista() throws Exception {
        Mockito.when(permisoService.listarActivos())
                .thenReturn(List.of(permiso));

        mockMvc.perform(get("/api/permisos/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void buscarPorId_debeRetornarPermiso() throws Exception {
        Mockito.when(permisoService.buscarPorId(1L))
                .thenReturn(Optional.of(permiso));

        mockMvc.perform(get("/api/permisos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("CREAR_TICKET"));
    }

    @Test
    void buscarPorId_noExiste_debeRetornar404() throws Exception {
        Mockito.when(permisoService.buscarPorId(99L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/permisos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void guardar_debeCrearPermiso() throws Exception {
        Mockito.when(permisoService.guardar(any()))
                .thenReturn(permiso);

        mockMvc.perform(post("/api/permisos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(permiso)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("CREAR_TICKET"));
    }

    @Test
    void guardar_nombreVacio_debeRetornar400() throws Exception {
        Permiso invalido = Permiso.builder().descripcion("sin nombre").build();

        mockMvc.perform(post("/api/permisos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_debeRetornarPermisoActualizado() throws Exception {
        Mockito.when(permisoService.actualizar(eq(1L), any()))
                .thenReturn(permiso);

        mockMvc.perform(put("/api/permisos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(permiso)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("CREAR_TICKET"));
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        Mockito.doNothing().when(permisoService).eliminar(1L);

        mockMvc.perform(delete("/api/permisos/1"))
                .andExpect(status().isNoContent());
    }
}