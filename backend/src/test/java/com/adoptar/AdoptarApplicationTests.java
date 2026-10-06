package com.adoptar;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests de humo: verifican que la aplicación arranca y que las migraciones corren
 * contra un PostgreSQL real. Si alguien rompe la configuración o una migración,
 * la CI lo detecta acá.
 */
@SpringBootTest
@ActiveProfiles("test") // perfil "test": no carga los datos de prueba de desarrollo
@Import(TestcontainersConfiguration.class)
class AdoptarApplicationTests {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void laAplicacionArranca() {
        // Si el contexto de Spring no levanta, este test falla antes de llegar acá.
    }

    @Test
    void laMigracionInicialCreaLasOnceTablas() {
        List<String> tablas = jdbc.queryForList("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_type = 'BASE TABLE'
                  AND table_name <> 'flyway_schema_history'
                """, String.class);

        assertThat(tablas).containsExactlyInAnyOrder(
                "usuarios", "adoptantes", "refugios",
                "mascotas", "fotos_mascota",
                "preguntas", "solicitudes", "respuestas", "entrevistas", "adopciones",
                "notificaciones");
    }
}
