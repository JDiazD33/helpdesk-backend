package com.helpdesk.helpdesk_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProblemaResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private boolean activo;
    private Long categoriaId;
    private String categoriaNombre;
}
