package com.helpdesk.helpdesk_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.helpdesk.helpdesk_backend.dto.ComentarioRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ComentarioResponseDTO;
import com.helpdesk.helpdesk_backend.model.TicketComentario;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketComentarioMapper {

    @Mapping(source = "ticketId", target = "ticket.id")
    TicketComentario toEntity(ComentarioRequestDTO requestDTO);  
    
    @Mapping(source = "ticket.id", target = "ticketId")
    @Mapping(source = "usuario.id", target = "usuarioId")
    @Mapping(source = "usuario.nombres", target = "usuarioNombre")
    @Mapping(target = "esSistema", source = "esSistema")
    ComentarioResponseDTO toResponseDTO(TicketComentario comentario);
}
