package com.helpdesk.helpdesk_backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.helpdesk.helpdesk_backend.constants.RolConstants;
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

        rolAdmin = Rol.builder().nombre("ADMIN").build();
        entityManager.persist(rolAdmin);

        rolUser = Rol.builder().nombre("USER").build();
        entityManager.persist(rolUser);

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
        Optional<Usuario> resultado = usuarioRepository.findByIdAndEmpresaId(usuarioActivo.getId(), empresaContexto.getId());
        
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEmail()).isEqualTo("juan.perez@alpha.com");
    }

    @Test
    void findByIdAndEmpresaId_CuandoUsuarioNoPerteneceAEmpresa_RetornaVacio() {
        Optional<Usuario> resultado = usuarioRepository.findByIdAndEmpresaId(usuarioActivo.getId(), otraEmpresa.getId());

        assertThat(resultado).isNotPresent();
    }

    @Test
    void findByEmpresaIdAndActivo_RetornaListaDeUsuariosDeLaEmpresa() {
        Usuario usuarioInactivo = Usuario.builder()
                .nombres("Ana")
                .apellidos("Gómez")
                .email("ana.gomez@alpha.com")
                .password("hash123")
                .empresa(empresaContexto)
                .rol(rolUser)
                .activo(false) 
                .build();
        entityManager.persist(usuarioInactivo);
        entityManager.flush();

        List<Usuario> resultado = usuarioRepository.findByEmpresaIdAndActivoExcluyendoOwner(
                empresaContexto.getId(), true, RolConstants.ADMIN_OWNER);

        assertThat(resultado).hasSize(1); 
        assertThat(resultado.get(0).getNombres()).isEqualTo("Juan");
    }

    @Test
    void findByEmpresaIdAndRolId_RetornaSoloUsuariosConEseRol() {
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

        List<Usuario> resultado = usuarioRepository.findByEmpresaIdAndRolIdExcluyendoOwner(
                empresaContexto.getId(), rolAdmin.getId(), RolConstants.ADMIN_OWNER);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getRol().getNombre()).isEqualTo("ADMIN");
        assertThat(resultado.get(0).getNombres()).isEqualTo("Juan");
    }

    @Test
    void existsByEmail_CuandoExiste_RetornaTrue() {
        boolean existe = usuarioRepository.existsByEmail("juan.perez@alpha.com");

        assertThat(existe).isTrue();
    }

}
