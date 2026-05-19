package com.helpdesk.helpdesk_backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.helpdesk.helpdesk_backend.model.CategoriaTicket;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.model.ProblemaTicket;

@DataJpaTest
public class ProblemaTicketRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProblemaTicketRepository problemaRepository;

    private CategoriaTicket categoria1;
    private CategoriaTicket categoria2;

    @BeforeEach
    void setUp() {
        // 1. Persistimos la Empresa obligatoria
        Empresa empresa = Empresa.builder()
                .nombre("Tech Solutions")
                .ruc("10203040501")
                .correoContacto("contacto@tech.com")
                .telefonoContacto("123456789")
                .activo(true)
                .build();
        entityManager.persist(empresa);

        // 2. Persistimos las Categorías
        categoria1 = CategoriaTicket.builder()
                .nombre("Hardware")
                .empresa(empresa)
                .activa(true)
                .build();
        entityManager.persist(categoria1);

        categoria2 = CategoriaTicket.builder()
                .nombre("Software")
                .empresa(empresa)
                .activa(true)
                .build();
        entityManager.persist(categoria2);

        // 3. Persistimos los Problemas para Categoria 1
        ProblemaTicket probActivoCat1 = ProblemaTicket.builder()
                .nombre("Pantalla Rota")
                .categoria(categoria1)
                .activo(true) // Activo
                .build();
        entityManager.persist(probActivoCat1);

        ProblemaTicket probInactivoCat1 = ProblemaTicket.builder()
                .nombre("Teclado atascado")
                .categoria(categoria1)
                .activo(false) // Inactivo
                .build();
        entityManager.persist(probInactivoCat1);

        // 4. Persistimos Problema para Categoria 2
        ProblemaTicket probActivoCat2 = ProblemaTicket.builder()
                .nombre("Error de Sistema Operativo")
                .categoria(categoria2)
                .activo(true)
                .build();
        entityManager.persist(probActivoCat2);

        entityManager.flush();
    }

    @Test
    void findAllByCategoriaIdAndActivoTrue_DebeRetornarSoloProblemasActivosDeLaCategoria() {
        List<ProblemaTicket> resultado = problemaRepository.findAllByCategoriaIdAndActivoTrue(categoria1.getId());
        
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Pantalla Rota");
        assertThat(resultado.get(0).isActivo()).isTrue();
    }

    @Test
    void existsByNombreAndCategoriaId_DebeValidarCorrectamenteLaExistenciaEnLaMismaCategoria() {
        boolean existeEnCat1 = problemaRepository.existsByNombreAndCategoriaId("Pantalla Rota", categoria1.getId());
        boolean existeEnCat2 = problemaRepository.existsByNombreAndCategoriaId("Pantalla Rota", categoria2.getId());
        
        assertThat(existeEnCat1).isTrue();
        // Garantiza que el mismo nombre podría repetirse si es una categoría diferente
        assertThat(existeEnCat2).isFalse(); 
    }
}
