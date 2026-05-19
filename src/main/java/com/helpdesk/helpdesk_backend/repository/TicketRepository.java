package com.helpdesk.helpdesk_backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.helpdesk.helpdesk_backend.model.Ticket;
import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;
import com.helpdesk.helpdesk_backend.model.enums.PrioridadTicket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Seguridad y Aislamiento
    // Útil para garantizar que los usuarios solo puedan acceder a los tickets que pertenecen a su empresa, 
    // evitando accesos no autorizados.
    Optional<Ticket> findByIdAndEmpresaId(Long id, Long empresaId);
    // útil para búsquedas rápidas por código de ticket dentro de la empresa del usuario autenticado, 
    // asegurando que no se expongan tickets de otras empresas.
    Optional<Ticket> findByCodigoAndEmpresaId(String codigo, Long empresaId);

    // Vistas de Usuario y Paginación
    // Útil para mostrar una lista paginada de tickets pertenecientes a la empresa del usuario autenticado, 
    // con filtros adicionales para clientes o agentes asignados.
    Page<Ticket> findAllByEmpresaId(Long empresaId, Pageable pageable);
    Page<Ticket> findAllByEmpresaIdAndClienteId(Long empresaId, Long clienteId, Pageable pageable);
    Page<Ticket> findAllByEmpresaIdAndAgenteAsignadoId(Long empresaId, Long agenteId, Pageable pageable);

    // Filtros Operativos y Tableros
    // Útil para mostrar una lista paginada de tickets pertenecientes a la empresa del usuario autenticado y 
    // filtrados por estado, ideal para tableros de control o vistas específicas de estado (ej. "Abiertos", "En Progreso", "Cerrados").
    Page<Ticket> findAllByEmpresaIdAndEstado(Long empresaId, EstadoTicket estado, Pageable pageable);
    // ideal para que los agentes prioricen su trabajo.
    Page<Ticket> findAllByEmpresaIdAndAgenteAsignadoIdIsNull(Long empresaId, Pageable pageable);
    // filtrados por prioridad y excluyendo un estado específico (ej. "Cerrados"), ideal para que los agentes se enfoquen 
    // en tickets prioritarios que aún requieren atención.
    Page<Ticket> findAllByEmpresaIdAndPrioridadAndEstadoNot(Long empresaId, PrioridadTicket prioridad, EstadoTicket estado, Pageable pageable);
    
    // Métricas y KPIs
    // Métrica: Número total de tickets por estado para una empresa específica
    long countByEmpresaIdAndEstado(Long empresaId, EstadoTicket estado);
    // Métrica: Tickets abiertos asignados a un agente específico (excluyendo cerrados)
    long countByEmpresaIdAndAgenteAsignadoIdAndEstadoNot(Long empresaId, Long agenteId, EstadoTicket estado);

    // Reportes y Analítica (Rendimiento en un periodo de tiempo)
    Page<Ticket> findAllByEmpresaIdAndEstadoAndFechaActualizacionBetween(Long empresaId, EstadoTicket estado, LocalDateTime inicio, LocalDateTime fin, Pageable pageable);
}
