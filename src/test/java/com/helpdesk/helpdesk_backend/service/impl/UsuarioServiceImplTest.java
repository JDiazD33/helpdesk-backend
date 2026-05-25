package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.exception.DuplicateResourceException;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuario;
    private Empresa empresa;
    private Rol rol;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().id(1L).nombre("Tech Solutions").build();
        rol = Rol.builder().id(1L).nombre("CLIENTE").build();

        usuario = Usuario.builder()
                .id(1L)
                .nombres("Juan")
                .apellidos("Perez")
                .email("juan@test.com")
                .password("123456")
                .activo(true)
                .empresa(empresa)
                .rol(rol)
                .build();
    }

    @Test
    void listarTodos_debeRetornarLista() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        List<Usuario> resultado = usuarioService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("Juan", resultado.get(0).getNombres());
    }

    @Test
    void buscarPorId_debeRetornarUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("juan@test.com", resultado.get().getEmail());
    }

    @Test
    void buscarPorId_noExiste_debeRetornarVacio() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertFalse(usuarioService.buscarPorId(99L).isPresent());
    }

    @Test
    void guardar_debeEncriptarPasswordYGuardar() {
        when(usuarioRepository.existsByEmail("juan@test.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hash-123456");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.guardar(usuario);

        assertNotNull(resultado);
        verify(passwordEncoder).encode("123456");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void guardar_emailDuplicado_lanzaExcepcion() {
        when(usuarioRepository.existsByEmail("juan@test.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                usuarioService.guardar(usuario));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void actualizar_debeActualizarYRetornarUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("nuevaPass")).thenReturn("hash-nueva");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario actualizado = Usuario.builder()
                .nombres("Juan Actualizado")
                .apellidos("Perez")
                .email("juan@test.com") // mismo email
                .password("nuevaPass")
                .activo(true)
                .empresa(empresa)
                .rol(rol)
                .build();

        Usuario resultado = usuarioService.actualizar(1L, actualizado);

        assertNotNull(resultado);
        verify(passwordEncoder).encode("nuevaPass");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void actualizar_sinPassword_noEncripta() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario actualizado = Usuario.builder()
                .nombres("Juan")
                .apellidos("Perez")
                .email("juan@test.com")
                .password("") // vacío → no encripta
                .activo(true)
                .empresa(empresa)
                .rol(rol)
                .build();

        usuarioService.actualizar(1L, actualizado);

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void actualizar_passwordNull_noEncripta() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario actualizado = Usuario.builder()
                .nombres("Juan")
                .apellidos("Perez")
                .email("juan@test.com")
                .password(null) // null → no encripta
                .activo(true)
                .empresa(empresa)
                .rol(rol)
                .build();

        usuarioService.actualizar(1L, actualizado);

        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void actualizar_noExiste_lanzaExcepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                usuarioService.actualizar(99L, usuario));
    }

    @Test
    void actualizar_emailDuplicado_lanzaExcepcion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("nuevo@test.com")).thenReturn(true);

        Usuario actualizado = Usuario.builder()
                .nombres("Juan")
                .apellidos("Perez")
                .email("nuevo@test.com") // diferente y duplicado
                .password("123456")
                .build();

        assertThrows(DuplicateResourceException.class, () ->
                usuarioService.actualizar(1L, actualizado));
    }

    @Test
    void actualizar_emailDiferenteNoDuplicado_debeActualizar() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario actualizado = Usuario.builder()
                .nombres("Juan")
                .apellidos("Perez")
                .email("nuevo@test.com") // diferente pero no duplicado
                .password("123456")
                .activo(true)
                .empresa(empresa)
                .rol(rol)
                .build();

        Usuario resultado = usuarioService.actualizar(1L, actualizado);

        assertNotNull(resultado);
        verify(usuarioRepository).existsByEmail("nuevo@test.com");
    }

    @Test
    void eliminar_debeDesactivarUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        usuarioService.eliminar(1L);

        assertFalse(usuario.isActivo());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void eliminar_noExiste_lanzaExcepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                usuarioService.eliminar(99L));
    }

    @Test
    void buscarPorEmail_debeRetornarUsuario() {
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.buscarPorEmail("juan@test.com");

        assertTrue(resultado.isPresent());
    }

    @Test
    void buscarPorEmail_noExiste_debeRetornarVacio() {
        when(usuarioRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertFalse(usuarioService.buscarPorEmail("ghost@test.com").isPresent());
    }

    @Test
    void existeEmail_debeRetornarTrue() {
        when(usuarioRepository.existsByEmail("juan@test.com")).thenReturn(true);

        assertTrue(usuarioService.existeEmail("juan@test.com"));
    }

    @Test
    void existeEmail_debeRetornarFalse() {
        when(usuarioRepository.existsByEmail("ghost@test.com")).thenReturn(false);

        assertFalse(usuarioService.existeEmail("ghost@test.com"));
    }

    @Test
    void listarPorEmpresa_debeRetornarLista() {
        when(usuarioRepository.findByEmpresaId(1L)).thenReturn(List.of(usuario));

        assertEquals(1, usuarioService.listarPorEmpresa(1L).size());
    }

    @Test
    void listarPorRol_debeRetornarLista() {
        when(usuarioRepository.findByRolId(1L)).thenReturn(List.of(usuario));

        assertEquals(1, usuarioService.listarPorRol(1L).size());
    }

    @Test
    void listarActivosPorEmpresa_debeRetornarLista() {
        when(usuarioRepository.findByEmpresaIdAndActivo(1L, true)).thenReturn(List.of(usuario));

        assertEquals(1, usuarioService.listarActivosPorEmpresa(1L, true).size());
    }

    @Test
    void listarPorEmpresaYRol_debeRetornarLista() {
        when(usuarioRepository.findByEmpresaIdAndRolId(1L, 1L)).thenReturn(List.of(usuario));

        assertEquals(1, usuarioService.listarPorEmpresaYRol(1L, 1L).size());
    }

    @Test
    void listarPorEstado_debeRetornarLista() {
        when(usuarioRepository.findByActivo(true)).thenReturn(List.of(usuario));

        assertEquals(1, usuarioService.listarPorEstado(true).size());
    }

    @Test
    void listarActivosPorEmpresaConDetalles_debeRetornarLista() {
        when(usuarioRepository.findActivosPorEmpresaConDetalles(1L)).thenReturn(List.of(usuario));

        assertEquals(1, usuarioService.listarActivosPorEmpresaConDetalles(1L).size());
    }

    @Test
    void buscarPorNombreOApellido_debeRetornarLista() {
        when(usuarioRepository.buscarPorNombreOApellido(1L, "Juan")).thenReturn(List.of(usuario));

        assertEquals(1, usuarioService.buscarPorNombreOApellido(1L, "Juan").size());
    }
}