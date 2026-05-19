package com.helpdesk.helpdesk_backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.helpdesk.helpdesk_backend.model.Empresa;

@DataJpaTest
public class EmpresaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmpresaRepository empresaRepository;

    private Empresa empresaActiva;
    private Empresa empresaInactiva;

    @BeforeEach
    void setUp() {
        // 1. Persistir Empresa Activa
        empresaActiva = Empresa.builder()
                .nombre("Empresa Alpha")
                .ruc("10000000001")
                .correoContacto("alpha@empresa.com")
                .telefonoContacto("111111111")
                .activo(true)
                .build();
        entityManager.persist(empresaActiva);

        // 2. Persistir Empresa Inactiva para validar que el borrado lógico sea respetado
        empresaInactiva = Empresa.builder()
                .nombre("Empresa Beta")
                .ruc("20000000002")
                .correoContacto("beta@empresa.com")
                .telefonoContacto("222222222")
                .activo(false) // Borrado lógico aplicado
                .build();
        entityManager.persist(empresaInactiva);

        entityManager.flush();
    }

    @Test
    void findByRucAndActivoTrue_CuandoExisteYEstaActiva_RetornaEmpresa() {
        // Act
        Optional<Empresa> resultado = empresaRepository.findByRucAndActivoTrue("10000000001");
    
        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Empresa Alpha");
    }

    @Test
    void findByRucAndActivoTrue_CuandoExistePeroEstaInactiva_RetornaVacio() {
        // Act - Buscamos la empresa Beta que tiene activo = false
        Optional<Empresa> resultado = empresaRepository.findByRucAndActivoTrue("20000000002");

        // Assert - No debe retornarla porque exigimos que esté activa
        assertThat(resultado).isNotPresent();
    }

    @Test
    void findByCorreoContactoAndActivoTrue_CuandoExisteYEstaActiva_RetornaEmpresa() {
        // Act
        Optional<Empresa> resultado = empresaRepository.findByCorreoContactoAndActivoTrue("alpha@empresa.com");
    
        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getRuc()).isEqualTo("10000000001");
    }

    @Test
    void findByIdAndActivoTrue_CuandoExisteYEstaActiva_RetornaEmpresa() {
        // Act
        Optional<Empresa> resultado = empresaRepository.findByIdAndActivoTrue(empresaActiva.getId());

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Empresa Alpha");
    }

    @Test
    void findByRuc_SinImportarEstado_RetornaEmpresa() {
        // Act - Usamos el método que no filtra por estado para buscar la empresa inactiva
        Optional<Empresa> resultado = empresaRepository.findByRuc("20000000002");

        // Assert - Aquí sí debe retornar la empresa Beta, aunque esté inactiva
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Empresa Beta");
        assertThat(resultado.get().isActivo()).isFalse();
    }
}
