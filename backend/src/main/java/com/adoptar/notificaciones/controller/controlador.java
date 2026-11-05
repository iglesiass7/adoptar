package com.adoptar.notificaciones.controller;

import com.adoptar.notificaciones.dto.NotificacionResponse;
import com.adoptar.notificaciones.service.NotificacionService;
import com.adoptar.shared.exception.AccesoDenegadoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el módulo M5 (Notificaciones).
 * <p>
 * Responsabilidad de la capa: traducir peticiones HTTP, extraer la identidad
 * del usuario logueado a partir del token JWT y delegar la orquestación y lógica
 * al {@link NotificacionService}.
 * <p>
 * Casos de uso cubiertos:
 * <ul>
 *   <li>CU-25: Consultar notificaciones del usuario autenticado (con opción de filtrar solo no leídas).</li>
 *   <li>CU-26: Marcar notificación como leída.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/notificaciones")
@Tag(name = "Notificaciones", description = "Avisos automáticos a los usuarios ante eventos y cambios de estado de solicitudes")
public class controlador {

    private final NotificacionService notificacionService;

    public controlador(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    /**
     * CU-25: Obtiene las notificaciones del usuario logueado.
     *
     * @param token          Token JWT inyectado por Spring Security con la sesión del usuario.
     * @param soloNoLeidas   Filtro opcional: si es true, devuelve solo las pendientes de lectura.
     * @return Lista de notificaciones ordenadas cronológicamente (más recientes primero).
     */
    @GetMapping
    @Operation(
            summary = "Listar notificaciones del usuario (CU-25)",
            description = "Devuelve el listado de notificaciones del usuario autenticado. "
                    + "Por defecto incluye todas; se puede filtrar con ?soloNoLeidas=true."
    )
    public List<NotificacionResponse> obtenerNotificaciones(
            @AuthenticationPrincipal Jwt token,
            @RequestParam(required = false, defaultValue = "false") boolean soloNoLeidas) {
        Long usuarioId = extraerUsuarioId(token);
        return notificacionService.obtenerNotificacionesDeUsuario(usuarioId, soloNoLeidas);
    }

    /**
     * Devuelve la cantidad de notificaciones no leídas del usuario.
     * Útil para pintar badges y la campanita de notificaciones en el frontend.
     */
    @GetMapping("/no-leidas/cantidad")
    @Operation(
            summary = "Cantidad de notificaciones no leídas",
            description = "Devuelve el número total de notificaciones pendientes de leer del usuario actual."
    )
    public long contarNoLeidas(@AuthenticationPrincipal Jwt token) {
        Long usuarioId = extraerUsuarioId(token);
        return notificacionService.contarNoLeidas(usuarioId);
    }

    /**
     * Consulta el detalle de una notificación por su ID.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener notificación por ID",
            description = "Devuelve una notificación específica si pertenece al usuario logueado."
    )
    public NotificacionResponse obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt token) {
        Long usuarioId = extraerUsuarioId(token);
        return notificacionService.obtenerPorId(id, usuarioId);
    }

    /**
     * CU-26: Marcar una notificación como leída.
     *
     * @param id    ID de la notificación a marcar.
     * @param token Token del usuario (se verifica pertenencia en el service).
     * @return La notificación con el estado actualizado.
     */
    @PatchMapping("/{id}/leida")
    @Operation(
            summary = "Marcar notificación como leída (CU-26)",
            description = "Marca una notificación como leída verificando que pertenezca al usuario autenticado."
    )
    public NotificacionResponse marcarComoLeida(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt token) {
        Long usuarioId = extraerUsuarioId(token);
        return notificacionService.marcarComoLeida(id, usuarioId);
    }

    /**
     * Marca todas las notificaciones pendientes del usuario como leídas.
     */
    @PatchMapping("/marcar-todas-leidas")
    @Operation(
            summary = "Marcar todas las notificaciones como leídas",
            description = "Actualiza todas las notificaciones no leídas del usuario al estado leída."
    )
    public ResponseEntity<Void> marcarTodasComoLeidas(@AuthenticationPrincipal Jwt token) {
        Long usuarioId = extraerUsuarioId(token);
        notificacionService.marcarTodasComoLeidas(usuarioId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina / descarta una notificación.
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar notificación",
            description = "Elimina permanentemente la notificación del usuario."
    )
    public ResponseEntity<Void> eliminarNotificacion(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt token) {
        Long usuarioId = extraerUsuarioId(token);
        notificacionService.eliminar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Extrae el ID del usuario del subject del token JWT emitido en el login.
     */
    private Long extraerUsuarioId(Jwt token) {
        if (token == null || token.getSubject() == null) {
            throw new AccesoDenegadoException("Se requiere autenticación para acceder a las notificaciones");
        }
        return Long.valueOf(token.getSubject());
    }
}

