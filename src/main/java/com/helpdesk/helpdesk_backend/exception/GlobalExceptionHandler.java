package com.helpdesk.helpdesk_backend.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ATrapa los RuntimeException (Nuestros errores de seguridad y reglas de negocio)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> manejarErroresDeNegocio(RuntimeException exception){
        // Construimos nuestra carta formal
        ErrorResponseDTO errorDTO = ErrorResponseDTO.builder()
            .mensaje(exception.getMessage()) // Sacamos el texto que escribió el Service
            .codigoEstado(HttpStatus.BAD_REQUEST.value()) // Le decimos a la web que fue un "Mal Pedido" (400)
            .fechaHora(LocalDateTime.now()) 
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDTO);
    }

    /**
     * Atrapa los errores de @Valid (Cuando el usuario deja campos en blanco en los DTOs)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarErroresDeValidacion(MethodArgumentNotValidException exception) {
        
        // Extraemos todos los mensajes de error de los @NotBlank, @NotNull, etc.
        List<String> errores = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorResponseDTO errorDTO = ErrorResponseDTO.builder()
                .mensaje("Error en los datos enviados. Por favor, revise el formulario.")
                .codigoEstado(HttpStatus.BAD_REQUEST.value())
                .fechaHora(LocalDateTime.now())
                .detalles(errores) // Aquí metemos la lista de campos que el usuario llenó mal
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDTO);
    }
}
