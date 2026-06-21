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
import com.helpdesk.helpdesk_backend.exception.DuplicateResourceException;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.repository.EmpresaRepository;
import com.helpdesk.helpdesk_backend.service.impl.EmpresaServiceImpl;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceImplTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private EmpresaServiceImpl empresaService;

    private EmpresaRequestDTO requestDTO;
    private Empresa empresaMock;

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
    }

    @Test
    void crearEmpresa_CuandoDatosSonValidos_GuardaYRetornaDTO() {
        when(empresaRepository.existsByRuc(requestDTO.getRuc())).thenReturn(false);
        when(empresaRepository.existsByCorreoContacto(requestDTO.getCorreoContacto())).thenReturn(false);
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresaMock);

        EmpresaResponseDTO resultado = empresaService.crearEmpresa(requestDTO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getRuc()).isEqualTo("10000000001");
        
        verify(empresaRepository, times(1)).save(any(Empresa.class));
    }

    @Test
    void crearEmpresa_CuandoRucYaExiste_LanzaExcepcion() {
        when(empresaRepository.existsByRuc(requestDTO.getRuc())).thenReturn(true);

        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            empresaService.crearEmpresa(requestDTO);
        });

        assertThat(exception.getMessage()).contains("Ya existe una empresa con el RUC");
        verify(empresaRepository, never()).save(any(Empresa.class)); 
    }

    @Test
    void obtenerEmpresaPorId_CuandoExiste_RetornaDTO() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresaMock));

        EmpresaResponseDTO resultado = empresaService.obtenerEmpresaPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    void obtenerEmpresaPorId_CuandoNoExiste_LanzaExcepcion() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            empresaService.obtenerEmpresaPorId(99L);
        });
    }

    @Test
    void listarEmpresasActivas_RetornaListaDeDTOs() {
        when(empresaRepository.listarEmpresasActivas()).thenReturn(List.of(empresaMock));

        List<EmpresaResponseDTO> resultado = empresaService.listarEmpresasActivas();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void actualizarEmpresa_CuandoExiste_ActualizaYRetornaDTO() {
        EmpresaRequestDTO updateRequest = EmpresaRequestDTO.builder()
                .nombre("Empresa Beta")
                .ruc("10000000001")
                .correoContacto("beta@empresa.com")
                .telefonoContacto("222222222")
                .build();

        Empresa empresaActualizadaMock = Empresa.builder()
                .id(1L)
                .nombre("Empresa Beta")
                .ruc("10000000001")
                .correoContacto("beta@empresa.com")
                .telefonoContacto("222222222")
                .activo(true)
                .build();

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresaMock));
        // No cambia ruc, no se llama existsByRuc
        // Cambia correo, pero existsByCorreoContacto false
        when(empresaRepository.existsByCorreoContacto(updateRequest.getCorreoContacto())).thenReturn(false);
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresaActualizadaMock);

        EmpresaResponseDTO resultado = empresaService.actualizarEmpresa(1L, updateRequest);

        assertThat(resultado.getNombre()).isEqualTo("Empresa Beta");
        assertThat(resultado.getCorreoContacto()).isEqualTo("beta@empresa.com");
    }

    @Test
    void eliminarEmpresa_CuandoExiste_AplicaBorradoLogico() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresaMock));

        empresaService.eliminarEmpresa(1L);

        assertThat(empresaMock.isActivo()).isFalse(); 
        verify(empresaRepository, times(1)).save(empresaMock); 
    }
}