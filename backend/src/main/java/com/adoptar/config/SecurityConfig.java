package com.adoptar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad de la API: cada pedido se autentica con el token JWT que entrega el login
 * ({@code POST /api/v1/auth/login}), en el encabezado {@code Authorization: Bearer <token>}.
 * <p>
 * Acá van solo las reglas generales por ruta. Las reglas propias de cada módulo
 * ("solo un refugio VERIFICADO publica", "solo el dueño ve su solicitud") van en su service.
 * <p>
 * En un controller, el usuario logueado se obtiene con {@code @AuthenticationPrincipal Jwt jwt}:
 * {@code jwt.getSubject()} es su id y {@code jwt.getClaimAsString("rol")} su rol.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // API REST sin sesiones ni formularios HTML: CSRF no aplica
                .csrf(csrf -> csrf.disable())
                // Cada request se autentica sola con su token: el servidor no guarda sesiones
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> rutas
                        // Públicas: registro y login, documentación, estado de la API y página de error
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health/**", "/actuator/info", "/error").permitAll()
                        // El catálogo se puede mirar sin cuenta; publicar o editar mascotas requiere token
                        .requestMatchers(HttpMethod.GET, "/api/v1/mascotas/**").permitAll()
                        // Administración: solo ADMIN
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // Todo lo demás: cualquier usuario logueado
                        .anyRequest().authenticated())
                .oauth2ResourceServer(recurso -> recurso
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(rolDelToken())))
                .build();
    }

    /** Toma el claim "rol" del token (ADOPTANTE, REFUGIO o ADMIN) para usarlo en hasRole(...). */
    private static JwtAuthenticationConverter rolDelToken() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(roles);
        return conversor;
    }
}
