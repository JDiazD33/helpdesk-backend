package com.helpdesk.helpdesk_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.helpdesk.helpdesk_backend.dto.TicketRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketResponseDTO;
import com.helpdesk.helpdesk_backend.model.Ticket;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(source = "problemaId", target = "problema.id")
    Ticket toEntity(TicketRequestDTO requestDTO);

    @Mapping(source = "cliente.id", target = "clienteId")
    @Mapping(source = "cliente.nombres", target = "clienteNombre")
    @Mapping(source = "agenteAsignado.id", target = "agenteAsignadoId")
    @Mapping(source = "agenteAsignado.nombres", target = "agenteNombre")
    @Mapping(source = "problema.id", target = "problemaId")
    @Mapping(source = "problema.nombre", target = "problemaNombre")
    TicketResponseDTO toResponseDTO(Ticket ticket);
    
}
