package com.helpdesk.helpdesk_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// Informacion que sale del sistema al consultar un ProblemaTicket
public class ProblemaResponseDTO {
    
    //Enlistamos todos los campos que queremos mostrar en la respuesta, incluyendo los relacionados con la categoría
    private Long id;
    private String nombre;
    private String descripcion;
    private boolean activo;
    private Long categoriaId;
    private String categoriaNombre;
}
