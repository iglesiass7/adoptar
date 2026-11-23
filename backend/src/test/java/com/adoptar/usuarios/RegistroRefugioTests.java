package com.adoptar.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adoptar.TestcontainersConfiguration;
import com.adoptar.usuarios.repository.UsuarioRepository;
import java.util.Map;
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
 * CU-02 · Registrar refugio: crea la cuenta y la ficha PENDIENTE juntas, o ninguna de las dos.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class RegistroRefugioTests {

    static final String REFUGIO_SUR = """
            {"email": "Sur@Refugio.org", "password": "clave-segura", "nombre": "Refugio Sur",
             "telefono": "11 4000-1000", "razonSocial": "Asociación Civil Refugio Sur", "cuit": "30-71234567-1",
             "descripcion": "Rescatamos perros y gatos.", "direccion": "Av. Ejemplo 1234",
             "provincia": "Buenos Aires", "localidad": "Quilmes"}
            """;

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void limpiar() {
        CuentasDePrueba.vaciarBase(jdbc);
    }

    @Test
    void creaLaCuentaConRolRefugioYLaFichaPendiente() throws Exception {
        registrar(REFUGIO_SUR)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("sur@refugio.org"))
                .andExpect(jsonPath("$.rol").value("REFUGIO"));

        Long id = usuarios.findByEmail("sur@refugio.org").orElseThrow().getId();
        Map<String, Object> ficha = jdbc.queryForMap("SELECT * FROM refugios WHERE usuario_id = ?", id);
        assertThat(ficha.get("razon_social")).isEqualTo("Asociación Civil Refugio Sur");
        assertThat(ficha.get("cuit")).isEqualTo("30-71234567-1");
        assertThat(ficha.get("estado_verificacion")).isEqualTo("PENDIENTE");
    }

    @Test
    void elRefugioRegistradoPuedeIniciarSesion() throws Exception {
        registrar(REFUGIO_SUR).andExpect(status().isCreated());

        new CuentasDePrueba(mvc, jdbc, usuarios, passwordEncoder).token("sur@refugio.org");
    }

    @Test
    void ignoraRolYEstadoQueMandaElCliente() throws Exception {
        registrar("""
                {"email": "vivo@refugio.org", "password": "clave-segura", "nombre": "Vivo",
                 "razonSocial": "Vivo SA", "cuit": "30-11111111-1", "provincia": "Buenos Aires",
                 "localidad": "Morón", "rol": "ADMIN", "estadoVerificacion": "VERIFICADO"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("REFUGIO"));

        assertThat(jdbc.queryForObject("SELECT estado_verificacion FROM refugios", String.class))
                .isEqualTo("PENDIENTE");
    }

    @Test
    void rechazaUnCuitRepetidoSinCrearLaCuenta() throws Exception {
        registrar(REFUGIO_SUR).andExpect(status().isCreated());

        registrar(REFUGIO_SUR.replace("Sur@Refugio.org", "otro@refugio.org"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Ya existe un refugio con el CUIT 30-71234567-1"));

        assertThat(usuarios.findByEmail("otro@refugio.org")).isEmpty();
    }

    @Test
    void rechazaUnMailYaRegistrado() throws Exception {
        registrar(REFUGIO_SUR).andExpect(status().isCreated());

        registrar(REFUGIO_SUR.replace("30-71234567-1", "30-99999999-9"))
                .andExpect(status().isConflict());

        assertThat(jdbc.queryForObject("SELECT count(*) FROM refugios", Integer.class)).isEqualTo(1);
    }

    @Test
    void rechazaDatosInvalidosCon400() throws Exception {
        // CUIT sin guiones y sin razón social
        registrar("""
                {"email": "mal@refugio.org", "password": "clave-segura", "nombre": "Mal",
                 "cuit": "30712345671", "provincia": "Buenos Aires", "localidad": "Quilmes"}
                """).andExpect(status().isBadRequest());

        assertThat(usuarios.count()).isZero();
    }

    private ResultActions registrar(String json) throws Exception {
        return mvc.perform(post("/api/v1/auth/registro/refugio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }
}
