package com.helpdesk.helpdesk_backend.service;

import java.util.List;
import java.util.Optional;
import com.helpdesk.helpdesk_backend.dto.UsuarioRolDTO;
import com.helpdesk.helpdesk_backend.model.Usuario;

public interface UsuarioService {
    List<Usuario> listarTodos();
    Optional<Usuario> buscarPorId(Long id);
    // Busca usuario validando que pertenezca a la empresa del tenant (multi-tenant safe)
    Optional<Usuario> buscarPorIdAndEmpresa(Long id, Long empresaId);
    Usuario guardar(Usuario usuario);
    // Actualizar restringido a la empresa del tenant; bloquea escalación a ADMIN_OWNER.
    Usuario actualizar(Long id, Long empresaId, Usuario usuario);
    // Eliminar (borrado lógico) restringido a la empresa del tenant.
    void eliminar(Long id, Long empresaId);
    Optional<Usuario> buscarPorEmail(String email);
    boolean existeEmail(String email);
    List<Usuario> listarPorEmpresa(Long empresaId);
    List<Usuario> listarPorRol(Long rolId);
    List<Usuario> listarActivosPorEmpresa(Long empresaId, boolean activo);
    List<Usuario> listarPorEmpresaYRol(Long empresaId, Long rolId);
    List<Usuario> listarPorEstado(boolean activo);
    List<Usuario> listarActivosPorEmpresaConDetalles(Long empresaId);
    List<Usuario> buscarPorNombreOApellido(Long empresaId, String termino);
    List<UsuarioRolDTO> contarUsuariosPorRol(Long empresaId);
    List<Usuario> listarAgentesActivos(Long empresaId);
    List<Usuario> listarConFiltros(Long empresaId, String search, List<Long> rolIds);
}
