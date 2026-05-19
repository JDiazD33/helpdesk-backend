package com.helpdesk.helpdesk_backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.helpdesk.helpdesk_backend.dto.ComentarioRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ComentarioResponseDTO;
import com.helpdesk.helpdesk_backend.mapper.TicketComentarioMapper;
import com.helpdesk.helpdesk_backend.model.Ticket;
import com.helpdesk.helpdesk_backend.model.TicketComentario;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;
import com.helpdesk.helpdesk_backend.repository.TicketComentarioRepository;
import com.helpdesk.helpdesk_backend.repository.TicketRepository;
import com.helpdesk.helpdesk_backend.repository.UsuarioRepository;
import com.helpdesk.helpdesk_backend.service.TicketComentarioService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketComentarioServiceImpl implements TicketComentarioService{

    private final TicketComentarioRepository comentarioRepository;
    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;
    private final TicketComentarioMapper comentarioMapper;

    @Override
    public ComentarioResponseDTO agregarComentario(ComentarioRequestDTO requestDTO, Long usuarioIdContexto,
            Long empresaIdContexto) {
        // 1. Validar el Ticket y asegurar que pertenece a la empresa del usuario
        Ticket ticket = ticketRepository.findByIdAndEmpresaId(requestDTO.getTicketId(), empresaIdContexto)
        .orElseThrow(() -> new RuntimeException("Ticket no encontrado o no pertenece a la empresa"));
        
        // 2. Regla Operativa: No permitir comentarios en tickets cerrados
        if (ticket.getEstado() == EstadoTicket.CERRADO) {
            throw new RuntimeException("No se pueden agregar comentarios a un ticket cerrado.");
        }

        // 3. Validar el Usuario que comenta
        Usuario usuario = usuarioRepository.findByIdAndEmpresaId(usuarioIdContexto, empresaIdContexto)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado o no pertenece a la empresa"));

        // 4. Mapear y guardar
        TicketComentario comentario = comentarioMapper.toEntity(requestDTO);
        comentario.setTicket(ticket);
        comentario.setUsuario(usuario);

        TicketComentario comentarioGuardado = comentarioRepository.save(comentario);
        return comentarioMapper.toResponseDTO(comentarioGuardado);
    }

    @Override
    public List<ComentarioResponseDTO> listarComentariosPorTicket(Long ticketId, Long empresaIdContexto) {
        // 1. Validar seguridad: El ticket debe existir y pertenecer a la empresa
        ticketRepository.findByIdAndEmpresaId(ticketId, empresaIdContexto)
        .orElseThrow(() -> new RuntimeException("Ticket no encontrado o no pertenece a la empresa"));

        // 2. Extraer comentarios ordenados
        return comentarioRepository.findAllByTicketIdOrderByFechaEnvioAsc(ticketId)
        .stream()
        .map(comentarioMapper::toResponseDTO)
        .collect(Collectors.toList());
    }

}
