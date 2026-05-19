package com.helpdesk.helpdesk_backend.service;

import java.util.List;

import com.helpdesk.helpdesk_backend.dto.UsuarioRequestDTO;
import com.helpdesk.helpdesk_backend.dto.UsuarioResponseDTO;

public interface UsuarioService {
    UsuarioResponseDTO crearUsuario(UsuarioRequestDTO requestDTO, Long empresaIdContexto);

    UsuarioResponseDTO obtenerUsuarioPorId(Long id, Long empresaIdContexto);

    List<UsuarioResponseDTO> listarUsuariosPorEmpresa(Long empresaIdContexto);

    List<UsuarioResponseDTO> listarUsuariosPorRol(Long empresaIdContexto, String rolNombre);

    UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO requestDTO, Long empresaIdContexto);
    
    void eliminarUsuario(Long id, Long empresaIdContexto);
}

