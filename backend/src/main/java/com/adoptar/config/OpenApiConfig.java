package com.adoptar.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Datos generales que muestra Swagger UI en http://localhost:8080/swagger-ui.html
 * <p>
 * Los endpoints se documentan solos a partir de los controllers.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI adoptarOpenApi() {
        return new OpenAPI().info(new Info()
                .title("AdoptAR API")
                .version("v1")
                .description("API REST de gestión de adopción de mascotas. "
                        + "Proyecto de Diseño de Sistemas, UTN FRBA."));
    }
}
