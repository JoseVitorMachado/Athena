package br.ufla.athena.requisitos.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Representação de um comentário e sua âncora opcional. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ComentarioResposta(UUID comentarioId, UUID requisitoId, UUID versaoId, UUID autor, String texto,
        Ancora ancora, List<UUID> mencoes, boolean resolvido, Instant atualizadoEm) {
    public record Ancora(String selecao, String situacao) { }
}
