package com.helpdesk.helpdesk_backend.service;

import java.util.List;

import com.helpdesk.helpdesk_backend.dto.CategoriaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.CategoriaResponseDTO;

public interface CategoriaTicketService {

    List<CategoriaResponseDTO> listarCategoriasActivas(Long empresaId);
    
    List<CategoriaResponseDTO> listarTodas(Long empresaId);

    CategoriaResponseDTO crearCategoria(CategoriaRequestDTO requestDTO, Long empresaId);

    CategoriaResponseDTO actualizarCategoria(Long id, CategoriaRequestDTO requestDTO, Long empresaId);

    void eliminarCategoria(Long id, Long empresaId);

}