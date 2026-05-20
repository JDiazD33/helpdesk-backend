package com.helpdesk.helpdesk_backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.helpdesk.helpdesk_backend.model.CategoriaTicket;
import com.helpdesk.helpdesk_backend.model.Empresa;

@DataJpaTest
public class CategoriaTicketRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoriaTicketRepository categoriaRepository;

    private Empresa empresa1;
    private Empresa empresa2;
    private CategoriaTicket categoriaActivaEmpresa1;

    @BeforeEach
    void setUp(){
        empresa1 = Empresa.builder()
                .nombre("Tech Solutions")
                .ruc("10203040501")
                .correoContacto("contacto@tech.com")
                .telefonoContacto("123456789")
                .activo(true)
                .build();
        entityManager.persist(empresa1);

        empresa2 = Empresa.builder()
                .nombre("Global Corp")
                .ruc("90807060501")
                .correoContacto("info@global.com")
                .telefonoContacto("987654321")
                .activo(true)
                .build();
        entityManager.persist(empresa2);

        categoriaActivaEmpresa1 = CategoriaTicket.builder()
                .nombre("Soporte Hardware")
                .descripcion("Problemas físicos")
                .empresa(empresa1)
                .activa(true)
                .build();
        entityManager.persist(categoriaActivaEmpresa1);

        CategoriaTicket categoriaInactivaEmpresa1 = CategoriaTicket.builder()
                .nombre("Soporte Software")
                .descripcion("Problemas lógicos")
                .empresa(empresa1)
                .activa(false)
                .build();
        entityManager.persist(categoriaInactivaEmpresa1);

        CategoriaTicket categoriaEmpresa2 = CategoriaTicket.builder()
                .nombre("Redes")
                .descripcion("Problemas de conectividad")
                .empresa(empresa2)
                .activa(true)
                .build();
        entityManager.persist(categoriaEmpresa2);

        entityManager.flush();
    }

    @Test
    void findAllByEmpresaIdAndActivaTrue_DebeRetornarSoloCategoriasActivasDeLaEmpresa() {
        List<CategoriaTicket> resultado = categoriaRepository.findByEmpresaIdAndActiva(empresa1.getId(), true);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Soporte Hardware");
        assertThat(resultado.get(0).isActiva()).isTrue();
    }

    @Test
    void findAllByEmpresaId_DebeRetornarTodasLasCategoriasDeLaEmpresa() {
        List<CategoriaTicket> resultado = categoriaRepository.findByEmpresaId(empresa1.getId());
        
        assertThat(resultado).hasSize(2);
    }

    @Test
    void existsByNombreAndEmpresaId_DebeRetornarTrueSiElNombreExisteEnLaEmpresa() {
        boolean existe = categoriaRepository.existsByNombreAndEmpresaId("Soporte Hardware", empresa1.getId());
        boolean noExiste = categoriaRepository.existsByNombreAndEmpresaId("Redes", empresa1.getId());
        
        assertThat(existe).isTrue();
        assertThat(noExiste).isFalse();
    }
}
