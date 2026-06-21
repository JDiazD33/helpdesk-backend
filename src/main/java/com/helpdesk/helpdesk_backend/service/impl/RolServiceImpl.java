package com.helpdesk.helpdesk_backend.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import com.helpdesk.helpdesk_backend.constants.RolConstants;
import com.helpdesk.helpdesk_backend.exception.DuplicateResourceException;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.model.Permiso;
import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.repository.PermisoRepository;
import com.helpdesk.helpdesk_backend.repository.RolRepository;
import com.helpdesk.helpdesk_backend.service.RolService;

@Service
@Transactional
public class RolServiceImpl implements RolService{

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    public RolServiceImpl(RolRepository rolRepository, PermisoRepository permisoRepository) {
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
    }

    /**
     * Lista todos los roles.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Rol> listarTodos() {
        return rolRepository.findAll();
    }

    /**
     * Busca un rol por su ID.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Rol> buscarPorId(Long id) {
        return rolRepository.findById(id);
    }

    /**
     * Guarda un nuevo rol verificando nombre duplicado.
     * Bloquea la creación de un rol con un nombre protegido del sistema
     * (ADMIN_OWNER, ADMIN_EMPRESA) para evitar escalada de privilegios.
     */
    @Override
    public Rol guardar(Rol rol) {
        validarNombreNoProtegido(rol.getNombre());
        if (rolRepository.existsByNombre(rol.getNombre())) {
            throw new DuplicateResourceException("Ya existe un rol con el nombre: " + rol.getNombre());
        }
        return rolRepository.save(rol);
    }

    /**
     * Actualiza un rol existente.
     * Bloquea la modificación (incluso el renombrado) de roles protegidos.
     */
    @Override
    public Rol actualizar(Long id, Rol rol) {
        Rol rolExistente = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + id));

        if (esRolProtegido(rolExistente)) {
            throw new IllegalArgumentException(
                    "El rol del sistema '" + rolExistente.getNombre() + "' no puede ser modificado");
        }
        validarNombreNoProtegido(rol.getNombre());

        if (!rolExistente.getNombre().equals(rol.getNombre()) && rolRepository.existsByNombre(rol.getNombre())) {
            throw new DuplicateResourceException("Ya existe otro rol con el nombre: " + rol.getNombre());
        }

        rolExistente.setNombre(rol.getNombre());
        rolExistente.setActivo(rol.isActivo());
        return rolRepository.save(rolExistente);
    }

    /**
     * Borrado lógico: desactiva el rol sin eliminarlo de la BD.
     * Bloquea la desactivación de roles protegidos.
     */
    @Override
    public void eliminar(Long id) {
        Rol rol = rolRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + id));
        if (esRolProtegido(rol)) {
            throw new IllegalArgumentException(
                    "El rol del sistema '" + rol.getNombre() + "' no puede ser desactivado");
        }
        rol.setActivo(false);
        rolRepository.save(rol);
    }

    /**
     * Busca un rol por su nombre.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Rol> buscarPorNombre(String nombre) {
        return rolRepository.findByNombre(nombre);
    }

    /**
     * Verifica si existe un rol por su nombre.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existePorNombre(String nombre) {
        return rolRepository.existsByNombre(nombre);
    }

    /**
     * Asigna permisos a un rol, reemplazando los anteriores.
     * Bloquea la asignación sobre roles protegidos.
     */
    @Override
    public Rol asignarPermisos(Long rolId, List<Long> permisoIds) {
        Rol rol = rolRepository.findByIdWithPermisos(rolId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + rolId));

        if (esRolProtegido(rol)) {
            throw new IllegalArgumentException(
                    "Los permisos del rol del sistema '" + rol.getNombre() + "' no pueden ser modificados");
        }

        Set<Permiso> permisos = new HashSet<>();
        for (Long permisoId : permisoIds) {
            Permiso permiso = permisoRepository.findById(permisoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con id: " + permisoId));
            permisos.add(permiso);
        }
        rol.setPermisos(permisos);
        return rolRepository.save(rol);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Rol> buscarPorIdConPermisos(Long id) {
        return rolRepository.findByIdWithPermisos(id);
    }

    private boolean esRolProtegido(Rol rol) {
        return rol != null && RolConstants.ROLES_PROTEGIDOS.contains(rol.getNombre());
    }

    private void validarNombreNoProtegido(String nombre) {
        if (nombre != null && RolConstants.ROLES_PROTEGIDOS.contains(nombre)) {
            throw new IllegalArgumentException(
                    "No se permite crear ni renombrar un rol con el nombre protegido '" + nombre + "'");
        }
    }
}
