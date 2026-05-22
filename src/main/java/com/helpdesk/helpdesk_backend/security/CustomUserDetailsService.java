package com.helpdesk.helpdesk_backend.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.repository.UsuarioRepository;

/**
 * Implementación de UserDetailsService de Spring Security.
 * Busca al usuario en la BD por email y construye el objeto UserDetails
 * con su contraseña encriptada y su rol como authority.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Carga un usuario por su email (username en Spring Security).
     * Spring Security agrega automáticamente el prefijo ROLE_ a las authorities.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmailWithRolYPermisos(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("El usuario está desactivado: " + email);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre()));
        if (usuario.getRol().getPermisos() != null) {
            usuario.getRol().getPermisos().stream()
                    .filter(p -> p.isActivo())
                    .forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getNombre())));
        }

        return new User(
                usuario.getEmail(),
                usuario.getPassword(),
                authorities
        );
    }
}
