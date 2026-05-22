package com.helpdesk.helpdesk_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpdesk.helpdesk_backend.dto.AsignarPermisosRequestDTO;
import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.security.CustomUserDetailsService;
import com.helpdesk.helpdesk_backend.security.JwtUtil;
import com.helpdesk.helpdesk_backend.service.RolService;

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

@WebMvcTest(RolController.class)
@AutoConfigureMockMvc(addFilters = false)
class RolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RolService rolService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Rol rol;

    @BeforeEach
    void setUp() {
        rol = Rol.builder()
                .id(1L)
                .nombre("ADMIN_EMPRESA")
                .activo(true)
                .build();
    }

    @Test
    void listarTodos_debeRetornarLista() throws Exception {
        Mockito.when(rolService.listarTodos()).thenReturn(List.of(rol));

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("ADMIN_EMPRESA"));
    }

    @Test
    void buscarPorId_debeRetornarRol() throws Exception {
        Mockito.when(rolService.buscarPorId(1L)).thenReturn(Optional.of(rol));

        mockMvc.perform(get("/api/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("ADMIN_EMPRESA"));
    }

    @Test
    void buscarPorId_noExiste_debeRetornar404() throws Exception {
        Mockito.when(rolService.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/roles/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarPorNombre_debeRetornarRol() throws Exception {
        Mockito.when(rolService.buscarPorNombre("ADMIN_EMPRESA")).thenReturn(Optional.of(rol));

        mockMvc.perform(get("/api/roles/nombre/ADMIN_EMPRESA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("ADMIN_EMPRESA"));
    }

    @Test
    void buscarPorNombre_noExiste_debeRetornar404() throws Exception {
        Mockito.when(rolService.buscarPorNombre("INEXISTENTE")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/roles/nombre/INEXISTENTE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void guardar_debeCrearRol() throws Exception {
        Mockito.when(rolService.guardar(any())).thenReturn(rol);

        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rol)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("ADMIN_EMPRESA"));
    }

    @Test
    void guardar_nombreVacio_debeRetornar400() throws Exception {
        Rol invalido = Rol.builder().build();

        mockMvc.perform(post("/api/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_debeRetornarRolActualizado() throws Exception {
        Mockito.when(rolService.actualizar(eq(1L), any())).thenReturn(rol);

        mockMvc.perform(put("/api/roles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rol)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("ADMIN_EMPRESA"));
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        Mockito.doNothing().when(rolService).eliminar(1L);

        mockMvc.perform(delete("/api/roles/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void buscarConPermisos_debeRetornarRol() throws Exception {
        Mockito.when(rolService.buscarPorIdConPermisos(1L)).thenReturn(Optional.of(rol));

        mockMvc.perform(get("/api/roles/1/permisos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("ADMIN_EMPRESA"));
    }

    @Test
    void buscarConPermisos_noExiste_debeRetornar404() throws Exception {
        Mockito.when(rolService.buscarPorIdConPermisos(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/roles/99/permisos"))
                .andExpect(status().isNotFound());
    }

    @Test
    void asignarPermisos_debeRetornarRolActualizado() throws Exception {
        AsignarPermisosRequestDTO request = new AsignarPermisosRequestDTO();
        request.setPermisoIds(List.of(1L, 2L));

        Mockito.when(rolService.asignarPermisos(eq(1L), anyList())).thenReturn(rol);

        mockMvc.perform(put("/api/roles/1/permisos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("ADMIN_EMPRESA"));
    }

    @Test
    void asignarPermisos_listaVacia_debeRetornar400() throws Exception {
        AsignarPermisosRequestDTO request = new AsignarPermisosRequestDTO();
        request.setPermisoIds(List.of());

        mockMvc.perform(put("/api/roles/1/permisos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}