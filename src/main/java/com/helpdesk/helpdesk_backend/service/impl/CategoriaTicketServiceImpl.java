package com.helpdesk.helpdesk_backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.helpdesk_backend.dto.CategoriaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.CategoriaResponseDTO;
import com.helpdesk.helpdesk_backend.dto.PageResponse;
import com.helpdesk.helpdesk_backend.exception.DuplicateResourceException;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.model.CategoriaTicket;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.repository.CategoriaTicketRepository;
import com.helpdesk.helpdesk_backend.repository.EmpresaRepository;
import com.helpdesk.helpdesk_backend.service.CategoriaTicketService;

@Service
@Transactional
public class CategoriaTicketServiceImpl implements CategoriaTicketService {

    private final CategoriaTicketRepository categoriaTicketRepository;
    private final EmpresaRepository empresaRepository;

    public CategoriaTicketServiceImpl(CategoriaTicketRepository categoriaTicketRepository, EmpresaRepository empresaRepository) {
        this.categoriaTicketRepository = categoriaTicketRepository;
        this.empresaRepository = empresaRepository;
    }

    private CategoriaResponseDTO mapToDTO(CategoriaTicket categoria) {
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        dto.setDescripcion(categoria.getDescripcion());
        dto.setActiva(categoria.isActiva());
        if (categoria.getEmpresa() != null) {
            dto.setEmpresaId(categoria.getEmpresa().getId());
            dto.setEmpresaNombre(categoria.getEmpresa().getNombre());
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarCategoriasActivas(Long empresaId) {
        return categoriaTicketRepository.findActivasPorEmpresaOrdenadas(empresaId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO obtenerPorId(Long id, Long empresaId) {
        CategoriaTicket categoria = categoriaTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría de ticket no encontrada con id: " + id));
        if (!categoria.getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("Categoría no pertenece a la empresa indicada");
        }
        return mapToDTO(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarTodas(Long empresaId) {
        return categoriaTicketRepository.findByEmpresaId(empresaId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarTodasGlobal() {
        return categoriaTicketRepository.findAllOrdered().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoriaResponseDTO> buscarPaginado(Long empresaId, int page, int limit, String search) {
        if (search != null && search.trim().isEmpty()) search = null;
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("nombre").ascending());
        List<CategoriaResponseDTO> data = categoriaTicketRepository
                .buscarPaginado(empresaId, search, pageable)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
        long total = categoriaTicketRepository.countBuscar(empresaId, search);
        int totalPages = (int) Math.ceil((double) total / limit);
        return new PageResponse<>(data, total, page, totalPages);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoriaResponseDTO> buscarPaginadoGlobal(int page, int limit, String search) {
        if (search != null && search.trim().isEmpty()) search = null;
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("nombre").ascending());
        List<CategoriaResponseDTO> data = categoriaTicketRepository
                .buscarPaginadoGlobal(search, pageable)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
        long total = categoriaTicketRepository.countBuscarGlobal(search);
        int totalPages = (int) Math.ceil((double) total / limit);
        return new PageResponse<>(data, total, page, totalPages);
    }

    @Override
    public CategoriaResponseDTO crearCategoria(CategoriaRequestDTO requestDTO, Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con id: " + empresaId));

        if (categoriaTicketRepository.existsByNombreAndEmpresaId(requestDTO.getNombre(), empresaId)) {
            throw new DuplicateResourceException("Ya existe una categoría con el nombre '" + requestDTO.getNombre() + "' para esta empresa");
        }

        CategoriaTicket categoria = new CategoriaTicket();
        categoria.setNombre(requestDTO.getNombre());
        categoria.setDescripcion(requestDTO.getDescripcion());
        categoria.setEmpresa(empresa);
        categoria.setActiva(true);

        CategoriaTicket nuevaCategoria = categoriaTicketRepository.save(categoria);
        return mapToDTO(nuevaCategoria);
    }

    @Override
    public CategoriaResponseDTO actualizarCategoria(Long id, CategoriaRequestDTO requestDTO, Long empresaId) {
        CategoriaTicket categoriaExistente = categoriaTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría de ticket no encontrada con id: " + id));

        if (!categoriaExistente.getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("Categoría no pertenece a la empresa indicada");
        }

        if (!categoriaExistente.getNombre().equals(requestDTO.getNombre()) 
            && categoriaTicketRepository.existsByNombreAndEmpresaId(requestDTO.getNombre(), empresaId)) {
            throw new DuplicateResourceException("Ya existe otra categoría con el nombre '" + requestDTO.getNombre() + "' para esta empresa");
        }

        categoriaExistente.setNombre(requestDTO.getNombre());
        categoriaExistente.setDescripcion(requestDTO.getDescripcion());

        CategoriaTicket categoriaActualizada = categoriaTicketRepository.save(categoriaExistente);
        return mapToDTO(categoriaActualizada);
    }

    @Override
    public void eliminarCategoria(Long id, Long empresaId) {
        CategoriaTicket categoriaExistente = categoriaTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría de ticket no encontrada con id: " + id));

        if (!categoriaExistente.getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("Categoría no pertenece a la empresa indicada");
        }

        categoriaExistente.setActiva(false);
        categoriaTicketRepository.save(categoriaExistente);
    }
}
