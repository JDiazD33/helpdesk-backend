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
import com.helpdesk.helpdesk_backend.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
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
        return ResponseEntity.ok(usuarioService.listarPorEmpresa(empresaId));
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
        return ResponseEntity.ok(usuarioService.listarActivosPorEmpresa(empresaId, activo));
    }

    @GetMapping("/empresa/{empresaId}/rol/{rolId}")
    public ResponseEntity<List<Usuario>> listarPorEmpresaYRol(
            @PathVariable Long empresaId,
            @PathVariable Long rolId) {
        return ResponseEntity.ok(usuarioService.listarPorEmpresaYRol(empresaId, rolId));
    }

    // ── Endpoints con queries JPQL personalizadas ──

    /** Usuarios activos con datos precargados (JOIN FETCH) */
    @GetMapping("/empresa/{empresaId}/detalle")
    public ResponseEntity<List<Usuario>> listarActivosConDetalles(@PathVariable Long empresaId) {
        return ResponseEntity.ok(usuarioService.listarActivosPorEmpresaConDetalles(empresaId));
    }

    /** Buscar usuarios por nombre o apellido */
    @GetMapping("/empresa/{empresaId}/buscar")
    public ResponseEntity<List<Usuario>> buscarPorNombre(
            @PathVariable Long empresaId,
            @RequestParam String termino) {
        return ResponseEntity.ok(usuarioService.buscarPorNombreOApellido(empresaId, termino));
    }

    @GetMapping("/conteo-rol")
    public ResponseEntity<List<UsuarioRolDTO>> contarPorRol() {
        return ResponseEntity.ok(usuarioService.contarUsuariosPorRol());
    }

    @GetMapping("/empresa/{empresaId}/agentes")
    public ResponseEntity<List<Usuario>> listarAgentesActivos(@PathVariable Long empresaId) {
        return ResponseEntity.ok(usuarioService.listarAgentesActivos(empresaId));
    }

    // ── CRUD ──

    @PostMapping
    public ResponseEntity<Usuario> guardar(@Valid @RequestBody Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.guardar(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizar(@PathVariable Long id, @Valid @RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.actualizar(id, usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
