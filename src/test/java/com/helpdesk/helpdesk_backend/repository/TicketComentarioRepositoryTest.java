package com.helpdesk.helpdesk_backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.model.Ticket;
import com.helpdesk.helpdesk_backend.model.TicketComentario;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
public class TicketComentarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TicketComentarioRepository comentarioRepository;

    private Ticket ticket;
    private Usuario usuario;
    private Empresa empresa;

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
        usuario = Usuario.builder()
                .nombres("Juan")
                .apellidos("Perez")
                .email("juan.perez@alpha.com")
                .password("123456")
                .empresa(empresa)
                .rol(rol)
                .activo(true)
                .build();

        entityManager.persist(empresa);
        entityManager.persist(usuario);

        ticket = Ticket.builder().codigo("TCK-1001").titulo("Falla de red").descripcion("No hay conexión a internet").empresa(empresa).cliente(usuario).estado(EstadoTicket.ABIERTO).build();
        entityManager.persist(ticket);

        TicketComentario c1 = TicketComentario.builder().mensaje("Hola, necesito ayuda").ticket(ticket).usuario(usuario).fechaEnvio(LocalDateTime.now().minusDays(1)).build();
        TicketComentario c2 = TicketComentario.builder().mensaje("Hola, necesito ayuda").ticket(ticket).usuario(usuario).fechaEnvio(LocalDateTime.now()).build();

        entityManager.persist(c1);
        entityManager.persist(c2);
        entityManager.flush();
    }

    @Test
    void findAllByTicketIdOrderByFechaEnvioAsc_DeberiaRetornarOrdenados() {
        List<TicketComentario> resultado = comentarioRepository.findAllByTicketIdOrderByFechaEnvioAsc(ticket.getId());

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getMensaje()).isEqualTo("Hola, necesito ayuda");
    }

    @Test
    void countByTicketId_DeberiaContarComentarios() {
        long cuenta = comentarioRepository.countByTicketId(ticket.getId());
        assertThat(cuenta).isEqualTo(2L);
    }
}
