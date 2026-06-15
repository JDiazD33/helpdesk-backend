package com.helpdesk.helpdesk_backend.repository;

import com.helpdesk.helpdesk_backend.dto.UsuarioRolDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.helpdesk.helpdesk_backend.model.Usuario;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

        // Buscar usuario por email.
        // Este método será clave para login, seguridad y validaciones.
        Optional<Usuario> findByEmail(String email);

        @Query("SELECT u FROM Usuario u " +
                        "JOIN FETCH u.rol r " +
                        "LEFT JOIN FETCH r.permisos " +
                        "JOIN FETCH u.empresa " +
                        "WHERE u.email = :email")
        Optional<Usuario> findByEmailWithRolYPermisos(@Param("email") String email);

        // Verificar si ya existe un usuario con ese email.
        // Importante para evitar correos duplicados al registrar usuarios.
        boolean existsByEmail(String email);

        // Listar usuarios de una empresa, excluyendo al ADMIN_OWNER del SaaS.
        // El owner tiene empresa_id asignado por la restricción NOT NULL,
        // pero NO pertenece lógicamente a ningún tenant. Nunca incluirlo.
        @Query("SELECT u FROM Usuario u " +
                        "WHERE u.empresa.id = :empresaId " +
                        "AND u.rol.nombre <> :rolOwner")
        List<Usuario> findByEmpresaIdExcluyendoOwner(
                        @Param("empresaId") Long empresaId,
                        @Param("rolOwner") String rolOwner);

        // Listar usuarios según el rol (global, sin filtro de tenant).
        List<Usuario> findByRolId(Long rolId);

        // Listar usuarios de una empresa filtrando además si están activos,
        // excluyendo al ADMIN_OWNER.
        @Query("SELECT u FROM Usuario u " +
                        "WHERE u.empresa.id = :empresaId " +
                        "AND u.activo = :activo " +
                        "AND u.rol.nombre <> :rolOwner")
        List<Usuario> findByEmpresaIdAndActivoExcluyendoOwner(
                        @Param("empresaId") Long empresaId,
                        @Param("activo") boolean activo,
                        @Param("rolOwner") String rolOwner);

        // Listar usuarios de una empresa con un rol específico,
        // excluyendo al ADMIN_OWNER.
        @Query("SELECT u FROM Usuario u " +
                        "WHERE u.empresa.id = :empresaId " +
                        "AND u.rol.id = :rolId " +
                        "AND u.rol.nombre <> :rolOwner")
        List<Usuario> findByEmpresaIdAndRolIdExcluyendoOwner(
                        @Param("empresaId") Long empresaId,
                        @Param("rolId") Long rolId,
                        @Param("rolOwner") String rolOwner);

        // Listar usuarios según si están activos o inactivos (global, sin filtro de tenant).
        List<Usuario> findByActivo(boolean activo);

        // Buscar usuario por ID y empresa (validación de seguridad multi-tenant).
        // Si el usuario resulta ser ADMIN_OWNER se permite (caso login del owner
        // sin tenant asignado en su sesión); el filtro por tenant sigue aplicando.
        Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId);

        // ─────────────────────────────────────────────────────
        // CONSULTAS JPQL PERSONALIZADAS (JOIN + WHERE + ORDER BY)
        // ─────────────────────────────────────────────────────

        /**
         * Usuarios activos de una empresa con sus datos de rol y empresa precargados,
         * excluyendo al ADMIN_OWNER. JOIN FETCH evita N+1.
         */
        @Query("SELECT u FROM Usuario u " +
                        "JOIN FETCH u.rol " +
                        "JOIN FETCH u.empresa " +
                        "WHERE u.empresa.id = :empresaId " +
                        "AND u.activo = true " +
                        "AND u.rol.nombre <> :rolOwner " +
                        "ORDER BY u.nombres ASC")
        List<Usuario> findActivosPorEmpresaConDetallesExcluyendoOwner(
                        @Param("empresaId") Long empresaId,
                        @Param("rolOwner") String rolOwner);

        /**
         * Buscar usuarios por nombre o apellido (búsqueda parcial, case-insensitive)
         * en el contexto de un tenant, excluyendo al ADMIN_OWNER.
         */
        @Query("SELECT u FROM Usuario u " +
                        "JOIN FETCH u.rol " +
                        "WHERE u.empresa.id = :empresaId " +
                        "AND u.rol.nombre <> :rolOwner " +
                        "AND (LOWER(u.nombres) LIKE LOWER(CONCAT('%', :termino, '%')) " +
                        "OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :termino, '%'))) " +
                        "ORDER BY u.nombres ASC")
        List<Usuario> buscarPorNombreOApellidoExcluyendoOwner(
                        @Param("empresaId") Long empresaId,
                        @Param("termino") String termino,
                        @Param("rolOwner") String rolOwner);

        // Contar usuarios por rol (global, sin filtro de tenant). Mantener intacto.
        @Query("""
                        SELECT new com.helpdesk.helpdesk_backend.dto.UsuarioRolDTO(
                        u.rol.nombre,
                        COUNT(u)
                        )
                        FROM Usuario u
                        GROUP BY u.rol.nombre
                        """)
        List<UsuarioRolDTO> contarUsuariosPorRol();

        // Contar usuarios por rol dentro de un tenant, excluyendo al ADMIN_OWNER.
        @Query("""
                        SELECT new com.helpdesk.helpdesk_backend.dto.UsuarioRolDTO(
                        u.rol.nombre,
                        COUNT(u)
                        )
                        FROM Usuario u
                        WHERE u.empresa.id = :empresaId
                        AND u.rol.nombre <> :rolOwner
                        GROUP BY u.rol.nombre
                        """)
        List<UsuarioRolDTO> contarUsuariosPorRolEmpresaExcluyendoOwner(
                        @Param("empresaId") Long empresaId,
                        @Param("rolOwner") String rolOwner);

        // Agentes activos de una empresa, excluyendo al ADMIN_OWNER
        // (defensa por si en algún escenario el owner tuviera ese nombre de rol).
        @Query("""
                        SELECT u FROM Usuario u
                        WHERE u.empresa.id = :empresaId
                        AND u.rol.nombre = 'AGENTE'
                        AND u.activo = true
                        AND u.rol.nombre <> :rolOwner
                        """)
        List<Usuario> listarAgentesActivosExcluyendoOwner(
                        @Param("empresaId") Long empresaId,
                        @Param("rolOwner") String rolOwner);

        /**
         * Búsqueda combinada con filtro por texto y roles.
         * Cuando se proporciona empresaId (contexto de tenant) se excluye al
         * ADMIN_OWNER para no contaminar las métricas del tenant.
         * Si empresaId es NULL, es una búsqueda global del owner y NO se excluye.
         */
        @Query("""
                        SELECT u FROM Usuario u
                        JOIN FETCH u.rol r
                        JOIN FETCH u.empresa e
                        WHERE (:empresaId IS NULL OR u.empresa.id = :empresaId)
                        AND (:empresaId IS NULL OR u.rol.nombre <> :rolOwner)
                        AND (:search IS NULL OR :search = '' OR
                            LOWER(u.nombres) LIKE LOWER(CONCAT('%', :search, '%')) OR
                            LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR
                            LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
                        AND (:rolIds IS NULL OR u.rol.id IN :rolIds)
                        ORDER BY u.nombres ASC
                        """)
        List<Usuario> buscarConFiltrosExcluyendoOwner(@Param("empresaId") Long empresaId,
                                       @Param("search") String search,
                                       @Param("rolIds") List<Long> rolIds,
                                       @Param("rolOwner") String rolOwner);
}
