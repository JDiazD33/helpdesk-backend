package com.helpdesk.helpdesk_backend.service.impl;

import com.helpdesk.helpdesk_backend.exception.DuplicateResourceException;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.model.Permiso;
import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.repository.PermisoRepository;
import com.helpdesk.helpdesk_backend.repository.RolRepository;

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
class RolServiceImplTest {

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PermisoRepository permisoRepository;

    @InjectMocks
    private RolServiceImpl rolService;

    private Rol rol;
    private Permiso permiso;

    @BeforeEach
    void setUp() {
        rol = Rol.builder()
                .id(1L)
                .nombre("CLIENTE")
                .activo(true)
                .build();

        permiso = Permiso.builder()
                .id(1L)
                .nombre("TICKET_LEER")
                .activo(true)
                .build();
    }

    @Test
    void listarTodos_debeRetornarLista() {
        when(rolRepository.findAll()).thenReturn(List.of(rol));

        List<Rol> resultado = rolService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("CLIENTE", resultado.get(0).getNombre());
    }

    @Test
    void buscarPorId_debeRetornarRol() {
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));

        Optional<Rol> resultado = rolService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("CLIENTE", resultado.get().getNombre());
    }

    @Test
    void buscarPorId_noExiste_debeRetornarVacio() {
        when(rolRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Rol> resultado = rolService.buscarPorId(99L);

        assertFalse(resultado.isPresent());
    }

    @Test
    void guardar_debeGuardarYRetornarRol() {
        when(rolRepository.existsByNombre("CLIENTE")).thenReturn(false);
        when(rolRepository.save(any(Rol.class))).thenReturn(rol);

        Rol resultado = rolService.guardar(rol);

        assertEquals("CLIENTE", resultado.getNombre());
        verify(rolRepository).save(rol);
    }

    @Test
    void guardar_nombreDuplicado_lanzaExcepcion() {
        when(rolRepository.existsByNombre("CLIENTE")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                rolService.guardar(rol));

        verify(rolRepository, never()).save(any());
    }

    @Test
    void actualizar_debeActualizarYRetornarRol() {
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(rolRepository.save(any(Rol.class))).thenReturn(rol);

        Rol mismoNombre = Rol.builder().nombre("CLIENTE").activo(true).build();
        Rol resultado = rolService.actualizar(1L, mismoNombre);

        assertNotNull(resultado);
        verify(rolRepository).save(any(Rol.class));
    }

    @Test
    void actualizar_noExiste_lanzaExcepcion() {
        when(rolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                rolService.actualizar(99L, rol));
    }

    @Test
    void actualizar_nombreDuplicado_lanzaExcepcion() {
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(rolRepository.existsByNombre("ADMIN_EMPRESA")).thenReturn(true);

        Rol nuevoRol = Rol.builder().nombre("ADMIN_EMPRESA").build();

        assertThrows(DuplicateResourceException.class, () ->
                rolService.actualizar(1L, nuevoRol));
    }

    @Test
    void actualizar_nombreDiferenteNoDuplicado_debeActualizar() {
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));
        when(rolRepository.existsByNombre("AGENTE")).thenReturn(false);
        when(rolRepository.save(any(Rol.class))).thenReturn(rol);

        Rol nuevoRol = Rol.builder().nombre("AGENTE").activo(true).build();

        Rol resultado = rolService.actualizar(1L, nuevoRol);

        assertNotNull(resultado);
        verify(rolRepository).existsByNombre("AGENTE");
    }

    @Test
    void eliminar_debeDesactivarRol() {
        when(rolRepository.findById(1L)).thenReturn(Optional.of(rol));

        rolService.eliminar(1L);

        assertFalse(rol.isActivo());
        verify(rolRepository).save(rol);
    }

    @Test
    void eliminar_noExiste_lanzaExcepcion() {
        when(rolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                rolService.eliminar(99L));
    }

    @Test
    void buscarPorNombre_debeRetornarRol() {
        when(rolRepository.findByNombre("CLIENTE")).thenReturn(Optional.of(rol));

        Optional<Rol> resultado = rolService.buscarPorNombre("CLIENTE");

        assertTrue(resultado.isPresent());
        assertEquals("CLIENTE", resultado.get().getNombre());
    }

    @Test
    void existePorNombre_debeRetornarTrue() {
        when(rolRepository.existsByNombre("CLIENTE")).thenReturn(true);

        assertTrue(rolService.existePorNombre("CLIENTE"));
    }

    @Test
    void existePorNombre_debeRetornarFalse() {
        when(rolRepository.existsByNombre("INEXISTENTE")).thenReturn(false);

        assertFalse(rolService.existePorNombre("INEXISTENTE"));
    }

    @Test
    void asignarPermisos_debeAsignarYRetornarRol() {
        when(rolRepository.findByIdWithPermisos(1L)).thenReturn(Optional.of(rol));
        when(permisoRepository.findById(1L)).thenReturn(Optional.of(permiso));
        when(rolRepository.save(any(Rol.class))).thenReturn(rol);

        Rol resultado = rolService.asignarPermisos(1L, List.of(1L));

        assertNotNull(resultado);
        verify(rolRepository).save(rol);
    }

    @Test
    void asignarPermisos_rolNoExiste_lanzaExcepcion() {
        when(rolRepository.findByIdWithPermisos(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                rolService.asignarPermisos(99L, List.of(1L)));
    }

    @Test
    void asignarPermisos_permisoNoExiste_lanzaExcepcion() {
        when(rolRepository.findByIdWithPermisos(1L)).thenReturn(Optional.of(rol));
        when(permisoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                rolService.asignarPermisos(1L, List.of(99L)));
    }

    @Test
    void buscarPorIdConPermisos_debeRetornarRol() {
        when(rolRepository.findByIdWithPermisos(1L)).thenReturn(Optional.of(rol));

        Optional<Rol> resultado = rolService.buscarPorIdConPermisos(1L);

        assertTrue(resultado.isPresent());
        assertEquals("CLIENTE", resultado.get().getNombre());
    }

    @Test
    void buscarPorIdConPermisos_noExiste_debeRetornarVacio() {
        when(rolRepository.findByIdWithPermisos(99L)).thenReturn(Optional.empty());

        Optional<Rol> resultado = rolService.buscarPorIdConPermisos(99L);

        assertFalse(resultado.isPresent());
    }
}