package com.helpdesk.helpdesk_backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketComentarioServiceImplTest {

    @Mock
    private TicketComentarioRepository comentarioRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TicketComentarioMapper comentarioMapper;

    @InjectMocks
    private TicketComentarioServiceImpl comentarioService;

    private Ticket ticket;
    private Usuario usuario;
    private TicketComentario comentario;
    private ComentarioRequestDTO requestDTO;
    private ComentarioResponseDTO responseDTO;

    @BeforeEach
    void setUp() {

        ticket = new Ticket();
        ticket.setId(1L);
        ticket.setEstado(EstadoTicket.ABIERTO);

        usuario = new Usuario();
        usuario.setId(1L);

        comentario = new TicketComentario();
        comentario.setId(1L);
        comentario.setMensaje("Comentario de prueba");
        comentario.setTicket(ticket);
        comentario.setUsuario(usuario);

        requestDTO = new ComentarioRequestDTO();
        requestDTO.setTicketId(1L);
        requestDTO.setMensaje("Comentario de prueba");

        responseDTO = new ComentarioResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setMensaje("Comentario de prueba");
    }

    @Test
    void agregarComentario_debeGuardarYRetornarComentario() {

        when(ticketRepository.findByIdAndEmpresaId(1L, 1L))
                .thenReturn(Optional.of(ticket));

        when(usuarioRepository.findByIdAndEmpresaId(1L, 1L))
                .thenReturn(Optional.of(usuario));

        when(comentarioMapper.toEntity(requestDTO))
                .thenReturn(comentario);

        when(comentarioRepository.save(any(TicketComentario.class)))
                .thenReturn(comentario);

        when(comentarioMapper.toResponseDTO(comentario))
                .thenReturn(responseDTO);

        ComentarioResponseDTO resultado = comentarioService.agregarComentario(requestDTO, 1L, 1L);

        assertNotNull(resultado);
        assertEquals("Comentario de prueba", resultado.getMensaje());

        verify(comentarioRepository).save(any(TicketComentario.class));
    }

    @Test
    void agregarComentario_ticketNoExiste_lanzaExcepcion() {

        when(ticketRepository.findByIdAndEmpresaId(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> comentarioService.agregarComentario(requestDTO, 1L, 1L));

        verify(comentarioRepository, never()).save(any());
    }

    @Test
    void agregarComentario_ticketCerrado_lanzaExcepcion() {

        ticket.setEstado(EstadoTicket.CERRADO);

        when(ticketRepository.findByIdAndEmpresaId(1L, 1L))
                .thenReturn(Optional.of(ticket));

        assertThrows(IllegalArgumentException.class, () -> comentarioService.agregarComentario(requestDTO, 1L, 1L));

        verify(comentarioRepository, never()).save(any());
    }

    @Test
    void agregarComentario_usuarioNoExiste_lanzaExcepcion() {

        when(ticketRepository.findByIdAndEmpresaId(1L, 1L))
                .thenReturn(Optional.of(ticket));

        when(usuarioRepository.findByIdAndEmpresaId(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> comentarioService.agregarComentario(requestDTO, 1L, 1L));

        verify(comentarioRepository, never()).save(any());
    }

    @Test
    void listarComentariosPorTicket_debeRetornarLista() {

        when(ticketRepository.findByIdAndEmpresaId(1L, 1L))
                .thenReturn(Optional.of(ticket));

        when(comentarioRepository.findAllByTicketIdOrderByFechaEnvioAsc(1L))
                .thenReturn(List.of(comentario));

        when(comentarioMapper.toResponseDTO(comentario))
                .thenReturn(responseDTO);

        List<ComentarioResponseDTO> resultado = comentarioService.listarComentariosPorTicket(1L, 1L);

        assertEquals(1, resultado.size());
        assertEquals("Comentario de prueba", resultado.get(0).getMensaje());
    }

    @Test
    void listarComentariosPorTicket_ticketNoExiste_lanzaExcepcion() {

        when(ticketRepository.findByIdAndEmpresaId(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> comentarioService.listarComentariosPorTicket(1L, 1L));
    }
}