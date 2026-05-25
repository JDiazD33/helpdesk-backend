package com.helpdesk.helpdesk_backend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioRolDTOTest {

    @Test
    void usuarioRolDTO_debeCrearseCorrectamente() {
        UsuarioRolDTO dto = new UsuarioRolDTO("CLIENTE", 5L);

        assertEquals("CLIENTE", dto.rol());
        assertEquals(5L, dto.cantidad());
        assertNotNull(dto.toString());
        assertEquals(dto, new UsuarioRolDTO("CLIENTE", 5L));
        assertEquals(dto.hashCode(), new UsuarioRolDTO("CLIENTE", 5L).hashCode());
        assertNotEquals(dto, new UsuarioRolDTO("ADMIN", 1L));
    }
}