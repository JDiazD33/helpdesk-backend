package com.helpdesk.helpdesk_backend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/*
    Entidad que representa a los usuarios del sistema.
    Se utiliza el nombre en singular siguiendo las convenciones de JPA.
 */
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no deben exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no deben exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String apellidos;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 120, message = "El email no debe exceder 120 caracteres")
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    /* WRITE_ONLY: Permite recibir la clave al crear/editar, pero nunca la incluye en las respuestas JSON */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Column(nullable = false, length = 255)// Longitud de 255 recomendada para hashes de contraseñas (BCrypt)
    private String password;

    @Size(max = 20, message = "El teléfono no debe exceder 20 caracteres")
    @Column(length = 20)
    private String telefono;

    /* Se usa el tipo primitivo boolean para asegurar que el valor por defecto sea false/true y no null */
    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    /* Registra automáticamente la fecha de creación. updatable = false impide que se modifique después */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /* Usuario debe tener empresa y rol, relaciones obligatorias (optional = false). 
    LAZY para optimizar carga; 
       JsonIgnoreProperties evita errores con los proxies de Hibernate al serializar a JSON */
    @NotNull(message = "La empresa es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Empresa empresa;

    @NotNull(message = "El rol es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rol_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Rol rol;
}
