package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.repository.TicketRepository;
import com.helpdesk.helpdesk_backend.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ReporteServiceImpl reporteService;

    @Test
    void ticketsPorMes_debeRetornarListaMapeada() {
        Object[] fila1 = {1, 5L};
        Object[] fila2 = {2, 3L};
        when(ticketRepository.contarPorMes(1L, 2025)).thenReturn(List.of(fila1, fila2));

        List<Map<String, Object>> resultado = reporteService.ticketsPorMes(1L, 2025);

        assertEquals(2, resultado.size());
        assertEquals(1, resultado.get(0).get("mes"));
        assertEquals(5L, resultado.get(0).get("total"));
        assertEquals(2, resultado.get(1).get("mes"));
        assertEquals(3L, resultado.get(1).get("total"));
    }

    @Test
    void ticketsPorMes_listaVacia_debeRetornarListaVacia() {
        when(ticketRepository.contarPorMes(1L, 2025)).thenReturn(List.of());

        List<Map<String, Object>> resultado = reporteService.ticketsPorMes(1L, 2025);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void contarUsuariosActivos_debeRetornarMapa() {
        Usuario u1 = Usuario.builder().id(1L).nombres("Juan").build();
        Usuario u2 = Usuario.builder().id(2L).nombres("Maria").build();

        when(usuarioRepository.findByEmpresaIdAndActivo(1L, true))
                .thenReturn(List.of(u1, u2));

        Map<String, Object> resultado = reporteService.contarUsuariosActivos(1L);

        assertEquals(1L, resultado.get("empresaId"));
        assertEquals(2L, resultado.get("usuariosActivos"));
    }

    @Test
    void contarUsuariosActivos_sinUsuarios_debeRetornarCero() {
        when(usuarioRepository.findByEmpresaIdAndActivo(1L, true)).thenReturn(List.of());

        Map<String, Object> resultado = reporteService.contarUsuariosActivos(1L);

        assertEquals(0L, resultado.get("usuariosActivos"));
    }
}