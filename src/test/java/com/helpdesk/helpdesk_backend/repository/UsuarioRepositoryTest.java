package com.helpdesk.helpdesk_backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.model.Usuario;

@DataJpaTest
public class UsuarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Empresa empresaContexto;
    private Empresa otraEmpresa;
    private Rol rolAdmin;
    private Rol rolUser;
    private Usuario usuarioActivo;

    @BeforeEach
    void setUp(){
        // 1. Persistir Empresas (Contexto principal y una ajena para validar aislamiento)
        empresaContexto = Empresa.builder()
                .nombre("Empresa Alpha")
                .ruc("10000000001")
                .correoContacto("alpha@empresa.com")
                .telefonoContacto("111111111")
                .activo(true)
                .build();
        entityManager.persist(empresaContexto);

        otraEmpresa = Empresa.builder()
                .nombre("Empresa Beta")
                .ruc("20000000002")
                .correoContacto("beta@empresa.com")
                .telefonoContacto("222222222")
                .activo(true)
                .build();
        entityManager.persist(otraEmpresa);

        // 2. Persistir Roles
        rolAdmin = Rol.builder().nombre("ADMIN").build();
        entityManager.persist(rolAdmin);

        rolUser = Rol.builder().nombre("USER").build();
        entityManager.persist(rolUser);

        // 3. Persistir Usuario de prueba en la Empresa Alpha
        usuarioActivo = Usuario.builder()
                .nombres("Juan")
                .apellidos("Pérez")
                .email("juan.perez@alpha.com")
                .password("hash123")
                .empresa(empresaContexto)
                .rol(rolAdmin)
                .activo(true)
                .build();
        entityManager.persist(usuarioActivo);

        entityManager.flush();
    }

    @Test
    void findByIdAndEmpresaId_CuandoUsuarioPerteneceAEmpresa_RetornaUsuario() {
        // Act
        Optional<Usuario> resultado = usuarioRepository.findByIdAndEmpresaId(usuarioActivo.getId(), empresaContexto.getId());
        
        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEmail()).isEqualTo("juan.perez@alpha.com");
    }

    @Test
    void findByIdAndEmpresaId_CuandoUsuarioNoPerteneceAEmpresa_RetornaVacio() {
        // Act - Intentamos buscar al usuario de Alpha usando el ID de la Empresa Beta (Simulando un ataque IDOR)
        Optional<Usuario> resultado = usuarioRepository.findByIdAndEmpresaId(usuarioActivo.getId(), otraEmpresa.getId());

        // Assert
        assertThat(resultado).isNotPresent();
    }

    @Test
    void findAllByEmpresaIdAndActivoTrue_RetornaListaDeUsuariosDeLaEmpresa() {
        // Arrange - Agregamos un usuario inactivo para validar el borrado lógico
        Usuario usuarioInactivo = Usuario.builder()
                .nombres("Ana")
                .apellidos("Gómez")
                .email("ana.gomez@alpha.com")
                .password("hash123")
                .empresa(empresaContexto)
                .rol(rolUser)
                .activo(false) // Inactivo
                .build();
        entityManager.persist(usuarioInactivo);
        entityManager.flush();

        // Act
        List<Usuario> resultado = usuarioRepository.findAllByEmpresaIdAndActivoTrue(empresaContexto.getId());

        // Assert
        assertThat(resultado).hasSize(1); // Solo debe traer a Juan (activo), ignorando a Ana (inactiva)
        assertThat(resultado.get(0).getNombres()).isEqualTo("Juan");
    }

    @Test
    void findAllByEmpresaIdAndRolNombreAndActivoTrue_RetornaSoloAgentes() {
        // Arrange - Agregamos un usuario normal (USER) a la misma empresa
        Usuario usuarioNormal = Usuario.builder()
                .nombres("Carlos")
                .apellidos("López")
                .email("carlos@alpha.com")
                .password("hash123")
                .empresa(empresaContexto)
                .rol(rolUser)
                .activo(true)
                .build();
        entityManager.persist(usuarioNormal);
        entityManager.flush();

        // Act - Buscamos solo a los que tienen rol ADMIN en la Empresa Alpha
        List<Usuario> resultado = usuarioRepository.findAllByEmpresaIdAndRolNombreAndActivoTrue(empresaContexto.getId(), "ADMIN");

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getRol().getNombre()).isEqualTo("ADMIN");
        assertThat(resultado.get(0).getNombres()).isEqualTo("Juan");
    }

    @Test
    void existsByEmail_CuandoExiste_RetornaTrue() {
        // Act
        boolean existe = usuarioRepository.existsByEmail("juan.perez@alpha.com");

        // Assert
        assertThat(existe).isTrue();
    }

    @Test
    void existsByIdAndEmpresaIdAndActivoTrue_CuandoCumpleCondiciones_RetornaTrue() {
        // Act
        boolean existe = usuarioRepository.existsByIdAndEmpresaIdAndActivoTrue(usuarioActivo.getId(), empresaContexto.getId());

        // Assert
        assertThat(existe).isTrue();
    }
}
