package com.adoptar.usuarios;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adoptar.usuarios.dominio.Usuario;
import com.adoptar.usuarios.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Ayudas para los tests del módulo: vaciar la base, crear cuentas e iniciar sesión.
 */
class CuentasDePrueba {

    static final String CLAVE = "clave-segura";

    private final MockMvc mvc;
    private final JdbcTemplate jdbc;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    CuentasDePrueba(MockMvc mvc, JdbcTemplate jdbc, UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.mvc = mvc;
        this.jdbc = jdbc;
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    /** Borra los usuarios y todo lo que cuelga de ellos (perfiles, refugios, etc.). */
    static void vaciarBase(JdbcTemplate jdbc) {
        jdbc.execute("TRUNCATE usuarios RESTART IDENTITY CASCADE");
    }

    void vaciarBase() {
        vaciarBase(jdbc);
    }

    Usuario adoptante(String email) {
        return usuarios.save(Usuario.nuevoAdoptante(email, passwordEncoder.encode(CLAVE), "Adoptante", null));
    }

    /** El admin no se registra desde la API: se crea un usuario y se le cambia el rol en la base. */
    Usuario admin(String email) {
        Usuario admin = adoptante(email);
        jdbc.update("UPDATE usuarios SET rol = 'ADMIN' WHERE id = ?", admin.getId());
        return admin;
    }

    String token(String email) throws Exception {
        return token(email, CLAVE);
    }

    String token(String email, String clave) throws Exception {
        String respuesta = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "%s"}
                                """.formatted(email, clave)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }

    static String bearer(String token) {
        return "Bearer " + token;
    }
}
