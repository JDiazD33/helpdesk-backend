package com.helpdesk.helpdesk_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.helpdesk_backend.service.ReporteService;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/tickets-mes")
    public ResponseEntity<List<Map<String, Object>>> ticketsPorMes(
            @RequestParam Long empresaId,
            @RequestParam int anio) {
        return ResponseEntity.ok(reporteService.ticketsPorMes(empresaId, anio));
    }

    @GetMapping("/usuarios-activos")
    public ResponseEntity<Map<String, Object>> usuariosActivos(@RequestParam Long empresaId) {
        return ResponseEntity.ok(reporteService.contarUsuariosActivos(empresaId));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(
            @RequestParam Long empresaId,
            @RequestParam(defaultValue = "2026") int anio) {
        return ResponseEntity.ok(reporteService.dashboard(empresaId, anio));
    }
}
