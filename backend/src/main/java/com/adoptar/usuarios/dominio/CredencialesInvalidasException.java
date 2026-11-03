package com.adoptar.usuarios.dominio;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Login fallido. Se traduce a HTTP 401.
 * <p>
 * El mensaje es siempre el mismo, sea que el mail no exista, la contraseña esté mal o la
 * cuenta esté desactivada: así nadie puede averiguar qué mails están registrados.
 */
public class CredencialesInvalidasException extends ResponseStatusException {

    public CredencialesInvalidasException() {
        super(HttpStatus.UNAUTHORIZED, "Mail o contraseña incorrectos");
    }
}
