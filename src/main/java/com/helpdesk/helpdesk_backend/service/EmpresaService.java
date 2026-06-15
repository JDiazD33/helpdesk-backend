package com.helpdesk.helpdesk_backend.service;

import java.util.List;
import com.helpdesk.helpdesk_backend.dto.EmpresaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.EmpresaResponseDTO;
import com.helpdesk.helpdesk_backend.dto.EmpresaTicketsDTO;

public interface EmpresaService {
    EmpresaResponseDTO crearEmpresa(EmpresaRequestDTO requestDTO);
    EmpresaResponseDTO obtenerEmpresaPorId(Long id);
    List<EmpresaResponseDTO> listarEmpresasActivas();
    EmpresaResponseDTO actualizarEmpresa(Long id, EmpresaRequestDTO requestDTO);
    void eliminarEmpresa(Long id);
    List<EmpresaResponseDTO> buscarPorNombre(String texto);
    List<EmpresaResponseDTO> buscarPorRuc(String ruc);
    List<EmpresaResponseDTO> listarEmpresasRecientes(int dias);
    List<EmpresaTicketsDTO> getRankingEmpresas();
}
