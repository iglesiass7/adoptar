package com.adoptar.usuarios.dto;

import com.adoptar.usuarios.dominio.Adoptante;
import com.adoptar.usuarios.dominio.TipoVivienda;
import java.time.LocalDate;

/**
 * Lo que la API muestra del perfil de un adoptante.
 */
public record PerfilAdoptanteResponse(Long usuarioId, LocalDate fechaNacimiento, String provincia,
                                      String localidad, TipoVivienda tipoVivienda, boolean tienePatio,
                                      boolean otrasMascotas) {

    public static PerfilAdoptanteResponse de(Adoptante adoptante) {
        return new PerfilAdoptanteResponse(adoptante.getUsuarioId(), adoptante.getFechaNacimiento(),
                adoptante.getProvincia(), adoptante.getLocalidad(), adoptante.getTipoVivienda(),
                adoptante.isTienePatio(), adoptante.isOtrasMascotas());
    }
}
