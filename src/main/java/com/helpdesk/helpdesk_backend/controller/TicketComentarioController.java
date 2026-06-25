package com.helpdesk.helpdesk_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.helpdesk_backend.dto.ComentarioRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ComentarioResponseDTO;
import com.helpdesk.helpdesk_backend.security.TenantSecurity;
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
    private final TenantSecurity tenant;

    @PostMapping
    @Operation(summary = "Agregar un comentario a un ticket")
    public ResponseEntity<ComentarioResponseDTO> agregarComentario(
            @Valid @RequestBody ComentarioRequestDTO requestDTO) {
        Long empresaId = tenant.getEmpresaId();
        Long usuarioId = tenant.getUserId();
        ComentarioResponseDTO response = comentarioService.agregarComentario(requestDTO, usuarioId, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/ticket/{ticketId}")
    @Operation(summary = "Listar todos los comentarios de un ticket")
    public ResponseEntity<List<ComentarioResponseDTO>> listarComentariosPorTicket(
            @PathVariable Long ticketId) {
        Long empresaId = tenant.getEmpresaId();
        return ResponseEntity.ok(comentarioService.listarComentariosPorTicket(ticketId, empresaId));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar comentarios de un usuario")
    public ResponseEntity<List<ComentarioResponseDTO>> listarPorUsuario(@PathVariable Long usuarioId) {
        Long empresaId = tenant.getEmpresaId();
        return ResponseEntity.ok(comentarioService.listarPorUsuario(usuarioId, empresaId));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar comentarios por texto")
    public ResponseEntity<List<ComentarioResponseDTO>> buscarPorTexto(@RequestParam String texto) {
        Long empresaId = tenant.getEmpresaId();
        return ResponseEntity.ok(comentarioService.buscarPorTexto(texto, empresaId));
    }

    @GetMapping("/ranking-usuarios")
    @Operation(summary = "Ranking de usuarios con mas comentarios")
    public ResponseEntity<List<Map<String, Object>>> rankingUsuarios(@RequestParam(required = false) Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(comentarioService.rankingUsuariosComentarios(resolvedId));
    }

    @GetMapping("/empresa/{empresaId}/recientes")
    @Operation(summary = "Comentarios recientes de una empresa")
    public ResponseEntity<List<ComentarioResponseDTO>> comentariosRecientes(
            @PathVariable Long empresaId,
            @RequestParam(defaultValue = "7") int dias) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(comentarioService.comentariosRecientesEmpresa(resolvedId, dias));
    }
}
