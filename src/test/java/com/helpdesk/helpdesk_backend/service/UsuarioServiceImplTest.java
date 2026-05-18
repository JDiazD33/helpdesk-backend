package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.dto.UsuarioRequestDTO;
import com.helpdesk.helpdesk_backend.dto.UsuarioResponseDTO;
import com.helpdesk.helpdesk_backend.mapper.UsuarioMapper;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.repository.EmpresaRepository;
import com.helpdesk.helpdesk_backend.repository.RolRepository;
import com.helpdesk.helpdesk_backend.repository.UsuarioRepository;
import com.helpdesk.helpdesk_backend.service.impl.UsuarioServiceImpl;

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
    private RolRepository rolRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private UsuarioRequestDTO requestDTO;
    private Usuario usuarioMock;
    private Empresa empresaMock;
    private Rol rolMock;
    private UsuarioResponseDTO responseDTO;

    private final Long EMPRESA_CONTEXTO_ID = 1L;

    @BeforeEach
    void setUp() {
        // Inicializamos los objetos comunes para no repetir código en cada test
        empresaMock = Empresa.builder().id(EMPRESA_CONTEXTO_ID).nombre("Empresa Alpha").build();
        rolMock = Rol.builder().id(1L).nombre("ADMIN").build();

        requestDTO = UsuarioRequestDTO.builder()
                .nombres("Juan")
                .apellidos("Perez")
                .email("juan@alpha.com")
                .password("secret")
                .rolId(1L)
                .build();

        usuarioMock = Usuario.builder()
                .id(100L)
                .nombres("Juan")
                .apellidos("Pérez")
                .email("juan@alpha.com")
                .empresa(empresaMock)
                .rol(rolMock)
                .activo(true)
                .build();

        responseDTO = UsuarioResponseDTO.builder()
                .id(100L)
                .nombres("Juan")
                .email("juan@alpha.com")
                .empresaId(EMPRESA_CONTEXTO_ID)
                .rolNombre("ADMIN")
                .build();
    }

    @Test
    void crearUsuario_CuandoDatosSonValidos_GuardaYRetornaResponseDTO(){
        // Arrage: Preparamos las respuestas de nuestros mocks
        when(usuarioRepository.existsByEmail(requestDTO.getEmail())).thenReturn(false);
        when(usuarioMapper.toEntity(requestDTO)).thenReturn(usuarioMock);

        // Simulamos la búsqueda de las entidades reales (Corrección de Objeto Huérfano)
        when(rolRepository.findById(requestDTO.getRolId())).thenReturn(Optional.of(rolMock));
        when(empresaRepository.findById(EMPRESA_CONTEXTO_ID)).thenReturn(Optional.of(empresaMock));

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioMock);
        when(usuarioMapper.toResponseDTO(usuarioMock)).thenReturn(responseDTO);

        // Act: Ejecutamos el método del servicio
        UsuarioResponseDTO resultado = usuarioService.crearUsuario(requestDTO, EMPRESA_CONTEXTO_ID);

        // Assert: Verificamos los resultados
        assertThat(resultado).isNotNull();
        assertThat(resultado.getEmail()).isEqualTo("juan@alpha.com");
        assertThat(resultado.getRolNombre()).isEqualTo("ADMIN");

        // Verificamos que efectivamente se haya llamado a la BD para validar Rol y Empresa
        verify(rolRepository, times(1)).findById(requestDTO.getRolId());
        verify(empresaRepository, times(1)).findById(EMPRESA_CONTEXTO_ID);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void crearUsuario_CuandoEmpresaContextoNoCoincide_LanzaExcepcionDeSeguridad() {
        // Arrange: Simulamos que alguien intenta crear un usuario para la empresa 1, pero su token es de la empresa 999
        Long empresaContextoHacker = 999L;

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.crearUsuario(requestDTO, empresaContextoHacker);
        });

        assertThat(exception.getMessage()).contains("Violación de seguridad");
        
        // Verificamos que la base de datos NUNCA fue tocada tras el fallo de seguridad
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void crearUsuario_CuandoEmailYaExiste_LanzaExcepcion() {
        // Arrange
        when(usuarioRepository.existsByEmail(requestDTO.getEmail())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.crearUsuario(requestDTO, EMPRESA_CONTEXTO_ID);
        });

        assertThat(exception.getMessage()).contains("ya está registrado");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void obtenerUsuarioPorId_CuandoPerteneceAEmpresa_RetornaUsuario() {
        // Arrange
        when(usuarioRepository.findByIdAndEmpresaId(100L, EMPRESA_CONTEXTO_ID))
                .thenReturn(Optional.of(usuarioMock));
        when(usuarioMapper.toResponseDTO(usuarioMock)).thenReturn(responseDTO);

        // Act
        UsuarioResponseDTO resultado = usuarioService.obtenerUsuarioPorId(100L, EMPRESA_CONTEXTO_ID);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(100L);
    }

    @Test
    void eliminarUsuario_CuandoPerteneceAEmpresa_AplicaBorradoLogico() {
        // Arrange
        when(usuarioRepository.findByIdAndEmpresaId(100L, EMPRESA_CONTEXTO_ID))
                .thenReturn(Optional.of(usuarioMock));

        // Act
        usuarioService.eliminarUsuario(100L, EMPRESA_CONTEXTO_ID);

        // Assert
        // Verificamos que el estado activo pasó a false
        assertThat(usuarioMock.isActivo()).isFalse(); 
        
        // Verificamos que el repositorio guardó la entidad mutada
        verify(usuarioRepository, times(1)).save(usuarioMock); 
    }

}
