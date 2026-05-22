package com.helpdesk.helpdesk_backend.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.helpdesk_backend.exception.DuplicateResourceException;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.repository.UsuarioRepository;
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
     * Lista todos los usuarios.
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
     * Actualiza un usuario existente.
     * Solo encripta la contraseña si se envía una nueva.
     */
    @Override
    public Usuario actualizar(Long id, Usuario usuario) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        if (!usuarioExistente.getEmail().equals(usuario.getEmail()) && usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new DuplicateResourceException("Ya existe otro usuario con el email: " + usuario.getEmail());
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
        usuarioExistente.setEmpresa(usuario.getEmpresa());
        usuarioExistente.setRol(usuario.getRol());

        return usuarioRepository.save(usuarioExistente);
    }

    /**
     * Borrado lógico: desactiva el usuario sin eliminarlo de la BD.
     */
    @Override
    public void eliminar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);
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
     * Lista los usuarios que pertenecen a una empresa.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarPorEmpresa(Long empresaId) {
        return usuarioRepository.findByEmpresaId(empresaId);
    }

    /**
     * Lista todos los usuarios de un cierto rol.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarPorRol(Long rolId) {
        return usuarioRepository.findByRolId(rolId);
    }

    /**
     * Lista los usuarios activos de una empresa especifica.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarActivosPorEmpresa(Long empresaId, boolean activo) {
        return usuarioRepository.findByEmpresaIdAndActivo(empresaId, activo);
    }

    /**
     * Lista usuarios de una empresa filtrados por un rol especifico.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarPorEmpresaYRol(Long empresaId, Long rolId) {
        return usuarioRepository.findByEmpresaIdAndRolId(empresaId, rolId);
    }

    /**
     * Lista usuarios por un estado especifico (activos o inactivos).
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarPorEstado(boolean activo) {
        return usuarioRepository.findByActivo(activo);
    }

    /**
     * Usuarios activos con datos de rol y empresa precargados (JPQL JOIN FETCH).
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarActivosPorEmpresaConDetalles(Long empresaId) {
        return usuarioRepository.findActivosPorEmpresaConDetalles(empresaId);
    }

    /**
     * Busca usuarios por nombre o apellido parcial (JPQL LIKE).
     */
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> buscarPorNombreOApellido(Long empresaId, String termino) {
        return usuarioRepository.buscarPorNombreOApellido(empresaId, termino);
    }
}
