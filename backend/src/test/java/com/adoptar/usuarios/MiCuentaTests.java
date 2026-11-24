package com.adoptar.usuarios;

import static com.adoptar.usuarios.CuentasDePrueba.bearer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adoptar.TestcontainersConfiguration;
import com.adoptar.usuarios.dominio.Rol;
import com.adoptar.usuarios.repository.UsuarioRepository;
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
 * CU-05 · Gestionar mi cuenta: editar mis datos y cambiar la contraseña.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class MiCuentaTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JdbcTemplate jdbc;

    CuentasDePrueba cuentas;
    String token;

    @BeforeEach
    void crearCuenta() throws Exception {
        cuentas = new CuentasDePrueba(mvc, jdbc, usuarios, passwordEncoder);
        cuentas.vaciarBase();
        cuentas.adoptante("carla@mail.com");
        token = cuentas.token("carla@mail.com");
    }

    @Test
    void editaNombreYTelefonoPeroNoMailNiRol() throws Exception {
        enviar("/api/v1/usuarios/yo", """
                {"nombre": "Carla Pérez", "telefono": "11 9999-0000", "email": "otro@mail.com", "rol": "ADMIN"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Carla Pérez"))
                .andExpect(jsonPath("$.telefono").value("11 9999-0000"))
                .andExpect(jsonPath("$.email").value("carla@mail.com"))
                .andExpect(jsonPath("$.rol").value("ADOPTANTE"));

        assertThat(usuarios.findByEmail("carla@mail.com").orElseThrow().getRol()).isEqualTo(Rol.ADOPTANTE);
    }

    @Test
    void rechazaUnNombreVacioCon400() throws Exception {
        enviar("/api/v1/usuarios/yo", """
                {"nombre": "  "}
                """).andExpect(status().isBadRequest());
    }

    @Test
    void cambiaLaContraseniaYSoloSirveLaNueva() throws Exception {
        enviar("/api/v1/usuarios/yo/password", """
                {"passwordActual": "clave-segura", "passwordNueva": "clave-nueva-2026"}
                """).andExpect(status().isNoContent());

        cuentas.token("carla@mail.com", "clave-nueva-2026");
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "carla@mail.com", "password": "clave-segura"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void noCambiaLaContraseniaSiLaActualEstaMal() throws Exception {
        enviar("/api/v1/usuarios/yo/password", """
                {"passwordActual": "cualquier-cosa", "passwordNueva": "clave-nueva-2026"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La contraseña actual no es correcta"));

        cuentas.token("carla@mail.com");
    }

    @Test
    void laContraseniaNuevaTieneQueTenerAlMenos8Caracteres() throws Exception {
        enviar("/api/v1/usuarios/yo/password", """
                {"passwordActual": "clave-segura", "passwordNueva": "corta"}
                """).andExpect(status().isBadRequest());

        cuentas.token("carla@mail.com");
    }

    @Test
    void sinTokenNoSePuedeEditarNada() throws Exception {
        mvc.perform(put("/api/v1/usuarios/yo").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Nadie"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions enviar(String ruta, String json) throws Exception {
        return mvc.perform(put(ruta)
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }
}
