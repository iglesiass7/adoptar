package com.adoptar.usuarios.dominio;

import com.adoptar.shared.exception.ReglaDeNegocioException;

/**
 * El CUIT es único: no puede haber dos refugios con el mismo. Se traduce a HTTP 409.
 */
public class CuitYaRegistradoException extends ReglaDeNegocioException {

    public CuitYaRegistradoException(String cuit) {
        super("Ya existe un refugio con el CUIT " + cuit);
    }
}
