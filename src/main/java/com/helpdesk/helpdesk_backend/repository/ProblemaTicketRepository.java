package com.helpdesk.helpdesk_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.helpdesk.helpdesk_backend.model.ProblemaTicket;

@Repository
public interface ProblemaTicketRepository extends JpaRepository<ProblemaTicket, Long> {

    // Listar problemas operativos de una categoría específica
    List<ProblemaTicket> findAllByCategoriaIdAndActivoTrue(Long categoriaId);

    // Validar que no haya problemas repetidos dentro de la misma categoria
    boolean existsByNombreAndCategoriaId(String nombre, Long categoriaId);
}
