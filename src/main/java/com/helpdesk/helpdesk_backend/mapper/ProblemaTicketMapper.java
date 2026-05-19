package com.helpdesk.helpdesk_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import com.helpdesk.helpdesk_backend.dto.ProblemaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ProblemaResponseDTO;
import com.helpdesk.helpdesk_backend.model.ProblemaTicket;

//Convertimos el mapper en un componente de Spring para que pueda ser inyectado en los servicios, 
// esto es importante para mantener una arquitectura limpia y aprovechar las ventajas de la inyección de dependencias.
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
// Mapper para la entidad ProblemaTicket, con mapeos específicos para manejar la relación con CategoriaTicket
public interface ProblemaTicketMapper {

    // Mapeamos el DTO de entrada a la entidad para guardarlo en la BD, con las reglas ignoramos campos gestionados 
    // por la lógica de negocio y mapeando correctamente, se tomara el valor del Id que envio el usuario a traves del DTO y 
    // lo conectara con el Id de la tabla CategoriaTicket en la BD, esto es importante para mantener 
    // la integridad referencial y evitar errores de mapeo.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(source = "categoriaId", target = "categoria.id")
    ProblemaTicket toEntity(ProblemaRequestDTO requestDTO);

    // Extraemos correctamente el ID y Nombre de la categoría de la BD y lo aplana en el DTO de respuesta, 
    // esto es importante para mostrar la información completa del problema incluyendo su categoría sin necesidad 
    // de hacer consultas adicionales en el frontend.
    @Mapping(source = "categoria.id", target = "categoriaId")   
    @Mapping(source = "categoria.nombre", target = "categoriaNombre")
    ProblemaResponseDTO toResponseDTO(ProblemaTicket problemaTicket);

    // Mapeamos el DTO de entrada a la entidad existente para actualizarla en la BD, esto es importante para actualizar 
    // solo los campos necesarios sin afectar otros atributos de la entidad.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(source = "categoriaId", target = "categoria.id")
    void updateEntityFromDTO(ProblemaRequestDTO requestDTO, @MappingTarget ProblemaTicket problemaTicket);
}
