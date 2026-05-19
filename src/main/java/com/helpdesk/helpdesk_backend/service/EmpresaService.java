package com.helpdesk.helpdesk_backend.service;

import java.util.List;

import com.helpdesk.helpdesk_backend.dto.EmpresaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.EmpresaResponseDTO;

public interface EmpresaService {

    EmpresaResponseDTO crearEmpresa(EmpresaRequestDTO requestDTO);
    
    EmpresaResponseDTO obtenerEmpresaPorId(Long id);
    
    List<EmpresaResponseDTO> listarEmpresasActivas();
    
    EmpresaResponseDTO actualizarEmpresa(Long id, EmpresaRequestDTO requestDTO);
    
    void eliminarEmpresa(Long id);
}
