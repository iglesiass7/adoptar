package com.adoptar.usuarios.dominio;

import com.adoptar.shared.EntidadAuditada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * Perfil del adoptante (tabla adoptantes). Se crea al completar el perfil (CU-04).
 * <p>
 * "Perfil completo" significa que existe esta fila: sin ella el adoptante no puede enviar
 * solicitudes (regla 4). Comparte el id con su cuenta en usuarios.
 */
@Entity
@Table(name = "adoptantes")
public class Adoptante extends EntidadAuditada {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false)
    private String provincia;

    @Column(nullable = false)
    private String localidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vivienda", nullable = false)
    private TipoVivienda tipoVivienda;

    @Column(name = "tiene_patio", nullable = false)
    private boolean tienePatio;

    @Column(name = "otras_mascotas", nullable = false)
    private boolean otrasMascotas;

    /** Solo para JPA. */
    protected Adoptante() {
    }

    public Adoptante(Long usuarioId, LocalDate fechaNacimiento, String provincia, String localidad,
                     TipoVivienda tipoVivienda, boolean tienePatio, boolean otrasMascotas) {
        this.usuarioId = usuarioId;
        actualizar(fechaNacimiento, provincia, localidad, tipoVivienda, tienePatio, otrasMascotas);
    }

    /** Reemplaza los datos del perfil por los nuevos. */
    public void actualizar(LocalDate fechaNacimiento, String provincia, String localidad,
                           TipoVivienda tipoVivienda, boolean tienePatio, boolean otrasMascotas) {
        this.fechaNacimiento = fechaNacimiento;
        this.provincia = provincia.trim();
        this.localidad = localidad.trim();
        this.tipoVivienda = tipoVivienda;
        this.tienePatio = tienePatio;
        this.otrasMascotas = otrasMascotas;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getProvincia() {
        return provincia;
    }

    public String getLocalidad() {
        return localidad;
    }

    public TipoVivienda getTipoVivienda() {
        return tipoVivienda;
    }

    public boolean isTienePatio() {
        return tienePatio;
    }

    public boolean isOtrasMascotas() {
        return otrasMascotas;
    }
}
