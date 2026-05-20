package com.helpdesk.helpdesk_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignarAgenteRequestDTO {

    @NotNull(message = "El ID del agente es obligatorio")
    private Long agenteId;
}
