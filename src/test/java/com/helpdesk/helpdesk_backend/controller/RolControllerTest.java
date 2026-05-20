package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.service.RolService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RolController.class)
class RolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RolService rolService;

    @Test
    void listarTodos_DebeRetornarListaYStatus200() throws Exception {
        Rol rol1 = Rol.builder().id(1L).nombre("ADMIN").build();
        Rol rol2 = Rol.builder().id(2L).nombre("USER").build();
        List<Rol> listaRoles = Arrays.asList(rol1, rol2);

        when(rolService.listarTodos()).thenReturn(listaRoles);

        mockMvc.perform(get("/api/roles")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("ADMIN"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nombre").value("USER"));
    }
}
