package com.helpdesk.helpdesk_backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO para que el cliente califique la atencion de un ticket resuelto.
 * La calificacion es de 1 a 5 estrellas.
 */
@Data
public class CalificacionRequestDTO {

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer calificacion;
}
