package com.helpdesk.helpdesk_backend.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.helpdesk_backend.repository.TicketRepository;
import com.helpdesk.helpdesk_backend.repository.UsuarioRepository;
import com.helpdesk.helpdesk_backend.service.ReporteService;

@Service
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;

    public ReporteServiceImpl(TicketRepository ticketRepository, UsuarioRepository usuarioRepository) {
        this.ticketRepository = ticketRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Map<String, Object>> ticketsPorMes(Long empresaId, int anio) {
        List<Object[]> filas = ticketRepository.contarPorMes(empresaId, anio);
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Object[] fila : filas) {
            Map<String, Object> item = new HashMap<>();
            item.put("mes", fila[0]);
            item.put("total", fila[1]);
            resultado.add(item);
        }
        return resultado;
    }

    @Override
    public Map<String, Object> contarUsuariosActivos(Long empresaId) {
        long total = usuarioRepository.findByEmpresaIdAndActivo(empresaId, true).size();
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("empresaId", empresaId);
        resultado.put("usuariosActivos", total);
        return resultado;
    }

    @Override
    public Map<String, Object> dashboard(Long empresaId, int anio) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("empresaId", empresaId);
        stats.put("anio", anio);

        List<Object[]> porEstado = ticketRepository.contarPorEstado(empresaId);
        Map<String, Long> estadoMap = new HashMap<>();
        for (Object[] fila : porEstado) {
            estadoMap.put(fila[0].toString(), (Long) fila[1]);
        }
        stats.put("ticketsPorEstado", estadoMap);

        stats.put("ticketsPorMes", ticketsPorMes(empresaId, anio));

        long usuariosActivos = usuarioRepository.findByEmpresaIdAndActivo(empresaId, true).size();
        stats.put("usuariosActivos", usuariosActivos);
        stats.put("totalUsuarios", (long) usuarioRepository.findByEmpresaId(empresaId).size());

        return stats;
    }
}
