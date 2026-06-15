package com.helpdesk.helpdesk_backend.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ComentarioResponseDTO {

    private Long id;
    private String mensaje;
    private LocalDateTime fechaEnvio;
    
    private Long ticketId;
    private Long usuarioId;
    private String usuarioNombre;

    private boolean esSistema;
}
