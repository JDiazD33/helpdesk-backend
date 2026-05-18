package com.helpdesk.helpdesk_backend.dto;

import java.time.LocalDateTime;

import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;
import com.helpdesk.helpdesk_backend.model.enums.PrioridadTicket;

import lombok.Data;

@Data
public class TicketResponseDTO {

    private Long id;
    private String codigo;
    private String titulo;
    private String descripcion;
    private EstadoTicket estado;
    private PrioridadTicket prioridad;
    private String justifiacionCierre;
    private String imagenCierre;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Relaciones Aplanadas
    private Long clienteId;
    private String clienteNombre; 
    
    private Long agenteAsignadoId;
    private String agenteNombre;
    
    private Long problemaId;
    private String problemaNombre;
}
