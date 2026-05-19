package com.helpdesk.helpdesk_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.helpdesk_backend.dto.RolResponseDTO;
import com.helpdesk.helpdesk_backend.service.RolService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    // Inyección de dependencia del servicio de roles
    private final RolService rolService;

    // Solo exponemos GET para lectura de los roles del sistema
    @GetMapping
    public ResponseEntity<List<RolResponseDTO>> obtenerTodosLosRoles(){
        return ResponseEntity.ok(rolService.obtenerTodosLosRoles());
    }

}
