package com.adoptar.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cambio de contraseña (CU-05). Pide la actual para que nadie la cambie con un token robado.
 */
public record CambioPasswordRequest(
        @NotBlank String passwordActual,
        @NotBlank @Size(min = 8, max = 72) String passwordNueva) {
}
