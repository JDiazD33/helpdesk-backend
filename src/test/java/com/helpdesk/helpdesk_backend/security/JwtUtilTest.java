package com.helpdesk.helpdesk_backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Mínimo 32 caracteres para HS256
        ReflectionTestUtils.setField(jwtUtil, "secret", "clave-secreta-super-segura-12345678");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1 hora
    }

    @Test
    void generateToken_debeGenerarTokenNoNulo() {
        String token = jwtUtil.generateToken("juan@test.com", "CLIENTE", 1L, 1L);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractEmail_debeRetornarEmailCorrecto() {
        String token = jwtUtil.generateToken("juan@test.com", "CLIENTE", 1L, 1L);
        assertEquals("juan@test.com", jwtUtil.extractEmail(token));
    }

    @Test
    void extractRol_debeRetornarRolCorrecto() {
        String token = jwtUtil.generateToken("juan@test.com", "ADMIN_EMPRESA", 1L, 1L);
        assertEquals("ADMIN_EMPRESA", jwtUtil.extractRol(token));
    }

    @Test
    void extractExpiration_debeRetornarFechaFutura() {
        String token = jwtUtil.generateToken("juan@test.com", "CLIENTE", 1L, 1L);
        Date expiration = jwtUtil.extractExpiration(token);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void validateToken_tokenValido_debeRetornarTrue() {
        String token = jwtUtil.generateToken("juan@test.com", "CLIENTE", 1L, 1L);
        assertTrue(jwtUtil.validateToken(token, "juan@test.com"));
    }

    @Test
    void validateToken_emailIncorrecto_debeRetornarFalse() {
        String token = jwtUtil.generateToken("juan@test.com", "CLIENTE", 1L, 1L);
        assertFalse(jwtUtil.validateToken(token, "otro@test.com"));
    }

    @Test
    void validateToken_tokenExpirado_debeRetornarFalse() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String token = jwtUtil.generateToken("juan@test.com", "CLIENTE", 1L, 1L);
        assertFalse(jwtUtil.validateToken(token, "juan@test.com"));
    }

    @Test
    void getExpirationSeconds_debeRetornarSegundos() {
        assertEquals(3600L, jwtUtil.getExpirationSeconds());
    }
}