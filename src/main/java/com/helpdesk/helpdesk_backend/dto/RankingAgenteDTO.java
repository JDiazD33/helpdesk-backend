package com.helpdesk.helpdesk_backend.dto;

public record RankingAgenteDTO(
    String agente,
    Long total
) {}

//Este DTO es utilizado en el método "rankingAgentes" del TicketRepository para mostrar un ranking de los agentes con más tickets asignados, lo cual es útil para evaluar el rendimiento de los agentes en el panel de control o dashboard del sistema.
