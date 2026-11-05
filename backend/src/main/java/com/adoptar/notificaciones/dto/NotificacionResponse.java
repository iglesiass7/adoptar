package com.adoptar.notificaciones.dto;

import com.adoptar.notificaciones.dominio.Notificacion;
import com.adoptar.notificaciones.dominio.TipoNotificacion;

import java.time.Instant;

/**
 * DTO (Data Transfer Object) de respuesta para las notificaciones.
 * <p>
 * Representa el contrato JSON que viaja hacia el frontend.
 * Evita exponer directamente la entidad de persistencia JPA.
 */
public record NotificacionResponse(
        Long id,
        Long usuarioId,
        TipoNotificacion tipo,
        String mensaje,
        boolean leida,
        Instant fechaCreacion
) {
    /**
     * Mapea una entidad {@link Notificacion} a su DTO de salida {@link NotificacionResponse}.
     */
    public static NotificacionResponse desdeEntidad(Notificacion notificacion) {
        return new NotificacionResponse(
                notificacion.getId(),
                notificacion.getUsuarioId(),
                notificacion.getTipo(),
                notificacion.getMensaje(),
                notificacion.isLeida(),
                notificacion.getCreatedAt()
        );
    }
}

