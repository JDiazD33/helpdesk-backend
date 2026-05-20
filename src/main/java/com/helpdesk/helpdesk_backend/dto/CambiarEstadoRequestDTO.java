package com.helpdesk.helpdesk_backend.dto;

import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambiarEstadoRequestDTO {

    @NotNull(message = "El estado es obligatorio")
    private EstadoTicket estado;

    // No le ponemos @NotBlank porque solo es obligatorio para algunos estados
    // La validación estricta (exigir justificación si es CERRADO) la hará tu Service
    private String justificacionCierre;
}
