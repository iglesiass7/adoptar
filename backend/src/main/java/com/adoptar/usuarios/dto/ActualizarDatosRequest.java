package com.adoptar.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos de la cuenta que el usuario puede cambiar (CU-05). El mail y el rol no se cambian.
 */
public record ActualizarDatosRequest(
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 30) String telefono) {
}
