package com.adoptar.usuarios.repository;

import com.adoptar.usuarios.dominio.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla usuarios. Los mails se buscan ya normalizados
 * (ver {@link Usuario#normalizarEmail(String)}).
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);

    Optional<Usuario> findByEmail(String email);
}
