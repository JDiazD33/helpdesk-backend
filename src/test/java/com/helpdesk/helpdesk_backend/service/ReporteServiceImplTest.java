package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.service.impl.ReporteServiceImpl;
import com.helpdesk.helpdesk_backend.repository.TicketRepository;
import com.helpdesk.helpdesk_backend.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import com.helpdesk.helpdesk_backend.model.Usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ReporteServiceImpl reporteService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void ticketsPorMes_DeberiaTransformarFilasEnMap() {
        Object[] fila = new Object[]{1, 5L};
        when(ticketRepository.contarPorMes(10L, 2024)).thenReturn(Collections.singletonList(fila));

        List<Map<String, Object>> resultado = reporteService.ticketsPorMes(10L, 2024);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).get("mes")).isEqualTo(1);
        assertThat(resultado.get(0).get("total")).isEqualTo(5L);
    }

    @Test
    void contarUsuariosActivos_DeberiaContarUsuarios() {
        when(usuarioRepository.findByEmpresaIdAndActivo(20L, true)).thenReturn(List.of(new Usuario(), new Usuario()));

        Map<String, Object> resultado = reporteService.contarUsuariosActivos(20L);

        assertThat(resultado.get("empresaId")).isEqualTo(20L);
        assertThat(resultado.get("usuariosActivos")).isEqualTo(2L);
    }
}
