package com.helpdesk.helpdesk_backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;
import com.helpdesk.helpdesk_backend.model.enums.PrioridadTicket;

import jakarta.persistence.*;
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

    /* Código de negocio único generado automáticamente (ej. TCK-20231024-A1B2C3D4). */
    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String titulo;
    
    /* columnDefinition = "TEXT": Permite descripciones extensas sin el límite de 255 caracteres del VARCHAR estándar. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    // Ya tenemos correo y telefono en la entidad Usuario, por lo que no es necesario repetirlos aquí.

    /* Persiste el nombre del Enum como String en la BD para mayor legibilidad y mantenibilidad. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTicket estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrioridadTicket prioridad;
    
    // Justificación para el cierre del ticket, solo se llena si el estado es CERRADO
    @Column(name = "justificacion_cierre", columnDefinition = "TEXT")
    private String justifiacionCierre;

    // Almacena la URL o path del archivo adjunto como evidencia de cierre.
    private String imagenCierre;
    
    /* Campos de auditoría automática para control de SLAs (Acuerdo de Nivel de Servicio) y tiempos de respuesta. */
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    /* Relaciones con FetchType.LAZY para evitar el problema de consultas N+1 y optimizar el rendimiento. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agente_asignado_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario agenteAsignado;

    // Eliminamos categoria para simplificar el modelo, ya que el campo "problema" puede ser suficiente para clasificar el ticket.

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problema_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ProblemaTicket problema;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Empresa empresa;

    /**
          Hook de JPA que se ejecuta antes de insertar el registro.
          Garantiza valores por defecto y la generacion del codigo unico de ticket.
    */
    @PrePersist
    public void prePersist() {

        if (this.estado == null) {
            this.estado = EstadoTicket.ABIERTO;
        }

        if (this.prioridad == null) {
            this.prioridad = PrioridadTicket.MEDIA;
        }

        if (this.codigo == null || this.codigo.isBlank()) {
            LocalDate hoy = LocalDate.now();
            // Formato de fecha para el código: YYYYMMDD, del cual extraeremos el año, mes y dia para generar un codigo unico por dia.
            String fechaParte = String.format("%04d%02d%02d", hoy.getYear(), hoy.getMonthValue(), hoy.getDayOfMonth());
            // Generamos un UUID aleatorio y tomamos los primeros 8 caracteres para asegurar la unicidad del código, 
            // incluso si se crean múltiples tickets el mismo día.
            this.codigo = "TCK-" + fechaParte + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }

}
