package com.helpdesk.helpdesk_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.helpdesk_backend.dto.PageResponse;
import com.helpdesk.helpdesk_backend.dto.ProblemaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ProblemaResponseDTO;
import com.helpdesk.helpdesk_backend.security.TenantSecurity;
import com.helpdesk.helpdesk_backend.service.ProblemaTicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/problemas")
public class ProblemaTicketController {

    private final ProblemaTicketService problemaTicketService;
    private final TenantSecurity tenant;

    public ProblemaTicketController(ProblemaTicketService problemaTicketService, TenantSecurity tenant) {
        this.problemaTicketService = problemaTicketService;
        this.tenant = tenant;
    }

    @GetMapping
    public ResponseEntity<List<ProblemaResponseDTO>> listarTodos(@RequestParam(required = false) Long categoriaId) {
        if (categoriaId != null) {
            return ResponseEntity.ok(problemaTicketService.listarTodosPorCategoria(categoriaId));
        }
        return ResponseEntity.ok(problemaTicketService.listarTodosPorCategoria(null));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProblemaResponseDTO>> listarPorCategoria(@PathVariable Long categoriaId, @RequestParam(required = false) Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        if (empresaId == null && !tenant.isAdminOwner()) {
            // No-owner sin empresaId: forzar al tenant del JWT.
            return ResponseEntity.ok(problemaTicketService.listarProblemasPorCategoria(categoriaId, resolvedId));
        }
        return ResponseEntity.ok(problemaTicketService.listarProblemasPorCategoria(categoriaId, resolvedId));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<?> listarActivosPorEmpresa(
            @PathVariable Long empresaId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "0") int limit,
            @RequestParam(required = false) Long categoriaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        if (page > 0 && limit > 0) {
            return ResponseEntity.ok(problemaTicketService.listarPaginado(resolvedId, page, limit, categoriaId));
        }
        return ResponseEntity.ok(problemaTicketService.listarActivosPorEmpresa(resolvedId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemaResponseDTO> obtenerPorId(@PathVariable Long id, @RequestParam Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(problemaTicketService.obtenerPorId(id, resolvedId));
    }

    @PostMapping
    public ResponseEntity<ProblemaResponseDTO> guardar(@Valid @RequestBody ProblemaRequestDTO requestDTO, @RequestParam Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(problemaTicketService.crearProblema(requestDTO, resolvedId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProblemaResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ProblemaRequestDTO requestDTO, @RequestParam Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(problemaTicketService.actualizarProblema(id, requestDTO, resolvedId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @RequestParam Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        problemaTicketService.eliminarProblema(id, resolvedId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conteo-categoria")
    public ResponseEntity<List<Map<String, Object>>> contarPorCategoria(@RequestParam(required = false) Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(problemaTicketService.contarProblemasPorCategoria(resolvedId));
    }

    @GetMapping("/todos")
    public ResponseEntity<PageResponse<ProblemaResponseDTO>> listarTodos(
            @RequestParam int page,
            @RequestParam int limit,
            @RequestParam(required = false) Long categoriaId) {
        return ResponseEntity.ok(problemaTicketService.listarTodosPaginado(page, limit, categoriaId));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProblemaResponseDTO>> buscarPorTexto(@RequestParam String texto) {
        return ResponseEntity.ok(problemaTicketService.buscarPorTexto(texto));
    }
}
