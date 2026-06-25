package com.helpdesk.helpdesk_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.helpdesk_backend.security.TenantSecurity;
import com.helpdesk.helpdesk_backend.service.ReporteService;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;
    private final TenantSecurity tenant;

    public ReporteController(ReporteService reporteService, TenantSecurity tenant) {
        this.reporteService = reporteService;
        this.tenant = tenant;
    }

    @GetMapping("/tickets-mes")
    public ResponseEntity<List<Map<String, Object>>> ticketsPorMes(
            @RequestParam Long empresaId,
            @RequestParam int anio,
            @RequestParam(required = false) Integer mes) {
        // Sanitiza el empresaId: el ADMIN_OWNER puede indicar cualquier empresa,
        // los demás roles quedan forzados al de su JWT.
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(reporteService.ticketsPorMes(resolvedId, anio, mes));
    }

    @GetMapping("/tickets-mes/todas")
    public ResponseEntity<List<Map<String, Object>>> ticketsPorMesGlobal(
            @RequestParam int anio,
            @RequestParam(required = false) Integer mes) {
        return ResponseEntity.ok(reporteService.ticketsPorMesGlobal(anio, mes));
    }

    @GetMapping("/usuarios-activos")
    public ResponseEntity<Map<String, Object>> usuariosActivos(@RequestParam Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(reporteService.contarUsuariosActivos(resolvedId));
    }

    @GetMapping("/usuarios-activos/todas")
    public ResponseEntity<Map<String, Object>> usuariosActivosGlobal() {
        return ResponseEntity.ok(reporteService.contarUsuariosActivosGlobal());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(
            @RequestParam Long empresaId,
            @RequestParam(defaultValue = "2026") int anio) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(reporteService.dashboard(resolvedId, anio));
    }

    @GetMapping("/dashboard/todas")
    public ResponseEntity<Map<String, Object>> dashboardGlobal(
            @RequestParam(defaultValue = "2026") int anio) {
        return ResponseEntity.ok(reporteService.dashboardGlobal(anio));
    }
}
