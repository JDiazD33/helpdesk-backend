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
    // Útil para validar que un ticket pertenece a la empresa del usuario autenticado antes de mostrarlo o permitir acciones sobre él.
    Optional<Ticket> findByIdAndEmpresaId(Long id, Long empresaId);
    // Útil para validar que un ticket con un código específico pertenece a la empresa del usuario autenticado, especialmente en integraciones o accesos externos.
    Optional<Ticket> findByCodigoAndEmpresaId(String codigo, Long empresaId);

    // Vistas de Usuario y Paginación
    // Útil para mostrar una lista paginada de tickets pertenecientes a la empresa del usuario autenticado, con filtros adicionales para clientes o agentes asignados.
    Page<Ticket> findAllByEmpresaId(Long empresaId, Pageable pageable);
    // Útil para mostrar una lista paginada de tickets pertenecientes a la empresa del usuario autenticado y asignados a un cliente específico, ideal para que los clientes vean solo sus tickets.
    Page<Ticket> findAllByEmpresaIdAndClienteId(Long empresaId, Long clienteId, Pageable pageable);
    // Útil para mostrar una lista paginada de tickets pertenecientes a la empresa del usuario autenticado y asignados a un agente específico, ideal para que los agentes vean solo los tickets que les han sido asignados.
    Page<Ticket> findAllByEmpresaIdAndAgenteAsignadoId(Long empresaId, Long agenteId, Pageable pageable);

    // Filtros Operativos y Tableros
    // Útil para mostrar una lista paginada de tickets pertenecientes a la empresa del usuario autenticado y filtrados por estado, ideal para tableros de control o vistas específicas de estado (ej. "Abiertos", "En Progreso", "Cerrados").
    Page<Ticket> findAllByEmpresaIdAndEstado(Long empresaId, EstadoTicket estado, Pageable pageable);
    // Útil para mostrar una lista paginada de tickets pertenecientes a la empresa del usuario autenticado y filtrados por prioridad, ideal para que los agentes prioricen su trabajo.
    Page<Ticket> findAllByEmpresaIdAndAgenteAsignadoIdIsNull(Long empresaId, Pageable pageable);
    // Útil para mostrar una lista paginada de tickets pertenecientes a la empresa del usuario autenticado, filtrados por prioridad y excluyendo un estado específico (ej. "Cerrados"), ideal para que los agentes se enfoquen en tickets prioritarios que aún requieren atención.
    Page<Ticket> findAllByEmpresaIdAndPrioridadAndEstadoNot(Long empresaId, PrioridadTicket prioridad, EstadoTicket estado, Pageable pageable);
    
    // Métricas y KPIs
    // Métrica: Número total de tickets por estado para una empresa específica
    long countByEmpresaIdAndEstado(Long empresaId, EstadoTicket estado);
    // Métrica: Tickets abiertos asignados a un agente específico (excluyendo cerrados)
    long countByEmpresaIdAndAgenteAsignadoIdAndEstadoNot(Long empresaId, Long agenteId, EstadoTicket estado);

    // Reportes y Analítica (Rendimiento en un periodo de tiempo)
    Page<Ticket> findAllByEmpresaIdAndEstadoAndFechaActualizacionBetween(Long empresaId, EstadoTicket estado, LocalDateTime inicio, LocalDateTime fin, Pageable pageable);
}
