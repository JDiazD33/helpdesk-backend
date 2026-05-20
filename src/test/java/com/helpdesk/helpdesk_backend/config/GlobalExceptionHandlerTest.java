package com.helpdesk.helpdesk_backend.config;

import com.helpdesk.helpdesk_backend.exception.GlobalExceptionHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleIllegalArgument_deberiaRetornarBadRequest() {
        String mensajeError = "Error de validación de negocio simulado";
        IllegalArgumentException ex = new IllegalArgumentException(mensajeError);

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();
        assertEquals(400, body.get("status"));
        assertEquals("Bad Request", body.get("error"));
        assertEquals(mensajeError, body.get("message"));
        
        assertTrue(body.containsKey("timestamp"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void handleGeneralException_deberiaRetornarInternalServerError() {
        String mensajeError = "Error inesperado del servidor";
        Exception ex = new Exception(mensajeError);

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGeneralException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();
        assertEquals(500, body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals(mensajeError, body.get("message"));
        
        assertTrue(body.containsKey("timestamp"));
        assertNotNull(body.get("timestamp"));
    }
}