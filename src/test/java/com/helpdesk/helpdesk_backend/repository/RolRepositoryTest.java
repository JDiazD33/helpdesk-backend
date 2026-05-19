package com.helpdesk.helpdesk_backend.repository;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.helpdesk.helpdesk_backend.model.Rol;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class RolRepositoryTest {

    @Autowired
    private RolRepository rolRepository;

    private Rol rolAdmin;

    @BeforeEach
    void setUp(){
        // Inicializamos datos de prueba antes de cada test
        rolAdmin = Rol.builder().nombre("ADMIN").build();
        rolRepository.save(rolAdmin);
    }

    @Test
    void findByNombre_CuandoExiste_RetornaRol(){
        // Act
        Optional<Rol> resultado = rolRepository.findByNombre("ADMIN");
        
        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("ADMIN");
    }

    @Test
    void findByNombre_CuandoNoExiste_RetornaVacio(){
        // Act
        Optional<Rol> resultado = rolRepository.findByNombre("USER_NO_EXISTE");

        // Assert
        assertThat(resultado).isNotPresent();
    }
}
