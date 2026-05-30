package com.helpdesk.helpdesk_backend.controller;

import java.util.List;
import java.util.Map;

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

import com.helpdesk.helpdesk_backend.dto.ProblemaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ProblemaResponseDTO;
import com.helpdesk.helpdesk_backend.service.ProblemaTicketService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/problemas")
public class ProblemaTicketController {

    private final ProblemaTicketService problemaTicketService;

    public ProblemaTicketController(ProblemaTicketService problemaTicketService) {
        this.problemaTicketService = problemaTicketService;
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProblemaResponseDTO>> listarPorCategoria(@PathVariable Long categoriaId, @RequestParam Long empresaId) {
        return ResponseEntity.ok(problemaTicketService.listarProblemasPorCategoria(categoriaId, empresaId));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<ProblemaResponseDTO>> listarActivosPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(problemaTicketService.listarActivosPorEmpresa(empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemaResponseDTO> obtenerPorId(@PathVariable Long id, @RequestParam Long empresaId) {
        return ResponseEntity.ok(problemaTicketService.obtenerPorId(id, empresaId));
    }

    @PostMapping
    public ResponseEntity<ProblemaResponseDTO> guardar(@Valid @RequestBody ProblemaRequestDTO requestDTO, @RequestParam Long empresaId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(problemaTicketService.crearProblema(requestDTO, empresaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProblemaResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ProblemaRequestDTO requestDTO, @RequestParam Long empresaId) {
        return ResponseEntity.ok(problemaTicketService.actualizarProblema(id, requestDTO, empresaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @RequestParam Long empresaId) {
        problemaTicketService.eliminarProblema(id, empresaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conteo-categoria")
    public ResponseEntity<List<Map<String, Object>>> contarPorCategoria() {
        return ResponseEntity.ok(problemaTicketService.contarProblemasPorCategoria());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProblemaResponseDTO>> buscarPorTexto(@RequestParam String texto) {
        return ResponseEntity.ok(problemaTicketService.buscarPorTexto(texto));
    }
}
