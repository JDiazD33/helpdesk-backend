package com.helpdesk.helpdesk_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProblemaRequestDTO {

    @NotBlank(message = "El nombre del problema es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre; 

    @Size(max = 250, message = "La descripcion no puede exceder los 250 caracteres")
    private String descripcion;

    @NotNull(message = "El ID de la categoría es obligatorio")
    private Long categoriaId;
}
