package com.helpdesk.helpdesk_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaRequestDTO {

    @NotBlank(message = "El RUC es obligatorio")
    @Size(max = 11, message = "El RUC no puede tener más de 11 caracteres")
    private String ruc;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede tener más de 150 caracteres")
    private String nombre;
    
    @NotBlank(message = "El teléfono de contacto es obligatorio")
    @Size(max = 12, message = "El teléfono de contacto no puede tener más de 12 caracteres")
    private String telefonoContacto;
    
    @NotBlank(message = "El correo de contacto es obligatorio")
    @Email(message = "Debe ser una dirección de correo electrónico válida")
    @Size(max = 100, message = "El correo no puede tener más de 100 caracteres")
    private String correoContacto;
}
