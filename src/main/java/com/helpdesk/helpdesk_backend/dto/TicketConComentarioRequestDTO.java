package com.helpdesk.helpdesk_backend.dto;

import com.helpdesk.helpdesk_backend.model.Ticket;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketConComentarioRequestDTO {

    @NotNull(message = "Los datos del ticket son obligatorios")
    @Valid
    private Ticket ticket;

    private String mensajeInicial;

    @NotNull(message = "El ID del usuario que comenta es obligatorio")
    private Long usuarioComentarioId;
}
