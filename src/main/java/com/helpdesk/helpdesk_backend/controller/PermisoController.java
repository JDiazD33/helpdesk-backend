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

import com.helpdesk.helpdesk_backend.model.Permiso;
import com.helpdesk.helpdesk_backend.service.PermisoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/permisos")
public class PermisoController {

    private final PermisoService permisoService;

    public PermisoController(PermisoService permisoService) {
        this.permisoService = permisoService;
    }

    @GetMapping
    public ResponseEntity<List<Permiso>> listarTodos() {
        return ResponseEntity.ok(permisoService.listarTodos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Permiso>> listarActivos() {
        return ResponseEntity.ok(permisoService.listarActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Permiso> buscarPorId(@PathVariable Long id) {
        return permisoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Permiso> guardar(@Valid @RequestBody Permiso permiso) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permisoService.guardar(permiso));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Permiso> actualizar(@PathVariable Long id, @Valid @RequestBody Permiso permiso) {
        return ResponseEntity.ok(permisoService.actualizar(id, permiso));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        permisoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Permiso>> buscarPorTexto(@RequestParam String texto) {
        return ResponseEntity.ok(permisoService.buscarPorTexto(texto));
    }
}
