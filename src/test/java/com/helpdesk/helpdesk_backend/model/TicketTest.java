package com.helpdesk.helpdesk_backend.model;

import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;
import com.helpdesk.helpdesk_backend.model.enums.PrioridadTicket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    @Test
    void prePersist_sinCodigo_debeGenerarCodigo() {
        Ticket ticket = new Ticket();

        ticket.prePersist();

        assertNotNull(ticket.getCodigo());
        assertTrue(ticket.getCodigo().startsWith("TCK-"));
    }

    @Test
    void prePersist_codigoVacio_debeGenerarCodigo() {
        Ticket ticket = new Ticket();
        ticket.setCodigo("   ");

        ticket.prePersist();

        assertTrue(ticket.getCodigo().startsWith("TCK-"));
    }

    @Test
    void prePersist_conCodigo_noSobreescribe() {
        Ticket ticket = new Ticket();
        ticket.setCodigo("TCK-9999");

        ticket.prePersist();

        assertEquals("TCK-9999", ticket.getCodigo());
    }

    @Test
    void prePersist_sinEstado_debeAsignarAbierto() {
        Ticket ticket = new Ticket();

        ticket.prePersist();

        assertEquals(EstadoTicket.ABIERTO, ticket.getEstado());
    }

    @Test
    void prePersist_conEstado_noSobreescribe() {
        Ticket ticket = new Ticket();
        ticket.setEstado(EstadoTicket.CERRADO);

        ticket.prePersist();

        assertEquals(EstadoTicket.CERRADO, ticket.getEstado());
    }

    @Test
    void prePersist_sinPrioridad_debeAsignarMedia() {
        Ticket ticket = new Ticket();

        ticket.prePersist();

        assertEquals(PrioridadTicket.MEDIA, ticket.getPrioridad());
    }

    @Test
    void prePersist_conPrioridad_noSobreescribe() {
        Ticket ticket = new Ticket();
        ticket.setPrioridad(PrioridadTicket.CRITICA);

        ticket.prePersist();

        assertEquals(PrioridadTicket.CRITICA, ticket.getPrioridad());
    }
}