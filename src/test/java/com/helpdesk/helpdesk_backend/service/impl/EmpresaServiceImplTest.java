package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.dto.EmpresaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.EmpresaResponseDTO;
import com.helpdesk.helpdesk_backend.exception.DuplicateResourceException;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.repository.EmpresaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceImplTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private EmpresaServiceImpl empresaService;

    private Empresa empresa;
    private EmpresaRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .id(1L)
                .nombre("Tech Solutions")
                .ruc("12345678901")
                .correoContacto("contacto@tech.com")
                .telefonoContacto("999999999")
                .activo(true)
                .build();

        requestDTO = EmpresaRequestDTO.builder()
                .nombre("Tech Solutions")
                .ruc("12345678901")
                .correoContacto("contacto@tech.com")
                .telefonoContacto("999999999")
                .build();
    }

    @Test
    void crearEmpresa_debeCrearYRetornarDTO() {
        when(empresaRepository.existsByRuc("12345678901")).thenReturn(false);
        when(empresaRepository.existsByCorreoContacto("contacto@tech.com")).thenReturn(false);
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);

        EmpresaResponseDTO resultado = empresaService.crearEmpresa(requestDTO);

        assertEquals("Tech Solutions", resultado.getNombre());
        assertEquals("12345678901", resultado.getRuc());
        verify(empresaRepository).save(any(Empresa.class));
    }

    @Test
    void crearEmpresa_rucDuplicado_lanzaExcepcion() {
        when(empresaRepository.existsByRuc("12345678901")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                empresaService.crearEmpresa(requestDTO));

        verify(empresaRepository, never()).save(any());
    }

    @Test
    void crearEmpresa_correoDuplicado_lanzaExcepcion() {
        when(empresaRepository.existsByRuc("12345678901")).thenReturn(false);
        when(empresaRepository.existsByCorreoContacto("contacto@tech.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                empresaService.crearEmpresa(requestDTO));

        verify(empresaRepository, never()).save(any());
    }

    @Test
    void obtenerEmpresaPorId_debeRetornarDTO() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        EmpresaResponseDTO resultado = empresaService.obtenerEmpresaPorId(1L);

        assertEquals("Tech Solutions", resultado.getNombre());
    }

    @Test
    void obtenerEmpresaPorId_noExiste_lanzaExcepcion() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                empresaService.obtenerEmpresaPorId(99L));
    }

    @Test
    void listarEmpresasActivas_debeRetornarSoloActivas() {
        Empresa inactiva = Empresa.builder()
                .id(2L)
                .nombre("Inactiva SA")
                .activo(false)
                .build();

        when(empresaRepository.findAll()).thenReturn(List.of(empresa, inactiva));

        List<EmpresaResponseDTO> resultado = empresaService.listarEmpresasActivas();

        assertEquals(1, resultado.size());
        assertEquals("Tech Solutions", resultado.get(0).getNombre());
    }

    @Test
    void actualizarEmpresa_debeActualizarYRetornarDTO() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);

        EmpresaResponseDTO resultado = empresaService.actualizarEmpresa(1L, requestDTO);

        assertNotNull(resultado);
        verify(empresaRepository).save(any(Empresa.class));
    }

    @Test
    void actualizarEmpresa_noExiste_lanzaExcepcion() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                empresaService.actualizarEmpresa(99L, requestDTO));
    }

    @Test
    void actualizarEmpresa_rucDuplicado_lanzaExcepcion() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.existsByRuc("99999999999")).thenReturn(true);

        EmpresaRequestDTO nuevoRequest = EmpresaRequestDTO.builder()
                .nombre("Tech Solutions")
                .ruc("99999999999") // diferente al existente
                .correoContacto("contacto@tech.com")
                .telefonoContacto("999999999")
                .build();

        assertThrows(DuplicateResourceException.class, () ->
                empresaService.actualizarEmpresa(1L, nuevoRequest));
    }

    @Test
    void actualizarEmpresa_correoDuplicado_lanzaExcepcion() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.existsByRuc("12345678901")).thenReturn(false);
        when(empresaRepository.existsByCorreoContacto("nuevo@tech.com")).thenReturn(true);

        EmpresaRequestDTO nuevoRequest = EmpresaRequestDTO.builder()
                .nombre("Tech Solutions")
                .ruc("12345678901")
                .correoContacto("nuevo@tech.com") // diferente al existente
                .telefonoContacto("999999999")
                .build();

        assertThrows(DuplicateResourceException.class, () ->
                empresaService.actualizarEmpresa(1L, nuevoRequest));
    }

    @Test
    void actualizarEmpresa_mismoRucYCorreo_noVerificaDuplicados() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);

        // mismo ruc y correo → no entra al &&
        EmpresaResponseDTO resultado = empresaService.actualizarEmpresa(1L, requestDTO);

        assertNotNull(resultado);
        verify(empresaRepository, never()).existsByRuc(any());
        verify(empresaRepository, never()).existsByCorreoContacto(any());
    }

    @Test
    void actualizarEmpresa_rucDiferenteNoDuplicado_debeActualizar() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.existsByRuc("99999999999")).thenReturn(false);
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);

        EmpresaRequestDTO nuevoRequest = EmpresaRequestDTO.builder()
                .nombre("Tech Solutions")
                .ruc("99999999999") // diferente pero no duplicado
                .correoContacto("contacto@tech.com")
                .telefonoContacto("999999999")
                .build();

        EmpresaResponseDTO resultado = empresaService.actualizarEmpresa(1L, nuevoRequest);

        assertNotNull(resultado);
        verify(empresaRepository).existsByRuc("99999999999");
    }

    @Test
    void actualizarEmpresa_correoDiferenteNoDuplicado_debeActualizar() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.existsByRuc("12345678901")).thenReturn(false);
        when(empresaRepository.existsByCorreoContacto("nuevo@tech.com")).thenReturn(false);
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);

        EmpresaRequestDTO nuevoRequest = EmpresaRequestDTO.builder()
                .nombre("Tech Solutions")
                .ruc("12345678901")
                .correoContacto("nuevo@tech.com") // diferente pero no duplicado
                .telefonoContacto("999999999")
                .build();

        EmpresaResponseDTO resultado = empresaService.actualizarEmpresa(1L, nuevoRequest);

        assertNotNull(resultado);
        verify(empresaRepository).existsByCorreoContacto("nuevo@tech.com");
    }

    @Test
    void eliminarEmpresa_debeDesactivar() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        empresaService.eliminarEmpresa(1L);

        assertFalse(empresa.isActivo());
        verify(empresaRepository).save(empresa);
    }

    @Test
    void eliminarEmpresa_noExiste_lanzaExcepcion() {
        when(empresaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                empresaService.eliminarEmpresa(99L));
    }
}