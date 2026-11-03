package com.adoptar.usuarios.dto;

import com.adoptar.usuarios.dominio.Rol;
import com.adoptar.usuarios.dominio.Usuario;

/**
 * Lo que la API muestra de un usuario. Nunca incluye el hash de la contraseña.
 */
public record UsuarioResponse(Long id, String email, String nombre, String telefono, Rol rol) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getEmail(), usuario.getNombre(),
                usuario.getTelefono(), usuario.getRol());
    }
}
