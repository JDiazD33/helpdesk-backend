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

import com.helpdesk.helpdesk_backend.dto.UsuarioRolDTO;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.security.TenantSecurity;
import com.helpdesk.helpdesk_backend.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final TenantSecurity tenant;

    public UsuarioController(UsuarioService usuarioService, TenantSecurity tenant) {
        this.usuarioService = usuarioService;
        this.tenant = tenant;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<Long> roles,
            @RequestParam(required = false) Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(usuarioService.listarConFiltros(resolvedId, search, roles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        Long empresaId = tenant.getEmpresaId();
        return usuarioService.buscarPorIdAndEmpresa(id, empresaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Usuario> buscarPorEmail(@PathVariable String email) {
        return usuarioService.buscarPorEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<Usuario>> listarPorEmpresa(@PathVariable Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(usuarioService.listarPorEmpresa(resolvedId));
    }

    @GetMapping("/rol/{rolId}")
    public ResponseEntity<List<Usuario>> listarPorRol(@PathVariable Long rolId) {
        return ResponseEntity.ok(usuarioService.listarPorRol(rolId));
    }

    @GetMapping("/estado")
    public ResponseEntity<List<Usuario>> listarPorEstado(@RequestParam boolean activo) {
        return ResponseEntity.ok(usuarioService.listarPorEstado(activo));
    }

    // ── Endpoints nuevos: filtros que faltaban ──

    @GetMapping("/empresa/{empresaId}/activos")
    public ResponseEntity<List<Usuario>> listarActivosPorEmpresa(
            @PathVariable Long empresaId,
            @RequestParam(defaultValue = "true") boolean activo) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(usuarioService.listarActivosPorEmpresa(resolvedId, activo));
    }

    @GetMapping("/empresa/{empresaId}/rol/{rolId}")
    public ResponseEntity<List<Usuario>> listarPorEmpresaYRol(
            @PathVariable Long empresaId,
            @PathVariable Long rolId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(usuarioService.listarPorEmpresaYRol(resolvedId, rolId));
    }

    // ── Endpoints con queries JPQL personalizadas ──

    /** Usuarios activos con datos precargados (JOIN FETCH) */
    @GetMapping("/empresa/{empresaId}/detalle")
    public ResponseEntity<List<Usuario>> listarActivosConDetalles(@PathVariable Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(usuarioService.listarActivosPorEmpresaConDetalles(resolvedId));
    }

    /** Buscar usuarios por nombre o apellido */
    @GetMapping("/empresa/{empresaId}/buscar")
    public ResponseEntity<List<Usuario>> buscarPorNombre(
            @PathVariable Long empresaId,
            @RequestParam String termino) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(usuarioService.buscarPorNombreOApellido(resolvedId, termino));
    }

    @GetMapping("/conteo-rol")
    public ResponseEntity<List<UsuarioRolDTO>> contarPorRol(@RequestParam(required = false) Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(usuarioService.contarUsuariosPorRol(resolvedId));
    }

    @GetMapping("/empresa/{empresaId}/agentes")
    public ResponseEntity<List<Usuario>> listarAgentesActivos(@PathVariable Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(usuarioService.listarAgentesActivos(resolvedId));
    }

    // ── CRUD ──

    @PostMapping
    public ResponseEntity<Usuario> guardar(@Valid @RequestBody Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.guardar(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Long id, @Valid @RequestBody Usuario usuario) {
        Long empresaId = tenant.getEmpresaId();
        return ResponseEntity.ok(usuarioService.actualizar(id, empresaId, usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Long empresaId = tenant.getEmpresaId();
        usuarioService.eliminar(id, empresaId);
        return ResponseEntity.noContent().build();
    }
}
