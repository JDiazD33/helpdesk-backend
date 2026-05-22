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
}
