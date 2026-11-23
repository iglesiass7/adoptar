package com.adoptar.usuarios.dominio;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Al cambiar la contraseña, la actual no coincide. Se traduce a HTTP 400.
 * <p>
 * No es 401 a propósito: el usuario sí está logueado, y un 401 haría que el frontend lo saque.
 */
public class PasswordActualIncorrectaException extends ResponseStatusException {

    public PasswordActualIncorrectaException() {
        super(HttpStatus.BAD_REQUEST, "La contraseña actual no es correcta");
    }
}
