package com.helpdesk.helpdesk_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ComentarioRequestDTO {

    @NotBlank(message = "El mensaje no puede estar vacio")
    private String mensaje;

    @NotNull(message = "El ID del ticket es obligatorio")
    private Long ticketId;
    
    // El usuario_id se sacara del contexto del token JWT, no del request.

}
