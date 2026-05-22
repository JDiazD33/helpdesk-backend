package com.helpdesk.helpdesk_backend.service;

import java.util.List;
import java.util.Optional;

import com.helpdesk.helpdesk_backend.model.Permiso;

public interface PermisoService {

    List<Permiso> listarTodos();

    List<Permiso> listarActivos();

    Optional<Permiso> buscarPorId(Long id);

    Permiso guardar(Permiso permiso);

    Permiso actualizar(Long id, Permiso permiso);

    void eliminar(Long id);
}
