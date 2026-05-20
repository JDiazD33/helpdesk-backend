package com.helpdesk.helpdesk_backend.exception;

/**
 * Excepción lanzada cuando se intenta crear un recurso que ya existe (duplicado).
 * Produce una respuesta HTTP 409 (Conflict).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String mensaje) {
        super(mensaje);
    }
}
