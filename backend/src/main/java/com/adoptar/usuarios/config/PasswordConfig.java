package com.adoptar.usuarios.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Cómo se guardan las contraseñas. Vive en el módulo de usuarios porque solo lo usan el
 * registro y el login, pero el bean está disponible para toda la aplicación.
 * <p>
 * BCrypt: el hash no se puede revertir, solo comparar. Es el mismo formato de los usuarios
 * de prueba (db/demo), así que con él pueden iniciar sesión con "adoptar123".
 */
@Configuration
public class PasswordConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
