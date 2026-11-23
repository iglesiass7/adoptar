package com.adoptar.usuarios.repository;

import com.adoptar.usuarios.dto.RegistroRefugioRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Crea la ficha inicial del refugio en la tabla refugios al registrarse (CU-02).
 * <p>
 * La tabla y su entidad son del módulo de refugios (M4). Para no tocar ese módulo, el registro
 * solo inserta la fila con lo mínimo y estado PENDIENTE; todo lo demás (ver, editar, verificar)
 * lo hace M4. Cuando M4 tenga un método para crear la ficha, esta clase se puede reemplazar
 * por una llamada a ese método.
 */
@Repository
public class FichaRefugioRepository {

    private final JdbcTemplate jdbc;

    public FichaRefugioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean existeCuit(String cuit) {
        return Boolean.TRUE.equals(
                jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM refugios WHERE cuit = ?)", Boolean.class, cuit));
    }

    /** Inserta la ficha con estado PENDIENTE (regla 11: hasta que el admin lo verifique, no publica). */
    public void crearPendiente(Long usuarioId, RegistroRefugioRequest datos) {
        jdbc.update("""
                        INSERT INTO refugios (usuario_id, razon_social, cuit, descripcion, direccion,
                                              provincia, localidad, estado_verificacion)
                        VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDIENTE')
                        """,
                usuarioId, datos.razonSocial().trim(), datos.cuit(), datos.descripcion(), datos.direccion(),
                datos.provincia().trim(), datos.localidad().trim());
    }
}
