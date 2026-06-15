package com.helpdesk.helpdesk_backend.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.helpdesk_backend.constants.RolConstants;
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
    public List<Map<String, Object>> ticketsPorMes(Long empresaId, int anio, Integer mes) {
        List<Object[]> filas = (mes != null)
                ? ticketRepository.contarPorMesYMes(empresaId, anio, mes)
                : ticketRepository.contarPorMes(empresaId, anio);
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
    public List<Map<String, Object>> ticketsPorMesGlobal(int anio, Integer mes) {
        List<Object[]> filas = (mes != null)
                ? ticketRepository.contarPorMesGlobalYMes(anio, mes)
                : ticketRepository.contarPorMesGlobal(anio);
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Object[] fila : filas) {
            Map<String, Object> item = new HashMap<>();
            item.put("mes", fila[0]);
            item.put("total", fila[1]);
            resultado.add(item);
        }
        return resultado;
    }

    /**
     * Conteo de usuarios activos de un tenant, EXCLUYENDO al ADMIN_OWNER.
     * El owner está artificialmente asociado a una empresa por la restricción
     * NOT NULL, pero NO pertenece lógicamente a ningún tenant.
     */
    @Override
    public Map<String, Object> contarUsuariosActivos(Long empresaId) {
        long total = usuarioRepository
                .findByEmpresaIdAndActivoExcluyendoOwner(empresaId, true, RolConstants.ADMIN_OWNER)
                .size();
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("empresaId", empresaId);
        resultado.put("usuariosActivos", total);
        return resultado;
    }

    /**
     * Conteo global de usuarios activos para el dashboard del ADMIN_OWNER.
     * Aquí SÍ se incluye al owner (es la vista global).
     */
    @Override
    public Map<String, Object> contarUsuariosActivosGlobal() {
        long total = usuarioRepository.findByActivo(true).size();
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("usuariosActivos", total);
        return resultado;
    }

    /**
     * Dashboard de un tenant. Todas las métricas de USUARIOS excluyen al ADMIN_OWNER.
     * Las métricas de tickets no requieren el filtro (los tickets no involucran al owner).
     */
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

        stats.put("ticketsPorMes", ticketsPorMes(empresaId, anio, null));

        long usuariosActivos = usuarioRepository
                .findByEmpresaIdAndActivoExcluyendoOwner(empresaId, true, RolConstants.ADMIN_OWNER)
                .size();
        stats.put("usuariosActivos", usuariosActivos);
        stats.put("totalUsuarios", (long) usuarioRepository
                .findByEmpresaIdExcluyendoOwner(empresaId, RolConstants.ADMIN_OWNER)
                .size());

        return stats;
    }

    /**
     * Dashboard global del ADMIN_OWNER. Incluye al owner en los conteos
     * porque es la vista multi-tenant.
     */
    @Override
    public Map<String, Object> dashboardGlobal(int anio) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("anio", anio);

        List<Object[]> porEstado = ticketRepository.contarPorEstadoGlobal();
        Map<String, Long> estadoMap = new HashMap<>();
        for (Object[] fila : porEstado) {
            estadoMap.put(fila[0].toString(), (Long) fila[1]);
        }
        stats.put("ticketsPorEstado", estadoMap);

        List<Object[]> filas = ticketRepository.contarPorMesGlobal(anio);
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Object[] fila : filas) {
            Map<String, Object> item = new HashMap<>();
            item.put("mes", fila[0]);
            item.put("total", fila[1]);
            resultado.add(item);
        }
        stats.put("ticketsPorMes", resultado);

        long usuariosActivos = usuarioRepository.findByActivo(true).size();
        stats.put("usuariosActivos", usuariosActivos);
        stats.put("totalUsuarios", usuarioRepository.count());

        return stats;
    }
}
