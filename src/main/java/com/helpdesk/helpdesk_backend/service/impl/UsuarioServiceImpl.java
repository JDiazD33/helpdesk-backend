package com.helpdesk.helpdesk_backend.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.helpdesk_backend.constants.RolConstants;
import com.helpdesk.helpdesk_backend.dto.UsuarioRolDTO;
import com.helpdesk.helpdesk_backend.exception.DuplicateResourceException;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.repository.UsuarioRepository;
import com.helpdesk.helpdesk_backend.security.SecurityUtils;
import com.helpdesk.helpdesk_backend.service.UsuarioService;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService{

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Lista todos los usuarios (vista global del ADMIN_OWNER, no excluye al owner).
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Busca un usuario por su ID.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Busca un usuario validando que pertenezca a la empresa del tenant.
     * El ADMIN_OWNER puede consultar usuarios de cualquier empresa.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorIdAndEmpresa(Long id, Long empresaId) {
        if (SecurityUtils.isAdminOwner()) {
            return usuarioRepository.findById(id);
        }
        return usuarioRepository.findByIdAndEmpresaId(id, empresaId);
    }

    /**
     * Guarda un nuevo usuario validando existencia de email.
     * Encripta la contraseña con BCrypt antes de persistir.
     */
    @Override
    public Usuario guardar(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new DuplicateResourceException("Ya existe un usuario con el email: " + usuario.getEmail());
        }
        // Encriptar contraseña con BCrypt
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    /**
     * Actualiza un usuario existente, restringido al tenant del JWT.
     * - NO permite cambiar la empresa del usuario (se conserva la del registro original).
     * - NO permite asignar el rol ADMIN_OWNER (rol reservado del sistema).
     * Solo encripta la contraseña si se envía una nueva.
     */
    @Override
    public Usuario actualizar(Long id, Long empresaId, Usuario usuario) {
        Usuario usuarioExistente = obtenerUsuarioDeTenant(id, empresaId);

        if (!usuarioExistente.getEmail().equals(usuario.getEmail()) && usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new DuplicateResourceException("Ya existe otro usuario con el email: " + usuario.getEmail());
        }

        // Bloquear escalación: nadie puede asignar el rol ADMIN_OWNER por esta vía.
        if (usuario.getRol() != null
                && RolConstants.ADMIN_OWNER.equals(usuario.getRol().getNombre())) {
            throw new AccessDeniedException("No está permitido asignar el rol " + RolConstants.ADMIN_OWNER);
        }

        usuarioExistente.setNombres(usuario.getNombres());
        usuarioExistente.setApellidos(usuario.getApellidos());
        usuarioExistente.setEmail(usuario.getEmail());
        // Solo encriptar si se envía una contraseña nueva
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            usuarioExistente.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        usuarioExistente.setTelefono(usuario.getTelefono());
        usuarioExistente.setActivo(usuario.isActivo());
        // La empresa es inmutable: se conserva la del registro original.
        usuarioExistente.setRol(usuario.getRol());

        return usuarioRepository.save(usuarioExistente);
    }

    /**
     * Borrado lógico: desactiva el usuario sin eliminarlo de la BD,
     * restringido a la empresa del tenant.
     */
    @Override
    public void eliminar(Long id, Long empresaId) {
        Usuario usuario = obtenerUsuarioDeTenant(id, empresaId);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    /**
     * Carga un usuario validando que pertenezca a la empresa del tenant.
     * El ADMIN_OWNER puede operar sobre usuarios de cualquier empresa.
     * Devuelve 404 (no 403) para no filtrar la existencia del recurso entre tenants.
     */
    private Usuario obtenerUsuarioDeTenant(Long id, Long empresaId) {
        if (SecurityUtils.isAdminOwner()) {
            return usuarioRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        }
        return usuarioRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    /**
     * Busca un usuario usando su email de manera opcional.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    /**
     * Verifica la existencia de un email.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    /**
     * Lista los usuarios que pertenecen a una empresa, EXCLUYENDO al ADMIN_OWNER.
     * El owner del SaaS está asociado artificialmente a una empresa por la
     * restricción NOT NULL pero NO pertenece lógicamente a ningún tenant.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarPorEmpresa(Long empresaId) {
        return usuarioRepository.findByEmpresaIdExcluyendoOwner(
                empresaId, RolConstants.ADMIN_OWNER);
    }

    /**
     * Lista todos los usuarios de un cierto rol (sin filtro de tenant).
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarPorRol(Long rolId) {
        return usuarioRepository.findByRolId(rolId);
    }

    /**
     * Lista los usuarios activos de una empresa, EXCLUYENDO al ADMIN_OWNER.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarActivosPorEmpresa(Long empresaId, boolean activo) {
        return usuarioRepository.findByEmpresaIdAndActivoExcluyendoOwner(
                empresaId, activo, RolConstants.ADMIN_OWNER);
    }

    /**
     * Lista usuarios de una empresa filtrados por rol, EXCLUYENDO al ADMIN_OWNER.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarPorEmpresaYRol(Long empresaId, Long rolId) {
        return usuarioRepository.findByEmpresaIdAndRolIdExcluyendoOwner(
                empresaId, rolId, RolConstants.ADMIN_OWNER);
    }

    /**
     * Lista usuarios por estado (activos/inactivos, sin filtro de tenant).
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarPorEstado(boolean activo) {
        return usuarioRepository.findByActivo(activo);
    }

    /**
     * Usuarios activos con datos de rol y empresa precargados, EXCLUYENDO al ADMIN_OWNER.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarActivosPorEmpresaConDetalles(Long empresaId) {
        return usuarioRepository.findActivosPorEmpresaConDetallesExcluyendoOwner(
                empresaId, RolConstants.ADMIN_OWNER);
    }

    /**
     * Busca usuarios por nombre o apellido parcial, EXCLUYENDO al ADMIN_OWNER.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> buscarPorNombreOApellido(Long empresaId, String termino) {
        return usuarioRepository.buscarPorNombreOApellidoExcluyendoOwner(
                empresaId, termino, RolConstants.ADMIN_OWNER);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioRolDTO> contarUsuariosPorRol(Long empresaId) {
        if (empresaId != null) {
            // Contexto de tenant: excluir al ADMIN_OWNER
            return usuarioRepository.contarUsuariosPorRolEmpresaExcluyendoOwner(
                    empresaId, RolConstants.ADMIN_OWNER);
        }
        // Vista global del owner: contar todos, incluyendo al owner
        return usuarioRepository.contarUsuariosPorRol();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarAgentesActivos(Long empresaId) {
        return usuarioRepository.listarAgentesActivosExcluyendoOwner(
                empresaId, RolConstants.ADMIN_OWNER);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarConFiltros(Long empresaId, String search, List<Long> rolIds) {
        if (search != null && search.trim().isEmpty()) search = null;
        if (rolIds != null && rolIds.isEmpty()) rolIds = null;
        if (search == null && rolIds == null) {
            if (empresaId == null) return listarTodos();
            // Pasa por listarPorEmpresa que ya excluye al owner
            return listarPorEmpresa(empresaId);
        }
        // Filtro combinado: solo excluye al owner cuando hay empresaId
        return usuarioRepository.buscarConFiltrosExcluyendoOwner(
                empresaId, search, rolIds, RolConstants.ADMIN_OWNER);
    }
}
