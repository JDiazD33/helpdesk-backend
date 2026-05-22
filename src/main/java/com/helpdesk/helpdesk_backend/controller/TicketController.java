package com.helpdesk.helpdesk_backend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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

import com.helpdesk.helpdesk_backend.dto.TicketConComentarioRequestDTO;
import com.helpdesk.helpdesk_backend.model.Ticket;
import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;
import com.helpdesk.helpdesk_backend.model.enums.PrioridadTicket;
import com.helpdesk.helpdesk_backend.service.TicketService;

import jakarta.validation.Valid;

//Controlador REST para gestionar los endpoints relacionados con los tickets
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    // Inyección de dependencias del servicio de tickets
    private final TicketService ticketService;

    // Constructor para inyectar el servicio de tickets
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> listarTodos() {
        return ResponseEntity.ok(ticketService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> buscarPorId(@PathVariable Long id) {
        return ticketService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<Ticket> buscarPorCodigo(@PathVariable String codigo) {
        return ticketService.buscarPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<Ticket>> listarPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(ticketService.listarPorEmpresaId(empresaId));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Ticket>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(ticketService.listarPorClienteId(clienteId));
    }

    @GetMapping("/agente/{agenteId}")
    public ResponseEntity<List<Ticket>> listarPorAgente(@PathVariable Long agenteId) {
        return ResponseEntity.ok(ticketService.listarPorAgenteAsignadoId(agenteId));
    }

    // ── Endpoints de filtros ──

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Ticket>> listarPorEstado(@PathVariable EstadoTicket estado) {
        return ResponseEntity.ok(ticketService.listarPorEstado(estado));
    }

    @GetMapping("/prioridad/{prioridad}")
    public ResponseEntity<List<Ticket>> listarPorPrioridad(@PathVariable PrioridadTicket prioridad) {
        return ResponseEntity.ok(ticketService.listarPorPrioridad(prioridad));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Ticket>> listarPorCategoria(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(ticketService.listarPorCategoriaId(categoriaId));
    }

    // ── Endpoints con queries JPQL personalizadas ──

    /** Tickets de una empresa con datos del cliente, ordenados por fecha DESC */
    @GetMapping("/empresa/{empresaId}/detalle")
    public ResponseEntity<List<Ticket>> listarPorEmpresaConDetalles(@PathVariable Long empresaId) {
        return ResponseEntity.ok(ticketService.listarPorEmpresaConDetalles(empresaId));
    }

    /** Filtro combinado: empresa + estado (opcional) + prioridad (opcional) */
    @GetMapping("/empresa/{empresaId}/filtrar")
    public ResponseEntity<List<Ticket>> filtrarTickets(
            @PathVariable Long empresaId,
            @RequestParam(required = false) EstadoTicket estado,
            @RequestParam(required = false) PrioridadTicket prioridad) {
        return ResponseEntity.ok(ticketService.filtrarTickets(empresaId, estado, prioridad));
    }

    /** Conteo de tickets por estado para dashboard */
    @GetMapping("/empresa/{empresaId}/conteo")
    public ResponseEntity<List<Object[]>> contarPorEstado(@PathVariable Long empresaId) {
        return ResponseEntity.ok(ticketService.contarPorEstado(empresaId));
    }

    /** Tickets de un agente filtrados por estado */
    @GetMapping("/agente/{agenteId}/estado/{estado}")
    public ResponseEntity<List<Ticket>> listarPorAgenteYEstado(
            @PathVariable Long agenteId,
            @PathVariable EstadoTicket estado) {
        return ResponseEntity.ok(ticketService.listarPorAgenteYEstado(agenteId, estado));
    }

    @GetMapping("/empresa/{empresaId}/periodo")
    public ResponseEntity<List<Ticket>> listarPorPeriodo(
            @PathVariable Long empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        LocalDateTime desde = inicio.atStartOfDay();
        LocalDateTime hasta = fin.atTime(LocalTime.MAX);
        return ResponseEntity.ok(ticketService.listarPorEmpresaYPeriodo(empresaId, desde, hasta));
    }

    @GetMapping("/empresa/{empresaId}/prioridad-alta")
    public ResponseEntity<List<Ticket>> listarPrioridadAlta(@PathVariable Long empresaId) {
        return ResponseEntity.ok(ticketService.listarPrioridadAltaPorEmpresa(empresaId));
    }

    // ── CRUD ──

    @PostMapping
    public ResponseEntity<Ticket> guardar(@Valid @RequestBody Ticket ticket) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.guardar(ticket));
    }

    @PostMapping("/completo")
    public ResponseEntity<Ticket> guardarConComentario(@Valid @RequestBody TicketConComentarioRequestDTO request) {
        Ticket creado = ticketService.guardarConComentarioInicial(
                request.getTicket(),
                request.getMensajeInicial(),
                request.getUsuarioComentarioId());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> actualizar(@PathVariable Long id, @Valid @RequestBody Ticket ticket) {
        return ResponseEntity.ok(ticketService.actualizar(id, ticket));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ticketService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
