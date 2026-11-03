package com.adoptar.usuarios.dominio;

import com.adoptar.shared.exception.ReglaDeNegocioException;

/**
 * Se lanza al registrar una cuenta con un mail que ya existe. Se traduce a HTTP 409.
 */
public class EmailYaRegistradoException extends ReglaDeNegocioException {

    public EmailYaRegistradoException(String email) {
        super("Ya existe una cuenta con el mail " + email);
    }
}
