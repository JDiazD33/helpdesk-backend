package com.helpdesk.helpdesk_backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.helpdesk_backend.dto.AsignarAgenteRequestDTO;
import com.helpdesk.helpdesk_backend.dto.CambiarEstadoRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketResponseDTO;
import com.helpdesk.helpdesk_backend.service.TicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

//Controlador REST para gestionar los endpoints relacionados con los tickets
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "TicketController", description = "Operaciones principales de la Mesa de Ayuda")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @Operation(summary = "Crear un nuevo ticket")
    public ResponseEntity<TicketResponseDTO> crearTicket(
            @Valid @RequestBody TicketRequestDTO requestDTO, 
            @RequestHeader ("X-Empresa-Id") Long empresaId, 
            @RequestHeader ("x-Usuario-Id") Long clienteId){
        TicketResponseDTO response = ticketService.crearTicket(requestDTO, clienteId, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un ticket por su ID")
    public ResponseEntity<TicketResponseDTO> obtenerPorId(
            @PathVariable Long id, 
            @RequestHeader ("X-Empresa-Id") Long empresaId){
        return ResponseEntity.ok(ticketService.obtenerPorId(id, empresaId));
    } 

    @PatchMapping("/{id}/asignar")
    @Operation(summary = "Asignar un ticket a un agente")
    public ResponseEntity<TicketResponseDTO> asignarAgente(
            @PathVariable Long id,
            @Valid @RequestBody AsignarAgenteRequestDTO requestDTO,
            @RequestHeader ("X-Empresa-Id") Long empresaId){
        return ResponseEntity.ok(ticketService.asignarAgente(id, requestDTO.getAgenteId(), empresaId));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar el estado de un ticket (Requiere justificación para cerrar)")
    public ResponseEntity<TicketResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequestDTO requestDTO,
            @RequestHeader("X-Empresa-Id") Long empresaId){
        return ResponseEntity.ok(ticketService.cambiarEstado(
            id, 
            requestDTO.getEstado(), 
            requestDTO.getJustificacionCierre(), 
            empresaId
        ));
    }

    // --- ENDPOINTS DE LISTADOS (Paginados) ---

    @GetMapping
    @Operation(summary = "Listar todos los tickets de la empresa (Vista Admin/Supervisor)")
    public ResponseEntity<Page<TicketResponseDTO>> listarTodos(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PageableDefault(size = 20) Pageable pageable){
        return ResponseEntity.ok(ticketService.listarTodosPorEmpresa(empresaId, pageable));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar tickets de un cliente específico (Vista Portal Cliente)")
    public ResponseEntity<Page<TicketResponseDTO>> listarPorCliente(
            @PathVariable Long clienteId,
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PageableDefault(size = 20) Pageable pageable){
        return ResponseEntity.ok(ticketService.listarPorCliente(clienteId, empresaId, pageable));
    }

    @GetMapping("/agente/{agenteId}")
    @Operation(summary = "Listar tickets asignados a un agente (Vista Bandeja de Entrada)")
    public ResponseEntity<Page<TicketResponseDTO>> listarPorAgente(
            @PathVariable Long agenteId,
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ticketService.listarPorAgente(agenteId, empresaId, pageable));
    }

    @GetMapping("/no-asignados")
    @Operation(summary = "Listar tickets sin agente asignado (Cola de Dispatch)")
    public ResponseEntity<Page<TicketResponseDTO>> listarNoAsignados(
            @RequestHeader("X-Empresa-Id") Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ticketService.listarNoAsignados(empresaId, pageable));
    }
}
