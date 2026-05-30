package com.helpdesk.helpdesk_backend.config;

import com.helpdesk.helpdesk_backend.model.Permiso;
import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.repository.PermisoRepository;
import com.helpdesk.helpdesk_backend.repository.RolRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PermisoRepository permisoRepository;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        dataInitializer = new DataInitializer(rolRepository, permisoRepository);
    }

    @Test
    void run_creaPermisosYRoles_cuandoNoExisten() throws Exception {
        when(permisoRepository.existsByNombre(any())).thenReturn(false);
        when(rolRepository.existsByNombre(any())).thenReturn(false);

        Rol owner = Rol.builder().id(1L).nombre("ADMIN_OWNER").build();
        Rol admin = Rol.builder().id(2L).nombre("ADMIN_EMPRESA").build();
        Rol agente = Rol.builder().id(3L).nombre("AGENTE").build();
        Rol cliente = Rol.builder().id(4L).nombre("CLIENTE").build();

        when(rolRepository.findByNombre("ADMIN_OWNER")).thenReturn(Optional.of(owner));
        when(rolRepository.findByNombre("ADMIN_EMPRESA")).thenReturn(Optional.of(admin));
        when(rolRepository.findByNombre("AGENTE")).thenReturn(Optional.of(agente));
        when(rolRepository.findByNombre("CLIENTE")).thenReturn(Optional.of(cliente));

        Permiso p1 = Permiso.builder().nombre("TICKET_LEER").build();
        Permiso p2 = Permiso.builder().nombre("TICKET_CREAR").build();
        Permiso p3 = Permiso.builder().nombre("TICKET_EDITAR").build();
        Permiso p4 = Permiso.builder().nombre("USUARIO_LEER").build();
        Permiso p5 = Permiso.builder().nombre("REPORTE_VER").build();

        when(permisoRepository.findAll()).thenReturn(List.of(p1, p2, p3, p4, p5));
        when(permisoRepository.findByNombre("TICKET_LEER")).thenReturn(Optional.of(p1));
        when(permisoRepository.findByNombre("TICKET_CREAR")).thenReturn(Optional.of(p2));
        when(permisoRepository.findByNombre("TICKET_EDITAR")).thenReturn(Optional.of(p3));
        when(permisoRepository.findByNombre("USUARIO_LEER")).thenReturn(Optional.of(p4));
        when(permisoRepository.findByNombre("REPORTE_VER")).thenReturn(Optional.of(p5));

        dataInitializer.run();

        verify(permisoRepository, times(12)).save(any(Permiso.class));
        verify(rolRepository, times(8)).save(any(Rol.class)); // 4 crear + 4 asignar permisos
    }

    @Test
    void run_noCreaDuplicados_cuandoYaExisten() throws Exception {
        when(permisoRepository.existsByNombre(any())).thenReturn(true);
        when(rolRepository.existsByNombre(any())).thenReturn(true);

        Rol owner = Rol.builder().id(1L).nombre("ADMIN_OWNER").build();
        Rol admin = Rol.builder().id(2L).nombre("ADMIN_EMPRESA").build();
        Rol agente = Rol.builder().id(3L).nombre("AGENTE").build();
        Rol cliente = Rol.builder().id(4L).nombre("CLIENTE").build();

        when(rolRepository.findByNombre("ADMIN_OWNER")).thenReturn(Optional.of(owner));
        when(rolRepository.findByNombre("ADMIN_EMPRESA")).thenReturn(Optional.of(admin));
        when(rolRepository.findByNombre("AGENTE")).thenReturn(Optional.of(agente));
        when(rolRepository.findByNombre("CLIENTE")).thenReturn(Optional.of(cliente));

        Permiso p1 = Permiso.builder().nombre("TICKET_LEER").build();
        Permiso p2 = Permiso.builder().nombre("TICKET_CREAR").build();
        Permiso p3 = Permiso.builder().nombre("TICKET_EDITAR").build();
        Permiso p4 = Permiso.builder().nombre("USUARIO_LEER").build();
        Permiso p5 = Permiso.builder().nombre("REPORTE_VER").build();

        when(permisoRepository.findAll()).thenReturn(List.of(p1, p2, p3, p4, p5));
        when(permisoRepository.findByNombre("TICKET_LEER")).thenReturn(Optional.of(p1));
        when(permisoRepository.findByNombre("TICKET_CREAR")).thenReturn(Optional.of(p2));
        when(permisoRepository.findByNombre("TICKET_EDITAR")).thenReturn(Optional.of(p3));
        when(permisoRepository.findByNombre("USUARIO_LEER")).thenReturn(Optional.of(p4));
        when(permisoRepository.findByNombre("REPORTE_VER")).thenReturn(Optional.of(p5));

        dataInitializer.run();

        verify(permisoRepository, never()).save(any(Permiso.class));
        verify(rolRepository, times(4)).save(any(Rol.class)); // solo asignar permisos a 4 roles
    }

    @Test
    void run_noAsignaPermisos_cuandoFaltaUnRol() throws Exception {
        when(permisoRepository.existsByNombre(any())).thenReturn(true);
        when(rolRepository.existsByNombre(any())).thenReturn(true);

        when(rolRepository.findByNombre("ADMIN_OWNER"))
                .thenReturn(Optional.of(Rol.builder().nombre("ADMIN_OWNER").build()));
        when(rolRepository.findByNombre("ADMIN_EMPRESA"))
                .thenReturn(Optional.of(Rol.builder().nombre("ADMIN_EMPRESA").build()));
        when(rolRepository.findByNombre("AGENTE"))
                .thenReturn(Optional.empty()); // este hace que sea null
        when(rolRepository.findByNombre("CLIENTE"))
                .thenReturn(Optional.of(Rol.builder().nombre("CLIENTE").build()));

        dataInitializer.run();

        verify(rolRepository, never()).save(any(Rol.class));
    }

    @Test
    void run_noAsignaPermisos_cuandoOwnerEsNull() throws Exception {
        when(permisoRepository.existsByNombre(any())).thenReturn(true);
        when(rolRepository.existsByNombre(any())).thenReturn(true);

        when(rolRepository.findByNombre("ADMIN_OWNER"))
                .thenReturn(Optional.empty()); // owner es null, primera condicion del if
        when(rolRepository.findByNombre("ADMIN_EMPRESA"))
                .thenReturn(Optional.of(Rol.builder().nombre("ADMIN_EMPRESA").build()));
        when(rolRepository.findByNombre("AGENTE"))
                .thenReturn(Optional.of(Rol.builder().nombre("AGENTE").build()));
        when(rolRepository.findByNombre("CLIENTE"))
                .thenReturn(Optional.of(Rol.builder().nombre("CLIENTE").build()));

        dataInitializer.run();

        verify(rolRepository, never()).save(any(Rol.class));
    }
}