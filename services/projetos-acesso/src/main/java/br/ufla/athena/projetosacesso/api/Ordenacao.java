package br.ufla.athena.projetosacesso.api;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Ordenações aceitas nas coleções (livro §6.2). O id é sempre o desempate, para a
 * paginação ser estável. Valor fora desta lista retorna 400.
 */
final class Ordenacao {

    static final String PADRAO = "atualizadoEm,desc";

    private Ordenacao() {
    }

    static Sort de(String parametro) {
        return switch (parametro) {
            case "atualizadoEm,desc" -> Sort.by(Sort.Order.desc("atualizadoEm"), Sort.Order.asc("id"));
            case "atualizadoEm,asc" -> Sort.by(Sort.Order.asc("atualizadoEm"), Sort.Order.asc("id"));
            case "id,asc" -> Sort.by(Sort.Order.asc("id"));
            case "id,desc" -> Sort.by(Sort.Order.desc("id"));
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ordenação não suportada: " + parametro);
        };
    }
}
