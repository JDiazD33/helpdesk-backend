package com.helpdesk.helpdesk_backend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {

    private Long id;
    private String nombres;
    private String apellidos;
    private String email;
    
    // El atributo password se omite intencionalmente en la respuesta para evitar exponer la contraseña o su hash.

    private String telefono;
    private boolean activo;
    private LocalDateTime fechaCreacion;

    // En lugar de enviar los objetos completos, enviamos IDs o campos básicos (aplanamiento útil para APIs REST)
    private Long empresaId;
    private String empresaNombre;
    
    private Long rolId;
    private String rolNombre;
}
