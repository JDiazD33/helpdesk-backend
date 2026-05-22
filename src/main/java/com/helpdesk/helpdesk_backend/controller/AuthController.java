package com.helpdesk.helpdesk_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.helpdesk.helpdesk_backend.dto.AuthResponse;
import com.helpdesk.helpdesk_backend.dto.LoginRequest;
import com.helpdesk.helpdesk_backend.dto.RegisterRequest;
import com.helpdesk.helpdesk_backend.exception.DuplicateResourceException;
import com.helpdesk.helpdesk_backend.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.repository.EmpresaRepository;
import com.helpdesk.helpdesk_backend.repository.RolRepository;
import com.helpdesk.helpdesk_backend.repository.UsuarioRepository;
import com.helpdesk.helpdesk_backend.security.JwtUtil;

import jakarta.validation.Valid;

/**
 * Controlador de autenticación.
 * Endpoints públicos para login y registro de usuarios CLIENTE.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager,
                          UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          EmpresaRepository empresaRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * POST /api/auth/login
     * Autentica al usuario con email y contraseña.
     * Devuelve un token JWT con los datos básicos del usuario.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Spring Security valida email + contraseña contra la BD
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of(
                        "status", 401,
                        "error", "Unauthorized",
                        "message", "Email o contraseña incorrectos"
                    ));
        }

        // Si la autenticación fue exitosa, obtener datos del usuario
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Generar token JWT
        String token = jwtUtil.generateToken(
                usuario.getEmail(),
                usuario.getRol().getNombre(),
                usuario.getEmpresa().getId()
        );

        // Construir respuesta con token + datos básicos
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .expiresIn(jwtUtil.getExpirationSeconds())
                .email(usuario.getEmail())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .rol(usuario.getRol().getNombre())
                .empresaId(usuario.getEmpresa().getId())
                .nombreEmpresa(usuario.getEmpresa().getNombre())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/register
     * Registro público: crea un usuario con rol CLIENTE por defecto.
     * Los usuarios AGENTE solo pueden ser creados por un ADMIN_EMPRESA.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        // Validar email no duplicado
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Ya existe un usuario con el email: " + request.getEmail());
        }

        // Obtener la empresa
        Empresa empresa = empresaRepository.findById(request.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada con id: " + request.getEmpresaId()));

        // Obtener el rol CLIENTE (siempre se asigna CLIENTE en registro público)
        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new ResourceNotFoundException("Rol CLIENTE no encontrado. Asegúrate de tener los roles iniciales en la BD."));

        // Crear el usuario
        Usuario nuevoUsuario = Usuario.builder()
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .empresa(empresa)
                .rol(rolCliente)
                .activo(true)
                .build();

        usuarioRepository.save(nuevoUsuario);

        // Generar token JWT para el usuario recién registrado
        String token = jwtUtil.generateToken(
                nuevoUsuario.getEmail(),
                rolCliente.getNombre(),
                empresa.getId()
        );

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .expiresIn(jwtUtil.getExpirationSeconds())
                .email(nuevoUsuario.getEmail())
                .nombres(nuevoUsuario.getNombres())
                .apellidos(nuevoUsuario.getApellidos())
                .rol(rolCliente.getNombre())
                .empresaId(empresa.getId())
                .nombreEmpresa(empresa.getNombre())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/logout
     * Con JWT stateless el cliente descarta el token; el servidor confirma la operación.
     */
    @PostMapping("/logout")
    public ResponseEntity<java.util.Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ResponseEntity.ok(java.util.Map.of("mensaje", "Sesion cerrada correctamente"));
    }
}
