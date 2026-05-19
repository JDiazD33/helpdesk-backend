package com.helpdesk.helpdesk_backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.helpdesk_backend.dto.EmpresaRequestDTO;
import com.helpdesk.helpdesk_backend.dto.EmpresaResponseDTO;
import com.helpdesk.helpdesk_backend.mapper.EmpresaMapper;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.repository.EmpresaRepository;
import com.helpdesk.helpdesk_backend.service.EmpresaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmpresaServiceImpl implements EmpresaService{

    private final EmpresaRepository empresaRepository;
    private final EmpresaMapper empresaMapper;

    @Override
    @Transactional
    public EmpresaResponseDTO crearEmpresa(EmpresaRequestDTO requestDTO) {
        // Validaciones de negocio: RUC y Correo únicos
        if (empresaRepository.findByRuc(requestDTO.getRuc()).isPresent()) {
            throw new RuntimeException("El RUC ya se encuentra registrado.");   
        }
        Empresa empresa = empresaMapper.toEntity(requestDTO);
        // El campo 'activo' se inicializa en true por el @Builder.Default
        // y la 'fechaCreacion' por @CreationTimestamp 
        Empresa empresaGuardada = empresaRepository.save(empresa);
        return empresaMapper.toResponseDTO(empresaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public EmpresaResponseDTO obtenerEmpresaPorId(Long id) {
        Empresa empresa = empresaRepository.findByIdAndActivoTrue(id)
        .orElseThrow(() -> new RuntimeException("Empresa no encontrada o inactiva"));
        return empresaMapper.toResponseDTO(empresa);
        
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpresaResponseDTO> listarEmpresasActivas() {
        return empresaRepository.findAll().stream().filter(Empresa::isActivo)
        .map(empresaMapper::toResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmpresaResponseDTO actualizarEmpresa(Long id, EmpresaRequestDTO requestDTO) {
        Empresa empresaExistente = empresaRepository.findByIdAndActivoTrue(id)
        .orElseThrow(()->new RuntimeException("Empresa no encontrada o inactiva"));
        empresaExistente.setNombre(requestDTO.getNombre());
        empresaExistente.setRuc(requestDTO.getRuc());
        empresaExistente.setCorreoContacto(requestDTO.getCorreoContacto());
        empresaExistente.setTelefonoContacto(requestDTO.getTelefonoContacto());

        Empresa empresaActualizada = empresaRepository.save(empresaExistente);
        return empresaMapper.toResponseDTO(empresaActualizada);
    }

    @Override
    public void eliminarEmpresa(Long id) {
        Empresa empresa = empresaRepository.findByIdAndActivoTrue(id)
        .orElseThrow(()->new RuntimeException("Empresa no encontrada o inactiva"));
        // Aplicamos el borrado lógico en lugar de eliminar el registro
        empresa.setActivo(false);
        empresaRepository.save(empresa);
    }
}
