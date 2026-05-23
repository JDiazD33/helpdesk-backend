package com.helpdesk.helpdesk_backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    @Test
    void passwordEncoder_debeRetornarBCrypt() {
        JwtAuthenticationFilter filter = mock(JwtAuthenticationFilter.class);
        SecurityConfig config = new SecurityConfig(filter);

        PasswordEncoder encoder = config.passwordEncoder();

        assertNotNull(encoder);
        assertTrue(encoder.matches("123456", encoder.encode("123456")));
    }

    @Test
    void corsConfigurationSource_debeRetornarConfiguracion() {
        JwtAuthenticationFilter filter = mock(JwtAuthenticationFilter.class);
        SecurityConfig config = new SecurityConfig(filter);

        CorsConfigurationSource source = config.corsConfigurationSource();

        assertNotNull(source);
    }
}