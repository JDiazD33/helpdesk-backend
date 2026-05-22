package com.helpdesk.helpdesk_backend.service;

import java.util.List;
import java.util.Optional;

import com.helpdesk.helpdesk_backend.model.Rol;

public interface RolService {

    //Devuelve roles de la bd
    List<Rol> listarTodos();

    //Busca rol por id, usa optional para evitar errores de inexistencia
    Optional<Rol> buscarPorId(Long id);

    //Crear nuevo rol
    Rol guardar(Rol rol);

    //Actualizar rol existente
    Rol actualizar(Long id, Rol rol);

    //Desactivar rol (borrado lógico)
    void eliminar(Long id);

    // Filtros
    //Busca rol por nombre, usa optional para evitar errores de inexistencia
    Optional<Rol> buscarPorNombre(String nombre);

    //Verifica si existe rol por nombre, para evitar duplicados
    boolean existePorNombre(String nombre);

    Rol asignarPermisos(Long rolId, List<Long> permisoIds);

    Optional<Rol> buscarPorIdConPermisos(Long id);
}
