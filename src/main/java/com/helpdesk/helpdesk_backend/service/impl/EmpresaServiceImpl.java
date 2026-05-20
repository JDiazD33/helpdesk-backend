package com.helpdesk.helpdesk_backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.helpdesk_backend.dto.EmpresaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.EmpresaResponseDTO;
import com.helpdesk.helpdesk_backend.exception.DuplicateResourceException;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.repository.EmpresaRepository;
import com.helpdesk.helpdesk_backend.service.EmpresaService;

@Service
@Transactional
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaServiceImpl(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    private EmpresaResponseDTO mapToDTO(Empresa empresa) {
        return EmpresaResponseDTO.builder()
                .id(empresa.getId())
                .nombre(empresa.getNombre())
                .ruc(empresa.getRuc())
                .correoContacto(empresa.getCorreoContacto())
                .telefonoContacto(empresa.getTelefonoContacto())
                .activo(empresa.isActivo())
                .fechaCreacion(empresa.getFechaCreacion())
                .build();
    }

    @Override
    public EmpresaResponseDTO crearEmpresa(EmpresaRequestDTO requestDTO) {
        if (empresaRepository.existsByRuc(requestDTO.getRuc())) {
            throw new DuplicateResourceException("Ya existe una empresa con el RUC: " + requestDTO.getRuc());
        }
        if (empresaRepository.existsByCorreoContacto(requestDTO.getCorreoContacto())) {
            throw new DuplicateResourceException("Ya existe una empresa con el correo: " + requestDTO.getCorreoContacto());
        }

        Empresa empresa = new Empresa();
        empresa.setNombre(requestDTO.getNombre());
        empresa.setRuc(requestDTO.getRuc());
        empresa.setCorreoContacto(requestDTO.getCorreoContacto());
        empresa.setTelefonoContacto(requestDTO.getTelefonoContacto());
        empresa.setActivo(true);

        Empresa nuevaEmpresa = empresaRepository.save(empresa);
        return mapToDTO(nuevaEmpresa);
    }

    @Override
    @Transactional(readOnly = true)
    public EmpresaResponseDTO obtenerEmpresaPorId(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con id: " + id));
        return mapToDTO(empresa);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpresaResponseDTO> listarEmpresasActivas() {
        return empresaRepository.findAll().stream()
                .filter(Empresa::isActivo)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EmpresaResponseDTO actualizarEmpresa(Long id, EmpresaRequestDTO requestDTO) {
        Empresa empresaExistente = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con id: " + id));

        if (!empresaExistente.getRuc().equals(requestDTO.getRuc()) && empresaRepository.existsByRuc(requestDTO.getRuc())) {
            throw new DuplicateResourceException("Ya existe otra empresa con el RUC: " + requestDTO.getRuc());
        }
        if (!empresaExistente.getCorreoContacto().equals(requestDTO.getCorreoContacto()) && empresaRepository.existsByCorreoContacto(requestDTO.getCorreoContacto())) {
            throw new DuplicateResourceException("Ya existe otra empresa con el correo: " + requestDTO.getCorreoContacto());
        }

        empresaExistente.setNombre(requestDTO.getNombre());
        empresaExistente.setRuc(requestDTO.getRuc());
        empresaExistente.setCorreoContacto(requestDTO.getCorreoContacto());
        empresaExistente.setTelefonoContacto(requestDTO.getTelefonoContacto());

        Empresa empresaActualizada = empresaRepository.save(empresaExistente);
        return mapToDTO(empresaActualizada);
    }

    @Override
    public void eliminarEmpresa(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con id: " + id));
        empresa.setActivo(false);
        empresaRepository.save(empresa);
    }
}
