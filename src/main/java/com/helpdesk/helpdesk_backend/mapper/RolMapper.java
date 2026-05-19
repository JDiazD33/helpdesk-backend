package com.helpdesk.helpdesk_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.helpdesk.helpdesk_backend.dto.RolResponseDTO;
import com.helpdesk.helpdesk_backend.model.Rol;

/**
 * Mapper para la entidad Rol.
 * Enfoque Read-Only: Solo necesitamos convertir de Entidad a DTO de respuesta.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RolMapper {

    // --- DE ENTIDAD A DTO DE SALIDA (RESPONSE) ---
    RolResponseDTO toResponseDTO(Rol rol);

}
