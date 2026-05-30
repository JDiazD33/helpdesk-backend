package com.helpdesk.helpdesk_backend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.helpdesk_backend.dto.ComentarioRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ComentarioResponseDTO;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
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
@Transactional
public class TicketComentarioServiceImpl implements TicketComentarioService {

    private final TicketComentarioRepository comentarioRepository;
    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;
    private final TicketComentarioMapper comentarioMapper;

    @Override
    public ComentarioResponseDTO agregarComentario(ComentarioRequestDTO requestDTO, Long usuarioIdContexto,
            Long empresaIdContexto) {
        Ticket ticket = ticketRepository.findByIdAndEmpresaId(requestDTO.getTicketId(), empresaIdContexto)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado o no pertenece a la empresa"));

        if (ticket.getEstado() == EstadoTicket.CERRADO) {
            throw new IllegalArgumentException("No se pueden agregar comentarios a un ticket cerrado.");
        }

        Usuario usuario = usuarioRepository.findByIdAndEmpresaId(usuarioIdContexto, empresaIdContexto)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado o no pertenece a la empresa"));

        TicketComentario comentario = comentarioMapper.toEntity(requestDTO);
        comentario.setTicket(ticket);
        comentario.setUsuario(usuario);

        TicketComentario comentarioGuardado = comentarioRepository.save(comentario);
        return comentarioMapper.toResponseDTO(comentarioGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> listarComentariosPorTicket(Long ticketId, Long empresaIdContexto) {
        ticketRepository.findByIdAndEmpresaId(ticketId, empresaIdContexto)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado o no pertenece a la empresa"));

        return comentarioRepository.findAllByTicketIdOrderByFechaEnvioAsc(ticketId)
                .stream()
                .map(comentarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> listarPorUsuario(Long usuarioId) {
        return comentarioRepository.listarPorUsuario(usuarioId).stream()
                .map(comentarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> buscarPorTexto(String texto) {
        return comentarioRepository.buscarPorTexto(texto).stream()
                .map(comentarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> rankingUsuariosComentarios() {
        List<Object[]> filas = comentarioRepository.rankingUsuariosComentarios();
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Object[] fila : filas) {
            Map<String, Object> item = new HashMap<>();
            item.put("usuario", fila[0]);
            item.put("total", fila[1]);
            resultado.add(item);
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> comentariosRecientesEmpresa(Long empresaId, int dias) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(dias);
        return comentarioRepository.comentariosRecientesEmpresa(empresaId, fechaLimite).stream()
                .map(comentarioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
