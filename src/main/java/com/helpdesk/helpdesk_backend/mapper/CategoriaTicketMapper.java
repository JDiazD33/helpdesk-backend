package com.helpdesk.helpdesk_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.helpdesk.helpdesk_backend.dto.CategoriaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.CategoriaResponseDTO;
import com.helpdesk.helpdesk_backend.model.CategoriaTicket;

@Mapper(componentModel = "spring")
public interface CategoriaTicketMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activa", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    CategoriaTicket toEntity(CategoriaRequestDTO requestDTO);

    CategoriaResponseDTO toResponseDTO(CategoriaTicket categoriaTicket);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activa", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    void updateEntityFromDTO(CategoriaRequestDTO requestDTO, @MappingTarget CategoriaTicket categoriaTicket);
}
