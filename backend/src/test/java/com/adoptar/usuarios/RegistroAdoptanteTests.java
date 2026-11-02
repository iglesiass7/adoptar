package com.adoptar.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adoptar.TestcontainersConfiguration;
import com.adoptar.usuarios.dominio.Rol;
import com.adoptar.usuarios.dominio.Usuario;
import com.adoptar.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * CU-01 · Registrar adoptante, probado de punta a punta: HTTP → service → PostgreSQL real.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class RegistroAdoptanteTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void limpiarUsuarios() {
        usuarios.deleteAll();
    }

    @Test
    void registraUnAdoptanteYDevuelve201() throws Exception {
        registrar("""
                {"email": "carla@mail.com", "password": "clave-segura", "nombre": "Carla", "telefono": "11 1234-5678"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("carla@mail.com"))
                .andExpect(jsonPath("$.rol").value("ADOPTANTE"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void guardaElMailEnMinusculas() throws Exception {
        registrar("""
                {"email": "Carla@Mail.COM", "password": "clave-segura", "nombre": "Carla"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("carla@mail.com"));

        assertThat(usuarios.findByEmail("carla@mail.com")).isPresent();
    }

    @Test
    void guardaLaContraseniaHasheadaNuncaEnTextoPlano() throws Exception {
        registrar("""
                {"email": "carla@mail.com", "password": "clave-segura", "nombre": "Carla"}
                """).andExpect(status().isCreated());

        Usuario guardado = usuarios.findByEmail("carla@mail.com").orElseThrow();
        assertThat(guardado.getPasswordHash()).isNotEqualTo("clave-segura");
        assertThat(passwordEncoder.matches("clave-segura", guardado.getPasswordHash())).isTrue();
    }

    @Test
    void rechazaUnMailYaRegistradoAunqueCambienLasMayusculas() throws Exception {
        registrar("""
                {"email": "carla@mail.com", "password": "clave-segura", "nombre": "Carla"}
                """).andExpect(status().isCreated());

        registrar("""
                {"email": "CARLA@mail.com", "password": "otra-clave", "nombre": "Otra Carla"}
                """)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Ya existe una cuenta con el mail carla@mail.com"));

        assertThat(usuarios.count()).isEqualTo(1);
    }

    @Test
    void ignoraElRolQueMandaElClienteYSiempreCreaUnAdoptante() throws Exception {
        registrar("""
                {"email": "colado@mail.com", "password": "clave-segura", "nombre": "Colado", "rol": "ADMIN"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("ADOPTANTE"));

        assertThat(usuarios.findByEmail("colado@mail.com").orElseThrow().getRol()).isEqualTo(Rol.ADOPTANTE);
    }

    @Test
    void rechazaDatosInvalidosCon400() throws Exception {
        registrar("""
                {"email": "no-es-un-mail", "password": "corta", "nombre": ""}
                """).andExpect(status().isBadRequest());

        assertThat(usuarios.count()).isZero();
    }

    private ResultActions registrar(String json) throws Exception {
        return mvc.perform(post("/api/v1/auth/registro/adoptante")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }
}
