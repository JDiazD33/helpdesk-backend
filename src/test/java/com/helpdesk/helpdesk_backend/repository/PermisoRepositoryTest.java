package com.helpdesk.helpdesk_backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.helpdesk.helpdesk_backend.model.Permiso;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

@DataJpaTest
public class PermisoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PermisoRepository permisoRepository;

    private Permiso activo1;
    private Permiso activo2;

    @BeforeEach
    void setUp() {
        activo1 = Permiso.builder().nombre("CREAR_TICKET").descripcion("Permite crear tickets").activo(true).build();
        activo2 = Permiso.builder().nombre("EDITAR_TICKET").descripcion("Permite editar tickets").activo(true).build();
        Permiso inactivo = Permiso.builder().nombre("BORRAR_TICKET").descripcion("Permite borrar tickets").activo(false).build();

        entityManager.persist(activo1);
        entityManager.persist(activo2);
        entityManager.persist(inactivo);
        entityManager.flush();
    }

    @Test
    void findAllActivosOrdenados_DeberiaRetornarSoloActivosOrdenados() {
        List<Permiso> resultado = permisoRepository.findAllActivosOrdenados();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNombre()).isEqualTo("CREAR_TICKET");
    }

    @Test
    void buscarPorTexto_DeberiaEncontrarPorNombre() {
        List<Permiso> resultado = permisoRepository.buscarPorTexto("EDITAR_TICKET");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("EDITAR_TICKET");
    }
}
