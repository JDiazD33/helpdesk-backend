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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.helpdesk_backend.dto.EmpresaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.EmpresaResponseDTO;
import com.helpdesk.helpdesk_backend.service.EmpresaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @PostMapping
    public ResponseEntity<EmpresaResponseDTO> crearEmpresa(@Valid @RequestBody EmpresaRequestDTO requestDTO){
        EmpresaResponseDTO nuevaEmpresa = empresaService.crearEmpresa(requestDTO);
        return new ResponseEntity<>(nuevaEmpresa, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaResponseDTO> obtenerEmpresa(@PathVariable Long id){
        return ResponseEntity.ok(empresaService.obtenerEmpresaPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<EmpresaResponseDTO>> listarEmpresasActivas() {
        return ResponseEntity.ok(empresaService.listarEmpresasActivas());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpresaResponseDTO> actualizarEmpresa(@PathVariable Long id, @Valid @RequestBody EmpresaRequestDTO requestDTO) {
        return ResponseEntity.ok(empresaService.actualizarEmpresa(id, requestDTO));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEmpresa(@PathVariable Long id){
        empresaService.eliminarEmpresa(id);
        return ResponseEntity.noContent().build();
    }   
}
