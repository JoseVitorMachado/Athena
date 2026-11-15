package br.ufla.athena.revisoes.api;

import br.ufla.athena.revisoes.dominio.Decisao;
import br.ufla.athena.revisoes.dominio.EstadoLote;
import br.ufla.athena.revisoes.dominio.Lote;
import br.ufla.athena.revisoes.dominio.Pendencia;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Schema Lote de docs/api/revisoes.yaml. */
public record LoteResposta(
        UUID loteId,
        UUID projetoId,
        UUID requisitoId,
        UUID submissaoId,
        UUID versaoId,
        String hashConteudo,
        EstadoLote estado,
        List<PendenciaResposta> pendencias,
        DecisaoResposta decisao,
        boolean preparacaoPendente,
        Instant atualizadoEm) {

    /** Schema Pendencia de docs/api/revisoes.yaml. */
    public record PendenciaResposta(
            UUID pendenciaId,
            String descricao,
            boolean bloqueante,
            boolean resolvida,
            UUID responsavelId,
            Instant atualizadoEm) {

        static PendenciaResposta de(Pendencia pendencia) {
            return new PendenciaResposta(pendencia.getId(), pendencia.getDescricao(), pendencia.isBloqueante(),
                    pendencia.isResolvida(), pendencia.getResponsavelId(), pendencia.getAtualizadoEm());
        }
    }

    public static LoteResposta de(Lote lote, Decisao decisao) {
        return new LoteResposta(lote.getId(), lote.getProjetoId(), lote.getRequisitoId(), lote.getSubmissaoId(),
                lote.getVersaoId(), lote.getHashConteudo(), lote.getEstado(),
                lote.getPendencias().stream().map(PendenciaResposta::de).toList(),
                decisao == null ? null : DecisaoResposta.de(decisao),
                lote.getEstado() == EstadoLote.PREPARADO,
                lote.getAtualizadoEm());
    }
}
