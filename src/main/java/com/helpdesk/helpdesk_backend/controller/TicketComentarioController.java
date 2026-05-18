package com.helpdesk.helpdesk_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.helpdesk_backend.dto.ComentarioRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ComentarioResponseDTO;
import com.helpdesk.helpdesk_backend.service.TicketComentarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comentarios")
@RequiredArgsConstructor
@Tag(name = "TicketComentarioController", description = "Gestion del hilo de comunicaciones de los tickets")
public class TicketComentarioController {

    private final TicketComentarioService comentarioService;

    @PostMapping
    @Operation(summary = "Agregar un comentario a un ticket")
    public ResponseEntity<ComentarioResponseDTO> agregarComentario(
            @Valid @RequestBody ComentarioRequestDTO requestDTO,
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @RequestHeader("x-Usuario-Id") Long usuarioId) {
        ComentarioResponseDTO response = comentarioService.agregarComentario(requestDTO, usuarioId, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/ticket/{ticketId}")
    @Operation(summary = "Listar todos los comentarios de un ticket")
    public ResponseEntity<List<ComentarioResponseDTO>> listarComentariosPorTicket(
            @PathVariable Long ticketId,
            @RequestHeader("X-Empresa-Id") Long empresaId) {
        return ResponseEntity.ok(comentarioService.listarComentariosPorTicket(ticketId, empresaId));
    }
}
