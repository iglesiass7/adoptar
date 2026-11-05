package com.adoptar.notificaciones.controller;

import com.adoptar.notificaciones.dto.NotificacionResponse;
import com.adoptar.notificaciones.service.NotificacionService;
import com.adoptar.shared.exception.AccesoDenegadoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el módulo M5 (Notificaciones).
 * <p>
 * Responsabilidad de la capa: traducir peticiones HTTP, identificar al usuario
 * y delegar la orquestación y lógica de negocio a {@link NotificacionService}.
 * <p>
 * Casos de uso cubiertos:
 * <ul>
 *   <li>CU-25: Consultar notificaciones del usuario autenticado (con filtro opcional de no leídas).</li>
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
     * @param authentication  Objeto de autenticación de Spring Security (compatible con JWT cuando esté configurado).
     * @param usuarioIdHeader Header opcional para probar endpoints antes del módulo de autenticación (X-Usuario-Id).
     * @param soloNoLeidas    Filtro opcional: si es true, devuelve solo las pendientes de lectura.
     * @return Lista de notificaciones ordenadas cronológicamente (más recientes primero).
     */
    @GetMapping
    @Operation(
            summary = "Listar notificaciones del usuario (CU-25)",
            description = "Devuelve el listado de notificaciones del usuario autenticado ordenadas de más recientes a más antiguas. "
                    + "Permite filtrar solo no leídas con ?soloNoLeidas=true."
    )
    public List<NotificacionResponse> obtenerNotificaciones(
            Authentication authentication,
            @Parameter(description = "ID del usuario para pruebas si no hay JWT configurado")
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader,
            @RequestParam(required = false, defaultValue = "false") boolean soloNoLeidas) {
        Long usuarioId = extraerUsuarioId(authentication, usuarioIdHeader);
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
    public long contarNoLeidas(
            Authentication authentication,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader) {
        Long usuarioId = extraerUsuarioId(authentication, usuarioIdHeader);
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
            Authentication authentication,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader) {
        Long usuarioId = extraerUsuarioId(authentication, usuarioIdHeader);
        return notificacionService.obtenerPorId(id, usuarioId);
    }

    /**
     * CU-26: Marcar una notificación como leída.
     *
     * @param id ID de la notificación a marcar.
     * @return La notificación con el estado actualizado.
     */
    @PatchMapping("/{id}/leida")
    @Operation(
            summary = "Marcar notificación como leída (CU-26)",
            description = "Marca una notificación como leída verificando que pertenezca al usuario autenticado."
    )
    public NotificacionResponse marcarComoLeida(
            @PathVariable Long id,
            Authentication authentication,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader) {
        Long usuarioId = extraerUsuarioId(authentication, usuarioIdHeader);
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
    public ResponseEntity<Void> marcarTodasComoLeidas(
            Authentication authentication,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader) {
        Long usuarioId = extraerUsuarioId(authentication, usuarioIdHeader);
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
            Authentication authentication,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioIdHeader) {
        Long usuarioId = extraerUsuarioId(authentication, usuarioIdHeader);
        notificacionService.eliminar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Extrae el ID del usuario desde la autenticación de Spring Security o desde el header de pruebas X-Usuario-Id.
     */
    private Long extraerUsuarioId(Authentication authentication, Long usuarioIdHeader) {
        if (authentication != null && authentication.getName() != null && !authentication.getName().equals("anonymousUser")) {
            try {
                return Long.valueOf(authentication.getName());
            } catch (NumberFormatException ignored) {
                // Si el principal no es numérico directo
            }
        }
        if (usuarioIdHeader != null) {
            return usuarioIdHeader;
        }
        throw new AccesoDenegadoException("Se requiere usuario autenticado o encabezado X-Usuario-Id");
    }
}
