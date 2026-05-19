package com.helpdesk.helpdesk_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.helpdesk.helpdesk_backend.model.Usuario;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository  extends JpaRepository <Usuario, Long > {
    
    // Login: El email debe ser unico en todo el sistema para que Spring Security funcione facil
    Optional<Usuario> findByEmail(String email);

    // Obtener un usuario asegurando que sea de MI empresa (Evita el hackeo de IDs en la URL)
    Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId);

    // Listar todos los usuarios activos de una empresa (Para el panel de administración)
    List<Usuario> findAllByEmpresaIdAndActivoTrue(Long empresaId);

    // Listar por Rol (Ej: Traer solo agentes activos de la Empresa X para asignarles un ticket)
    List<Usuario> findAllByEmpresaIdAndRolNombreAndActivoTrue(Long empresaId, String nombreRol);

    // Verificar si el correo ya existe (Para el registro de nuevos usuarios)
    boolean existsByEmail(String email);

    // Verificar si existe el usuario en la empresa y está activo
    boolean existsByIdAndEmpresaIdAndActivoTrue(Long id, Long empresaId);

}
