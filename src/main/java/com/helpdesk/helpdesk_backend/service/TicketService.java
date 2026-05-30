package com.helpdesk.helpdesk_backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.helpdesk.helpdesk_backend.model.Ticket;
import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;
import com.helpdesk.helpdesk_backend.model.enums.PrioridadTicket;

public interface TicketService {
    
    // Busca todos los tickets, sin filtros
    List<Ticket> listarTodos();

    // Busca ticket por id, usa optional para evitar errores de inexistencia
    Optional<Ticket> buscarPorId(Long id);

    // Crear nuevo ticket
    Ticket guardar(Ticket ticket);

    // Crear ticket y comentario inicial en una sola transacción
    Ticket guardarConComentarioInicial(Ticket ticket, String mensajeInicial, Long usuarioComentarioId);

    // Actualizar ticket existente
    Ticket actualizar(Long id, Ticket ticket);

    // Eliminar ticket por ID
    void eliminar(Long id);

    // Filtros
    // Busca ticket por código, usa optional para evitar errores de inexistencia
    Optional<Ticket> buscarPorCodigo(String codigo);

    // Lista tickets por empresa, cliente, agente asignado, categoría, estado o prioridad
    List<Ticket> listarPorEmpresaId(Long empresaId);

    List<Ticket> listarPorClienteId(Long clienteId);

    List<Ticket> listarPorAgenteAsignadoId(Long agenteAsignadoId);

    List<Ticket> listarPorCategoriaId(Long categoriaId);

    List<Ticket> listarPorEstado(EstadoTicket estado);

    List<Ticket> listarPorPrioridad(PrioridadTicket prioridad);

    // Lista tickets por empresa y estado o prioridad
    List<Ticket> listarPorEmpresaIdYEstado(Long empresaId, EstadoTicket estado);

    List<Ticket> listarPorEmpresaIdYPrioridad(Long empresaId, PrioridadTicket prioridad);
    
    // Verifica si existe ticket por código, para evitar duplicados
    boolean existePorCodigo(String codigo);

    // ─── Métodos con queries JPQL personalizadas ───

    // Tickets de una empresa con detalles del cliente, ordenados por fecha
    List<Ticket> listarPorEmpresaConDetalles(Long empresaId);

    // Filtro combinado: empresa + estado opcional + prioridad opcional
    List<Ticket> filtrarTickets(Long empresaId, EstadoTicket estado, PrioridadTicket prioridad);

    // Conteo de tickets por estado para dashboard
    List<Object[]> contarPorEstado(Long empresaId);

    // Tickets de un agente filtrados por estado
    List<Ticket> listarPorAgenteYEstado(Long agenteId, EstadoTicket estado);

    List<Ticket> listarPorEmpresaYPeriodo(Long empresaId, LocalDateTime inicio, LocalDateTime fin);

    List<Ticket> listarPrioridadAltaPorEmpresa(Long empresaId);

    // ─── 4 NUEVOS MÉTODOS JPQL ───

    List<Ticket> buscarPorTexto(Long empresaId, String texto);

    List<Ticket> listarSinAsignar(Long empresaId);

    List<Ticket> listarPorClienteConDetalles(Long clienteId, Long empresaId);

    List<Ticket> listarActualizadosRecientemente(Long empresaId, int dias);
}
