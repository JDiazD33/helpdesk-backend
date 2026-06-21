package com.helpdesk.helpdesk_backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.helpdesk.helpdesk_backend.dto.CambiarEstadoRequestDTO;
import com.helpdesk.helpdesk_backend.dto.CierreRequestDTO;
import com.helpdesk.helpdesk_backend.model.Ticket;
import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;
import com.helpdesk.helpdesk_backend.model.enums.PrioridadTicket;

public interface TicketService {
    
    // Busca todos los tickets, con filtro opcional por asignación de agente (null=todos, true=con agente, false=sin agente)
    List<Ticket> listarTodos();

    List<Ticket> listarTodos(Boolean asignado);

    // Busca ticket por id, usa optional para evitar errores de inexistencia
    Optional<Ticket> buscarPorId(Long id);

    // Busca ticket validando que pertenezca a la empresa del tenant (multi-tenant safe)
    Optional<Ticket> buscarPorIdAndEmpresa(Long id, Long empresaId);

    // Crear nuevo ticket
    Ticket guardar(Ticket ticket);

    // Crear ticket y comentario inicial en una sola transacción
    Ticket guardarConComentarioInicial(Ticket ticket, String mensajeInicial, Long usuarioComentarioId);

    // Actualizar ticket existente, restringido a la empresa del tenant
    Ticket actualizar(Long id, Long empresaId, Ticket ticket);

    // Eliminar ticket por ID, restringido a la empresa del tenant
    void eliminar(Long id, Long empresaId);

    // Filtros
    // Busca ticket por código, usa optional para evitar errores de inexistencia
    Optional<Ticket> buscarPorCodigo(String codigo);

    // Lista tickets por empresa, con filtro opcional por asignación de agente
    List<Ticket> listarPorEmpresaId(Long empresaId);

    List<Ticket> listarPorEmpresaId(Long empresaId, Boolean asignado);

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

    List<Ticket> listarPrioridadAltaGlobal();

    // ─── 4 NUEVOS MÉTODOS JPQL ───

    List<Ticket> buscarPorTexto(Long empresaId, String texto);

    List<Ticket> listarSinAsignar(Long empresaId);

    List<Ticket> listarSinAsignarGlobal();

    List<Ticket> listarPorPeriodoGlobal(LocalDateTime inicio, LocalDateTime fin);

    List<Ticket> listarPorClienteConDetalles(Long clienteId, Long empresaId);

    List<Ticket> listarActualizadosRecientemente(Long empresaId, int dias);

    Ticket cambiarEstado(Long id, Long empresaId, CambiarEstadoRequestDTO request);

    Ticket asignarAgente(Long id, Long empresaId, Long agenteId);

    Ticket guardarCierre(Long id, Long empresaId, CierreRequestDTO request);
}
