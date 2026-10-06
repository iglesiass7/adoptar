package com.adoptar.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Activa la auditoría de JPA: completa solas las columnas created_at y updated_at
 * de las entidades que extienden {@link com.adoptar.shared.EntidadAuditada}.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
