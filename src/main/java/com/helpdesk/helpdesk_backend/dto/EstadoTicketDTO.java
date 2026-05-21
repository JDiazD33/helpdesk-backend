package com.helpdesk.helpdesk_backend.dto;
import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;

public record EstadoTicketDTO(
    EstadoTicket estado,
    Long cantidad
) {}

//Este DTO fue creado para almacenar el resultado de la consulta que cuenta cuantos tickets hay por cada estado dentro de una empresa específica. Es utilizado en el método "contarTicketsPorEstado" del TicketRepository para mostrar un resumen de la cantidad de tickets en cada estado (Abierto, En Progreso, Cerrado) en el panel de control o dashboard del sistema.