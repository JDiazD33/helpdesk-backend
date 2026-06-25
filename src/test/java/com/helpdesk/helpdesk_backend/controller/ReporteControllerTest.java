package com.helpdesk.helpdesk_backend.controller;

import com.helpdesk.helpdesk_backend.security.CustomUserDetailsService;
import com.helpdesk.helpdesk_backend.security.JwtUtil;
import com.helpdesk.helpdesk_backend.security.TenantSecurity;
import com.helpdesk.helpdesk_backend.service.ReporteService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReporteController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReporteService reporteService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private TenantSecurity tenant;

    @Test
    void ticketsPorMes_debeRetornarLista() throws Exception {
        Mockito.when(tenant.resolveEmpresaId(1L)).thenReturn(1L);
        Mockito.when(reporteService.ticketsPorMes(1L, 2025, null))
                .thenReturn(List.of(
                        Map.of("mes", 1, "total", 5),
                        Map.of("mes", 2, "total", 3)
                ));

        mockMvc.perform(get("/api/reportes/tickets-mes")
                .param("empresaId", "1")
                .param("anio", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mes").value(1))
                .andExpect(jsonPath("$[0].total").value(5));
    }

    @Test
    void usuariosActivos_debeRetornarMapa() throws Exception {
        Mockito.when(tenant.resolveEmpresaId(1L)).thenReturn(1L);
        Mockito.when(reporteService.contarUsuariosActivos(1L))
                .thenReturn(Map.of("total", 10, "admins", 2, "agentes", 3, "clientes", 5));

        mockMvc.perform(get("/api/reportes/usuarios-activos")
                .param("empresaId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10));
    }
}