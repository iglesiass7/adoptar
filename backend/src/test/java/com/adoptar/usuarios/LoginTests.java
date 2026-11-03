package com.adoptar.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adoptar.TestcontainersConfiguration;
import com.adoptar.usuarios.dominio.Usuario;
import com.adoptar.usuarios.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Iniciar sesión: mail y contraseña correctos devuelven un token firmado con el rol adentro.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class LoginTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtDecoder jwtDecoder;

    @Autowired
    JdbcTemplate jdbc;

    Usuario carla;

    @BeforeEach
    void crearUsuario() {
        usuarios.deleteAll();
        carla = usuarios.save(Usuario.nuevoAdoptante("carla@mail.com",
                passwordEncoder.encode("clave-segura"), "Carla", null));
    }

    @Test
    void conMailYContraseniaCorrectosDevuelveUnTokenConElRol() throws Exception {
        String respuesta = login("carla@mail.com", "clave-segura")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEn").exists())
                .andExpect(jsonPath("$.usuario.email").value("carla@mail.com"))
                .andExpect(jsonPath("$.usuario.rol").value("ADOPTANTE"))
                .andReturn().getResponse().getContentAsString();

        Jwt token = jwtDecoder.decode(JsonPath.read(respuesta, "$.token"));
        assertThat(token.getSubject()).isEqualTo(carla.getId().toString());
        assertThat(token.getClaimAsString("rol")).isEqualTo("ADOPTANTE");
        assertThat(token.getClaimAsString("email")).isEqualTo("carla@mail.com");
    }

    @Test
    void elMailNoDistingueMayusculas() throws Exception {
        login("CARLA@Mail.com", "clave-segura").andExpect(status().isOk());
    }

    @Test
    void conContraseniaIncorrectaResponde401() throws Exception {
        login("carla@mail.com", "otra-clave")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Mail o contraseña incorrectos"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void conMailInexistenteRespondeLoMismoQueConContraseniaIncorrecta() throws Exception {
        login("nadie@mail.com", "clave-segura")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Mail o contraseña incorrectos"));
    }

    @Test
    void unaCuentaDesactivadaNoPuedeIniciarSesion() throws Exception {
        jdbc.update("UPDATE usuarios SET activo = FALSE WHERE id = ?", carla.getId());

        login("carla@mail.com", "clave-segura").andExpect(status().isUnauthorized());
    }

    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, password)));
    }
}
