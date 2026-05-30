package com.helpdesk.helpdesk_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpdesk.helpdesk_backend.dto.LoginRequest;
import com.helpdesk.helpdesk_backend.dto.RegisterRequest;
import com.helpdesk.helpdesk_backend.model.Empresa;
import com.helpdesk.helpdesk_backend.model.Rol;
import com.helpdesk.helpdesk_backend.model.Usuario;
import com.helpdesk.helpdesk_backend.repository.EmpresaRepository;
import com.helpdesk.helpdesk_backend.repository.RolRepository;
import com.helpdesk.helpdesk_backend.repository.UsuarioRepository;
import com.helpdesk.helpdesk_backend.security.CustomUserDetailsService;
import com.helpdesk.helpdesk_backend.security.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        // Mock dependencias
        @MockBean
        private AuthenticationManager authenticationManager;

        @MockBean
        private UsuarioRepository usuarioRepository;

        @MockBean
        private RolRepository rolRepository;

        @MockBean
        private EmpresaRepository empresaRepository;

        @MockBean
        private PasswordEncoder passwordEncoder;

        @MockBean
        private JwtUtil jwtUtil;

        @MockBean
        private CustomUserDetailsService customUserDetailsService;

        @Autowired
        private ObjectMapper objectMapper;

        private Usuario usuario;
        private Empresa empresa;
        private Rol rol;

        @BeforeEach
        void setUp() {

                // Empresa simulada
                empresa = Empresa.builder()
                                .id(1L)
                                .nombre("Tech Solutions")
                                .build();

                // Rol simulado
                rol = Rol.builder()
                                .id(1L)
                                .nombre("CLIENTE")
                                .build();

                // Usuario simulado
                usuario = Usuario.builder()
                                .id(1L)
                                .nombres("Juan")
                                .apellidos("Perez")
                                .email("juan@test.com")
                                .password("123")
                                .empresa(empresa)
                                .rol(rol)
                                .activo(true)
                                .build();
        }

        @Test
        void login_debeRetornarToken() throws Exception {

                LoginRequest request = new LoginRequest();
                request.setEmail("juan@test.com");
                request.setPassword("123456");

                Mockito.when(authenticationManager.authenticate(any()))
                                .thenReturn(null);

                Mockito.when(usuarioRepository.findByEmail("juan@test.com"))
                                .thenReturn(Optional.of(usuario));

                Mockito.when(jwtUtil.generateToken(anyString(), anyString(), anyLong(), anyLong()))
                                .thenReturn("token-prueba");

                Mockito.when(jwtUtil.getExpirationSeconds()).thenReturn(3600L);

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("token-prueba"))
                                .andExpect(jsonPath("$.email").value("juan@test.com"))
                                .andExpect(jsonPath("$.rol").value("CLIENTE"));
        }

        @Test
        void login_usuarioNoExiste_debeRetornar404() throws Exception {

                LoginRequest request = new LoginRequest();
                request.setEmail("ghost@test.com");
                request.setPassword("123456"); // ← CORREGIDO (mínimo @NotBlank, era "123" que sí pasa pero era
                                               // inconsistente)

                Mockito.when(authenticationManager.authenticate(any()))
                                .thenReturn(null);

                Mockito.when(usuarioRepository.findByEmail(anyString()))
                                .thenReturn(Optional.empty());

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void register_empresaNoExiste() throws Exception {

                RegisterRequest request = new RegisterRequest();
                request.setNombres("Juan"); // ← CORREGIDO
                request.setApellidos("Perez"); // ← CORREGIDO
                request.setEmail("nuevo@test.com");
                request.setPassword("123456"); // ← CORREGIDO (mínimo 6 chars)
                request.setEmpresaId(999L);

                Mockito.when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
                Mockito.when(empresaRepository.findById(999L)).thenReturn(Optional.empty());

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void register_rolNoExiste() throws Exception {

                RegisterRequest request = new RegisterRequest();
                request.setNombres("Juan"); // ← CORREGIDO
                request.setApellidos("Perez"); // ← CORREGIDO
                request.setEmail("nuevo@test.com");
                request.setPassword("123456"); // ← CORREGIDO
                request.setEmpresaId(1L);

                Mockito.when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
                Mockito.when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
                Mockito.when(rolRepository.findByNombre("CLIENTE")).thenReturn(Optional.empty());

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void register_emailDuplicado_debeRetornarConflict() throws Exception {

                RegisterRequest request = new RegisterRequest();
                request.setNombres("Juan"); // ← CORREGIDO
                request.setApellidos("Perez"); // ← CORREGIDO
                request.setEmail("repetido@test.com");
                request.setPassword("123456"); // ← CORREGIDO
                request.setEmpresaId(1L); // ← CORREGIDO

                Mockito.when(usuarioRepository.existsByEmail("repetido@test.com")).thenReturn(true);

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict());
        }

        @Test
        void register_debeCrearUsuario() throws Exception {

                RegisterRequest request = new RegisterRequest();
                request.setNombres("Juan");
                request.setApellidos("Perez");
                request.setEmail("nuevo@test.com");
                request.setPassword("123456"); // ← CORREGIDO (era "123", menos de 6 chars)
                request.setTelefono("999999999");
                request.setEmpresaId(1L);

                Mockito.when(usuarioRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
                Mockito.when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
                Mockito.when(rolRepository.findByNombre("CLIENTE")).thenReturn(Optional.of(rol));
                Mockito.when(passwordEncoder.encode("123456")).thenReturn("123-encriptado"); // ← CORREGIDO
                // save() asigna el ID al objeto original (simula comportamiento real de JPA)
                Mockito.doAnswer(invocation -> {
                    Usuario u = invocation.getArgument(0);
                    if (u.getId() == null) u.setId(1L);
                    return u;
                }).when(usuarioRepository).save(any(Usuario.class));
                Mockito.when(jwtUtil.generateToken(anyString(), anyString(), anyLong(), anyLong())).thenReturn("jwt-register");
                Mockito.when(jwtUtil.getExpirationSeconds()).thenReturn(3600L);

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.token").value("jwt-register"))
                                .andExpect(jsonPath("$.email").value("nuevo@test.com"));

                Mockito.verify(usuarioRepository).save(any(Usuario.class));
        }

        @Test
        void login_conCredencialesIncorrectas_debeRetornar401()
                        throws Exception {

                LoginRequest request = new LoginRequest();

                request.setEmail("error@test.com");
                request.setPassword("incorrecto");

                // Simular excepción
                Mockito.doThrow(
                                new BadCredentialsException(
                                                "Credenciales inválidas"))
                                .when(authenticationManager)
                                .authenticate(any());

                mockMvc.perform(

                                post("/api/auth/login")

                                                .contentType(
                                                                MediaType.APPLICATION_JSON)

                                                .content(
                                                                objectMapper.writeValueAsString(
                                                                                request)))

                                .andExpect(status().isUnauthorized())

                                .andExpect(
                                                jsonPath("$.message")
                                                                .value("Email o contraseña incorrectos"));
        }

        @Test
        void logout_debeRetornarMensaje()
                        throws Exception {

                mockMvc.perform(
                                post("/api/auth/logout"))

                                .andExpect(status().isOk())

                                .andExpect(
                                                jsonPath("$.mensaje")
                                                                .value(
                                                                                "Sesion cerrada correctamente"));
        }

}