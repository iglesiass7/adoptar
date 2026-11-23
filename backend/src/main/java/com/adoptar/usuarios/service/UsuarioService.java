package com.adoptar.usuarios.service;

import com.adoptar.shared.exception.RecursoNoEncontradoException;
import com.adoptar.usuarios.dominio.CredencialesInvalidasException;
import com.adoptar.usuarios.dominio.CuitYaRegistradoException;
import com.adoptar.usuarios.dominio.EmailYaRegistradoException;
import com.adoptar.usuarios.dominio.Usuario;
import com.adoptar.usuarios.dto.LoginRequest;
import com.adoptar.usuarios.dto.LoginResponse;
import com.adoptar.usuarios.dto.RegistroAdoptanteRequest;
import com.adoptar.usuarios.dto.RegistroRefugioRequest;
import com.adoptar.usuarios.dto.UsuarioResponse;
import com.adoptar.usuarios.repository.FichaRefugioRepository;
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
    private final FichaRefugioRepository fichasRefugio;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, TokenService tokenService,
                          FichaRefugioRepository fichasRefugio) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.fichasRefugio = fichasRefugio;
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
     * CU-02 · Registrar refugio. Crea la cuenta y su ficha en estado PENDIENTE, en una sola
     * transacción: si algo falla, no queda ni la cuenta ni la ficha.
     *
     * @throws EmailYaRegistradoException si el mail ya tiene una cuenta
     * @throws CuitYaRegistradoException  si ya hay un refugio con ese CUIT
     */
    @Transactional
    public UsuarioResponse registrarRefugio(RegistroRefugioRequest datos) {
        String email = Usuario.normalizarEmail(datos.email());
        if (usuarios.existsByEmail(email)) {
            throw new EmailYaRegistradoException(email);
        }
        if (fichasRefugio.existeCuit(datos.cuit())) {
            throw new CuitYaRegistradoException(datos.cuit());
        }

        Usuario refugio = usuarios.save(Usuario.nuevoRefugio(email, passwordEncoder.encode(datos.password()),
                datos.nombre(), datos.telefono()));
        fichasRefugio.crearPendiente(refugio.getId(), datos);
        return UsuarioResponse.de(refugio);
    }

    /**
     * Datos de una cuenta.
     *
     * @throws RecursoNoEncontradoException si no existe
     */
    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return usuarios.findById(id)
                .map(UsuarioResponse::de)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
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
