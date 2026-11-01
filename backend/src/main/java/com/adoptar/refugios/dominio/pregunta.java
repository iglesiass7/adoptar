package com.adoptar.refugios.dominio;

import com.adoptar.shared.EntidadAuditada;
import jakarta.persistence.*;

@Entity
@Table(name = "preguntas")
public class pregunta extends EntidadAuditada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refugio_id", nullable = false)
    private refugio refugio;

    @Column(nullable = false, length = 300)
    private String texto;

    @Column(nullable = false, length = 10)
    private String tipo; // TEXTO, SI_NO, NUMERO (según el SQL)

    @Column(nullable = false)
    private Boolean obligatoria = true;

    @Column(nullable = false)
    private Short orden = 0;

    @Column(nullable = false)
    private Boolean activa = true;

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public refugio getRefugio() { return refugio; }
    public void setRefugio(refugio refugio) { this.refugio = refugio; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Boolean getObligatoria() { return obligatoria; }
    public void setObligatoria(Boolean obligatoria) { this.obligatoria = obligatoria; }

    public Short getOrden() { return orden; }
    public void setOrden(Short orden) { this.orden = orden; }

    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
}