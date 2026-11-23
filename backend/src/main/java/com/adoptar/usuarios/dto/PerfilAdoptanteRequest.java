package com.adoptar.usuarios.dto;

import com.adoptar.usuarios.dominio.TipoVivienda;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Datos del perfil del adoptante (CU-04). Todos son obligatorios: así queda "completo".
 */
public record PerfilAdoptanteRequest(
        @NotNull @Past LocalDate fechaNacimiento,
        @NotBlank @Size(max = 60) String provincia,
        @NotBlank @Size(max = 80) String localidad,
        @NotNull TipoVivienda tipoVivienda,
        @NotNull Boolean tienePatio,
        @NotNull Boolean otrasMascotas) {
}
