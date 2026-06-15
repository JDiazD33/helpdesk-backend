package com.helpdesk.helpdesk_backend.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.helpdesk_backend.dto.PageResponse;
import com.helpdesk.helpdesk_backend.dto.ProblemaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ProblemaResponseDTO;
import com.helpdesk.helpdesk_backend.exception.DuplicateResourceException;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.model.CategoriaTicket;
import com.helpdesk.helpdesk_backend.model.ProblemaTicket;
import com.helpdesk.helpdesk_backend.repository.CategoriaTicketRepository;
import com.helpdesk.helpdesk_backend.repository.ProblemaTicketRepository;
import com.helpdesk.helpdesk_backend.service.ProblemaTicketService;

@Service
@Transactional
public class ProblemaTicketServiceImpl implements ProblemaTicketService {

    private final ProblemaTicketRepository problemaTicketRepository;
    private final CategoriaTicketRepository categoriaTicketRepository;

    public ProblemaTicketServiceImpl(ProblemaTicketRepository problemaTicketRepository, CategoriaTicketRepository categoriaTicketRepository) {
        this.problemaTicketRepository = problemaTicketRepository;
        this.categoriaTicketRepository = categoriaTicketRepository;
    }

    private ProblemaResponseDTO mapToDTO(ProblemaTicket problema) {
        ProblemaResponseDTO dto = new ProblemaResponseDTO();
        dto.setId(problema.getId());
        dto.setNombre(problema.getNombre());
        dto.setDescripcion(problema.getDescripcion());
        dto.setActivo(problema.isActivo());
        dto.setCategoriaId(problema.getCategoria().getId());
        dto.setCategoriaNombre(problema.getCategoria().getNombre());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProblemaResponseDTO> listarActivosPorEmpresa(Long empresaId) {
        return problemaTicketRepository.findActivosPorEmpresa(empresaId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProblemaResponseDTO> listarPaginado(Long empresaId, int page, int limit, Long categoriaId) {
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("nombre").ascending());
        List<ProblemaResponseDTO> data = problemaTicketRepository
                .findActivosPorEmpresaPaged(empresaId, categoriaId, pageable)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
        long total = problemaTicketRepository.countActivosPorEmpresa(empresaId, categoriaId);
        int totalPages = (int) Math.ceil((double) total / limit);
        return new PageResponse<>(data, total, page, totalPages);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProblemaResponseDTO> listarTodosPaginado(int page, int limit, Long categoriaId) {
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("nombre").ascending());
        List<ProblemaResponseDTO> data = problemaTicketRepository
                .findAllPaged(categoriaId, pageable)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
        long total = problemaTicketRepository.countAll(categoriaId);
        int totalPages = (int) Math.ceil((double) total / limit);
        return new PageResponse<>(data, total, page, totalPages);
    }

    @Override
    @Transactional(readOnly = true)
    public ProblemaResponseDTO obtenerPorId(Long id, Long empresaId) {
        ProblemaTicket problema = problemaTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problema no encontrado con id: " + id));
        if (!problema.getCategoria().getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("El problema no pertenece a la empresa indicada");
        }
        return mapToDTO(problema);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> contarProblemasPorCategoria(Long empresaId) {
        List<Object[]> filas = problemaTicketRepository.contarProblemasPorCategoria(empresaId);
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Object[] fila : filas) {
            Map<String, Object> item = new HashMap<>();
            item.put("categoria", fila[0]);
            item.put("total", fila[1]);
            resultado.add(item);
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProblemaResponseDTO> buscarPorTexto(String texto) {
        return problemaTicketRepository.buscarPorTexto(texto).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProblemaResponseDTO> listarTodosPorCategoria(Long categoriaId) {
        return problemaTicketRepository.findAllByCategoriaId(categoriaId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProblemaResponseDTO> listarProblemasPorCategoria(Long categoriaId, Long empresaId) {
        CategoriaTicket categoria = categoriaTicketRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + categoriaId));
        if (!categoria.getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("Categoría no pertenece a la empresa indicada");
        }

        return problemaTicketRepository.findAllByCategoriaIdAndActivoTrue(categoriaId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProblemaResponseDTO crearProblema(ProblemaRequestDTO requestDTO, Long empresaId) {
        CategoriaTicket categoria = categoriaTicketRepository.findById(requestDTO.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + requestDTO.getCategoriaId()));
        
        if (!categoria.getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("Categoría no pertenece a la empresa indicada");
        }

        if (problemaTicketRepository.existsByNombreAndCategoriaId(requestDTO.getNombre(), requestDTO.getCategoriaId())) {
            throw new DuplicateResourceException("Ya existe un problema con el nombre: " + requestDTO.getNombre() + " en esta categoría");
        }

        ProblemaTicket problema = new ProblemaTicket();
        problema.setNombre(requestDTO.getNombre());
        problema.setDescripcion(requestDTO.getDescripcion());
        problema.setCategoria(categoria);
        problema.setActivo(true);

        ProblemaTicket nuevoProblema = problemaTicketRepository.save(problema);
        return mapToDTO(nuevoProblema);
    }

    @Override
    public ProblemaResponseDTO actualizarProblema(Long id, ProblemaRequestDTO requestDTO, Long empresaId) {
        ProblemaTicket problemaExistente = problemaTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problema no encontrado con id: " + id));

        if (!problemaExistente.getCategoria().getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("El problema no pertenece a la empresa indicada");
        }

        if (!problemaExistente.getNombre().equals(requestDTO.getNombre()) && 
            problemaTicketRepository.existsByNombreAndCategoriaId(requestDTO.getNombre(), requestDTO.getCategoriaId())) {
            throw new DuplicateResourceException("Ya existe un problema con el nombre: " + requestDTO.getNombre() + " en esta categoría");
        }

        if (!problemaExistente.getCategoria().getId().equals(requestDTO.getCategoriaId())) {
            CategoriaTicket nuevaCategoria = categoriaTicketRepository.findById(requestDTO.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + requestDTO.getCategoriaId()));
            if (!nuevaCategoria.getEmpresa().getId().equals(empresaId)) {
                throw new ResourceNotFoundException("La nueva categoría no pertenece a la empresa indicada");
            }
            problemaExistente.setCategoria(nuevaCategoria);
        }

        problemaExistente.setNombre(requestDTO.getNombre());
        problemaExistente.setDescripcion(requestDTO.getDescripcion());

        ProblemaTicket problemaActualizado = problemaTicketRepository.save(problemaExistente);
        return mapToDTO(problemaActualizado);
    }

    @Override
    public void eliminarProblema(Long id, Long empresaId) {
        ProblemaTicket problema = problemaTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problema no encontrado con id: " + id));

        if (!problema.getCategoria().getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("El problema no pertenece a la empresa indicada");
        }

        problema.setActivo(false);
        problemaTicketRepository.save(problema);
    }
}
