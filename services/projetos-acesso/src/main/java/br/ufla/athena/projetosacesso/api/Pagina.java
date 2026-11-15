package br.ufla.athena.projetosacesso.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

/** Envelope de paginação das coleções (livro §6.2). */
public record Pagina<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        @JsonProperty("_links") Map<String, Link> links) {

    public record Link(String href) {
    }

    public static <T> Pagina<T> de(Page<T> pagina, String linkSelf) {
        return new Pagina<>(pagina.getContent(), pagina.getNumber(), pagina.getSize(),
                pagina.getTotalElements(), pagina.getTotalPages(), Map.of("self", new Link(linkSelf)));
    }
}
