package com.helpdesk.helpdesk_backend.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.helpdesk_backend.exception.DuplicateResourceException;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.model.Permiso;
import com.helpdesk.helpdesk_backend.repository.PermisoRepository;
import com.helpdesk.helpdesk_backend.service.PermisoService;

@Service
@Transactional
public class PermisoServiceImpl implements PermisoService {

    private final PermisoRepository permisoRepository;

    public PermisoServiceImpl(PermisoRepository permisoRepository) {
        this.permisoRepository = permisoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permiso> listarTodos() {
        return permisoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permiso> listarActivos() {
        return permisoRepository.findAllActivosOrdenados();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Permiso> buscarPorId(Long id) {
        return permisoRepository.findById(id);
    }

    @Override
    public Permiso guardar(Permiso permiso) {
        if (permisoRepository.existsByNombre(permiso.getNombre())) {
            throw new DuplicateResourceException("Ya existe un permiso con el nombre: " + permiso.getNombre());
        }
        return permisoRepository.save(permiso);
    }

    @Override
    public Permiso actualizar(Long id, Permiso permiso) {
        Permiso existente = permisoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con id: " + id));

        if (!existente.getNombre().equals(permiso.getNombre())
                && permisoRepository.existsByNombre(permiso.getNombre())) {
            throw new DuplicateResourceException("Ya existe otro permiso con el nombre: " + permiso.getNombre());
        }

        existente.setNombre(permiso.getNombre());
        existente.setDescripcion(permiso.getDescripcion());
        existente.setActivo(permiso.isActivo());
        return permisoRepository.save(existente);
    }

    @Override
    public void eliminar(Long id) {
        Permiso permiso = permisoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con id: " + id));
        permiso.setActivo(false);
        permisoRepository.save(permiso);
    }
}
