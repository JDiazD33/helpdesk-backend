package com.helpdesk.helpdesk_backend.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.helpdesk_backend.dto.TicketRequestDTO;
import com.helpdesk.helpdesk_backend.dto.TicketResponseDTO;
import com.helpdesk.helpdesk_backend.mapper.TicketMapper;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.model.ProblemaTicket;
import com.helpdesk.helpdesk_backend.model.Ticket;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.model.enums.EstadoTicket;
import com.helpdesk.helpdesk_backend.repository.EmpresaRepository;
import com.helpdesk.helpdesk_backend.repository.ProblemaTicketRepository;
import com.helpdesk.helpdesk_backend.repository.TicketRepository;
import com.helpdesk.helpdesk_backend.repository.UsuarioRepository;
import com.helpdesk.helpdesk_backend.service.TicketService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService{
    
    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final ProblemaTicketRepository problemaRepository;
    private final TicketMapper ticketMapper;

    @Override
    @Transactional
    public TicketResponseDTO crearTicket(TicketRequestDTO requestDTO, Long clienteIdContexto, Long empresaIdContexto) {
        // 1. Validar Cliente y Empresa (Multitenancy)
        Usuario cliente = usuarioRepository.findByIdAndEmpresaId(clienteIdContexto, empresaIdContexto)
        .orElseThrow(() -> new RuntimeException("Cliente no encontrado en la empresa o no pertenece a la empresa"));
        
        Empresa empresa = empresaRepository.findById(empresaIdContexto)
        .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        // 2. Validar Problema y su pertenencia a la empresa (Transitividad jerárquica)
        ProblemaTicket problema = problemaRepository.findById(requestDTO.getProblemaId())
        .orElseThrow(() -> new RuntimeException("Problema no encontrado"));

        if (!problema.getCategoria().getEmpresa().getId().equals(empresaIdContexto)) {
            throw new RuntimeException("Violacion de seguridad: El problema no pertenece a su empresa.");
        }

        // 3. Mapear y ensamblar la entidad
        Ticket ticket = ticketMapper.toEntity(requestDTO);
        ticket.setCliente(cliente);
        ticket.setProblema(problema);
        ticket.setEmpresa(empresa);
        // El @PrePersist se encargará de poner el estado en ABIERTO y generar el código autogenerado

        Ticket ticketGuardado = ticketRepository.save(ticket);
        return ticketMapper.toResponseDTO(ticketGuardado);
    }

    @Override
    @Transactional
    public TicketResponseDTO asignarAgente(Long ticketId, Long agenteId, Long empresaIdContexto) {
        Ticket ticket = ticketRepository.findByIdAndEmpresaId(ticketId, empresaIdContexto)
        .orElseThrow(() -> new RuntimeException("Ticket no encontrado o no pertenece a la empresa"));
        
        Usuario agente = usuarioRepository.findByIdAndEmpresaId(agenteId, empresaIdContexto)
        .orElseThrow(() -> new RuntimeException("Agente no encontrado o no pertenece a la empresa"));

        ticket.setAgenteAsignado(agente);

        // Regla de Negocio: Si estaba ABIERTO, pasa a EN_PROGRESO al asignar un agente
        if (ticket.getEstado() == EstadoTicket.ABIERTO) {
            ticket.setEstado(EstadoTicket.EN_PROGRESO);
        }

        return ticketMapper.toResponseDTO(ticketRepository.save(ticket));
    }

    @Override
    @Transactional
    public TicketResponseDTO cambiarEstado(Long ticketId, EstadoTicket nuevoEstado, String justificacionCierre,
            Long empresaIdContexto) {
        Ticket ticket = ticketRepository.findByIdAndEmpresaId(ticketId, empresaIdContexto)
        .orElseThrow(() -> new RuntimeException("Ticket no encontrado o no pertenece a la empresa"));

        if (nuevoEstado == EstadoTicket.RESUELTO || nuevoEstado == EstadoTicket.CERRADO) {
            if (justificacionCierre == null || justificacionCierre.trim().isEmpty()) {
                throw new RuntimeException("Justificación de cierre es obligatoria para resolver o cerrar un ticket.");  
            }
            ticket.setJustifiacionCierre(justificacionCierre);
        }

        ticket.setEstado(nuevoEstado);
        return ticketMapper.toResponseDTO(ticketRepository.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponseDTO obtenerPorId(Long ticketId, Long empresaIdContexto) {
        Ticket ticket = ticketRepository.findByIdAndEmpresaId(ticketId, empresaIdContexto)
        .orElseThrow(() -> new RuntimeException("Ticket no encontrado o no pertenece a la empresa"));
        return ticketMapper.toResponseDTO(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponseDTO> listarTodosPorEmpresa(Long empresaIdContexto, Pageable pageable) {
        return ticketRepository.findAllByEmpresaId(empresaIdContexto, pageable).map(ticketMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponseDTO> listarPorCliente(Long clienteId, Long empresaIdContexto, Pageable pageable) {
        return ticketRepository.findAllByEmpresaIdAndClienteId(empresaIdContexto, clienteId, pageable)
        .map(ticketMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponseDTO> listarPorAgente(Long agenteId, Long empresaIdContexto, Pageable pageable) {
        return ticketRepository.findAllByEmpresaIdAndAgenteAsignadoId(empresaIdContexto, agenteId, pageable)
        .map(ticketMapper::toResponseDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponseDTO> listarNoAsignados(Long empresaIdContexto, Pageable pageable) {
        return ticketRepository.findAllByEmpresaIdAndAgenteAsignadoIdIsNull(empresaIdContexto, pageable)
        .map(ticketMapper::toResponseDTO);
    }

}
