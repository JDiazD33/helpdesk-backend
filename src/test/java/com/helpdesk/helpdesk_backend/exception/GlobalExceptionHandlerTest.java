package com.helpdesk.helpdesk_backend.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleResourceNotFound_DeberiaRetornar404() {
        ResponseEntity<Map<String, Object>> response = handler.handleResourceNotFound(
                new ResourceNotFoundException("Recurso no encontrado")
        );

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        assertThat(response.getBody()).containsEntry("error", "Not Found");
        assertThat(response.getBody()).containsEntry("message", "Recurso no encontrado");
    }

    @Test
    void handleDuplicateResource_DeberiaRetornar409() {
        ResponseEntity<Map<String, Object>> response = handler.handleDuplicateResource(
                new DuplicateResourceException("Ya existe un recurso")
        );

        assertThat(response.getStatusCodeValue()).isEqualTo(409);
        assertThat(response.getBody()).containsEntry("error", "Conflict");
        assertThat(response.getBody()).containsEntry("message", "Ya existe un recurso");
    }

    @Test
    void handleValidationErrors_DeberiaRetornar400ConErroresPorCampo() throws NoSuchMethodException {
        Method method = getClass().getDeclaredMethod("dummyMethod", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "objeto");
        bindingResult.rejectValue("nombre", "NotBlank", "El nombre es obligatorio");

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);
        ResponseEntity<Map<String, Object>> response = handler.handleValidationErrors(exception);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("error", "Validation Failed");
        assertThat(response.getBody()).containsEntry("message", "Errores de validación en los campos enviados");
        assertThat((Map<String, String>) response.getBody().get("fieldErrors")).containsEntry("nombre", "El nombre es obligatorio");
    }

    @Test
    void handleIllegalArgument_DeberiaRetornar400() {
        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(
                new IllegalArgumentException("Argumento inválido")
        );

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("error", "Bad Request");
        assertThat(response.getBody()).containsEntry("message", "Argumento inválido");
    }

    @Test
    void handleGeneralException_DeberiaRetornar500ConMensajeGenerico() {
        ResponseEntity<Map<String, Object>> response = handler.handleGeneralException(
                new Exception("Detalle interno de Hibernate: SQL error, tabla 'usuario' no existe...")
        );

        assertThat(response.getStatusCodeValue()).isEqualTo(500);
        assertThat(response.getBody()).containsEntry("error", "Internal Server Error");
        // El mensaje interno NO debe filtrarse al cliente
        assertThat(response.getBody()).containsEntry("message", "Error interno del servidor");
        assertThat(response.getBody().get("message").toString())
                .doesNotContain("Hibernate")
                .doesNotContain("SQL")
                .doesNotContain("tabla");
    }

    @SuppressWarnings("unused")
    private void dummyMethod(String parametro) {
        // Método auxiliar para generar MethodParameter en las pruebas
    }
}
