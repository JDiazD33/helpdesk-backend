package com.helpdesk.helpdesk_backend.service;

import java.util.List;
import java.util.Map;

public interface ReporteService {

    List<Map<String, Object>> ticketsPorMes(Long empresaId, int anio, Integer mes);

    List<Map<String, Object>> ticketsPorMesGlobal(int anio, Integer mes);

    Map<String, Object> contarUsuariosActivos(Long empresaId);

    Map<String, Object> contarUsuariosActivosGlobal();

    Map<String, Object> dashboard(Long empresaId, int anio);

    Map<String, Object> dashboardGlobal(int anio);
}
