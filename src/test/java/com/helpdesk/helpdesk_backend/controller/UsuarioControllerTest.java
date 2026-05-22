package com.helpdesk.helpdesk_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.security.CustomUserDetailsService;
import com.helpdesk.helpdesk_backend.security.JwtUtil;
import com.helpdesk.helpdesk_backend.service.UsuarioService;

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

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        Empresa empresa = Empresa.builder().id(1L).nombre("Tech Solutions").build();
        Rol rol = Rol.builder().id(1L).nombre("CLIENTE").build();

        usuario = Usuario.builder()
                .id(1L)
                .nombres("Juan")
                .apellidos("Perez")
                .email("juan@test.com")
                .password("123456")
                .activo(true)
                .empresa(empresa)
                .rol(rol)
                .build();
    }

    @Test
    void listarTodos_debeRetornarLista() throws Exception {
        Mockito.when(usuarioService.listarTodos()).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombres").value("Juan"));
    }

    @Test
    void buscarPorId_debeRetornarUsuario() throws Exception {
        Mockito.when(usuarioService.buscarPorId(1L)).thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("juan@test.com"));
    }

    @Test
    void buscarPorId_noExiste_debeRetornar404() throws Exception {
        Mockito.when(usuarioService.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarPorEmail_debeRetornarUsuario() throws Exception {
        Mockito.when(usuarioService.buscarPorEmail("juan@test.com")).thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/api/usuarios/email/juan@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Juan"));
    }

    @Test
    void buscarPorEmail_noExiste_debeRetornar404() throws Exception {
        Mockito.when(usuarioService.buscarPorEmail("ghost@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/usuarios/email/ghost@test.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarPorEmpresa_debeRetornarLista() throws Exception {
        Mockito.when(usuarioService.listarPorEmpresa(1L)).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/usuarios/empresa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombres").value("Juan"));
    }

    @Test
    void listarPorRol_debeRetornarLista() throws Exception {
        Mockito.when(usuarioService.listarPorRol(1L)).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/usuarios/rol/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listarPorEstado_activos_debeRetornarLista() throws Exception {
        Mockito.when(usuarioService.listarPorEstado(true)).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/usuarios/estado")
                .param("activo", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void listarActivosPorEmpresa_debeRetornarLista() throws Exception {
        Mockito.when(usuarioService.listarActivosPorEmpresa(1L, true)).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/usuarios/empresa/1/activos")
                .param("activo", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void listarPorEmpresaYRol_debeRetornarLista() throws Exception {
        Mockito.when(usuarioService.listarPorEmpresaYRol(1L, 1L)).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/usuarios/empresa/1/rol/1"))
                .andExpect(status().isOk());
    }

    @Test
    void listarActivosConDetalles_debeRetornarLista() throws Exception {
        Mockito.when(usuarioService.listarActivosPorEmpresaConDetalles(1L)).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/usuarios/empresa/1/detalle"))
                .andExpect(status().isOk());
    }

    @Test
    void buscarPorNombre_debeRetornarLista() throws Exception {
        Mockito.when(usuarioService.buscarPorNombreOApellido(1L, "Juan")).thenReturn(List.of(usuario));

        mockMvc.perform(get("/api/usuarios/empresa/1/buscar")
                .param("termino", "Juan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombres").value("Juan"));
    }

    @Test
    void guardar_debeCrearUsuario() throws Exception {
        Mockito.when(usuarioService.guardar(any())).thenReturn(usuario);

        String json = """
                {
                    "nombres": "Juan",
                    "apellidos": "Perez",
                    "email": "juan@test.com",
                    "password": "123456",
                    "activo": true,
                    "empresa": { "id": 1 },
                    "rol": { "id": 1 }
                }
                """;

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("juan@test.com"));

        Mockito.verify(usuarioService).guardar(any(Usuario.class));
    }

    @Test
    void guardar_camposVacios_debeRetornar400() throws Exception {
        Usuario invalido = Usuario.builder().build();

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizar_debeRetornarUsuarioActualizado() throws Exception {
        Mockito.when(usuarioService.actualizar(eq(1L), any())).thenReturn(usuario);

        String json = """
                {
                    "nombres": "Juan",
                    "apellidos": "Perez",
                    "email": "juan@test.com",
                    "password": "123456",
                    "activo": true,
                    "empresa": { "id": 1 },
                    "rol": { "id": 1 }
                }
                """;

        mockMvc.perform(put("/api/usuarios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Juan"));

        Mockito.verify(usuarioService).actualizar(eq(1L), any(Usuario.class));
    }

    @Test
    void eliminar_debeRetornarNoContent() throws Exception {
        Mockito.doNothing().when(usuarioService).eliminar(1L);

        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNoContent());
    }
}