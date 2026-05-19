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
public class EmpresaResponseDTO {

    private Long id;
    private String nombre;
    private String ruc;
    private String correoContacto;
    private String telefonoContacto;
    private boolean activo;
    private LocalDateTime fechaCreacion;
}
