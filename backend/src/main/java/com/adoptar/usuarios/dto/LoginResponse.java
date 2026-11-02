package com.adoptar.usuarios.dto;

import java.time.Instant;

/**
 * Respuesta del login: el token para mandar en cada pedido, en el encabezado
 * {@code Authorization: Bearer <token>}, y los datos del usuario que inició sesión.
 */
public record LoginResponse(String token, String tipo, Instant expiraEn, UsuarioResponse usuario) {

    public LoginResponse(String token, Instant expiraEn, UsuarioResponse usuario) {
        this(token, "Bearer", expiraEn, usuario);
    }
}
