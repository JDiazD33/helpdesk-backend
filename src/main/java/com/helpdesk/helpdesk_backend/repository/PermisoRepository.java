package com.helpdesk.helpdesk_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import com.helpdesk.helpdesk_backend.model.Permiso;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    Optional<Permiso> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    List<Permiso> findByActivo(boolean activo);

    // Listar permisos activos ordenados alfabéticamente
    @Query("""
            SELECT p FROM Permiso p
            WHERE p.activo = true
            ORDER BY p.nombre ASC
            """)
    List<Permiso> findAllActivosOrdenados();

    // Buscar permisos por nombre o descripción
    @Query("""
            SELECT p FROM Permiso p
            WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))
            ORDER BY p.nombre ASC
            """)
    List<Permiso> buscarPorTexto(@Param("texto") String texto);
}
