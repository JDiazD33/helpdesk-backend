package com.helpdesk.helpdesk_backend.security;

import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.model.Permiso;
import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private CustomUserDetailsService customUserDetailsService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        customUserDetailsService = new CustomUserDetailsService(usuarioRepository);

        Permiso permiso = Permiso.builder()
                .id(1L)
                .nombre("TICKET_LEER")
                .activo(true)
                .build();

        Permiso permisoInactivo = Permiso.builder()
                .id(2L)
                .nombre("TICKET_ELIMINAR")
                .activo(false)
                .build();

        Rol rol = Rol.builder()
                .id(1L)
                .nombre("CLIENTE")
                .permisos(Set.of(permiso, permisoInactivo))
                .build();

        Empresa empresa = Empresa.builder()
                .id(1L)
                .nombre("Tech Solutions")
                .build();

        usuario = Usuario.builder()
                .id(1L)
                .nombres("Juan")
                .apellidos("Perez")
                .email("juan@test.com")
                .password("123456")
                .activo(true)
                .rol(rol)
                .empresa(empresa)
                .build();
    }

    @Test
    void loadUserByUsername_debeRetornarUserDetails() {
        when(usuarioRepository.findByEmailWithRolYPermisos("juan@test.com"))
                .thenReturn(Optional.of(usuario));

        UserDetails result = customUserDetailsService.loadUserByUsername("juan@test.com");

        assertEquals("juan@test.com", result.getUsername());
        assertEquals("123456", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE")));
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("TICKET_LEER")));
        // permiso inactivo no debe estar
        assertFalse(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("TICKET_ELIMINAR")));
    }

    @Test
    void loadUserByUsername_usuarioNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByEmailWithRolYPermisos("ghost@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("ghost@test.com"));
    }

    @Test
    void loadUserByUsername_usuarioInactivo_lanzaExcepcion() {
        usuario.setActivo(false);

        when(usuarioRepository.findByEmailWithRolYPermisos("juan@test.com"))
                .thenReturn(Optional.of(usuario));

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("juan@test.com"));

        assertTrue(ex.getMessage().contains("desactivado"));
    }
}