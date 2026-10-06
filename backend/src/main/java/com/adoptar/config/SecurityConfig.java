package com.adoptar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad de la ITERACIÓN 0: todo abierto.
 * <p>
 * En la iteración 1 se agrega la autenticación con JWT y se restringe cada ruta según el rol.
 * Queda configurada desde ahora para que después sea cambiar reglas, y no "activar la
 * seguridad" cuando ya hay 30 endpoints escritos.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // API REST sin sesiones ni formularios HTML: CSRF no aplica
                .csrf(csrf -> csrf.disable())
                // Cada request se autentica sola (con el token, a partir de la iteración 1)
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> rutas
                        // TODO iteración 1: restringir /api/** según el rol del usuario
                        .anyRequest().permitAll())
                .build();
    }
}
