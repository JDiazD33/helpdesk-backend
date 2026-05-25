package com.helpdesk.helpdesk_backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmpresaTicketsDTOTest {

    @Test
    void empresaTicketsDTO_debeCrearseCorrectamente() {
        EmpresaTicketsDTO dto = new EmpresaTicketsDTO("Tech Solutions", 10L);

        assertEquals("Tech Solutions", dto.empresa());
        assertEquals(10L, dto.cantidadTickets());
    }
}