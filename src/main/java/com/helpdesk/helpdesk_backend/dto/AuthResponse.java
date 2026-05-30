package com.helpdesk.helpdesk_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO para la respuesta de autenticación.
 * Devuelve el token JWT y los datos básicos del usuario autenticado.
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private Long expiresIn;
    private String email;
    private String nombres;
    private String apellidos;
    private String rol;
    private Long empresaId;
    private String nombreEmpresa;
    private Long usuarioId;
}
