package com.helpdesk.helpdesk_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CierreRequestDTO {

    @NotBlank(message = "La justificación es obligatoria")
    @Size(min = 10, message = "La justificación debe tener al menos 10 caracteres")
    private String justificacionCierre;

    private String imagenCierre;

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;
}
