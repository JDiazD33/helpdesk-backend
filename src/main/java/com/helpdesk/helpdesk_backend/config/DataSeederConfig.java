package com.helpdesk.helpdesk_backend.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.repository.RolRepository;

@Configuration
public class DataSeederConfig {

    @Bean
    public CommandLineRunner initRoles(RolRepository rolRepository){
        return args -> {
            // Verificamos si ya existen roles en la base de datos para evitar duplicados
            if (rolRepository.count() == 0) {
                Rol admin = new Rol();
                admin.setNombre("ADMIN_EMPRESA");

                Rol agente = new Rol();
                agente.setNombre("AGENTE");

                Rol cliente = new Rol();
                cliente.setNombre("CLIENTE");

                // Guardamos los roles en la base de datos
                rolRepository.saveAll(List.of(admin, agente, cliente));

                System.out.println("DEFAULT SEEDER: Roles inicializados correctamente en la base de datos.");
            } else {
                System.out.println("DEFAULT SEEDER: Roles ya existen en la base de datos, no se han creado duplicados.");
            }
        };
    }
}
