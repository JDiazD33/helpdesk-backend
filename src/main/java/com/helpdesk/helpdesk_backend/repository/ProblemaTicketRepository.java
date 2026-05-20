package com.helpdesk.helpdesk_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.helpdesk.helpdesk_backend.model.ProblemaTicket;

@Repository
public interface ProblemaTicketRepository extends JpaRepository<ProblemaTicket, Long> {

    // Listar problemas operativos de una categoría específica
    List<ProblemaTicket> findAllByCategoriaIdAndActivoTrue(Long categoriaId);

    // Validar que no haya problemas repetidos dentro de la misma categoria
    boolean existsByNombreAndCategoriaId(String nombre, Long categoriaId);

    @Query("SELECT p FROM ProblemaTicket p " +
           "JOIN FETCH p.categoria c " +
           "WHERE c.empresa.id = :empresaId " +
           "AND p.activo = true " +
           "ORDER BY p.nombre ASC")
    List<ProblemaTicket> findActivosPorEmpresa(@Param("empresaId") Long empresaId);
}
