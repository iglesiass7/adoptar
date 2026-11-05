package com.adoptar.notificaciones.dominio;

/**
 * Tipos de notificaciones automáticas del sistema AdoptAR.
 * <p>
 * Se corresponden con los eventos del ciclo de vida de una solicitud de adopción
 * y avisos generales a los usuarios (adoptantes y refugios).
 */
public enum TipoNotificacion {
    /** El refugio recibe aviso de que ingresó una nueva postulación para su mascota. */
    SOLICITUD_RECIBIDA,

    /** El adoptante recibe aviso de que el refugio comenzó a revisar su postulación. */
    SOLICITUD_EN_REVISION,

    /** El adoptante recibe aviso de que se coordinó o agendó una entrevista. */
    ENTREVISTA_PROGRAMADA,

    /** El adoptante recibe aviso de que su postulación fue aprobada (mascota reservada). */
    SOLICITUD_APROBADA,

    /** El adoptante recibe aviso de que su postulación fue rechazada (incluye motivo). */
    SOLICITUD_RECHAZADA,

    /** Aviso cuando una solicitud se cancela. */
    SOLICITUD_CANCELADA,

    /** Aviso cuando una solicitud pasa 30 días sin revisión y expira automáticamente. */
    SOLICITUD_EXPIRADA,

    /** Notificación final cuando se confirma y concreta la entrega de la mascota. */
    ADOPCION_COMPLETADA,

    /** Notificaciones generales del sistema o avisos administrativos. */
    SISTEMA
}

