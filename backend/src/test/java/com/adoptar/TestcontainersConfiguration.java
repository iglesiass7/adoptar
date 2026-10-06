package com.adoptar;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Levanta un PostgreSQL real dentro de Docker para los tests (Testcontainers).
 * <p>
 * {@code @ServiceConnection} hace que Spring se conecte a ese contenedor en lugar de
 * a la base de docker compose. Requiere Docker Desktop abierto.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        // Misma versión que docker-compose.yml
        return new PostgreSQLContainer("postgres:17");
    }
}
