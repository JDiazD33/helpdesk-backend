package com.helpdesk.helpdesk_backend.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;
import com.helpdesk.helpdesk_backend.model.enums.PrioridadTicket;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "ticket")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
/**
    Entidad principal que gestiona el ciclo de vida de los incidentes o requerimientos.
    Centraliza la relación entre clientes, agentes y empresas.
 */
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Código de negocio único generado automáticamente en el Service (ej. TCK-XXXX). */
    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @NotBlank(message = "El título del ticket es obligatorio")
    @Size(max = 150, message = "El título no debe exceder 150 caracteres")
    @Column(nullable = false, length = 150)
    private String titulo;
    
    /* columnDefinition = "TEXT": Permite descripciones extensas sin el límite de 255 caracteres del VARCHAR estándar. */
    @NotBlank(message = "La descripción del ticket es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Size(max = 20, message = "El teléfono no debe exceder 20 caracteres")
    @Column(length = 20)
    private String telefonoReportante;

    @Size(max = 120, message = "El correo no debe exceder 120 caracteres")
    @Column(length = 120)
    private String correoReportante;

    /* Persiste el nombre del Enum como String en la BD para mayor legibilidad y mantenibilidad. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTicket estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadTicket prioridad;
    
    // Justificación para el cierre del ticket, solo se llena si el estado es CERRADO
    @Column(columnDefinition = "TEXT")
    private String justificacionCierre; 

    // Almacena la URL o path del archivo adjunto como evidencia de cierre.
    private String imagenCierre;

    /* Calificación que el CLIENTE da al agente cuando el ticket pasa a RESUELTO.
       Rango 1-5 (estrellas). Es nullable: solo se llena si el cliente califica. */
    @Column(name = "calificacion_agente")
    private Integer calificacionAgente;
    
    /* Campos de auditoría automática para control de SLAs (Acuerdo de Nivel de Servicio) y tiempos de respuesta. */
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    /* Relaciones con FetchType.LAZY para evitar el problema de consultas N+1 y optimizar el rendimiento. */
    @NotNull(message = "El cliente es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_asignado_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario agenteAsignado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private CategoriaTicket categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problema_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProblemaTicket problema;

    @NotNull(message = "La empresa es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Empresa empresa;

    @PrePersist
    public void prePersist() {
        if (this.codigo == null || this.codigo.trim().isEmpty()) {
            this.codigo = "TCK-" + (1000 + new java.util.Random().nextInt(9000));
        }
        if (this.estado == null) {
            this.estado = com.helpdesk.helpdesk_backend.model.enums.EstadoTicket.ABIERTO;
        }
        if (this.prioridad == null) {
            this.prioridad = com.helpdesk.helpdesk_backend.model.enums.PrioridadTicket.MEDIA;
        }
    }

}
