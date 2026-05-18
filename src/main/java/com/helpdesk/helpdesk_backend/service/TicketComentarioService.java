package com.helpdesk.helpdesk_backend.service;

import java.util.List;

import com.helpdesk.helpdesk_backend.dto.ComentarioRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ComentarioResponseDTO;

public interface TicketComentarioService {

    ComentarioResponseDTO agregarComentario(ComentarioRequestDTO requestDTO, Long usuarioIdContexto, Long empresaIdContexto);

    List<ComentarioResponseDTO> listarComentariosPorTicket(Long ticketId, Long empresaIdContexto);
    
}
