package com.helpdesk.helpdesk_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.helpdesk.helpdesk_backend.model.Usuario;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository  extends JpaRepository <Usuario, Long > {

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

    // Listar todos los usuarios de una empresa específica.
    // Útil en un sistema SaaS donde cada empresa tiene sus propios usuarios.
    List<Usuario> findByEmpresaId(Long empresaId);

    // Listar usuarios según el rol.
    // Sirve, por ejemplo, para obtener solo agentes o solo clientes.
    List<Usuario> findByRolId(Long rolId);

    // Listar usuarios de una empresa filtrando además si están activos.
    // Ejemplo: usuarios activos de la empresa 1.
    List<Usuario> findByEmpresaIdAndActivo(Long empresaId, boolean activo);

    // Listar usuarios de una empresa con un rol específico.
    // Ejemplo: agentes de soporte de una empresa.
    List<Usuario> findByEmpresaIdAndRolId(Long empresaId, Long rolId);

    // Listar usuarios según si están activos o inactivos.
    // Puede usarse en el panel de administración.
    List<Usuario> findByActivo(boolean activo);

    // Buscar usuario por ID y empresa (validación de seguridad multi-tenant)
    Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId);

    // ─────────────────────────────────────────────────────
    // CONSULTAS JPQL PERSONALIZADAS (JOIN + WHERE + ORDER BY)
    // ─────────────────────────────────────────────────────

    /**
     * Usuarios activos de una empresa con sus datos de rol y empresa precargados.
     * JOIN FETCH evita consultas N+1 y optimiza el rendimiento.
     * Ordenados alfabéticamente por nombre.
     */
    @Query("SELECT u FROM Usuario u " +
           "JOIN FETCH u.rol " +
           "JOIN FETCH u.empresa " +
           "WHERE u.empresa.id = :empresaId " +
           "AND u.activo = true " +
           "ORDER BY u.nombres ASC")
    List<Usuario> findActivosPorEmpresaConDetalles(@Param("empresaId") Long empresaId);

    /**
     * Buscar usuarios por nombre o apellido (búsqueda parcial, case-insensitive).
     * Útil para la funcionalidad de buscar usuarios en el panel admin.
     */
    @Query("SELECT u FROM Usuario u " +
           "JOIN FETCH u.rol " +
           "WHERE u.empresa.id = :empresaId " +
           "AND (LOWER(u.nombres) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :termino, '%'))) " +
           "ORDER BY u.nombres ASC")
    List<Usuario> buscarPorNombreOApellido(@Param("empresaId") Long empresaId,
                                           @Param("termino") String termino);

}
