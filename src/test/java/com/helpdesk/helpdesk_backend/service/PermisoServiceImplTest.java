package com.helpdesk.helpdesk_backend.service;

import com.helpdesk.helpdesk_backend.exception.DuplicateResourceException;
import com.helpdesk.helpdesk_backend.model.Permiso;
import com.helpdesk.helpdesk_backend.repository.PermisoRepository;
import com.helpdesk.helpdesk_backend.service.impl.PermisoServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        permiso = Permiso.builder().id(1L).nombre("CREAR_TICKET").descripcion("Permite crear tickets").activo(true).build();
    }

    @Test
    void listarTodos_DeberiaRetornarLista() {
        when(permisoRepository.findAll()).thenReturn(List.of(permiso));

        List<Permiso> resultado = permisoService.listarTodos();

        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(1);
        verify(permisoRepository).findAll();
    }

    @Test
    void guardar_DeberiaGuardarCuandoNoExiste() {
        when(permisoRepository.existsByNombre(permiso.getNombre())).thenReturn(false);
        when(permisoRepository.save(permiso)).thenReturn(permiso);

        Permiso resultado = permisoService.guardar(permiso);

        assertThat(resultado.getNombre()).isEqualTo("CREAR_TICKET");
        verify(permisoRepository).save(permiso);
    }

    @Test
    void guardar_DeberiaLanzarSiNombreDuplicado() {
        when(permisoRepository.existsByNombre(permiso.getNombre())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> permisoService.guardar(permiso));
        verify(permisoRepository, never()).save(any());
    }

    @Test
    void eliminar_DeberiaMarcarComoInactivo() {
        when(permisoRepository.findById(1L)).thenReturn(Optional.of(permiso));

        permisoService.eliminar(1L);

        assertThat(permiso.isActivo()).isFalse();
        verify(permisoRepository).save(permiso);
    }
}
