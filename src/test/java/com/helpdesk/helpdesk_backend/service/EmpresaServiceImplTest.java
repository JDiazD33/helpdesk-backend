package com.helpdesk.helpdesk_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.helpdesk.helpdesk_backend.dto.EmpresaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.EmpresaResponseDTO;
import com.helpdesk.helpdesk_backend.mapper.EmpresaMapper;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.repository.EmpresaRepository;
import com.helpdesk.helpdesk_backend.service.impl.EmpresaServiceImpl;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceImplTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private EmpresaMapper empresaMapper;

    @InjectMocks
    private EmpresaServiceImpl empresaService;

    private EmpresaRequestDTO requestDTO;
    private Empresa empresaMock;
    private EmpresaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = EmpresaRequestDTO.builder()
                .nombre("Empresa Alpha")
                .ruc("10000000001")
                .correoContacto("alpha@empresa.com")
                .telefonoContacto("111111111")
                .build();

        empresaMock = Empresa.builder()
                .id(1L)
                .nombre("Empresa Alpha")
                .ruc("10000000001")
                .correoContacto("alpha@empresa.com")
                .telefonoContacto("111111111")
                .activo(true)
                .build();

        responseDTO = EmpresaResponseDTO.builder()
                .id(1L)
                .nombre("Empresa Alpha")
                .ruc("10000000001")
                .correoContacto("alpha@empresa.com")
                .telefonoContacto("111111111")
                .activo(true)
                .build();
    }

    @Test
    void crearEmpresa_CuandoDatosSonValidos_GuardaYRetornaDTO() {
        // Arrange
        when(empresaRepository.findByRuc(requestDTO.getRuc())).thenReturn(Optional.empty());
        when(empresaMapper.toEntity(requestDTO)).thenReturn(empresaMock);
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresaMock);
        when(empresaMapper.toResponseDTO(empresaMock)).thenReturn(responseDTO);

        // Act
        EmpresaResponseDTO resultado = empresaService.crearEmpresa(requestDTO);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getRuc()).isEqualTo("10000000001");
        
        verify(empresaRepository, times(1)).findByRuc(requestDTO.getRuc());
        verify(empresaRepository, times(1)).save(any(Empresa.class));
    }

    @Test
    void crearEmpresa_CuandoRucYaExiste_LanzaExcepcion() {
        // Arrange: Simulamos que ya existe una empresa con ese RUC
        when(empresaRepository.findByRuc(requestDTO.getRuc())).thenReturn(Optional.of(empresaMock));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            empresaService.crearEmpresa(requestDTO);
        });

        assertThat(exception.getMessage()).contains("El RUC ya se encuentra registrado");
        verify(empresaRepository, never()).save(any(Empresa.class)); // Verificamos que no se intentó guardar
    }

    @Test
    void obtenerEmpresaPorId_CuandoExisteYEstaActiva_RetornaDTO() {
        // Arrange
        when(empresaRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(empresaMock));
        when(empresaMapper.toResponseDTO(empresaMock)).thenReturn(responseDTO);

        // Act
        EmpresaResponseDTO resultado = empresaService.obtenerEmpresaPorId(1L);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    void obtenerEmpresaPorId_CuandoNoExisteOEstaInactiva_LanzaExcepcion() {
        // Arrange
        when(empresaRepository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            empresaService.obtenerEmpresaPorId(99L);
        });
    }

    @Test
    void listarEmpresasActivas_RetornaListaDeDTOs() {
        // Arrange
        Empresa empresaInactiva = Empresa.builder().id(2L).activo(false).build();
        List<Empresa> empresas = Arrays.asList(empresaMock, empresaInactiva);
        
        when(empresaRepository.findAll()).thenReturn(empresas);
        when(empresaMapper.toResponseDTO(empresaMock)).thenReturn(responseDTO);

        // Act
        List<EmpresaResponseDTO> resultado = empresaService.listarEmpresasActivas();

        // Assert
        assertThat(resultado).hasSize(1); // Debe filtrar la inactiva
        assertThat(resultado.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void actualizarEmpresa_CuandoExiste_ActualizaYRetornaDTO() {
        // Arrange
        EmpresaRequestDTO updateRequest = EmpresaRequestDTO.builder()
                .nombre("Empresa Beta")
                .ruc("10000000001")
                .correoContacto("beta@empresa.com")
                .telefonoContacto("222222222")
                .build();

        EmpresaResponseDTO updateResponse = EmpresaResponseDTO.builder()
                .id(1L)
                .nombre("Empresa Beta")
                .ruc("10000000001")
                .correoContacto("beta@empresa.com")
                .telefonoContacto("222222222")
                .activo(true)
                .build();

        when(empresaRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(empresaMock));
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresaMock);
        when(empresaMapper.toResponseDTO(empresaMock)).thenReturn(updateResponse);

        // Act
        EmpresaResponseDTO resultado = empresaService.actualizarEmpresa(1L, updateRequest);

        // Assert
        assertThat(resultado.getNombre()).isEqualTo("Empresa Beta");
        assertThat(resultado.getCorreoContacto()).isEqualTo("beta@empresa.com");
    }

    @Test
    void eliminarEmpresa_CuandoExiste_AplicaBorradoLogico() {
        // Arrange
        when(empresaRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(empresaMock));

        // Act
        empresaService.eliminarEmpresa(1L);

        // Assert
        assertThat(empresaMock.isActivo()).isFalse(); // Validamos que el estado cambió a falso
        verify(empresaRepository, times(1)).save(empresaMock); // Validamos que se persistió el cambio
    }
}