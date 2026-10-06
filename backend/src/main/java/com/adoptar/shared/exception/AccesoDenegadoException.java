package com.adoptar.shared.exception;

/**
 * Se lanza cuando el usuario está autenticado pero el recurso no es suyo
 * (por ejemplo, un refugio que intenta aprobar una solicitud de otro refugio).
 * Se traduce a HTTP 403.
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
