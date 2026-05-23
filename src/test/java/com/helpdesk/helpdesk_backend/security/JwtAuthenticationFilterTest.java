package com.helpdesk.helpdesk_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);
        SecurityContextHolder.clearContext();

        userDetails = new User(
                "juan@test.com",
                "123456",
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );
    }

    @Test
    void doFilterInternal_sinHeader_continuaSinAutenticar() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_headerSinBearer_continuaSinAutenticar() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_tokenValido_autenticaUsuario() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-valido");
        when(jwtUtil.extractEmail("token-valido")).thenReturn("juan@test.com");
        when(userDetailsService.loadUserByUsername("juan@test.com")).thenReturn(userDetails);
        when(jwtUtil.validateToken("token-valido", "juan@test.com")).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("juan@test.com",
                SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternal_tokenInvalido_continuaSinAutenticar() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-invalido");
        when(jwtUtil.extractEmail("token-invalido")).thenReturn("juan@test.com");
        when(userDetailsService.loadUserByUsername("juan@test.com")).thenReturn(userDetails);
        when(jwtUtil.validateToken("token-invalido", "juan@test.com")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_tokenLanzaExcepcion_continuaSinAutenticar() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-roto");
        when(jwtUtil.extractEmail("token-roto")).thenThrow(new RuntimeException("Token expirado"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}