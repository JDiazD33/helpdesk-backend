package com.helpdesk.helpdesk_backend.service;

import java.util.List;

import com.helpdesk.helpdesk_backend.dto.ProblemaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ProblemaResponseDTO;

public interface ProblemaTicketService {

    List<ProblemaResponseDTO> listarProblemasPorCategoria(Long categoriaId, Long empresaId);

    ProblemaResponseDTO crearProblema(ProblemaRequestDTO requestDTO, Long empresaId);

    ProblemaResponseDTO actualizarProblema(Long id, ProblemaRequestDTO requestDTO, Long empresaId);

    void eliminarProblema(Long id, Long empresaId);
}
