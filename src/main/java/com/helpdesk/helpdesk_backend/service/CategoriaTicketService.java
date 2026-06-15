package com.helpdesk.helpdesk_backend.service;

import java.util.List;

import com.helpdesk.helpdesk_backend.dto.CategoriaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.CategoriaResponseDTO;
import com.helpdesk.helpdesk_backend.dto.PageResponse;

public interface CategoriaTicketService {

    List<CategoriaResponseDTO> listarCategoriasActivas(Long empresaId);
    
    List<CategoriaResponseDTO> listarTodas(Long empresaId);

    List<CategoriaResponseDTO> listarTodasGlobal();

    PageResponse<CategoriaResponseDTO> buscarPaginado(Long empresaId, int page, int limit, String search);

    PageResponse<CategoriaResponseDTO> buscarPaginadoGlobal(int page, int limit, String search);

    CategoriaResponseDTO obtenerPorId(Long id, Long empresaId);

    CategoriaResponseDTO crearCategoria(CategoriaRequestDTO requestDTO, Long empresaId);

    CategoriaResponseDTO actualizarCategoria(Long id, CategoriaRequestDTO requestDTO, Long empresaId);

    void eliminarCategoria(Long id, Long empresaId);

}