package br.ufla.athena.revisoes.api;

import br.ufla.athena.revisoes.dominio.AplicacaoEmRequisitos;
import br.ufla.athena.revisoes.dominio.Decisao;
import br.ufla.athena.revisoes.dominio.TipoDecisao;
import java.time.Instant;
import java.util.UUID;

/** Schema Decisao de docs/api/revisoes.yaml. */
public record DecisaoResposta(
        UUID decisaoId,
        UUID loteId,
        TipoDecisao decisao,
        String justificativa,
        UUID submissaoId,
        UUID versaoId,
        String hashConteudo,
        UUID autor,
        Instant data,
        AplicacaoEmRequisitos aplicacaoEmRequisitos) {

    public static DecisaoResposta de(Decisao decisao) {
        return new DecisaoResposta(decisao.getId(), decisao.getLoteId(), decisao.getDecisao(),
                decisao.getJustificativa(), decisao.getSubmissaoId(), decisao.getVersaoId(),
                decisao.getHashConteudo(), decisao.getAutor(), decisao.getData(),
                decisao.getAplicacaoEmRequisitos());
    }
}
