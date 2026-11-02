package com.adoptar.usuarios.service;

import com.adoptar.usuarios.dominio.EmailYaRegistradoException;
import com.adoptar.usuarios.dominio.Usuario;
import com.adoptar.usuarios.dto.RegistroAdoptanteRequest;
import com.adoptar.usuarios.dto.UsuarioResponse;
import com.adoptar.usuarios.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de cuentas de usuario.
 */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * CU-01 · Registrar adoptante. Crea solo la cuenta: el perfil se completa en CU-04.
     *
     * @throws EmailYaRegistradoException si el mail ya tiene una cuenta
     */
    @Transactional
    public UsuarioResponse registrarAdoptante(RegistroAdoptanteRequest datos) {
        String email = Usuario.normalizarEmail(datos.email());
        if (usuarios.existsByEmail(email)) {
            throw new EmailYaRegistradoException(email);
        }

        Usuario adoptante = Usuario.nuevoAdoptante(email, passwordEncoder.encode(datos.password()),
                datos.nombre(), datos.telefono());
        return UsuarioResponse.de(usuarios.save(adoptante));
    }
}
