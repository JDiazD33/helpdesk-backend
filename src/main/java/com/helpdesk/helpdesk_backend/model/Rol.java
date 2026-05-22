package com.helpdesk.helpdesk_backend.model;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "rol")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
/*
    Entidad que define los roles de acceso en el sistema (ej. ADMIN_EMPRESA, AGENTE, CLIENTE).
    Se mantiene en singular para respetar las convenciones de JPA.
 */
public class Rol {
  
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* unique = true: No pueden existir dos roles con el mismo nombre.
       length = 20: Espacio optimizado, suficiente para nombres de roles estándar. */
    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = 20, message = "El nombre del rol no debe exceder 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String nombre;

    /* Estado lógico: Permite inhabilitar un rol sin eliminar los usuarios asociados. */
    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    @ManyToMany
    @JoinTable(
        name = "rol_permiso",
        joinColumns = @JoinColumn(name = "rol_id"),
        inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "roles"})
    @Builder.Default
    private Set<Permiso> permisos = new HashSet<>();
}
