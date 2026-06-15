package com.helpdesk.helpdesk_backend.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import com.helpdesk.helpdesk_backend.model.ProblemaTicket;

@Repository
public interface ProblemaTicketRepository extends JpaRepository<ProblemaTicket, Long> {

    // Listar problemas operativos de una categoría específica
    List<ProblemaTicket> findAllByCategoriaIdAndActivoTrue(Long categoriaId);

    // Listar todos los problemas de una categoría (sin filtrar por activo/empresa)
    List<ProblemaTicket> findAllByCategoriaId(Long categoriaId);

    // Validar que no haya problemas repetidos dentro de la misma categoria
    boolean existsByNombreAndCategoriaId(String nombre, Long categoriaId);

    // Contar cuántos problemas activos hay por categoría
    @Query("""
            SELECT p.categoria.nombre, COUNT(p)
            FROM ProblemaTicket p
            JOIN p.categoria c
            WHERE p.activo = true
            AND (:empresaId IS NULL OR c.empresa.id = :empresaId)
            GROUP BY p.categoria.nombre
            ORDER BY COUNT(p) DESC
            """)
    List<Object[]> contarProblemasPorCategoria(@Param("empresaId") Long empresaId);

    // Buscar problemas por texto en nombre o descripción
    @Query("""
            SELECT p FROM ProblemaTicket p
            WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))
            """)
    List<ProblemaTicket> buscarPorTexto(String texto);
    
    // Listar problemas activos de una empresa específica
    @Query("SELECT p FROM ProblemaTicket p " +
           "JOIN FETCH p.categoria c " +
           "WHERE c.empresa.id = :empresaId " +
           "AND p.activo = true " +
           "ORDER BY p.nombre ASC")
    List<ProblemaTicket> findActivosPorEmpresa(@Param("empresaId") Long empresaId);

    // Listar problemas activos paginados con filtro opcional por categoría
    @Query("SELECT p FROM ProblemaTicket p " +
           "JOIN FETCH p.categoria c " +
           "WHERE c.empresa.id = :empresaId " +
           "AND p.activo = true " +
           "AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)")
    List<ProblemaTicket> findActivosPorEmpresaPaged(
            @Param("empresaId") Long empresaId,
            @Param("categoriaId") Long categoriaId,
            Pageable pageable);

    // Contar problemas activos con filtro opcional por categoría
    @Query("SELECT COUNT(p) FROM ProblemaTicket p " +
           "JOIN p.categoria c " +
           "WHERE c.empresa.id = :empresaId " +
           "AND p.activo = true " +
           "AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)")
    long countActivosPorEmpresa(
            @Param("empresaId") Long empresaId,
            @Param("categoriaId") Long categoriaId);

    // ── Globales (sin filtro de empresa) para ADMIN_OWNER ──

    @Query("SELECT p FROM ProblemaTicket p " +
           "JOIN FETCH p.categoria c " +
           "WHERE p.activo = true " +
           "AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)")
    List<ProblemaTicket> findAllPaged(
            @Param("categoriaId") Long categoriaId,
            Pageable pageable);

    @Query("SELECT COUNT(p) FROM ProblemaTicket p " +
           "WHERE p.activo = true " +
           "AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)")
    long countAll(@Param("categoriaId") Long categoriaId);
}
