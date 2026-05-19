package com.helpdesk.helpdesk_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.helpdesk.helpdesk_backend.model.CategoriaTicket;

@Repository
public interface CategoriaTicketRepository extends JpaRepository<CategoriaTicket, Long> {

    // Para administración: Lista todas las categorías (activas e inactivas) de una empresa
    List<CategoriaTicket> findAllByEmpresaId(Long empresaId);

    // Para uso operativo: Lista categorías activas de una empresa específica
    List<CategoriaTicket> findAllByEmpresaIdAndActivaTrue(Long empresaId);

    // Para validar duplicados en la misma empresa al crear o editar
    boolean existsByNombreAndEmpresaId(String nombre, Long empresaId);
    
    // Búsqueda segura: Garantiza que la categoría exista, esté activa y pertenezca a la empresa
    Optional<CategoriaTicket> findByIdAndEmpresaIdAndActivaTrue(Long id, Long empresaId);
}
