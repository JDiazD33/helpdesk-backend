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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.helpdesk_backend.dto.CategoriaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.CategoriaResponseDTO;
import com.helpdesk.helpdesk_backend.service.CategoriaTicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorias de Tickets", description = "Endpoints para gestionar las categorias (Maestros)")
public class CategoriaTicketController {

    private final CategoriaTicketService categoriaService;
    
    @Operation(summary = "Listar categorias activas", description = "Devuelve solo las categorías operativas para poblar selectores.")
    @GetMapping("/activas")
    public ResponseEntity<List<CategoriaResponseDTO>> listarCategoriasActivas(@RequestHeader("X-Empresa-Id") Long empresaId){
        return ResponseEntity.ok(categoriaService.listarCategoriasActivas(empresaId));
    }

    @Operation(summary = "Listar todas las categorías", description = "Devuelve el historial completo (activas e inactivas) para administración.")
    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarTodas(@RequestHeader("X-Empresa-Id") Long empresaId){
        return ResponseEntity.ok(categoriaService.listarTodas(empresaId));
    }

    @Operation(summary = "Crear una nueva categoría")
    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> crearCategoria(@Valid @RequestBody CategoriaRequestDTO requestDTO, @RequestHeader("X-Empresa-Id") Long empresaId){
        CategoriaResponseDTO nuevaCategoria = categoriaService.crearCategoria(requestDTO, empresaId);
        return new ResponseEntity<>(nuevaCategoria, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar una categoría existente")
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> actualizarCategoria(@PathVariable Long id, @Valid @RequestBody CategoriaRequestDTO requestDTO, @RequestHeader("X-Empresa-Id") Long empresaId){
        return ResponseEntity.ok(categoriaService.actualizarCategoria(id, requestDTO, empresaId));
    }

    @Operation(summary = "Desactivar una categoría (Borrado lógico)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id, @RequestHeader("X-Empresa-Id") Long empresaId){
        categoriaService.eliminarCategoria(id, empresaId);
        return ResponseEntity.noContent().build();
    } 
}
