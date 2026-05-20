package com.helpdesk.helpdesk_backend.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsignarPermisosRequestDTO {

    @NotEmpty(message = "Debe indicar al menos un permiso")
    private List<Long> permisoIds;
}
