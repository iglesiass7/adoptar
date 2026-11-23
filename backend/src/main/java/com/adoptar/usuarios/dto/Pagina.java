package com.adoptar.usuarios.dto;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/**
 * Una página de resultados, con los datos necesarios para pedir la siguiente.
 */
public record Pagina<T>(List<T> contenido, int pagina, int tamanio, long totalElementos, int totalPaginas) {

    public static <E, T> Pagina<T> de(Page<E> pagina, Function<E, T> convertir) {
        return new Pagina<>(pagina.getContent().stream().map(convertir).toList(), pagina.getNumber(),
                pagina.getSize(), pagina.getTotalElements(), pagina.getTotalPages());
    }
}
