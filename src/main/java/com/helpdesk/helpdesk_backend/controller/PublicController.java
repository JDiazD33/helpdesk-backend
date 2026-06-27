package com.helpdesk.helpdesk_backend.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.helpdesk_backend.dto.CategoriaResponseDTO;
import com.helpdesk.helpdesk_backend.dto.ProblemaResponseDTO;
import com.helpdesk.helpdesk_backend.dto.TicketAnonimoRequestDTO;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.repository.UsuarioRepository;
import com.helpdesk.helpdesk_backend.service.CategoriaTicketService;
import com.helpdesk.helpdesk_backend.service.ProblemaTicketService;
import com.helpdesk.helpdesk_backend.service.TicketService;

import jakarta.validation.Valid;

/**
 * Controlador de endpoints publicos (sin autenticacin).
 *
 * Da soporte al reporte de tickets sin iniciar sesion: a partir del correo del
 * reportante se identifica su empresa (debe ser un usuario CLIENTE ya registrado),
 * y se exponen las categorias/problemas de esa empresa para armar el formulario.
 *
 * La empresa NO se elige manualmente: se infiere del correo, esto evita exponer
 * el catalogo de una empresa a cualquier visitante y mantiene el aislamiento
 * multi-tenant del catálogo.
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaTicketService categoriaService;
    private final ProblemaTicketService problemaService;
    private final TicketService ticketService;

    public PublicController(UsuarioRepository usuarioRepository,
                            CategoriaTicketService categoriaService,
                            ProblemaTicketService problemaService,
                            TicketService ticketService) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaService = categoriaService;
        this.problemaService = problemaService;
        this.ticketService = ticketService;
    }

    /**
     * GET /api/public/empresa?correo=...
     * identifica la empresa del correo ingresado.
     * devuelve { empresaId, nombreEmpresa } o 404 si el correo no existe
     *
     * no revela más datos de la empresa que el nombre para mostrarlo en la UI
     */
    @GetMapping("/empresa")
    public ResponseEntity<Map<String, Object>> identificarEmpresa(@RequestParam String correo) {
        Usuario usuario = usuarioRepository.findByEmail(correo.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No encontramos una cuenta asociada al correo: " + correo
                        + ". Regístrate o contacta a soporte."));

        // Solo los clientes pueden usar el reporte publico. Admins y agentes
        // deben usar su panel autenticado.
        String nombreRol = usuario.getRol() != null ? usuario.getRol().getNombre() : null;
        if (!"CLIENTE".equals(nombreRol)) {
            throw new IllegalArgumentException(
                    "El reporte publico de tickets es exclusivo para clientes. "
                    + "Inicia sesion con tu cuenta para gestionar tickets.");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("empresaId", usuario.getEmpresa().getId());
        body.put("nombreEmpresa", usuario.getEmpresa().getNombre());
        return ResponseEntity.ok(body);
    }

    /**
     * GET /api/public/categorias?empresaId=...
     * Categorias activas de la empresa identificada (para el selector).
     */
    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaResponseDTO>> listarCategorias(@RequestParam Long empresaId) {
        return ResponseEntity.ok(categoriaService.listarCategoriasActivas(empresaId));
    }

    /**
     * GET /api/public/problemas?categoriaId=...&empresaId=...
     * Problemas activos de una categoría dentro de la empresa identificada.
     */
    @GetMapping("/problemas")
    public ResponseEntity<List<ProblemaResponseDTO>> listarProblemas(
            @RequestParam Long categoriaId,
            @RequestParam Long empresaId) {
        return ResponseEntity.ok(problemaService.listarProblemasPorCategoria(categoriaId, empresaId));
    }

    /**
     * POST /api/public/tickets
     * Crea un ticket reportado sin iniciar sesión.
     * El correo debe corresponder a un usuario CLIENTE registrado; la empresa
     * se infiere de ahí dentro del servicio.
     */
    @PostMapping("/tickets")
    public ResponseEntity<Map<String, Object>> crearTicketAnonimo(@Valid @RequestBody TicketAnonimoRequestDTO request) {
        var creado = ticketService.guardarAnonimo(request);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("codigo", creado.getCodigo());
        body.put("mensaje", "Ticket creado con exito");
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
}
