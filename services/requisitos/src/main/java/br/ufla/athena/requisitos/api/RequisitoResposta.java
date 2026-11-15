package br.ufla.athena.requisitos.api;

import br.ufla.athena.requisitos.dominio.Requisito;
import br.ufla.athena.requisitos.dominio.SituacaoRequisito;
import br.ufla.athena.requisitos.dominio.Versao;
import java.time.Instant;
import java.util.UUID;

/** Schema Requisito de docs/api/requisitos.yaml. */
public record RequisitoResposta(
        UUID requisitoId,
        UUID projetoId,
        String codigo,
        UUID documentoId,
        UUID epicoId,
        UUID responsavelId,
        String tipo,
        String titulo,
        SituacaoRequisito situacao,
        VersaoResposta candidata,
        UUID versaoPublicadaId,
        Instant atualizadoEm) {

    public static RequisitoResposta de(Requisito requisito) {
        return new RequisitoResposta(
                requisito.getId(),
                requisito.getProjetoId(),
                requisito.getCodigo(),
                requisito.getDocumentoId(),
                requisito.getEpicoId(),
                requisito.getResponsavelId(),
                requisito.getTipo(),
                requisito.getTitulo(),
                requisito.getSituacao(),
                requisito.candidata().map(VersaoResposta::de).orElse(null),
                requisito.versaoPublicada().map(Versao::getId).orElse(null),
                requisito.getAtualizadoEm());
    }
}
