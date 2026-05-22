package com.helpdesk.helpdesk_backend.config;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.helpdesk.helpdesk_backend.model.Permiso;
import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.repository.PermisoRepository;
import com.helpdesk.helpdesk_backend.repository.RolRepository;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PermisoRepository permisoRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private Permiso buildPermiso(String nombre) {
        return Permiso.builder().nombre(nombre).descripcion("Permiso " + nombre).activo(true).build();
    }

    private Rol buildRol(String nombre) {
        return Rol.builder().nombre(nombre).activo(true).build();
    }

    /**
     * Stubs compartidos marcados como lenient: no causan UnnecessaryStubbingException
     * cuando algún test no llega a invocar asignarPermisosARoles().
     */
    @BeforeEach
    void stubRolesYPermisos() {
        List<String> todos = List.of(
                "USUARIO_LEER", "USUARIO_CREAR", "USUARIO_EDITAR", "USUARIO_ELIMINAR",
                "TICKET_LEER", "TICKET_CREAR", "TICKET_EDITAR", "TICKET_ELIMINAR",
                "ROL_GESTIONAR", "PERMISO_GESTIONAR", "EMPRESA_GESTIONAR", "REPORTE_VER"
        );

        // lenient() evita UnnecessaryStubbingException en tests que no llegan
        // a asignarPermisosARoles() y por tanto nunca usan estos stubs.
        lenient().when(rolRepository.findByNombre("ADMIN_EMPRESA"))
                .thenReturn(Optional.of(buildRol("ADMIN_EMPRESA")));
        lenient().when(rolRepository.findByNombre("AGENTE"))
                .thenReturn(Optional.of(buildRol("AGENTE")));
        lenient().when(rolRepository.findByNombre("CLIENTE"))
                .thenReturn(Optional.of(buildRol("CLIENTE")));

        todos.forEach(n ->
                lenient().when(permisoRepository.findByNombre(n))
                        .thenReturn(Optional.of(buildPermiso(n))));

        lenient().when(permisoRepository.findAll())
                .thenReturn(todos.stream().map(this::buildPermiso).toList());
    }

    // ---------------------------------------------------------------
    // crearPermisosPorDefecto
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Crea los 12 permisos cuando ninguno existe")
    void run_creaLos12Permisos_cuandoNingunoExiste() throws Exception {
        when(permisoRepository.existsByNombre(any())).thenReturn(false);
        when(rolRepository.existsByNombre(any())).thenReturn(true);

        dataInitializer.run();

        verify(permisoRepository, times(12)).save(any(Permiso.class));
    }

    @Test
    @DisplayName("No crea permisos que ya existen")
    void run_noCreaDuplicados_cuandoPermisoYaExiste() throws Exception {
        when(permisoRepository.existsByNombre(any())).thenReturn(true);
        when(rolRepository.existsByNombre(any())).thenReturn(true);

        dataInitializer.run();

        verify(permisoRepository, never()).save(any(Permiso.class));
    }

    @Test
    @DisplayName("Los permisos se guardan con activo=true y descripcion correcta")
    void run_permisoGuardadoConAtributosCorrectos() throws Exception {
        when(permisoRepository.existsByNombre("TICKET_LEER")).thenReturn(false);
        when(permisoRepository.existsByNombre(argThat(n -> !n.equals("TICKET_LEER")))).thenReturn(true);
        when(rolRepository.existsByNombre(any())).thenReturn(true);

        dataInitializer.run();

        ArgumentCaptor<Permiso> captor = ArgumentCaptor.forClass(Permiso.class);
        verify(permisoRepository).save(captor.capture());
        Permiso guardado = captor.getValue();

        assertThat(guardado.getNombre()).isEqualTo("TICKET_LEER");
        assertThat(guardado.getDescripcion()).isEqualTo("Permiso TICKET_LEER");
        assertThat(guardado.isActivo()).isTrue();
    }

    // ---------------------------------------------------------------
    // crearRolesPorDefecto
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Crea los 3 roles cuando ninguno existe")
    void run_creaLos3Roles_cuandoNingunoExiste() throws Exception {
        when(permisoRepository.existsByNombre(any())).thenReturn(true);
        when(rolRepository.existsByNombre(any())).thenReturn(false);

        dataInitializer.run();

        verify(rolRepository, times(3)).save(argThat(Rol::isActivo));
    }

    @Test
    @DisplayName("No crea roles que ya existen")
    void run_noCreaDuplicados_cuandoRolYaExiste() throws Exception {
        when(permisoRepository.existsByNombre(any())).thenReturn(true);
        when(rolRepository.existsByNombre(any())).thenReturn(true);

        dataInitializer.run();

        verify(rolRepository, times(0)).save(argThat(r ->
                r.getNombre().equals("ADMIN_EMPRESA") ||
                r.getNombre().equals("AGENTE") ||
                r.getNombre().equals("CLIENTE")
        ));
    }

    @Test
    @DisplayName("Los roles se crean con activo=true")
    void run_rolesCreados_conActivoTrue() throws Exception {
        when(permisoRepository.existsByNombre(any())).thenReturn(true);
        when(rolRepository.existsByNombre("ADMIN_EMPRESA")).thenReturn(false);
        when(rolRepository.existsByNombre("AGENTE")).thenReturn(true);
        when(rolRepository.existsByNombre("CLIENTE")).thenReturn(true);

        dataInitializer.run();

        ArgumentCaptor<Rol> captor = ArgumentCaptor.forClass(Rol.class);
        verify(rolRepository, times(4)).save(captor.capture());
        Rol rolCreado = captor.getAllValues().stream()
                .filter(r -> r.getNombre().equals("ADMIN_EMPRESA") && r.getPermisos() == null)
                .findFirst().orElseThrow();
        assertThat(rolCreado.isActivo()).isTrue();
    }

    // ---------------------------------------------------------------
    // asignarPermisosARoles
    // ---------------------------------------------------------------

    @Test
    @DisplayName("Admin recibe todos los permisos disponibles")
    void run_adminRecibeTodasLosPermisos() throws Exception {
        when(permisoRepository.existsByNombre(any())).thenReturn(true);
        when(rolRepository.existsByNombre(any())).thenReturn(true);

        dataInitializer.run();

        ArgumentCaptor<Rol> captor = ArgumentCaptor.forClass(Rol.class);
        verify(rolRepository, times(3)).save(captor.capture());

        Rol admin = captor.getAllValues().stream()
                .filter(r -> r.getNombre().equals("ADMIN_EMPRESA"))
                .findFirst().orElseThrow();

        assertThat(admin.getPermisos()).hasSize(12);
    }

    @Test
    @DisplayName("Agente recibe exactamente 4 permisos correctos")
    void run_agenteRecibe4Permisos() throws Exception {
        when(permisoRepository.existsByNombre(any())).thenReturn(true);
        when(rolRepository.existsByNombre(any())).thenReturn(true);

        dataInitializer.run();

        ArgumentCaptor<Rol> captor = ArgumentCaptor.forClass(Rol.class);
        verify(rolRepository, times(3)).save(captor.capture());

        Rol agente = captor.getAllValues().stream()
                .filter(r -> r.getNombre().equals("AGENTE"))
                .findFirst().orElseThrow();

        Set<String> nombres = agente.getPermisos().stream()
                .map(Permiso::getNombre).collect(java.util.stream.Collectors.toSet());

        assertThat(nombres).containsExactlyInAnyOrder(
                "TICKET_LEER", "TICKET_EDITAR", "USUARIO_LEER", "REPORTE_VER"
        );
    }

    @Test
    @DisplayName("Cliente recibe exactamente 2 permisos correctos")
    void run_clienteRecibe2Permisos() throws Exception {
        when(permisoRepository.existsByNombre(any())).thenReturn(true);
        when(rolRepository.existsByNombre(any())).thenReturn(true);

        dataInitializer.run();

        ArgumentCaptor<Rol> captor = ArgumentCaptor.forClass(Rol.class);
        verify(rolRepository, times(3)).save(captor.capture());

        Rol cliente = captor.getAllValues().stream()
                .filter(r -> r.getNombre().equals("CLIENTE"))
                .findFirst().orElseThrow();

        Set<String> nombres = cliente.getPermisos().stream()
                .map(Permiso::getNombre).collect(java.util.stream.Collectors.toSet());

        assertThat(nombres).containsExactlyInAnyOrder("TICKET_LEER", "TICKET_CREAR");
    }

    @Test
    @DisplayName("No asigna permisos si algún rol no existe en la BD")
    void run_noAsignaPermisos_siRolFaltaEnBD() throws Exception {
        when(permisoRepository.existsByNombre(any())).thenReturn(true);
        when(rolRepository.existsByNombre(any())).thenReturn(true);
        when(rolRepository.findByNombre("AGENTE")).thenReturn(Optional.empty());

        dataInitializer.run();

        verify(rolRepository, never()).save(argThat(r -> r.getPermisos() != null));
    }

    @Test
    @DisplayName("Lanza IllegalStateException si un permiso requerido no existe en BD")
    void run_lanzaExcepcion_siPermisoRequeridoNoExiste() {
        when(permisoRepository.existsByNombre(any())).thenReturn(true);
        when(rolRepository.existsByNombre(any())).thenReturn(true);
        when(permisoRepository.findByNombre("TICKET_LEER")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> dataInitializer.run());
    }

    @Test
    @DisplayName("Admin recibe lista vacía si findAll no retorna permisos")
    void run_adminConListaVacia_siPermisosFindAllRetornaVacio() throws Exception {
        when(permisoRepository.existsByNombre(any())).thenReturn(true);
        when(rolRepository.existsByNombre(any())).thenReturn(true);
        when(permisoRepository.findAll()).thenReturn(Collections.emptyList());

        dataInitializer.run();

        ArgumentCaptor<Rol> captor = ArgumentCaptor.forClass(Rol.class);
        verify(rolRepository, times(3)).save(captor.capture());

        Rol admin = captor.getAllValues().stream()
                .filter(r -> r.getNombre().equals("ADMIN_EMPRESA"))
                .findFirst().orElseThrow();

        assertThat(admin.getPermisos()).isEmpty();
    }
}