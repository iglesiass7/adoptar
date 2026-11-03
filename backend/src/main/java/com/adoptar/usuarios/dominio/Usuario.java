package com.adoptar.usuarios.dominio;

import com.adoptar.shared.EntidadAuditada;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Locale;

/**
 * Cuenta de acceso al sistema (tabla usuarios). Cada adoptante, refugio y admin tiene una.
 * <p>
 * La contraseña nunca se guarda tal cual: solo su hash BCrypt.
 */
@Entity
@Table(name = "usuarios")
public class Usuario extends EntidadAuditada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String nombre;

    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo;

    /** Solo para JPA. */
    protected Usuario() {
    }

    private Usuario(String email, String passwordHash, String nombre, String telefono, Rol rol) {
        this.email = normalizarEmail(email);
        this.passwordHash = passwordHash;
        this.nombre = nombre.trim();
        this.telefono = telefono;
        this.rol = rol;
        this.activo = true;
    }

    /** Crea la cuenta de un adoptante. El perfil (tabla adoptantes) se completa después, en CU-04. */
    public static Usuario nuevoAdoptante(String email, String passwordHash, String nombre, String telefono) {
        return new Usuario(email, passwordHash, nombre, telefono, Rol.ADOPTANTE);
    }

    /** El mail se guarda sin espacios y en minúsculas: "Ana@X.com " y "ana@x.com" son el mismo. */
    public static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }
}
