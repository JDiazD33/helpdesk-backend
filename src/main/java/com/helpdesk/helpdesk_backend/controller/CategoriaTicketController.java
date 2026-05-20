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
import com.helpdesk.helpdesk_backend.service.CategoriaTicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaTicketController {

    private final CategoriaTicketService categoriaTicketService;

    public CategoriaTicketController(CategoriaTicketService categoriaTicketService) {
        this.categoriaTicketService = categoriaTicketService;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarTodos(@RequestParam Long empresaId) {
        return ResponseEntity.ok(categoriaTicketService.listarTodas(empresaId));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<CategoriaResponseDTO>> listarPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(categoriaTicketService.listarTodas(empresaId));
    }

    @GetMapping("/activas")
    public ResponseEntity<List<CategoriaResponseDTO>> listarPorActiva(@RequestParam Long empresaId) {
        return ResponseEntity.ok(categoriaTicketService.listarCategoriasActivas(empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> obtenerPorId(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        return ResponseEntity.ok(categoriaTicketService.obtenerPorId(id, empresaId));
    }

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> guardar(@Valid @RequestBody CategoriaRequestDTO requestDTO, @RequestParam Long empresaId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaTicketService.crearCategoria(requestDTO, empresaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequestDTO requestDTO, @RequestParam Long empresaId) {
        return ResponseEntity.ok(categoriaTicketService.actualizarCategoria(id, requestDTO, empresaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @RequestParam Long empresaId) {
        categoriaTicketService.eliminarCategoria(id, empresaId);
        return ResponseEntity.noContent().build();
    }
}
