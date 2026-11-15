package br.ufla.athena.requisitos.api;

import br.ufla.athena.requisitos.dominio.Publicacao;
import java.time.Instant;
import java.util.UUID;

/** Schema Publicacao de docs/api/requisitos.yaml. */
public record PublicacaoResposta(
        UUID publicacaoId,
        UUID requisitoId,
        UUID versaoId,
        UUID submissaoId,
        UUID retratoId,
        UUID autor,
        Instant data) {

    public static PublicacaoResposta de(Publicacao publicacao) {
        return new PublicacaoResposta(publicacao.getId(), publicacao.getRequisitoId(), publicacao.getVersaoId(),
                publicacao.getSubmissaoId(), publicacao.getRetratoId(), publicacao.getAutor(), publicacao.getData());
    }
}
