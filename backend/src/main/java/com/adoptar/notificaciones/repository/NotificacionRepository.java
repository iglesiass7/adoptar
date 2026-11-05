package com.adoptar.notificaciones.repository;

import com.adoptar.notificaciones.dominio.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad {@link Notificacion}.
 * <p>
 * Aprovecha el índice de PostgreSQL {@code ix_notificaciones_usuario (usuario_id, leida, created_at DESC)}
 * para consultas eficientes sobre el buzón de notificaciones de cada usuario.
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /**
     * Obtiene todas las notificaciones de un usuario ordenadas por fecha descendente (más nuevas primero).
     */
    List<Notificacion> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);

    /**
     * Obtiene solo las notificaciones pendientes de lectura de un usuario.
     */
    List<Notificacion> findByUsuarioIdAndLeidaFalseOrderByCreatedAtDesc(Long usuarioId);

    /**
     * Cuenta la cantidad de notificaciones no leídas de un usuario.
     */
    long countByUsuarioIdAndLeidaFalse(Long usuarioId);

    /**
     * Busca una notificación asegurando que pertenezca al usuario indicado.
     */
    Optional<Notificacion> findByIdAndUsuarioId(Long id, Long usuarioId);

    /**
     * Actualiza en lote todas las notificaciones no leídas de un usuario al estado leída.
     */
    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.usuarioId = :usuarioId AND n.leida = false")
    int marcarTodasComoLeidasDeUsuario(@Param("usuarioId") Long usuarioId);
}

