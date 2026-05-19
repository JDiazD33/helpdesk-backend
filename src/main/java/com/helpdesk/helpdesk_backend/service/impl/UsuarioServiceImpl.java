package com.helpdesk.helpdesk_backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.helpdesk.helpdesk_backend.dto.UsuarioRequestDTO;
import com.helpdesk.helpdesk_backend.dto.UsuarioResponseDTO;
import com.helpdesk.helpdesk_backend.mapper.UsuarioMapper;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.repository.EmpresaRepository;
import com.helpdesk.helpdesk_backend.repository.RolRepository;
import com.helpdesk.helpdesk_backend.repository.UsuarioRepository;
import com.helpdesk.helpdesk_backend.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService{

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final RolRepository rolRepository;
    private final EmpresaRepository empresaRepository;

    @Override
    @Transactional
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO requestDTO, Long empresaIdContexto) {

        if (usuarioRepository.existsByEmail(requestDTO.getEmail())) {
            throw new RuntimeException("El correo electronico ya esta registrado en el sistema.");
        }

        Usuario usuario = usuarioMapper.toEntity(requestDTO);

        Rol rol = rolRepository.findById(requestDTO.getRolId())
            .orElseThrow(()->new RuntimeException("El rol especificado no existe."));
        usuario.setRol(rol);

        Empresa empresa = empresaRepository.findById(empresaIdContexto)
            .orElseThrow(()->new RuntimeException("Empresa no encontrada"));
        usuario.setEmpresa(empresa);

        // TODO: Encriptar la contraseña usando BCrypt antes de guardar. 
        // Como la seguridad (Spring Security + JWT) se implementará más adelante, 
        // por ahora la guardaremos tal cual viene en el request.
        usuario.setPassword(requestDTO.getPassword());

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return usuarioMapper.toResponseDTO(usuarioGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerUsuarioPorId(Long id, Long empresaIdContexto) {
        Usuario usuario = usuarioRepository.findByIdAndEmpresaId(id, empresaIdContexto)
        .orElseThrow(()->new RuntimeException("Usuario no encontrado o no pertenece a su empresa"));
        return usuarioMapper.toResponseDTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarUsuariosPorEmpresa(Long empresaIdContexto) {
        return usuarioRepository.findAllByEmpresaIdAndActivoTrue(empresaIdContexto).stream()
        .map(usuarioMapper::toResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarUsuariosPorRol(Long empresaIdContexto, String rolNombre) {
        return usuarioRepository.findAllByEmpresaIdAndRolNombreAndActivoTrue(empresaIdContexto, rolNombre).stream()
        .map(usuarioMapper::toResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO requestDTO, Long empresaIdContexto) {
        Usuario usuarioExistente = usuarioRepository.findByIdAndEmpresaId(id, empresaIdContexto)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado o no pertenece a su empresa"));
        
        // Actualizamos los datos basicos
        usuarioExistente.setNombres(requestDTO.getNombres());
        usuarioExistente.setApellidos(requestDTO.getApellidos());
        usuarioExistente.setTelefono(requestDTO.getTelefono());

        // Si el correo cambia, validar que no exista ya en la BD
        if (!usuarioExistente.getEmail().equals(requestDTO.getEmail()) && usuarioRepository.existsByEmail(requestDTO.getEmail())) {
            throw new RuntimeException("El nuevo correo ya se encuentra en uso.");
        }
        usuarioExistente.setEmail(requestDTO.getEmail());

        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
        return usuarioMapper.toResponseDTO(usuarioActualizado);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id, Long empresaIdContexto) {
        //Aseguramos que el usuario a eliminar pertenezca a la empresa que realiza la peticion
        Usuario usuario = usuarioRepository.findByIdAndEmpresaId(id, empresaIdContexto)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado o no pertenece a su empresa"));

        //Inhabilitamos la cuenta mediante borrado logico
        usuario.setActivo(false);
        usuarioRepository.save(usuario);   
    }
}
