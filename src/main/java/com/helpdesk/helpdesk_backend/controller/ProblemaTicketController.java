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

import com.helpdesk.helpdesk_backend.dto.ProblemaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ProblemaResponseDTO;
import com.helpdesk.helpdesk_backend.service.ProblemaTicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/problemas")
@RequiredArgsConstructor
@Tag(name = "Problemas de Tickets", description = "Endpoints para gestionar los problemas especificos por categoria (Maestros)")
public class ProblemaTicketController {

    private final ProblemaTicketService problemaService;

    @Operation(summary = "Listar problemas por categoría", description = "Devuelve los problemas activos asociados a una categoría específica.")
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProblemaResponseDTO>> listarProblemasPorCategoria(
            @PathVariable Long categoriaId,
            @RequestHeader("X-Empresa-Id") Long empresaId) {
        return ResponseEntity.ok(problemaService.listarProblemasPorCategoria(categoriaId, empresaId));
    }

    @Operation(summary = "Crear un nuevo problema")
    @PostMapping
    public ResponseEntity<ProblemaResponseDTO> crearProblema(
            @Valid @RequestBody ProblemaRequestDTO requestDTO,
            @RequestHeader("X-Empresa-Id") Long empresaId) {
        ProblemaResponseDTO nuevoProblema = problemaService.crearProblema(requestDTO, empresaId);
        return new ResponseEntity<>(nuevoProblema, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar un problema existente")
    @PutMapping("/{id}")
    public ResponseEntity<ProblemaResponseDTO> actualizarProblema(
            @PathVariable Long id,
            @Valid @RequestBody ProblemaRequestDTO requestDTO,
            @RequestHeader("X-Empresa-Id") Long empresaId) {
        return ResponseEntity.ok(problemaService.actualizarProblema(id, requestDTO, empresaId));
    }

    @Operation(summary = "Desactivar un problema (Borrado lógico)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProblema(
            @PathVariable Long id,
            @RequestHeader("X-Empresa-Id") Long empresaId) {
        problemaService.eliminarProblema(id, empresaId);
        return ResponseEntity.noContent().build();
    }
}
