package com.helpdesk.helpdesk_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaResponseDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private boolean activa;
    // utiles para el ADMIN_OWNER, que opera sobre varias empresas y necesita
    // saber a cual pertenece cada categoria (filtro y eliminacion).
    private Long empresaId;
    private String empresaNombre;
}
