package com.helpdesk.helpdesk_backend.controller;

import java.util.List;

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

import com.helpdesk.helpdesk_backend.dto.CategoriaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.CategoriaResponseDTO;
import com.helpdesk.helpdesk_backend.dto.PageResponse;
import com.helpdesk.helpdesk_backend.security.TenantSecurity;
import com.helpdesk.helpdesk_backend.service.CategoriaTicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaTicketController {

    private final CategoriaTicketService categoriaTicketService;
    private final TenantSecurity tenant;

    public CategoriaTicketController(CategoriaTicketService categoriaTicketService, TenantSecurity tenant) {
        this.categoriaTicketService = categoriaTicketService;
        this.tenant = tenant;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarTodos(@RequestParam Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(categoriaTicketService.listarTodas(resolvedId));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<CategoriaResponseDTO>> listarPorEmpresa(@PathVariable Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(categoriaTicketService.listarTodas(resolvedId));
    }

    @GetMapping("/todas")
    public ResponseEntity<List<CategoriaResponseDTO>> listarTodasGlobal() {
        return ResponseEntity.ok(categoriaTicketService.listarTodasGlobal());
    }

    @GetMapping("/paginado")
    public ResponseEntity<PageResponse<CategoriaResponseDTO>> listarPaginado(
            @RequestParam Long empresaId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) String search) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(categoriaTicketService.buscarPaginado(resolvedId, page, limit, search));
    }

    @GetMapping("/paginado/todas")
    public ResponseEntity<PageResponse<CategoriaResponseDTO>> listarPaginadoGlobal(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(categoriaTicketService.buscarPaginadoGlobal(page, limit, search));
    }

    @GetMapping("/activas")
    public ResponseEntity<List<CategoriaResponseDTO>> listarPorActiva(@RequestParam Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(categoriaTicketService.listarCategoriasActivas(resolvedId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> obtenerPorId(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(categoriaTicketService.obtenerPorId(id, resolvedId));
    }

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> guardar(@Valid @RequestBody CategoriaRequestDTO requestDTO, @RequestParam Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaTicketService.crearCategoria(requestDTO, resolvedId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequestDTO requestDTO, @RequestParam Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(categoriaTicketService.actualizarCategoria(id, requestDTO, resolvedId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @RequestParam Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        categoriaTicketService.eliminarCategoria(id, resolvedId);
        return ResponseEntity.noContent().build();
    }
}
