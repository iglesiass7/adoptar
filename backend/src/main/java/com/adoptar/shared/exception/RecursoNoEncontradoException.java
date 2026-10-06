package com.adoptar.shared.exception;

/**
 * Se lanza cuando se pide algo que no existe. Se traduce a HTTP 404.
 * <p>
 * Ejemplo: {@code throw new RecursoNoEncontradoException("Mascota", id);}
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Object id) {
        super(recurso + " con id " + id + " no existe");
    }
}
