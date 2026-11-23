package com.adoptar.usuarios.service;

import com.adoptar.shared.exception.RecursoNoEncontradoException;
import com.adoptar.shared.exception.ReglaDeNegocioException;
import com.adoptar.usuarios.dominio.Rol;
import com.adoptar.usuarios.dominio.Usuario;
import com.adoptar.usuarios.dto.Pagina;
import com.adoptar.usuarios.dto.UsuarioResponse;
import com.adoptar.usuarios.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso del admin sobre las cuentas: listarlas, desactivarlas y reactivarlas.
 * El rol ADMIN lo controla SecurityConfig (todo /api/v1/admin/**).
 */
@Service
public class AdministracionUsuariosService {

    public static final int TAMANIO_MAXIMO = 100;

    private final UsuarioRepository usuarios;

    public AdministracionUsuariosService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    /** Lista las cuentas de a páginas, ordenadas por id. Con {@code rol} null trae todas. */
    @Transactional(readOnly = true)
    public Pagina<UsuarioResponse> listar(Rol rol, int pagina, int tamanio) {
        PageRequest pedido = PageRequest.of(Math.max(pagina, 0), Math.clamp(tamanio, 1, TAMANIO_MAXIMO),
                Sort.by("id"));
        Page<Usuario> resultado = rol == null ? usuarios.findAll(pedido) : usuarios.findByRol(rol, pedido);
        return Pagina.de(resultado, UsuarioResponse::de);
    }

    /**
     * Desactiva una cuenta: no puede iniciar sesión y su token deja de servir.
     *
     * @throws ReglaDeNegocioException      si el admin intenta desactivarse a sí mismo
     * @throws RecursoNoEncontradoException si la cuenta no existe
     */
    @Transactional
    public UsuarioResponse desactivar(Long id, Long adminId) {
        if (id.equals(adminId)) {
            throw new ReglaDeNegocioException("No podés desactivar tu propia cuenta");
        }
        Usuario usuario = buscar(id);
        usuario.desactivar();
        return UsuarioResponse.de(usuario);
    }

    /**
     * @throws RecursoNoEncontradoException si la cuenta no existe
     */
    @Transactional
    public UsuarioResponse reactivar(Long id) {
        Usuario usuario = buscar(id);
        usuario.reactivar();
        return UsuarioResponse.de(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }
}
