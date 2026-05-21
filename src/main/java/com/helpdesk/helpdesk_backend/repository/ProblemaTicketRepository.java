package com.helpdesk.helpdesk_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.helpdesk.helpdesk_backend.model.ProblemaTicket;

@Repository
public interface ProblemaTicketRepository extends JpaRepository<ProblemaTicket, Long> {

    // Listar problemas operativos de una categoría específica
    List<ProblemaTicket> findAllByCategoriaIdAndActivoTrue(Long categoriaId);

    // Validar que no haya problemas repetidos dentro de la misma categoria
    boolean existsByNombreAndCategoriaId(String nombre, Long categoriaId);

    // Contar cuántos problemas activos hay por categoría
    @Query("""
            SELECT p.categoria.nombre, COUNT(p)
            FROM ProblemaTicket p
            WHERE p.activo = true
            GROUP BY p.categoria.nombre
            """)
    List<Object[]> contarProblemasPorCategoria();

    // Buscar problemas por texto en nombre o descripción
    @Query("""
            SELECT p FROM ProblemaTicket p
            WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))
            """)
    List<ProblemaTicket> buscarPorTexto(String texto);
}
