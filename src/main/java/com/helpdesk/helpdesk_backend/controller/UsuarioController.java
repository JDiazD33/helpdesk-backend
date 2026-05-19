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

import com.helpdesk.helpdesk_backend.dto.UsuarioRequestDTO;
import com.helpdesk.helpdesk_backend.dto.UsuarioResponseDTO;
import com.helpdesk.helpdesk_backend.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // TODO: Cuando se implemente Spring Security + JWT, el "empresaIdContexto" 
    // se extraerá automáticamente del token del usuario autenticado, 
    // eliminando la necesidad de pedirlo por Header.

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crearUsuario(
        @Valid @RequestBody UsuarioRequestDTO requestDTO, 
        @RequestHeader("X-Empresa-Id") Long empresaIdContexto) {

        UsuarioResponseDTO nuevoUsuario = usuarioService.crearUsuario(requestDTO, empresaIdContexto);
        return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuario(@PathVariable Long id, @RequestHeader("X-Empresa-Id") Long empresaIdContexto) {
        return ResponseEntity.ok(usuarioService.obtenerUsuarioPorId(id, empresaIdContexto));
    }
    
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuariosPorEmpresa(@RequestHeader("X-Empresa-Id") Long empresaIdContexto){
        return ResponseEntity.ok(usuarioService.listarUsuariosPorEmpresa(empresaIdContexto));    
    }

    @GetMapping("/rol/{rolNombre}")
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuariosPorRol(@PathVariable String rolNombre, @RequestHeader("X-Empresa-Id") Long empresaIdContexto) {
        return ResponseEntity.ok(usuarioService.listarUsuariosPorRol(empresaIdContexto, rolNombre));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioRequestDTO requestDTO, @RequestHeader("X-Empresa-Id") Long empresaIdContexto) {      
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, requestDTO, empresaIdContexto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id, @RequestHeader("X-Empresa-Id") Long empresaIdContexto){
        usuarioService.eliminarUsuario(id, empresaIdContexto);
        return ResponseEntity.noContent().build();
    }
}
