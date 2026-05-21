package com.helpdesk.helpdesk_backend.exception;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ErrorResponseDTO {
    // El texto del error (Ej. "El ticket no pertenece a su empresa")
    private String mensaje; 
    
    // El código de estado web (Ej. 400, 403, 404, 500)
    private int codigoEstado; 
    
    // La hora exacta en la que ocurrió el error
    private LocalDateTime fechaHora; 

    // (Opcional) Una lista de detalles, útil si hay varios errores de validación a la vez
    private List<String> detalles; 
}
