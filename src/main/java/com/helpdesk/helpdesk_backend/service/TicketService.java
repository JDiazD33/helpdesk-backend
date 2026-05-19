package com.helpdesk.helpdesk_backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.helpdesk.helpdesk_backend.dto.TicketRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketResponseDTO;
import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;

public interface TicketService {
    
    TicketResponseDTO crearTicket(TicketRequestDTO requestDTO, Long clienteIdContexto, Long empresaIdContexto);

    TicketResponseDTO asignarAgente(Long ticketId, Long agenteId, Long empresaIdContexto);

    TicketResponseDTO cambiarEstado(Long ticketId, EstadoTicket nuevoEstado, String justificacionCierre, Long empresaIdContexto);

    TicketResponseDTO obtenerPorId(Long ticketId, Long empresaIdContexto);

    // Listados Operativos
    Page<TicketResponseDTO> listarTodosPorEmpresa(Long empresaIdContexto, Pageable pageable);

    Page<TicketResponseDTO> listarPorCliente(Long clienteId, Long empresaIdContexto, Pageable pageable);

    Page<TicketResponseDTO> listarPorAgente(Long agenteId, Long empresaIdContexto, Pageable pageable);
 
    Page<TicketResponseDTO> listarNoAsignados(Long empresaIdContexto, Pageable pageable);
}
