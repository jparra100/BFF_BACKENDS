package cl.duoc.bancoxyz.web.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> contenido,
        int pagina,
        int tamanio,
        long totalElementos,
        int totalPaginas
) {
    public static <T> PageResponse<T> from(List<T> content, int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("La página no puede ser negativa");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("El tamaño debe estar entre 1 y 100");
        }

        long offset = (long) page * size;
        int fromIndex = (int) Math.min(offset, content.size());
        int toIndex = Math.min(fromIndex + size, content.size());
        int pages = content.isEmpty() ? 0 : (int) Math.ceil((double) content.size() / size);

        return new PageResponse<>(
                List.copyOf(content.subList(fromIndex, toIndex)),
                page,
                size,
                content.size(),
                pages
        );
    }
}

