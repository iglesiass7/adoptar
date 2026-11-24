package com.adoptar.usuarios.repository;

import com.adoptar.usuarios.dominio.Adoptante;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla adoptantes. El id es el del usuario dueño del perfil.
 */
public interface AdoptanteRepository extends JpaRepository<Adoptante, Long> {
}
