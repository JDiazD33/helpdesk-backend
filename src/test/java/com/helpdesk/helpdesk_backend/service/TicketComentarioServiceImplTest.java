package com.helpdesk.helpdesk_backend.service;

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
import com.helpdesk.helpdesk_backend.service.impl.TicketComentarioServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private ComentarioRequestDTO requestDTO;
    private TicketComentario comentarioEntity;
    private ComentarioResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        ticket = Ticket.builder().id(10L).estado(EstadoTicket.ABIERTO).build();
        usuario = Usuario.builder().id(5L).build();

        requestDTO = new ComentarioRequestDTO();
        requestDTO.setTicketId(10L);
        requestDTO.setMensaje("Hello World!");

        comentarioEntity = TicketComentario.builder().id(1L).mensaje("Hello World!").build();

        responseDTO = new ComentarioResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setMensaje("Hello World!");
    }

    @Test
    void agregarComentario_DeberiaGuardarYRetornarDTO() {
        when(ticketRepository.findByIdAndEmpresaId(10L, 1L)).thenReturn(Optional.of(ticket));
        when(usuarioRepository.findByIdAndEmpresaId(5L, 1L)).thenReturn(Optional.of(usuario));
        when(comentarioMapper.toEntity(any())).thenReturn(comentarioEntity);
        when(comentarioRepository.save(any(TicketComentario.class))).thenReturn(comentarioEntity);
        when(comentarioMapper.toResponseDTO(comentarioEntity)).thenReturn(responseDTO);

        ComentarioResponseDTO resultado = comentarioService.agregarComentario(requestDTO, 5L, 1L);

        assertThat(resultado.getMensaje()).isEqualTo("Hello World!");
        verify(comentarioRepository).save(any(TicketComentario.class));
    }

    @Test
    void agregarComentario_DeberiaLanzarSiTicketCerrado() {
        ticket.setEstado(EstadoTicket.CERRADO);
        when(ticketRepository.findByIdAndEmpresaId(10L, 1L)).thenReturn(Optional.of(ticket));

        assertThrows(IllegalArgumentException.class, () -> comentarioService.agregarComentario(requestDTO, 5L, 1L));
    }

    @Test
    void listarComentariosPorTicket_DeberiaMapearYRetornar() {
        when(ticketRepository.findByIdAndEmpresaId(10L, 1L)).thenReturn(Optional.of(ticket));
        when(comentarioRepository.findAllByTicketIdOrderByFechaEnvioAsc(10L)).thenReturn(List.of(comentarioEntity));
        when(comentarioMapper.toResponseDTO(any(TicketComentario.class))).thenReturn(responseDTO);

        List<ComentarioResponseDTO> resultado = comentarioService.listarComentariosPorTicket(10L, 1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getMensaje()).isEqualTo("Hello World!");
    }
}
