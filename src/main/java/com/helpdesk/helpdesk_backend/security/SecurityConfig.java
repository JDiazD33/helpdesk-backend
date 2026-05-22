package com.helpdesk.helpdesk_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración central de Spring Security.
 * - Autenticación stateless con JWT (sin sesiones)
 * - BCrypt para encriptar contraseñas
 * - CORS habilitado para el frontend
 * - Endpoints protegidos por roles (ADMIN_EMPRESA, AGENTE, CLIENTE)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Codificador de contraseñas BCrypt.
     * Se inyecta en UsuarioServiceImpl para encriptar las contraseñas al guardar.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager necesario para el proceso de login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configuración de CORS para permitir peticiones desde el frontend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173", "http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Cadena de filtros de seguridad.
     * Define qué endpoints son públicos y cuáles requieren autenticación/rol.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Habilitar CORS con la configuración definida arriba
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Deshabilitar CSRF (no necesario con JWT stateless)
            .csrf(csrf -> csrf.disable())
            // Sin sesiones: cada request se autentica con el token JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Reglas de autorización
            .authorizeHttpRequests(auth -> auth
                // ── Endpoints públicos (sin token) ──
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                // ── Empresas: solo ADMIN_EMPRESA ──
                .requestMatchers(HttpMethod.GET, "/api/empresas/**").hasRole("ADMIN_EMPRESA")
                .requestMatchers(HttpMethod.POST, "/api/empresas/**").hasRole("ADMIN_EMPRESA")
                .requestMatchers(HttpMethod.PUT, "/api/empresas/**").hasRole("ADMIN_EMPRESA")
                .requestMatchers(HttpMethod.DELETE, "/api/empresas/**").hasRole("ADMIN_EMPRESA")

                // ── Roles y permisos: solo ADMIN_EMPRESA ──
                .requestMatchers("/api/roles/**").hasRole("ADMIN_EMPRESA")
                .requestMatchers("/api/permisos/**").hasRole("ADMIN_EMPRESA")

                // ── Reportes: ADMIN y AGENTE ──
                .requestMatchers("/api/reportes/**").hasAnyRole("ADMIN_EMPRESA", "AGENTE")

                // ── Comentarios: usuarios autenticados ──
                .requestMatchers("/api/comentarios/**").authenticated()

                // ── Usuarios: ADMIN_EMPRESA todo, AGENTE solo lectura ──
                .requestMatchers(HttpMethod.GET, "/api/usuarios/**").hasAnyRole("ADMIN_EMPRESA", "AGENTE")
                .requestMatchers(HttpMethod.POST, "/api/usuarios/**").hasRole("ADMIN_EMPRESA")
                .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").hasRole("ADMIN_EMPRESA")
                .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("ADMIN_EMPRESA")

                // ── Categorías y Problemas: lectura para todos, escritura ADMIN_EMPRESA ──
                .requestMatchers(HttpMethod.GET, "/api/categorias/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/categorias/**").hasRole("ADMIN_EMPRESA")
                .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasRole("ADMIN_EMPRESA")
                .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasRole("ADMIN_EMPRESA")

                .requestMatchers(HttpMethod.GET, "/api/problemas/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/problemas/**").hasRole("ADMIN_EMPRESA")
                .requestMatchers(HttpMethod.PUT, "/api/problemas/**").hasRole("ADMIN_EMPRESA")
                .requestMatchers(HttpMethod.DELETE, "/api/problemas/**").hasRole("ADMIN_EMPRESA")

                // ── Tickets: CLIENTE solo lectura y creación; edición/eliminación ADMIN y AGENTE ──
                .requestMatchers(HttpMethod.GET, "/api/tickets/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/tickets/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/tickets/**").hasAnyRole("ADMIN_EMPRESA", "AGENTE")
                .requestMatchers(HttpMethod.DELETE, "/api/tickets/**").hasAnyRole("ADMIN_EMPRESA", "AGENTE")

                // ── Cualquier otro endpoint requiere autenticación ──
                .anyRequest().authenticated()
            )
            // Agregar el filtro JWT ANTES del filtro de autenticación estándar
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
