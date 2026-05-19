package com.helpdesk.helpdesk_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.helpdesk.helpdesk_backend.dto.EmpresaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.EmpresaResponseDTO;
import com.helpdesk.helpdesk_backend.model.Empresa;

/**
 * Mapper para la conversión entre Empresa (Entity) y sus DTOs.
 * componentModel = MappingConstants.ComponentModel.SPRING permite inyectar el mapper en el Service.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmpresaMapper {
    // --- DE ENTIDAD A DTO DE SALIDA (RESPONSE) ---
    // Mapeo directo de todos los atributos de la entidad al DTO de respuesta
    EmpresaResponseDTO toResponseDTO(Empresa empresa);

    // --- DE DTO DE ENTRADA (REQUEST) A ENTIDAD ---
    // Ignoramos los campos que son gestionados por la BD o el ciclo de vida de la entidad
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    Empresa toEntity(EmpresaRequestDTO requestDTO);

}
