package com.helpdesk.helpdesk_backend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.helpdesk_backend.dto.AsignarAgenteRequestDTO;
import com.helpdesk.helpdesk_backend.dto.CambiarEstadoRequestDTO;
import com.helpdesk.helpdesk_backend.dto.CalificacionRequestDTO;
import com.helpdesk.helpdesk_backend.dto.CierreRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketConComentarioRequestDTO;
import com.helpdesk.helpdesk_backend.model.Ticket;
import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;
import com.helpdesk.helpdesk_backend.model.enums.PrioridadTicket;
import com.helpdesk.helpdesk_backend.security.TenantSecurity;
import com.helpdesk.helpdesk_backend.service.FileStorageService;
import com.helpdesk.helpdesk_backend.service.TicketService;

import jakarta.validation.Valid;

//Controlador REST para gestionar los endpoints relacionados con los tickets
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    // Inyeccion de dependencias del servicio de tickets
    private final TicketService ticketService;
    private final FileStorageService fileStorageService;
    private final TenantSecurity tenant;

    // Constructor para inyectar el servicio de tickets
    public TicketController(TicketService ticketService, FileStorageService fileStorageService,
                            TenantSecurity tenant) {
        this.ticketService = ticketService;
        this.fileStorageService = fileStorageService;
        this.tenant = tenant;
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> listarTodos(
            @RequestParam(required = false) Boolean agente) {
        return ResponseEntity.ok(ticketService.listarTodos(agente));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> buscarPorId(@PathVariable Long id) {
        Long empresaId = tenant.getEmpresaId();
        return ticketService.buscarPorIdAndEmpresa(id, empresaId)
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
    public ResponseEntity<List<Ticket>> listarPorEmpresa(
            @PathVariable Long empresaId,
            @RequestParam(required = false) Boolean agente) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(ticketService.listarPorEmpresaId(resolvedId, agente));
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
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(ticketService.listarPorEmpresaConDetalles(resolvedId));
    }

    /** Filtro combinado: empresa + estado (opcional) + prioridad (opcional) */
    @GetMapping("/empresa/{empresaId}/filtrar")
    public ResponseEntity<List<Ticket>> filtrarTickets(
            @PathVariable Long empresaId,
            @RequestParam(required = false) EstadoTicket estado,
            @RequestParam(required = false) PrioridadTicket prioridad) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(ticketService.filtrarTickets(resolvedId, estado, prioridad));
    }

    /** Conteo de tickets por estado para dashboard */
    @GetMapping("/empresa/{empresaId}/conteo")
    public ResponseEntity<List<Object[]>> contarPorEstado(@PathVariable Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(ticketService.contarPorEstado(resolvedId));
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
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        LocalDateTime desde = inicio.atStartOfDay();
        LocalDateTime hasta = fin.atTime(LocalTime.MAX);
        return ResponseEntity.ok(ticketService.listarPorEmpresaYPeriodo(resolvedId, desde, hasta));
    }

    @GetMapping("/empresa/{empresaId}/prioridad-alta")
    public ResponseEntity<List<Ticket>> listarPrioridadAlta(@PathVariable Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(ticketService.listarPrioridadAltaPorEmpresa(resolvedId));
    }

    @GetMapping("/prioridad-alta")
    public ResponseEntity<List<Ticket>> listarPrioridadAltaGlobal() {
        return ResponseEntity.ok(ticketService.listarPrioridadAltaGlobal());
    }

    // ── 4 nuevas consultas JPQL ──

    @GetMapping("/empresa/{empresaId}/buscar")
    public ResponseEntity<List<Ticket>> buscarPorTexto(
            @PathVariable Long empresaId,
            @RequestParam String texto) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(ticketService.buscarPorTexto(resolvedId, texto));
    }

    @GetMapping("/empresa/{empresaId}/sin-asignar")
    public ResponseEntity<List<Ticket>> listarSinAsignar(@PathVariable Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(ticketService.listarSinAsignar(resolvedId));
    }

    @GetMapping("/sin-asignar")
    public ResponseEntity<List<Ticket>> listarSinAsignarGlobal() {
        return ResponseEntity.ok(ticketService.listarSinAsignarGlobal());
    }

    @GetMapping("/periodo")
    public ResponseEntity<List<Ticket>> listarPorPeriodoGlobal(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        LocalDateTime desde = inicio.atStartOfDay();
        LocalDateTime hasta = fin.atTime(LocalTime.MAX);
        return ResponseEntity.ok(ticketService.listarPorPeriodoGlobal(desde, hasta));
    }

    @GetMapping("/cliente/{clienteId}/empresa/{empresaId}/detalle")
    public ResponseEntity<List<Ticket>> listarPorClienteConDetalles(
            @PathVariable Long clienteId,
            @PathVariable Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(ticketService.listarPorClienteConDetalles(clienteId, resolvedId));
    }

    @GetMapping("/empresa/{empresaId}/actualizados")
    public ResponseEntity<List<Ticket>> listarActualizadosRecientemente(
            @PathVariable Long empresaId,
            @RequestParam(defaultValue = "7") int dias) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(ticketService.listarActualizadosRecientemente(resolvedId, dias));
    }

    // ── Cambio de estado ──

    @PutMapping("/{id}/estado")
    public ResponseEntity<Ticket> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoRequestDTO request) {
        Long empresaId = tenant.getEmpresaId();
        return ResponseEntity.ok(ticketService.cambiarEstado(id, empresaId, request));
    }

    // ── Asignar agente ──

    @PutMapping("/{id}/asignar")
    public ResponseEntity<Ticket> asignarAgente(
            @PathVariable Long id,
            @Valid @RequestBody AsignarAgenteRequestDTO request) {
        Long empresaId = tenant.getEmpresaId();
        return ResponseEntity.ok(ticketService.asignarAgente(id, empresaId, request.getAgenteId()));
    }

    // ── Subir imagen de cierre ──

    @PostMapping("/{id}/imagen-cierre")
    public ResponseEntity<Map<String, String>> subirImagenCierre(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        String contentType = file.getContentType();
        if (!List.of("image/jpeg", "image/png", "image/webp").contains(contentType)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tipo de archivo no permitido. Solo JPG, PNG o WEBP."));
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo supera los 5MB."));
        }
        String ruta = fileStorageService.guardarImagen(file, id);
        return ResponseEntity.ok(Map.of("imagenCierre", ruta));
    }

    // ── Guardar cierre (justificación + imagen) ──

    @PatchMapping("/{id}/cierre")
    public ResponseEntity<Ticket> guardarCierre(
            @PathVariable Long id,
            @Valid @RequestBody CierreRequestDTO request) {
        Long empresaId = tenant.getEmpresaId();
        return ResponseEntity.ok(ticketService.guardarCierre(id, empresaId, request));
    }

    // ── Calificacion del agente por parte del cliente ──

    /**
     * El cliente dueño califica (1-5 estrellas) la atención de un ticket resuelto.
     * La validación de que sea el cliente dueño se hace en el service.
     */
    @PostMapping("/{id}/calificar")
    public ResponseEntity<Ticket> calificarTicket(
            @PathVariable Long id,
            @Valid @RequestBody CalificacionRequestDTO request) {
        Long empresaId = tenant.getEmpresaId();
        return ResponseEntity.ok(ticketService.calificarTicket(id, empresaId, request));
    }

    /**
     * Ranking de mejores agentes por promedio de calificacion.
     * Cada fila: [agenteId, nombres, apellidos, promedioCalificacion, totalTickets].
     * empresaId opcional (la vista global del ADMIN_OWNER no lo envía).
     */
    @GetMapping("/ranking-agentes")
    public ResponseEntity<List<Object[]>> rankingMejoresAgentes(
            @RequestParam(required = false) Long empresaId) {
        Long resolvedId = tenant.resolveEmpresaId(empresaId);
        return ResponseEntity.ok(ticketService.rankingMejoresAgentes(resolvedId));
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
        Long empresaId = tenant.getEmpresaId();
        return ResponseEntity.ok(ticketService.actualizar(id, empresaId, ticket));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Long empresaId = tenant.getEmpresaId();
        ticketService.eliminar(id, empresaId);
        return ResponseEntity.noContent().build();
    }
}
