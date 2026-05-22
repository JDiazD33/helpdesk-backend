package com.helpdesk.helpdesk_backend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "empresa")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
/* 
    Entidad que representa a las empresas clientes del sistema.
    Se utiliza el nombre en singular siguiendo las convenciones de JPA. 
*/
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(max = 150, message = "El nombre no debe exceder 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nombre;

    @NotBlank(message = "El RUC es obligatorio")
    @Size(max = 15, message = "El RUC no debe exceder 15 caracteres")
    @Column(nullable = false, unique = true, length = 15)
    private String ruc;

    @NotBlank(message = "El correo de contacto es obligatorio")
    @Email(message = "El correo de contacto debe ser un email válido")
    @Size(max = 120, message = "El correo no debe exceder 120 caracteres")
    @Column(name = "correo_contacto", nullable = false, unique = true, length = 120)
    private String correoContacto;

    @NotBlank(message = "El teléfono de contacto es obligatorio")
    @Size(max = 20, message = "El teléfono no debe exceder 20 caracteres")
    @Column(name = "telefono_contacto", nullable = false, length = 20)
    private String telefonoContacto;
    
    /* Estado lógico: Permite inhabilitar una empresa sin perder su historial transaccional. */
    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;
    
    /* Auditoría: Se registra automáticamente al insertar el registro y no permite modificaciones posteriores. */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
}
