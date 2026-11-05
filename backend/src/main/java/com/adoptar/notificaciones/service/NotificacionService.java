package com.adoptar.notificaciones.service;

import com.adoptar.notificaciones.dominio.Notificacion;
import com.adoptar.notificaciones.dominio.SolicitudEstadoCambiadoEvent;
import com.adoptar.notificaciones.dominio.TipoNotificacion;
import com.adoptar.notificaciones.dto.NotificacionResponse;
import com.adoptar.notificaciones.repository.NotificacionRepository;
import com.adoptar.shared.exception.AccesoDenegadoException;
import com.adoptar.shared.exception.RecursoNoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de aplicación para la gestión de notificaciones.
 * <p>
 * Orquesta los casos de uso CU-25 y CU-26, abre transacciones y hace cumplir
 * las reglas de acceso (un usuario solo ve y modifica sus propias notificaciones).
 * <p>
 * Implementa el patrón Observer mediante un listener de eventos de Spring ({@link #alCambiarEstadoSolicitud})
 * para recibir avisos de otros módulos de forma desacoplada.
 */
@Service
@Transactional(readOnly = true)
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    /**
     * CU-25: Lista las notificaciones de un usuario ordenadas por fecha descendente.
     */
    public List<NotificacionResponse> obtenerNotificacionesDeUsuario(Long usuarioId, boolean soloNoLeidas) {
        List<Notificacion> notificaciones = soloNoLeidas
                ? notificacionRepository.findByUsuarioIdAndLeidaFalseOrderByCreatedAtDesc(usuarioId)
                : notificacionRepository.findByUsuarioIdOrderByCreatedAtDesc(usuarioId);

        return notificaciones.stream()
                .map(NotificacionResponse::desdeEntidad)
                .toList();
    }

    /**
     * Cuenta la cantidad de notificaciones pendientes de leer del usuario.
     */
    public long contarNoLeidas(Long usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    /**
     * Obtiene una notificación específica validando que pertenezca al usuario autenticado.
     */
    public NotificacionResponse obtenerPorId(Long id, Long usuarioId) {
        Notificacion notificacion = buscarPorIdYValidarPertenencia(id, usuarioId);
        return NotificacionResponse.desdeEntidad(notificacion);
    }

    /**
     * CU-26: Marca una notificación como leída.
     */
    @Transactional
    public NotificacionResponse marcarComoLeida(Long id, Long usuarioId) {
        Notificacion notificacion = buscarPorIdYValidarPertenencia(id, usuarioId);
        notificacion.marcarComoLeida();
        Notificacion guardada = notificacionRepository.save(notificacion);
        return NotificacionResponse.desdeEntidad(guardada);
    }

    /**
     * Marca todas las notificaciones pendientes del usuario como leídas en lote.
     */
    @Transactional
    public void marcarTodasComoLeidas(Long usuarioId) {
        notificacionRepository.marcarTodasComoLeidasDeUsuario(usuarioId);
    }

    /**
     * Elimina una notificación asegurando que pertenezca al usuario.
     */
    @Transactional
    public void eliminar(Long id, Long usuarioId) {
        Notificacion notificacion = buscarPorIdYValidarPertenencia(id, usuarioId);
        notificacionRepository.delete(notificacion);
    }

    /**
     * Crea y persiste una nueva notificación en el sistema.
     */
    @Transactional
    public NotificacionResponse crearNotificacion(Long usuarioId, TipoNotificacion tipo, String mensaje) {
        Notificacion notificacion = Notificacion.crear(usuarioId, tipo, mensaje);
        Notificacion guardada = notificacionRepository.save(notificacion);
        log.info("Notificación creada para usuario {}: [{}] {}", usuarioId, tipo, mensaje);
        return NotificacionResponse.desdeEntidad(guardada);
    }

    /**
     * Patrón Observer: escucha eventos de dominio emitidos ante cambios en las solicitudes
     * de adopción y genera la notificación automáticamente sin acoplar los módulos.
     */
    @EventListener
    @Transactional
    public void alCambiarEstadoSolicitud(SolicitudEstadoCambiadoEvent evento) {
        String mensaje = construirMensaje(evento);
        crearNotificacion(evento.usuarioDestinatarioId(), evento.tipo(), mensaje);
    }

    private Notificacion buscarPorIdYValidarPertenencia(Long id, Long usuarioId) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación", id));

        if (!notificacion.perteneceA(usuarioId)) {
            throw new AccesoDenegadoException("No tenés permiso para acceder a esta notificación");
        }

        return notificacion;
    }

    private String construirMensaje(SolicitudEstadoCambiadoEvent evento) {
        if (evento.detalle() != null && !evento.detalle().isBlank()) {
            return "Tu solicitud para " + evento.nombreMascota() + ": " + evento.detalle();
        }
        return "Tu solicitud para " + evento.nombreMascota() + " ha cambiado al estado: " + evento.nuevoEstado();
    }
}

