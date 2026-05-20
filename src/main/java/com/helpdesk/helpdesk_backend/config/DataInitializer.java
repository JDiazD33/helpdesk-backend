package com.helpdesk.helpdesk_backend.config;

import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.helpdesk.helpdesk_backend.model.Permiso;
import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.repository.PermisoRepository;
import com.helpdesk.helpdesk_backend.repository.RolRepository;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    public DataInitializer(RolRepository rolRepository, PermisoRepository permisoRepository) {
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
    }

    @Override
    public void run(String... args) {
        crearPermisosPorDefecto();
        crearRolesPorDefecto();
        asignarPermisosARoles();
    }

    private void crearPermisosPorDefecto() {
        List<String> nombres = List.of(
                "USUARIO_LEER", "USUARIO_CREAR", "USUARIO_EDITAR", "USUARIO_ELIMINAR",
                "TICKET_LEER", "TICKET_CREAR", "TICKET_EDITAR", "TICKET_ELIMINAR",
                "ROL_GESTIONAR", "PERMISO_GESTIONAR", "EMPRESA_GESTIONAR", "REPORTE_VER"
        );
        for (String nombre : nombres) {
            if (!permisoRepository.existsByNombre(nombre)) {
                permisoRepository.save(Permiso.builder()
                        .nombre(nombre)
                        .descripcion("Permiso " + nombre)
                        .activo(true)
                        .build());
            }
        }
    }

    private void crearRolesPorDefecto() {
        crearRolSiNoExiste("ADMIN_EMPRESA");
        crearRolSiNoExiste("AGENTE");
        crearRolSiNoExiste("CLIENTE");
    }

    private void crearRolSiNoExiste(String nombre) {
        if (!rolRepository.existsByNombre(nombre)) {
            rolRepository.save(Rol.builder().nombre(nombre).activo(true).build());
        }
    }

    private void asignarPermisosARoles() {
        Rol admin = rolRepository.findByNombre("ADMIN_EMPRESA").orElse(null);
        Rol agente = rolRepository.findByNombre("AGENTE").orElse(null);
        Rol cliente = rolRepository.findByNombre("CLIENTE").orElse(null);
        if (admin == null || agente == null || cliente == null) {
            return;
        }

        admin.setPermisos(Set.copyOf(permisoRepository.findAll()));
        agente.setPermisos(Set.of(
                permiso("TICKET_LEER"), permiso("TICKET_EDITAR"),
                permiso("USUARIO_LEER"), permiso("REPORTE_VER")
        ));
        cliente.setPermisos(Set.of(
                permiso("TICKET_LEER"), permiso("TICKET_CREAR")
        ));
        rolRepository.save(admin);
        rolRepository.save(agente);
        rolRepository.save(cliente);
    }

    private Permiso permiso(String nombre) {
        return permisoRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("Permiso no encontrado: " + nombre));
    }
}
