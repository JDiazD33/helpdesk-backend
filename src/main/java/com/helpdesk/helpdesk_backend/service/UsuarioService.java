package com.helpdesk.helpdesk_backend.service;

import java.util.List;
import java.util.Optional;

import com.helpdesk.helpdesk_backend.model.Usuario;

public interface UsuarioService {

    //Buscar todos los usuarios
    List<Usuario> listarTodos();

    //Buscar usuario por id, usa optional para evitar errores de inexistencia
    Optional<Usuario> buscarPorId(Long id);

    //Crear nuevo usuario
    Usuario guardar(Usuario usuario);

    //Actualizar usuario existente
    Usuario actualizar(Long id, Usuario usuario);

    //Desactivar usuario (borrado lógico)
    void eliminar(Long id);

    // Filtros
    //Busca usuario por email, usa optional para evitar errores de inexistencia
    Optional<Usuario> buscarPorEmail(String email);
    
    //Verifica si existe usuario por email, para evitar duplicados
    boolean existeEmail(String email);

    //Lista usuarios por empresa
    List<Usuario> listarPorEmpresa(Long empresaId);

    //Lista usuarios por rol
    List<Usuario> listarPorRol (Long rolId);

    //Lista usuarios activos por empresa
    List<Usuario> listarActivosPorEmpresa(Long empresaId, boolean activo);

    //Lista usuarios por empresa y rol
    List<Usuario> listarPorEmpresaYRol(Long empresaId, Long rolId);

    //Lista usuarios por estado activo/inactivo
    List<Usuario> listarPorEstado(boolean activo);

    // ─── Métodos con queries JPQL personalizadas ───

    // Usuarios activos de una empresa con datos precargados
    List<Usuario> listarActivosPorEmpresaConDetalles(Long empresaId);

    // Buscar usuarios por nombre o apellido
    List<Usuario> buscarPorNombreOApellido(Long empresaId, String termino);
}
