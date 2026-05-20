package com.helpdesk.helpdesk_backend.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class TicketTest {

    @Test
    void instanciacion_Ticket_deberiaFuncionar() {
        Ticket ticket = new Ticket();
        assertNotNull(ticket);
    }
}
