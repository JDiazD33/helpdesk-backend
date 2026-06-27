package com.helpdesk.helpdesk_backend.dto;

import com.helpdesk.helpdesk_backend.model.enums.PrioridadTicket;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO para el reporte publico de un ticket sin necesidad de iniciar sesion.
 *
 * El reportante NO esta autenticado, pero su correo SÍ debe corresponder a un
 * usuario CLIENTE registrado. A partir de ese correo el sistema identifica la
 * empresa a la que pertenece (no se pide empresa manualmente).
 *
 * La empresa NO viene en el DTO: se resuelve en el servicio a partir del correo.
 */
@Getter
@Setter
public class TicketAnonimoRequestDTO {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 120, message = "El correo no debe exceder 120 caracteres")
    private String correo;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{9}$", message = "El teléfono debe tener exactamente 9 dígitos")
    private String telefono;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 150, message = "El título debe tener entre 5 y 150 caracteres")
    private String titulo;

    @NotNull(message = "La prioridad es obligatoria")
    private PrioridadTicket prioridad;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;

    /** El problema es opcional: algunas empresas pueden no tener problemas configurados. */
    private Long problemaId;

    @NotBlank(message = "La descripción del problema es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    private String descripcion;
}

