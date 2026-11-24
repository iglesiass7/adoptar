package com.adoptar.usuarios;

import static com.adoptar.usuarios.CuentasDePrueba.bearer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adoptar.TestcontainersConfiguration;
import com.adoptar.usuarios.dominio.Usuario;
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
 * El admin lista, desactiva y reactiva cuentas. Una cuenta desactivada no entra más,
 * ni con login nuevo ni con el token que ya tenía.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class AdministracionUsuariosTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarios;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JdbcTemplate jdbc;

    CuentasDePrueba cuentas;
    Usuario admin;
    Usuario carla;
    String tokenAdmin;

    @BeforeEach
    void crearCuentas() throws Exception {
        cuentas = new CuentasDePrueba(mvc, jdbc, usuarios, passwordEncoder);
        cuentas.vaciarBase();
        admin = cuentas.admin("admin@mail.com");
        carla = cuentas.adoptante("carla@mail.com");
        tokenAdmin = cuentas.token("admin@mail.com");
    }

    @Test
    void listaLasCuentasYFiltraPorRol() throws Exception {
        mvc.perform(get("/api/v1/admin/usuarios").header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.contenido[0].email").value("admin@mail.com"))
                .andExpect(jsonPath("$.contenido[0].passwordHash").doesNotExist());

        mvc.perform(get("/api/v1/admin/usuarios").param("rol", "ADOPTANTE")
                        .header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].email").value("carla@mail.com"));
    }

    @Test
    void unaCuentaDesactivadaNoPuedeIniciarSesionNiUsarSuToken() throws Exception {
        String tokenCarla = cuentas.token("carla@mail.com");

        desactivar(carla.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        // El token que ya tenía deja de servir, aunque no haya vencido
        mvc.perform(get("/api/v1/usuarios/yo").header("Authorization", bearer(tokenCarla)))
                .andExpect(status().isUnauthorized());

        // Y tampoco puede sacar uno nuevo
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "carla@mail.com", "password": "clave-segura"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void alReactivarlaVuelveAEntrar() throws Exception {
        desactivar(carla.getId()).andExpect(status().isOk());

        mvc.perform(post("/api/v1/admin/usuarios/{id}/reactivar", carla.getId())
                        .header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));

        cuentas.token("carla@mail.com");
    }

    @Test
    void elAdminNoPuedeDesactivarseASiMismo() throws Exception {
        desactivar(admin.getId())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("No podés desactivar tu propia cuenta"));
    }

    @Test
    void unaCuentaQueNoExisteResponde404() throws Exception {
        desactivar(9999L).andExpect(status().isNotFound());
    }

    @Test
    void soloElAdminPuedeAdministrarCuentas() throws Exception {
        mvc.perform(post("/api/v1/admin/usuarios/{id}/desactivar", admin.getId())
                        .header("Authorization", bearer(cuentas.token("carla@mail.com"))))
                .andExpect(status().isForbidden());
    }

    private ResultActions desactivar(Long id) throws Exception {
        return mvc.perform(post("/api/v1/admin/usuarios/{id}/desactivar", id)
                .header("Authorization", bearer(tokenAdmin)));
    }
}
