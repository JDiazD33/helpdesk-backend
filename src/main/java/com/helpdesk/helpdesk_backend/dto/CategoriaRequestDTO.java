package com.helpdesk.helpdesk_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaRequestDTO {

    @NotBlank(message = "El nombre de la categoria es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
    private String nombre;

    @Size(max = 250, message = "La descripcion no puede exceder los 250 caracteres")
    private String descripcion;
}
