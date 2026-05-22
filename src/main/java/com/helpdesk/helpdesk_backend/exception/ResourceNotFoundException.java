package com.helpdesk.helpdesk_backend.exception;

/**
 * Excepción lanzada cuando un recurso solicitado no existe en la base de datos.
 * Produce una respuesta HTTP 404 (Not Found).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
}
