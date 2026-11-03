package com.adoptar.usuarios;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Reglas generales de acceso de SecurityConfig: qué rutas son públicas, cuáles piden token
 * y cuáles son solo para ADMIN.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class SeguridadTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void crearUsuarios() {
        usuarios.deleteAll();
        usuarios.save(Usuario.nuevoAdoptante("carla@mail.com", passwordEncoder.encode("clave-segura"), "Carla", null));
        Usuario admin = usuarios.save(Usuario.nuevoAdoptante("admin@mail.com",
                passwordEncoder.encode("clave-segura"), "Admin", null));
        jdbc.update("UPDATE usuarios SET rol = 'ADMIN' WHERE id = ?", admin.getId());
    }

    @Test
    void sinTokenNoSePuedeEntrarAUnaRutaProtegida() throws Exception {
        mvc.perform(get("/api/v1/usuarios/yo")).andExpect(status().isUnauthorized());
    }

    @Test
    void conTokenSeEntraYSeSabeQuienEs() throws Exception {
        mvc.perform(get("/api/v1/usuarios/yo").header("Authorization", "Bearer " + token("carla@mail.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("carla@mail.com"))
                .andExpect(jsonPath("$.rol").value("ADOPTANTE"));
    }

    @Test
    void unTokenAdulteradoSeRechaza() throws Exception {
        String adulterado = token("carla@mail.com") + "x";

        mvc.perform(get("/api/v1/usuarios/yo").header("Authorization", "Bearer " + adulterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void lasRutasDeAdministracionSonSoloParaAdmin() throws Exception {
        mvc.perform(get("/api/v1/admin/cualquiera")).andExpect(status().isUnauthorized());

        mvc.perform(get("/api/v1/admin/cualquiera").header("Authorization", "Bearer " + token("carla@mail.com")))
                .andExpect(status().isForbidden());

        // El admin pasa la seguridad: el 404 es porque esa ruta todavía no existe
        mvc.perform(get("/api/v1/admin/cualquiera").header("Authorization", "Bearer " + token("admin@mail.com")))
                .andExpect(status().isNotFound());
    }

    @Test
    void elCatalogoSePuedeMirarSinCuentaPeroNoModificar() throws Exception {
        // 404 y no 401: pasa la seguridad (el endpoint de mascotas todavía no existe)
        mvc.perform(get("/api/v1/mascotas")).andExpect(status().isNotFound());

        mvc.perform(post("/api/v1/mascotas").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void laDocumentacionYElEstadoDeLaApiSonPublicos() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    private String token(String email) throws Exception {
        String respuesta = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "clave-segura"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(respuesta, "$.token");
    }
}
