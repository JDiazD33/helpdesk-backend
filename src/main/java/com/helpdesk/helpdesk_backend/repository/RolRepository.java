package com.helpdesk.helpdesk_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.helpdesk.helpdesk_backend.model.Rol;

public interface RolRepository extends JpaRepository<Rol, Long> {

    // Útil para buscar roles estáticos por su nombre de BD (ej. "ADMIN" o "USER")suarios.
    Optional<Rol> findByNombre(String nombre);
}
