package com.helpdesk.helpdesk_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.helpdesk.helpdesk_backend.model.CategoriaTicket;

public interface CategoriaTicketRepository extends JpaRepository<CategoriaTicket, Long> {
    // Listar categorías activas o inactivas.
    // Será útil para filtros y administración de categorías.
    List<CategoriaTicket> findByActiva(boolean activa);

    // Listar todas las categorías de una empresa.
    // En el sistema SaaS cada empresa tendrá sus propias categorías.
    List<CategoriaTicket> findByEmpresaId(Long empresaId);

    // Listar categorías de una empresa filtrando si están activas.
    // Ejemplo: categorías activas de la empresa 2.
    List<CategoriaTicket> findByEmpresaIdAndActiva(Long empresaId, boolean activa);

    // Buscar una categoría por nombre dentro de una empresa.
    // Sirve para validar que no se repita el nombre en la misma empresa.
    Optional<CategoriaTicket> findByNombreAndEmpresaId(String nombre, Long empresaId);

    // Verificar si ya existe una categoría con ese nombre en una empresa.
    // Importante para evitar duplicados por empresa.
    boolean existsByNombreAndEmpresaId(String nombre, Long empresaId);

    // ─────────────────────────────────────────────────────
    // CONSULTAS JPQL PERSONALIZADAS (JOIN + WHERE + ORDER BY)
    // ─────────────────────────────────────────────────────

    /**
     * Categorías activas de una empresa con datos de empresa precargados,
     * ordenadas alfabéticamente. JOIN FETCH optimiza la consulta.
     */
    @Query("SELECT c FROM CategoriaTicket c " +
           "JOIN FETCH c.empresa " +
           "WHERE c.empresa.id = :empresaId " +
           "AND c.activa = true " +
           "ORDER BY c.nombre ASC")
    List<CategoriaTicket> findActivasPorEmpresaOrdenadas(@Param("empresaId") Long empresaId);
}
