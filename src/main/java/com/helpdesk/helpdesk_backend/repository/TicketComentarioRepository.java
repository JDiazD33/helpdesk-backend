package com.helpdesk.helpdesk_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.helpdesk.helpdesk_backend.model.TicketComentario;

@Repository
public interface TicketComentarioRepository extends JpaRepository<TicketComentario, Long> {

    // Trazabilidad cronológica
    List<TicketComentario> findAllByTicketIdOrderByFechaEnvioAsc(Long ticketId);

    // Contadores rapidos para la interfaz de usuario
    long countByTicketId(Long ticketId);

    void deleteByTicketId(Long ticketId);
}
