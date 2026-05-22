package com.helpdesk.helpdesk_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.helpdesk.helpdesk_backend.model.Rol;

public interface RolRepository extends JpaRepository<Rol, Long> {

    // Buscar un rol por su nombre.
    // Ejemplo: ADMIN_EMPRESA, AGENTE, CLIENTE.
    // Será útil cuando se asignen roles a los usuarios.
    Optional<Rol> findByNombre(String nombre);

    // Verificar si ya existe un rol con ese nombre.
    // Servirá para evitar duplicados si luego se permite registrar roles.
    boolean existsByNombre(String nombre);

    @Query("SELECT r FROM Rol r LEFT JOIN FETCH r.permisos WHERE r.id = :id")
    Optional<Rol> findByIdWithPermisos(@Param("id") Long id);
}
