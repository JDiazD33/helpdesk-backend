package com.helpdesk.helpdesk_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.helpdesk.helpdesk_backend.dto.ProblemaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ProblemaResponseDTO;
import com.helpdesk.helpdesk_backend.model.ProblemaTicket;

@Mapper(componentModel = "spring")
public interface ProblemaTicketMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    // Instanciamos parcialmente la categoría usando el ID proporcionado
    @Mapping(source = "categoriaId", target = "categoria.id")
    ProblemaTicket toEntity(ProblemaRequestDTO requestDTO);

    // Extraemos correctamente el ID y Nombre de la categoría hacia el ResponseDTO
    @Mapping(source = "categoria.id", target = "categoriaId")
    @Mapping(source = "categoria.nombre", target = "categoriaNombre")
    ProblemaResponseDTO toResponseDTO(ProblemaTicket problemaTicket);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(source = "categoriaId", target = "categoria.id")
    void updateEntityFromDTO(ProblemaRequestDTO requestDTO, @MappingTarget ProblemaTicket problemaTicket);
}
