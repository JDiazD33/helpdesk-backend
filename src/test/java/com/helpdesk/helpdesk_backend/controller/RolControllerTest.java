package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.dto.RolResponseDTO;
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

    // Mock del servicio
    @MockitoBean
    private RolService rolService;

    @Test
    void obtenerTodosLosRoles_DebeRetornarListaYStatus200() throws Exception {
        // Arrage
        RolResponseDTO dto1 = RolResponseDTO.builder().id(1L).nombre("ADMIN").build();
        RolResponseDTO dto2 = RolResponseDTO.builder().id(2L).nombre("USER").build();
        List<RolResponseDTO> listaRoles = Arrays.asList(dto1,dto2);

        when(rolService.obtenerTodosLosRoles()).thenReturn(listaRoles);

        // Act & Assert
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
