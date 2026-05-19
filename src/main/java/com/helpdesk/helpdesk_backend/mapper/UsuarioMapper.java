package com.helpdesk.helpdesk_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.helpdesk.helpdesk_backend.dto.UsuarioRequestDTO;
import com.helpdesk.helpdesk_backend.dto.UsuarioResponseDTO;
import com.helpdesk.helpdesk_backend.model.Usuario;

/**
 * Mapper para la conversión entre Usuario (Entity) y sus DTOs.
 * componentModel = MappingConstants.ComponentModel.SPRING permite inyectar el mapper con @Autowired o constructores.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UsuarioMapper {

    // --- DE ENTIDAD A DTO DE SALIDA (RESPONSE) ---
    // Extraemos los IDs de las relaciones
    @Mapping(source = "empresa.id", target = "empresaId")
    @Mapping(source = "rol.id", target = "rolId")
    // Extraemos los nombres explícitamente para evitar devolver los objetos completos
    @Mapping(source = "empresa.nombre", target = "empresaNombre")
    @Mapping(source = "rol.nombre", target = "rolNombre")
    UsuarioResponseDTO toResponseDTO(Usuario usuario);

    // --- DE DTO DE ENTRADA (REQUEST) A ENTIDAD ---
    // Mapeamos los IDs del DTO entrante a los IDs de los objetos relacionados dentro de la entidad
    @Mapping(source = "rolId", target = "rol.id")
    // Ignoramos los campos que son gestionados por la BD o la lógica de negocio (SLA, auditoría, estado)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    // Agregamos para ignorar la empresa, ya que no se recibe en el DTO de usuario y se asignará en la lógica de negocio
    @Mapping(target = "empresa", ignore = true)
    Usuario toEntity(UsuarioRequestDTO requestDTO);
}
