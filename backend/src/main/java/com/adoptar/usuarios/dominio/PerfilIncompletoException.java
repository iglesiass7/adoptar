package com.adoptar.usuarios.dominio;

import com.adoptar.shared.exception.ReglaDeNegocioException;

/**
 * Regla 4: un adoptante sin perfil completo no puede enviar solicitudes. Se traduce a HTTP 409.
 */
public class PerfilIncompletoException extends ReglaDeNegocioException {

    public PerfilIncompletoException() {
        super("Completá tu perfil de adoptante antes de enviar una solicitud");
    }
}
