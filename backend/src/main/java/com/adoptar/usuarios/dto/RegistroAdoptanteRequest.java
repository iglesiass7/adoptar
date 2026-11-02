package com.adoptar.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear la cuenta de un adoptante (CU-01).
 * <p>
 * No tiene campo "rol" a propósito: si alguien manda {@code "rol": "ADMIN"}, se ignora.
 */
public record RegistroAdoptanteRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 30) String telefono) {
}
