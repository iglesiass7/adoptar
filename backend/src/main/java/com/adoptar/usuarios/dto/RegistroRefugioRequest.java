package com.adoptar.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para registrar un refugio (CU-02): la cuenta de acceso y los datos básicos de su ficha.
 * <p>
 * Igual que en el registro de adoptante, no hay campo "rol" ni "estado": el refugio siempre
 * nace con rol REFUGIO y estado PENDIENTE, aunque el cliente mande otra cosa.
 */
public record RegistroRefugioRequest(
        // Cuenta (tabla usuarios)
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 30) String telefono,
        // Ficha (tabla refugios)
        @NotBlank @Size(max = 150) String razonSocial,
        @NotBlank @Pattern(regexp = "^[0-9]{2}-[0-9]{8}-[0-9]$", message = "debe tener el formato 30-12345678-9")
        String cuit,
        String descripcion,
        @Size(max = 200) String direccion,
        @NotBlank @Size(max = 60) String provincia,
        @NotBlank @Size(max = 80) String localidad) {
}
