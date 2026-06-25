package com.helpdesk.helpdesk_backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.model.Ticket;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;
import com.helpdesk.helpdesk_backend.model.enums.PrioridadTicket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
public class TicketRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TicketRepository ticketRepository;

    private Empresa empresa;
    private Usuario cliente;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .nombre("Empresa Alpha")
                .ruc("20123456789")
                .correoContacto("contacto@alpha.com")
                .telefonoContacto("987654321")
                .activo(true)
                .build();
        Rol rol = Rol.builder().nombre("CLIENTE").build();
        entityManager.persist(rol);
        cliente = Usuario.builder()
                .nombres("Juan")
                .apellidos("Perez")
                .email("juan@alpha.com")
                .password("123456")
                .empresa(empresa)
                .rol(rol)
                .activo(true)
                .build();

        entityManager.persist(empresa);
        entityManager.persist(cliente);

        Ticket t1 = Ticket.builder()
                .codigo("TCK-1001")
                .titulo("Falla de red")
                .descripcion("No hay conexión a internet")
                .empresa(empresa)
                .cliente(cliente)
                .estado(EstadoTicket.ABIERTO)
                .prioridad(PrioridadTicket.MEDIA)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Ticket t2 = Ticket.builder()
                .codigo("TCK-1002")
                .titulo("Falla de computadora")
                .descripcion("La computadora no enciende")
                .empresa(empresa)
                .cliente(cliente)
                .estado(EstadoTicket.ABIERTO)
                .prioridad(PrioridadTicket.ALTA)
                .fechaCreacion(LocalDateTime.now())
                .build();

        entityManager.persist(t1);
        entityManager.persist(t2);
        entityManager.flush();
    }

    @Test
    void findByEmpresaId_DeberiaRetornarTicketsDeEmpresa() {
        List<Ticket> resultado = ticketRepository.findByEmpresaId(empresa.getId());

        assertThat(resultado).hasSize(2);
    }

    @Test
    void existsByCodigo_DeberiaDetectarCodigoExistente() {
        boolean existe = ticketRepository.existsByCodigo("TCK-1001");
        assertThat(existe).isTrue();
    }
}
