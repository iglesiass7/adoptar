package com.adoptar.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Datos generales que muestra Swagger UI en http://localhost:8080/swagger-ui.html
 * <p>
 * Los endpoints se documentan solos a partir de los controllers.
 */
@Configuration
public class OpenApiConfig {

    /** Nombre del esquema de seguridad: agrega el botón "Authorize" para pegar el token. */
    public static final String TOKEN = "token";

    @Bean
    OpenAPI adoptarOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AdoptAR API")
                        .version("v1")
                        .description("API REST de gestión de adopción de mascotas. "
                                + "Proyecto de Diseño de Sistemas, UTN FRBA.\n\n"
                                + "Para probar endpoints protegidos: hacé login en POST /api/v1/auth/login, "
                                + "copiá el token y pegalo en el botón Authorize."))
                .components(new Components().addSecuritySchemes(TOKEN, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                // Por defecto todos los endpoints piden token; los públicos lo aclaran con @SecurityRequirements
                .addSecurityItem(new SecurityRequirement().addList(TOKEN));
    }
}
