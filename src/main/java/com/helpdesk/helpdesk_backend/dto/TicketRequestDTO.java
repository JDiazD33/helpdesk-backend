package com.helpdesk.helpdesk_backend.dto;

import com.helpdesk.helpdesk_backend.model.enums.PrioridadTicket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketRequestDTO {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "El título no puede exceder los 255 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "La prioridad es obligatoria")
    private PrioridadTicket prioridad;

    @NotNull(message = "El problema es obligatorio")
    private Long problemaId;

    // El cliente_id se obtendrá del contexto de seguridad (quien hace la petición)
    // El agenteAsignadoId no se incluye en la creación, se asigna después.
}
