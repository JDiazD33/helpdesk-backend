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
 * Configuracion central de Spring Security.
 * Roles: ADMIN_OWNER (super admin global), ADMIN_EMPRESA (admin de una empresa),
 *        AGENTE (soporte), CLIENTE (usuario final).
 * - ADMIN_OWNER tiene acceso TOTAL a todo, sin restriccion de empresa.
 * - ADMIN_EMPRESA solo opera dentro de su empresa (validacion en Service).
 * - AGENTE y CLIENTE mantienen sus permisos actuales.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173", "http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints publicos
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                // Recursos estaticos de uploads (imagenes de cierre servidas por WebConfig).
                // Cualquiera con la URL puede verlas: aceptable porque el nombre de archivo
                // es un UUID y la UI las embebe en un modal. Si en el futuro se endurece
                // este acceso, mover a un controller que valide el JWT antes de servir el binario.
                .requestMatchers("/uploads/**").permitAll()

                // ── Endpoints solo ADMIN_OWNER y ADMIN_EMPRESA ──
                .requestMatchers("/api/empresas/**").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")
                .requestMatchers("/api/problemas/conteo-categoria", "/api/problemas/buscar").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")
                .requestMatchers("/api/comentarios/usuario/**", "/api/comentarios/buscar", "/api/comentarios/ranking-usuarios").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")
                .requestMatchers("/api/comentarios/empresa/*/recientes").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")
                .requestMatchers("/api/usuarios/conteo-rol").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")
                .requestMatchers("/api/usuarios/empresa/*/agentes").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")
                .requestMatchers("/api/reportes/dashboard").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")
                .requestMatchers("/api/tickets/empresa/*/buscar", "/api/tickets/empresa/*/sin-asignar", "/api/tickets/empresa/*/actualizados").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")

                // Roles y permisos:
                //   GET (listar / buscar) → ADMIN_OWNER y ADMIN_EMPRESA (necesario para
                //     filtrar usuarios por rol y para asignar un rol al crear usuario)
                //   POST/PUT/DELETE → solo ADMIN_OWNER (defensa contra escalada de privilegios)
                .requestMatchers(HttpMethod.GET, "/api/roles/**").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")
                .requestMatchers(HttpMethod.POST, "/api/roles/**").hasRole("ADMIN_OWNER")
                .requestMatchers(HttpMethod.PUT, "/api/roles/**").hasRole("ADMIN_OWNER")
                .requestMatchers(HttpMethod.DELETE, "/api/roles/**").hasRole("ADMIN_OWNER")

                .requestMatchers(HttpMethod.GET, "/api/permisos/**").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")
                .requestMatchers(HttpMethod.POST, "/api/permisos/**").hasRole("ADMIN_OWNER")
                .requestMatchers(HttpMethod.PUT, "/api/permisos/**").hasRole("ADMIN_OWNER")
                .requestMatchers(HttpMethod.DELETE, "/api/permisos/**").hasRole("ADMIN_OWNER")

                // Reportes: ADMIN_OWNER, ADMIN_EMPRESA y AGENTE
                .requestMatchers("/api/reportes/**").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA", "AGENTE")

                // Comentarios: cualquier autenticado
                .requestMatchers("/api/comentarios/**").authenticated()

                // Usuarios: ADMIN_OWNER todo, ADMIN_EMPRESA todo, AGENTE solo lectura
                .requestMatchers(HttpMethod.GET, "/api/usuarios/**").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA", "AGENTE")
                .requestMatchers(HttpMethod.POST, "/api/usuarios/**").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")
                .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")
                .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")

                // Categorias y Problemas
                .requestMatchers(HttpMethod.GET, "/api/categorias/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/categorias/**").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")
                .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")
                .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")

                .requestMatchers(HttpMethod.GET, "/api/problemas/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/problemas/**").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")
                .requestMatchers(HttpMethod.PUT, "/api/problemas/**").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")
                .requestMatchers(HttpMethod.DELETE, "/api/problemas/**").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA")

                // Tickets
                .requestMatchers(HttpMethod.GET, "/api/tickets/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/tickets/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/tickets/**").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA", "AGENTE")
                .requestMatchers(HttpMethod.PUT, "/api/tickets/*/estado", "/api/tickets/*/asignar").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA", "AGENTE")
                .requestMatchers(HttpMethod.DELETE, "/api/tickets/**").hasAnyRole("ADMIN_OWNER", "ADMIN_EMPRESA", "AGENTE")

                // Cualquier otro endpoint autenticado
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
