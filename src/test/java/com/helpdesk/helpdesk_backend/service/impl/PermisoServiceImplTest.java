package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.exception.DuplicateResourceException;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.model.Permiso;
import com.helpdesk.helpdesk_backend.repository.PermisoRepository;

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
class PermisoServiceImplTest {

    @Mock
    private PermisoRepository permisoRepository;

    @InjectMocks
    private PermisoServiceImpl permisoService;

    private Permiso permiso;

    @BeforeEach
    void setUp() {
        permiso = Permiso.builder()
                .id(1L)
                .nombre("TICKET_LEER")
                .descripcion("Permite leer tickets")
                .activo(true)
                .build();
    }

    @Test
    void listarTodos_debeRetornarLista() {
        when(permisoRepository.findAll()).thenReturn(List.of(permiso));

        List<Permiso> resultado = permisoService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("TICKET_LEER", resultado.get(0).getNombre());
    }

    @Test
    void listarActivos_debeRetornarLista() {
        when(permisoRepository.findAllActivosOrdenados()).thenReturn(List.of(permiso));

        List<Permiso> resultado = permisoService.listarActivos();

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).isActivo());
    }

    @Test
    void buscarPorId_debeRetornarPermiso() {
        when(permisoRepository.findById(1L)).thenReturn(Optional.of(permiso));

        Optional<Permiso> resultado = permisoService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("TICKET_LEER", resultado.get().getNombre());
    }

    @Test
    void buscarPorId_noExiste_debeRetornarVacio() {
        when(permisoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Permiso> resultado = permisoService.buscarPorId(99L);

        assertFalse(resultado.isPresent());
    }

    @Test
    void guardar_debeGuardarYRetornarPermiso() {
        when(permisoRepository.existsByNombre("TICKET_LEER")).thenReturn(false);
        when(permisoRepository.save(any(Permiso.class))).thenReturn(permiso);

        Permiso resultado = permisoService.guardar(permiso);

        assertEquals("TICKET_LEER", resultado.getNombre());
        verify(permisoRepository).save(permiso);
    }

    @Test
    void guardar_nombreDuplicado_lanzaExcepcion() {
        when(permisoRepository.existsByNombre("TICKET_LEER")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                permisoService.guardar(permiso));

        verify(permisoRepository, never()).save(any());
    }

    @Test
    void actualizar_debeActualizarYRetornarPermiso() {
        when(permisoRepository.findById(1L)).thenReturn(Optional.of(permiso));
        when(permisoRepository.save(any(Permiso.class))).thenReturn(permiso);

        Permiso nuevoPermiso = Permiso.builder()
                .nombre("TICKET_LEER") // mismo nombre
                .descripcion("Nueva descripción")
                .activo(true)
                .build();

        Permiso resultado = permisoService.actualizar(1L, nuevoPermiso);

        assertNotNull(resultado);
        verify(permisoRepository).save(any(Permiso.class));
    }

    @Test
    void actualizar_noExiste_lanzaExcepcion() {
        when(permisoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                permisoService.actualizar(99L, permiso));
    }

    @Test
    void actualizar_nombreDuplicado_lanzaExcepcion() {
        when(permisoRepository.findById(1L)).thenReturn(Optional.of(permiso));
        when(permisoRepository.existsByNombre("TICKET_CREAR")).thenReturn(true);

        Permiso nuevoPermiso = Permiso.builder()
                .nombre("TICKET_CREAR") // diferente y duplicado
                .descripcion("desc")
                .activo(true)
                .build();

        assertThrows(DuplicateResourceException.class, () ->
                permisoService.actualizar(1L, nuevoPermiso));
    }

    @Test
    void actualizar_nombreDiferenteNoDuplicado_debeActualizar() {
        when(permisoRepository.findById(1L)).thenReturn(Optional.of(permiso));
        when(permisoRepository.existsByNombre("TICKET_CREAR")).thenReturn(false);
        when(permisoRepository.save(any(Permiso.class))).thenReturn(permiso);

        Permiso nuevoPermiso = Permiso.builder()
                .nombre("TICKET_CREAR") // diferente pero no duplicado
                .descripcion("desc")
                .activo(true)
                .build();

        Permiso resultado = permisoService.actualizar(1L, nuevoPermiso);

        assertNotNull(resultado);
        verify(permisoRepository).existsByNombre("TICKET_CREAR");
        verify(permisoRepository).save(any(Permiso.class));
    }

    @Test
    void eliminar_debeDesactivarPermiso() {
        when(permisoRepository.findById(1L)).thenReturn(Optional.of(permiso));

        permisoService.eliminar(1L);

        assertFalse(permiso.isActivo());
        verify(permisoRepository).save(permiso);
    }

    @Test
    void eliminar_noExiste_lanzaExcepcion() {
        when(permisoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                permisoService.eliminar(99L));
    }
}