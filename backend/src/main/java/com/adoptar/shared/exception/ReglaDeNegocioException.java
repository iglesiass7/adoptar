package com.adoptar.shared.exception;

/**
 * Se lanza cuando el pedido tiene buen formato pero viola una regla del dominio:
 * la mascota no está disponible, el adoptante ya tiene 3 solicitudes activas, etc.
 * <p>
 * Se traduce a HTTP 409 (Conflict). Las excepciones de cada módulo extienden de esta,
 * por ejemplo {@code TransicionInvalidaException} en el módulo de solicitudes.
 */
public class ReglaDeNegocioException extends RuntimeException {

    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }
}
