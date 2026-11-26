package com.xochitepec.patrimonio.common;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

/** Página serializable estable (evita exponer PageImpl de Spring Data). */
public record PageResponse<T>(List<T> contenido, int pagina, int tamanio, long totalElementos, int totalPaginas) {

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(page.getContent().stream().map(mapper).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
