/**
 * M5 — Notificaciones.
 * <p>
 * Avisos a los usuarios ante cada cambio de estado de una solicitud (patrón Observer,
 * con eventos de dominio). Casos de uso CU-25 y CU-26. Tabla: {@code notificaciones}.
 * <p>
 * Dentro del paquete se respetan las capas: Controller → Service → Repository → Dominio.
 * Controller: Get, update, pause, delete, next
 * service: logica de negocio de las notificaciones
 * Repository: acceso a la base de datos
 * Dominio: contiene las entidades, los objetos de valor, las reglas de cómo funcionan el negocio
 */
package com.adoptar.notificaciones;
