package com.helpdesk.helpdesk_backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.helpdesk_backend.dto.ProblemaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.ProblemaResponseDTO;
import com.helpdesk.helpdesk_backend.mapper.ProblemaTicketMapper;
import com.helpdesk.helpdesk_backend.model.CategoriaTicket;
import com.helpdesk.helpdesk_backend.model.ProblemaTicket;
import com.helpdesk.helpdesk_backend.repository.CategoriaTicketRepository;
import com.helpdesk.helpdesk_backend.repository.ProblemaTicketRepository;
import com.helpdesk.helpdesk_backend.service.ProblemaTicketService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProblemaTicketServiceImpl implements ProblemaTicketService {

    private final ProblemaTicketRepository problemaRepository;
    private final CategoriaTicketRepository categoriaRepository;
    private final ProblemaTicketMapper problemaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProblemaResponseDTO> listarProblemasPorCategoria(Long categoriaId, Long empresaId) {
        // Validamos que la categoría pertenezca a la empresa del usuario
        categoriaRepository.findByIdAndEmpresaIdAndActivaTrue(categoriaId, empresaId)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada, no activa o no pertenece a la empresa"));
        
        return problemaRepository.findAllByCategoriaIdAndActivoTrue(categoriaId).stream()
                .map(problemaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProblemaResponseDTO crearProblema(ProblemaRequestDTO requestDTO, Long empresaId) {
        // Buscamos la categoría real para asegurar que el objeto esté completo para el DTO
        CategoriaTicket categoria = categoriaRepository.findByIdAndEmpresaIdAndActivaTrue(requestDTO.getCategoriaId(), empresaId)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada, inactiva o no pertenece a la empresa"));
        
        if (problemaRepository.existsByNombreAndCategoriaId(requestDTO.getNombre(), requestDTO.getCategoriaId())) {
            throw new RuntimeException("El problema ya existe dentro de esta categoria");
        }

        ProblemaTicket problema = problemaMapper.toEntity(requestDTO);
        problema.setCategoria(categoria);
        problema.setActivo(true);

        ProblemaTicket problemaGuardado = problemaRepository.save(problema);
        return problemaMapper.toResponseDTO(problemaGuardado);
    }

    @Override
    @Transactional
    public ProblemaResponseDTO actualizarProblema(Long id, ProblemaRequestDTO requestDTO, Long empresaId) {
        ProblemaTicket problema = problemaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problema no encontrado."));

        if (!problema.isActivo()) {
            throw new RuntimeException("No se puede editar un problema inactivo");
        }

        // Buscamos la categoría real para prevenir IDOR y asegurar que esté completa
        CategoriaTicket nuevaCategoria = categoriaRepository.findByIdAndEmpresaIdAndActivaTrue(requestDTO.getCategoriaId(), empresaId)
                .orElseThrow(() -> new RuntimeException("La categoria asignada no existe o no pertenece a su empresa"));

        if (!problema.getNombre().equalsIgnoreCase(requestDTO.getNombre()) 
                && problemaRepository.existsByNombreAndCategoriaId(requestDTO.getNombre(), requestDTO.getCategoriaId())) {
            throw new RuntimeException("Ya existe un problema con ese nombre en la categoria seleccionada"); 
        }

        problemaMapper.updateEntityFromDTO(requestDTO, problema);
        problema.setCategoria(nuevaCategoria);

        return problemaMapper.toResponseDTO(problemaRepository.save(problema));
    }

    @Override
    @Transactional
    public void eliminarProblema(Long id, Long empresaId) {
        ProblemaTicket problema = problemaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problema no encontrado."));

        // Validamos la propiedad transitiva: el problema debe pertenecer a una categoría de la empresa autenticada
        if (!problema.getCategoria().getEmpresa().getId().equals(empresaId)) {
            throw new RuntimeException("No tiene permisos para eliminar este problema");
        }

        // Borrado lógico
        problema.setActivo(false);
        problemaRepository.save(problema);
    }

}
