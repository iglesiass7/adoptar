package com.adoptar.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Datos para iniciar sesión.
 */
public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password) {
}
