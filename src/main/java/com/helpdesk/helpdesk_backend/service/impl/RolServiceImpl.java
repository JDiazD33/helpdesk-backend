package com.helpdesk.helpdesk_backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.helpdesk_backend.dto.RolResponseDTO;
import com.helpdesk.helpdesk_backend.mapper.RolMapper;
import com.helpdesk.helpdesk_backend.repository.RolRepository;
import com.helpdesk.helpdesk_backend.service.RolService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService{

    private final RolRepository rolRepository;
    private final RolMapper rolMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RolResponseDTO> obtenerTodosLosRoles() {
        return rolRepository.findAll().stream().map(rolMapper::toResponseDTO).collect(Collectors.toList());
    }
    
}
