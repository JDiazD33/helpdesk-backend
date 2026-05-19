package com.helpdesk.helpdesk_backend.service;

import java.util.List;

import com.helpdesk.helpdesk_backend.dto.RolResponseDTO;

public interface RolService {

    // Único método necesario para que el Front-End pueda listar los roles disponibles
    List<RolResponseDTO> obtenerTodosLosRoles();

}
