package com.adoptar.usuarios.dominio;

/**
 * Los tres roles del sistema. Se guardan como texto en usuarios.rol
 * (ver el CHECK ck_usuarios_rol de V1).
 * <p>
 * El rol nunca lo elige quien se registra: lo decide el endpoint que usa.
 * ADMIN no tiene registro público.
 */
public enum Rol {
    ADOPTANTE,
    REFUGIO,
    ADMIN
}
