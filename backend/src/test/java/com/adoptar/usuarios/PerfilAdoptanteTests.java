package com.adoptar.usuarios;

import static com.adoptar.usuarios.CuentasDePrueba.bearer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adoptar.TestcontainersConfiguration;
import com.adoptar.usuarios.dominio.PerfilIncompletoException;
import com.adoptar.usuarios.dominio.Usuario;
import com.adoptar.usuarios.repository.AdoptanteRepository;
import com.adoptar.usuarios.repository.UsuarioRepository;
import com.adoptar.usuarios.service.PerfilAdoptanteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * CU-04 · Completar el perfil del adoptante, y la regla 4 que usan las solicitudes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class PerfilAdoptanteTests {

    static final String PERFIL = """
            {"fechaNacimiento": "1995-04-12", "provincia": "Buenos Aires", "localidad": "Lanús",
             "tipoVivienda": "CASA", "tienePatio": true, "otrasMascotas": false}
            """;

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    AdoptanteRepository adoptantes;

    @Autowired
    PerfilAdoptanteService perfilService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JdbcTemplate jdbc;

    CuentasDePrueba cuentas;
    Usuario carla;
    String token;

    @BeforeEach
    void crearAdoptante() throws Exception {
        cuentas = new CuentasDePrueba(mvc, jdbc, usuarios, passwordEncoder);
        cuentas.vaciarBase();
        carla = cuentas.adoptante("carla@mail.com");
        token = cuentas.token("carla@mail.com");
    }

    @Test
    void sinPerfilCompletoResponde404() throws Exception {
        mvc.perform(get("/api/v1/usuarios/yo/perfil").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void completaElPerfilYDespuesSePuedeVer() throws Exception {
        guardar(PERFIL)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(carla.getId()))
                .andExpect(jsonPath("$.tipoVivienda").value("CASA"));

        mvc.perform(get("/api/v1/usuarios/yo/perfil").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaNacimiento").value("1995-04-12"))
                .andExpect(jsonPath("$.localidad").value("Lanús"))
                .andExpect(jsonPath("$.tienePatio").value(true));
    }

    @Test
    void guardarDeNuevoActualizaElMismoPerfil() throws Exception {
        guardar(PERFIL).andExpect(status().isOk());

        guardar("""
                {"fechaNacimiento": "1995-04-12", "provincia": "Córdoba", "localidad": "Río Cuarto",
                 "tipoVivienda": "DEPARTAMENTO", "tienePatio": false, "otrasMascotas": true}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provincia").value("Córdoba"))
                .andExpect(jsonPath("$.tipoVivienda").value("DEPARTAMENTO"));

        assertThat(adoptantes.count()).isEqualTo(1);
    }

    @Test
    void rechazaDatosInvalidosCon400() throws Exception {
        // Fecha en el futuro, provincia vacía y una vivienda que no existe
        guardar("""
                {"fechaNacimiento": "2999-01-01", "provincia": "", "localidad": "Lanús",
                 "tipoVivienda": "CASTILLO", "tienePatio": true, "otrasMascotas": false}
                """).andExpect(status().isBadRequest());

        guardar("""
                {"provincia": "Buenos Aires", "localidad": "Lanús"}
                """).andExpect(status().isBadRequest());

        assertThat(adoptantes.count()).isZero();
    }

    @Test
    void unRefugioNoTienePerfilDeAdoptante() throws Exception {
        Usuario refugio = cuentas.adoptante("refugio@mail.com");
        jdbc.update("UPDATE usuarios SET rol = 'REFUGIO' WHERE id = ?", refugio.getId());

        mvc.perform(put("/api/v1/usuarios/yo/perfil")
                        .header("Authorization", bearer(cuentas.token("refugio@mail.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PERFIL))
                .andExpect(status().isForbidden());
    }

    @Test
    void reglaCuatroSinPerfilNoPuedeSolicitar() throws Exception {
        assertThat(perfilService.tienePerfilCompleto(carla.getId())).isFalse();
        assertThatThrownBy(() -> perfilService.exigirPerfilCompleto(carla.getId()))
                .isInstanceOf(PerfilIncompletoException.class);

        guardar(PERFIL).andExpect(status().isOk());

        assertThat(perfilService.tienePerfilCompleto(carla.getId())).isTrue();
        perfilService.exigirPerfilCompleto(carla.getId());
    }

    private ResultActions guardar(String json) throws Exception {
        return mvc.perform(put("/api/v1/usuarios/yo/perfil")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }
}
