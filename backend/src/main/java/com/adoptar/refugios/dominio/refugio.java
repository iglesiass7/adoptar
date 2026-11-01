package com.adoptar.refugios.dominio;

import com.adoptar.shared.EntidadAuditada;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "refugios")
public class refugio extends EntidadAuditada {

    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(nullable = false, unique = true, length = 13)
    private String cuit;

    private String descripcion;
    private String direccion;

    @Column(nullable = false)
    private String provincia;

    @Column(nullable = false)
    private String localidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_verificacion", nullable = false)
    private estadoverificacion estadoVerificacion = estadoverificacion.PENDIENTE;

    // Relación con el cuestionario (M3, pero lo maneja el refugio)
    @OneToMany(mappedBy = "refugio", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<pregunta> cuestionario = new ArrayList<>();

    // --- Getters y Setters ---
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }

    public String getCuit() { return cuit; }
    public void setCuit(String cuit) { this.cuit = cuit; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }

    public estadoverificacion getEstadoVerificacion() { return estadoVerificacion; }
    public void setEstadoVerificacion(estadoverificacion estadoVerificacion) { this.estadoVerificacion = estadoVerificacion; }

    public List<pregunta> getCuestionario() { return cuestionario; }
    public void setCuestionario(List<pregunta> cuestionario) { this.cuestionario = cuestionario; }

    // Regla de negocio clave
    public boolean puedePublicar() {
        return this.estadoVerificacion == estadoverificacion.VERIFICADO;
    }
}