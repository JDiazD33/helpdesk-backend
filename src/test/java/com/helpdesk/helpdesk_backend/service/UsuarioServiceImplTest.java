package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.repository.UsuarioRepository;
import com.helpdesk.helpdesk_backend.service.impl.UsuarioServiceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuarioMock;
    private Empresa empresaMock;
    private Rol rolMock;

    @BeforeEach
    void setUp() {
        empresaMock = Empresa.builder().id(1L).nombre("Empresa Alpha").build();
        rolMock = Rol.builder().id(1L).nombre("ADMIN").build();

        usuarioMock = Usuario.builder()
                .id(100L)
                .nombres("Juan")
                .apellidos("Pérez")
                .email("juan@alpha.com")
                .password("secret")
                .empresa(empresaMock)
                .rol(rolMock)
                .activo(true)
                .build();
    }

    @Test
    void guardar_CuandoDatosSonValidos_GuardaYRetornaUsuario(){
        when(usuarioRepository.existsByEmail(usuarioMock.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(usuarioMock.getPassword())).thenReturn("encodedSecret");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);

        Usuario resultado = usuarioService.guardar(usuarioMock);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEmail()).isEqualTo("juan@alpha.com");
        assertThat(resultado.getPassword()).isEqualTo("encodedSecret");

        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void guardar_CuandoEmailYaExiste_LanzaExcepcion() {
        when(usuarioRepository.existsByEmail(usuarioMock.getEmail())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.guardar(usuarioMock);
        });

        assertThat(exception.getMessage()).contains("Ya existe");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void buscarPorId_RetornaUsuario() {
        when(usuarioRepository.findById(100L)).thenReturn(Optional.of(usuarioMock));

        Optional<Usuario> resultado = usuarioService.buscarPorId(100L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(100L);
    }

    @Test
    void eliminar_AplicaBorradoLogico() {
        when(usuarioRepository.findByIdAndEmpresaId(100L, 1L)).thenReturn(Optional.of(usuarioMock));

        usuarioService.eliminar(100L, 1L);

        assertThat(usuarioMock.isActivo()).isFalse(); 
        verify(usuarioRepository, times(1)).save(usuarioMock); 
    }

}
