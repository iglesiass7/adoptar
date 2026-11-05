package com.adoptar.notificaciones.dominio;

/**
 * Evento de dominio emitido ante un cambio de estado en una solicitud de adopción.
 * <p>
 * Implementa el patrón Observer: el caso de uso que cambia el estado de la solicitud
 * publica este evento, y el módulo de notificaciones lo escucha para generar el aviso
 * correspondiente sin acoplar ambos módulos.
 */
public record SolicitudEstadoCambiadoEvent(
        Long usuarioDestinatarioId,
        Long solicitudId,
        TipoNotificacion tipo,
        String nombreMascota,
        String nuevoEstado,
        String detalle
) {}

