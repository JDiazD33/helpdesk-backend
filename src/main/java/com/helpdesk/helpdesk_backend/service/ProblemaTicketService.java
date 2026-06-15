package com.helpdesk.helpdesk_backend.service;

import java.util.List;
import java.util.Map;
import com.helpdesk.helpdesk_backend.dto.PageResponse;
import com.helpdesk.helpdesk_backend.dto.ProblemaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ProblemaResponseDTO;

public interface ProblemaTicketService {
    List<ProblemaResponseDTO> listarProblemasPorCategoria(Long categoriaId, Long empresaId);
    List<ProblemaResponseDTO> listarTodosPorCategoria(Long categoriaId);
    List<ProblemaResponseDTO> listarActivosPorEmpresa(Long empresaId);
    PageResponse<ProblemaResponseDTO> listarPaginado(Long empresaId, int page, int limit, Long categoriaId);
    PageResponse<ProblemaResponseDTO> listarTodosPaginado(int page, int limit, Long categoriaId);
    ProblemaResponseDTO obtenerPorId(Long id, Long empresaId);
    ProblemaResponseDTO crearProblema(ProblemaRequestDTO requestDTO, Long empresaId);
    ProblemaResponseDTO actualizarProblema(Long id, ProblemaRequestDTO requestDTO, Long empresaId);
    void eliminarProblema(Long id, Long empresaId);
    List<Map<String, Object>> contarProblemasPorCategoria(Long empresaId);
    List<ProblemaResponseDTO> buscarPorTexto(String texto);
}
