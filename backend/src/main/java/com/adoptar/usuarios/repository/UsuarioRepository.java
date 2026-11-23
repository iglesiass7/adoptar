package com.adoptar.usuarios.repository;

import com.adoptar.usuarios.dominio.Rol;
import com.adoptar.usuarios.dominio.Usuario;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla usuarios. Los mails se buscan ya normalizados
 * (ver {@link Usuario#normalizarEmail(String)}).
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmail(String email);

    Optional<Usuario> findByEmail(String email);

    /** Se consulta en cada pedido con token: una cuenta desactivada no entra aunque su token no haya vencido. */
    boolean existsByIdAndActivoTrue(Long id);

    Page<Usuario> findByRol(Rol rol, Pageable pageable);
}
