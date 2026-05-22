package com.helpdesk.helpdesk_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.helpdesk.helpdesk_backend.model.Ticket;
import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;
import com.helpdesk.helpdesk_backend.model.enums.PrioridadTicket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
        // Buscar un ticket por su código.
        // Será útil para mostrar, consultar o ubicar tickets rápidamente.
        Optional<Ticket> findByCodigo(String codigo);

        // Listar todos los tickets de una empresa.
        // Permite separar la información por empresa en el sistema SaaS.
        List<Ticket> findByEmpresaId(Long empresaId);

        // Listar tickets creados por un cliente específico.
        // Se usará cuando un usuario quiera ver sus propios tickets.
        List<Ticket> findByClienteId(Long clienteId);

        // Listar tickets asignados a un agente específico.
        // Muy útil para que cada agente vea lo que tiene pendiente.
        List<Ticket> findByAgenteAsignadoId(Long agenteAsignadoId);

        // Listar tickets por categoría.
        // Ayuda en filtros, reportes y organización.
        List<Ticket> findByCategoriaId(Long categoriaId);

        // Listar tickets por estado.
        // Ejemplo: ABIERTO, EN_PROCESO, RESUELTO, CERRADO.
        List<Ticket> findByEstado(EstadoTicket estado);

        // Listar tickets por problema específico.
        // Permite agrupar tickets relacionados a un mismo problema o incidencia.
        List<Ticket> findByProblemaId(Long problemaId);

        // Listar tickets por prioridad.
        // Ejemplo: BAJA, MEDIA, ALTA, CRITICA.
        List<Ticket> findByPrioridad(PrioridadTicket prioridad);

        // Listar tickets de una empresa filtrados por estado.
        // Ejemplo: tickets abiertos de una empresa específica.
        List<Ticket> findByEmpresaIdAndEstado(Long empresaId, EstadoTicket estado);

        // Listar tickets de una empresa filtrados por prioridad.
        // Ejemplo: tickets críticos de una empresa específica.
        List<Ticket> findByEmpresaIdAndPrioridad(Long empresaId, PrioridadTicket prioridad);

        // Verificar si ya existe un ticket con ese código.
        // Servirá para evitar duplicados.
        boolean existsByCodigo(String codigo);

        // Buscar ticket por ID y empresa (validación de seguridad multi-tenant)
        Optional<Ticket> findByIdAndEmpresaId(Long id, Long empresaId);

        // ─────────────────────────────────────────────────────
        // CONSULTAS JPQL PERSONALIZADAS (JOIN + WHERE + ORDER BY)
        // ─────────────────────────────────────────────────────

        /**
         * Tickets de una empresa con datos completos del cliente y empresa,
         * ordenados por fecha de creación descendente (más recientes primero).
         * Usa JOIN FETCH para evitar el problema N+1.
         */
        @Query("SELECT t FROM Ticket t " +
                        "JOIN FETCH t.cliente " +
                        "JOIN FETCH t.empresa " +
                        "WHERE t.empresa.id = :empresaId " +
                        "ORDER BY t.fechaCreacion DESC")
        List<Ticket> findByEmpresaConDetallesOrdenado(@Param("empresaId") Long empresaId);

        /**
         * Filtro combinado: tickets de una empresa con estado y/o prioridad opcionales.
         * Si un parámetro es null, se ignora en el filtro (consulta dinámica).
         */
        @Query("SELECT t FROM Ticket t " +
                        "WHERE t.empresa.id = :empresaId " +
                        "AND (:estado IS NULL OR t.estado = :estado) " +
                        "AND (:prioridad IS NULL OR t.prioridad = :prioridad) " +
                        "ORDER BY t.fechaCreacion DESC")
        List<Ticket> filtrarTickets(@Param("empresaId") Long empresaId,
                        @Param("estado") EstadoTicket estado,
                        @Param("prioridad") PrioridadTicket prioridad);

        /**
         * Conteo de tickets agrupados por estado para una empresa.
         * Útil para dashboards y reportes.
         */
        @Query("SELECT t.estado, COUNT(t) FROM Ticket t " +
                        "WHERE t.empresa.id = :empresaId " +
                        "GROUP BY t.estado")
        List<Object[]> contarPorEstado(@Param("empresaId") Long empresaId);

        /**
         * Tickets de un agente con estado específico, incluyendo datos del cliente.
         * Ejemplo: ver los tickets EN_PROGRESO de un agente.
         */
        @Query("SELECT t FROM Ticket t " +
                        "JOIN FETCH t.cliente " +
                        "WHERE t.agenteAsignado.id = :agenteId " +
                        "AND t.estado = :estado " +
                        "ORDER BY t.prioridad DESC, t.fechaCreacion ASC")
        List<Ticket> findByAgenteYEstado(@Param("agenteId") Long agenteId,
                        @Param("estado") EstadoTicket estado);

        @Query("SELECT t FROM Ticket t " +
                        "WHERE t.empresa.id = :empresaId " +
                        "AND t.fechaCreacion BETWEEN :inicio AND :fin " +
                        "ORDER BY t.fechaCreacion DESC")
        List<Ticket> findByEmpresaYPeriodo(@Param("empresaId") Long empresaId,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fin") LocalDateTime fin);

        @Query("SELECT MONTH(t.fechaCreacion), COUNT(t) FROM Ticket t " +
                        "WHERE t.empresa.id = :empresaId " +
                        "AND YEAR(t.fechaCreacion) = :anio " +
                        "GROUP BY MONTH(t.fechaCreacion) " +
                        "ORDER BY MONTH(t.fechaCreacion)")
        List<Object[]> contarPorMes(@Param("empresaId") Long empresaId, @Param("anio") int anio);

        @Query("SELECT t FROM Ticket t " +
                        "WHERE t.empresa.id = :empresaId " +
                        "AND t.prioridad IN ('ALTA', 'CRITICA') " +
                        "ORDER BY t.fechaCreacion DESC")
        List<Ticket> findPrioridadAltaPorEmpresa(@Param("empresaId") Long empresaId);
}
