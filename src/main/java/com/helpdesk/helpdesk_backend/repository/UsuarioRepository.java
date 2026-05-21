package com.helpdesk.helpdesk_backend.repository;

import com.helpdesk.helpdesk_backend.dto.UsuarioRolDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.helpdesk.helpdesk_backend.model.Usuario;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Login: El email debe ser unico en todo el sistema para que Spring Security
    // funcione facil
    Optional<Usuario> findByEmail(String email);

    // Obtener un usuario asegurando que sea de MI empresa (Evita el hackeo de IDs
    // en la URL)
    Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId);

    // Listar todos los usuarios activos de una empresa (Para el panel de
    // administración)
    List<Usuario> findAllByEmpresaIdAndActivoTrue(Long empresaId);

    // Listar por Rol (Ej: Traer solo agentes activos de la Empresa X para
    // asignarles un ticket)
    List<Usuario> findAllByEmpresaIdAndRolNombreAndActivoTrue(Long empresaId, String nombreRol);

    // Verificar si el correo ya existe (Para el registro de nuevos usuarios)
    boolean existsByEmail(String email);

    // Verificar si existe el usuario en la empresa y está activo
    boolean existsByIdAndEmpresaIdAndActivoTrue(Long id, Long empresaId);

    // Buscar usuarios por nombre
    @Query("""
            SELECT u FROM Usuario u
            WHERE LOWER(u.nombres) LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :texto, '%'))
            """)
    List<Usuario> buscarPorNombreOApellido(String texto);

    // Contar usuarios por rol
    @Query("""
            SELECT new com.helpdesk.helpdesk_backend.dto.UsuarioRolDTO(
                u.rol.nombre,
                COUNT(u)
            )
            FROM Usuario u
            GROUP BY u.rol.nombre
            """)
    List<UsuarioRolDTO> contarUsuariosPorRol();

    // Agentes activos de una empresa
    @Query("""
            SELECT u FROM Usuario u
            WHERE u.empresa.id = :empresaId
            AND u.rol.nombre = 'AGENTE'
            AND u.activo = true
            """)
    List<Usuario> listarAgentesActivos(Long empresaId);
}
