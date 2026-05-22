package com.helpdesk.helpdesk_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.service.UsuarioService;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService usuarioService;

    private Usuario usuario;
    private final Long EMPRESA_CONTEXTO_ID = 1L;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(100L);
        usuario.setNombres("Juan");
        usuario.setApellidos("Pérez");
        usuario.setEmail("juan@alpha.com");
    }

    @Test
    void guardar_ConDatosValidos_RetornaStatus201() throws Exception {
        when(usuarioService.guardar(any(Usuario.class))).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.email").value("juan@alpha.com"));
    }

    @Test
    void buscarPorId_Existente_RetornaStatus200() throws Exception {
        when(usuarioService.buscarPorId(100L)).thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/api/usuarios/{id}", 100L)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Juan"))
                .andExpect(jsonPath("$.email").value("juan@alpha.com"));
    }

    @Test
    void listarPorEmpresa_RetornaListaYStatus200() throws Exception {
        List<Usuario> lista = Arrays.asList(usuario);
        when(usuarioService.listarPorEmpresa(EMPRESA_CONTEXTO_ID)).thenReturn(lista);

        mockMvc.perform(get("/api/usuarios/empresa/{empresaId}", EMPRESA_CONTEXTO_ID)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].email").value("juan@alpha.com"));
    }

    @Test
    void eliminar_RetornaStatus204() throws Exception {
        mockMvc.perform(delete("/api/usuarios/{id}", 100L))
                .andExpect(status().isNoContent());
    }
}
