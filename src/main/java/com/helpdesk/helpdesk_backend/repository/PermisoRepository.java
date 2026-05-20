package com.helpdesk.helpdesk_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.helpdesk.helpdesk_backend.model.Permiso;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    Optional<Permiso> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    List<Permiso> findByActivo(boolean activo);

    @Query("SELECT p FROM Permiso p WHERE p.activo = true ORDER BY p.nombre ASC")
    List<Permiso> findAllActivosOrdenados();
}
