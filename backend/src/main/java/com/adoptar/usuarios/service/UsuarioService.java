package com.adoptar.usuarios.service;

import com.adoptar.usuarios.dominio.CredencialesInvalidasException;
import com.adoptar.usuarios.dominio.EmailYaRegistradoException;
import com.adoptar.usuarios.dominio.Usuario;
import com.adoptar.usuarios.dto.LoginRequest;
import com.adoptar.usuarios.dto.LoginResponse;
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
    private final TokenService tokenService;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
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

    /**
     * Iniciar sesión: controla mail y contraseña y entrega el token.
     *
     * @throws CredencialesInvalidasException si el mail no existe, la contraseña no coincide
     *                                        o la cuenta está desactivada
     */
    @Transactional(readOnly = true)
    public LoginResponse iniciarSesion(LoginRequest datos) {
        Usuario usuario = usuarios.findByEmail(Usuario.normalizarEmail(datos.email()))
                .filter(Usuario::isActivo)
                .filter(u -> passwordEncoder.matches(datos.password(), u.getPasswordHash()))
                .orElseThrow(CredencialesInvalidasException::new);

        TokenService.Token token = tokenService.generar(usuario);
        return new LoginResponse(token.valor(), token.expiraEn(), UsuarioResponse.de(usuario));
    }
}
