package com.helpdesk.helpdesk_backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.helpdesk_backend.dto.CategoriaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.CategoriaResponseDTO;
import com.helpdesk.helpdesk_backend.mapper.CategoriaTicketMapper;
import com.helpdesk.helpdesk_backend.model.CategoriaTicket;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.repository.CategoriaTicketRepository;
import com.helpdesk.helpdesk_backend.repository.EmpresaRepository;
import com.helpdesk.helpdesk_backend.service.CategoriaTicketService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class CategoriaTicketServiceImpl implements CategoriaTicketService {

    private final CategoriaTicketRepository categoriaRepository;
    private final EmpresaRepository empresaRepository;
    private final CategoriaTicketMapper categoriaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarCategoriasActivas(Long empresaId) {
        return categoriaRepository.findAllByEmpresaIdAndActivaTrue(empresaId).stream()
                .map(categoriaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarTodas(Long empresaId) {
        return categoriaRepository.findAllByEmpresaId(empresaId).stream()
                .map(categoriaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoriaResponseDTO crearCategoria(CategoriaRequestDTO requestDTO, Long empresaId) {
        if (categoriaRepository.existsByNombreAndEmpresaId(requestDTO.getNombre(), empresaId)) {
            throw new RuntimeException("Ya existe una categoría con este nombre en la empresa");
        }
        Empresa empresa = empresaRepository.findById(empresaId).orElseThrow(() -> new RuntimeException("Empresa no encontrada."));
        CategoriaTicket categoria = categoriaMapper.toEntity(requestDTO);
        categoria.setEmpresa(empresa);
        categoria.setActiva(true);

        CategoriaTicket categoriaGuardada = categoriaRepository.save(categoria);
        return categoriaMapper.toResponseDTO(categoriaGuardada);
    }

    @Override
    public CategoriaResponseDTO actualizarCategoria(Long id, CategoriaRequestDTO requestDTO, Long empresaId) {
        CategoriaTicket categoria = categoriaRepository.findByIdAndEmpresaIdAndActivaTrue(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada o inactiva"));

        if (!categoria.getNombre().equalsIgnoreCase(requestDTO.getNombre()) && categoriaRepository.existsByNombreAndEmpresaId(requestDTO.getNombre(), empresaId)) {
            throw new RuntimeException("Ya existe otra categoria con este nombre en la empresa");            
        }

        categoriaMapper.updateEntityFromDTO(requestDTO, categoria);
        return categoriaMapper.toResponseDTO(categoriaRepository.save(categoria));
    }

    @Override
    public void eliminarCategoria(Long id, Long empresaId) {
        CategoriaTicket categoria = categoriaRepository.findByIdAndEmpresaIdAndActivaTrue(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada o ya está inactiva"));
        //Borrado Logico
        categoria.setActiva(false);
        categoriaRepository.save(categoria);
    }
}
