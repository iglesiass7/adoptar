package com.adoptar.notificaciones.dominio;

import com.adoptar.shared.EntidadAuditada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Entidad de dominio que representa una notificación enviada a un usuario.
 * <p>
 * Mapea la tabla {@code notificaciones} de PostgreSQL.
 * Extiende {@link EntidadAuditada} para que Spring maneje automáticamente {@code created_at} y {@code updated_at}.
 * <p>
 * Contiene la lógica de negocio y las reglas del dominio sobre las notificaciones.
 */
@Entity
@Table(name = "notificaciones")
public class Notificacion extends EntidadAuditada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoNotificacion tipo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(nullable = false)
    private boolean leida = false;

    /** Constructor requerido por JPA / Hibernate. */
    protected Notificacion() {
    }

    /**
     * Constructor principal del dominio.
     */
    public Notificacion(Long usuarioId, TipoNotificacion tipo, String mensaje) {
        this.usuarioId = Objects.requireNonNull(usuarioId, "El usuarioId no puede ser nulo");
        this.tipo = Objects.requireNonNull(tipo, "El tipo de notificación no puede ser nulo");
        this.mensaje = validarMensaje(mensaje);
        this.leida = false;
    }

    /**
     * Método fábrica de dominio para crear una nueva notificación no leída.
     */
    public static Notificacion crear(Long usuarioId, TipoNotificacion tipo, String mensaje) {
        return new Notificacion(usuarioId, tipo, mensaje);
    }

    /**
     * Regla de negocio: marcar la notificación como leída.
     */
    public void marcarComoLeida() {
        this.leida = true;
    }

    /**
     * Regla de negocio / seguridad: verifica si la notificación pertenece al usuario indicado.
     */
    public boolean perteneceA(Long usuarioId) {
        return this.usuarioId != null && this.usuarioId.equals(usuarioId);
    }

    private static String validarMensaje(String mensaje) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            throw new IllegalArgumentException("El mensaje de la notificación no puede estar vacío");
        }
        if (mensaje.length() > 500) {
            throw new IllegalArgumentException("El mensaje no puede superar los 500 caracteres");
        }
        return mensaje.trim();
    }

    // --- Getters ---

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public boolean isLeida() {
        return leida;
    }
}

