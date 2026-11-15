package br.ufla.athena.rastreabilidade.api;

import br.ufla.athena.rastreabilidade.dominio.Relacao;
import br.ufla.athena.rastreabilidade.dominio.TipoRelacao;
import java.time.Instant;
import java.util.UUID;

/** Schema Relacao de docs/api/rastreabilidade.yaml. */
public record RelacaoResposta(
        UUID relacaoId,
        UUID projetoId,
        UUID origemId,
        UUID destinoId,
        TipoRelacao tipo,
        String justificativa,
        Instant atualizadoEm) {

    public static RelacaoResposta de(Relacao relacao) {
        return new RelacaoResposta(relacao.getId(), relacao.getProjetoId(), relacao.getOrigemId(),
                relacao.getDestinoId(), relacao.getTipo(), relacao.getJustificativa(), relacao.getAtualizadoEm());
    }
}
