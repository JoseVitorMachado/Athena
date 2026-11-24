package br.ufla.athena.requisitos.aplicacao;

import br.ufla.athena.requisitos.dominio.EstadoVersao;
import br.ufla.athena.requisitos.dominio.SituacaoRequisito;
import java.util.Objects;
import java.util.UUID;

public final class RegrasRevisao {
    private RegrasRevisao() {
    }

    public static String motivoDescarte(UUID candidata, EstadoVersao estado, String hashAtual,
                                        UUID ultimaSubmissao, UUID versaoEvento, String hashEvento,
                                        UUID submissaoEvento) {
        if (candidata == null || !candidata.equals(versaoEvento)) {
            return "A versão informada não é a candidata atual.";
        }
        if (estado != EstadoVersao.AGUARDANDO_REVISAO) {
            return "A candidata não está aguardando revisão.";
        }
        if (!Objects.equals(hashAtual, hashEvento)) {
            return "O hash do conteúdo diverge.";
        }
        if (ultimaSubmissao == null || !ultimaSubmissao.equals(submissaoEvento)) {
            return "A submissão não é a mais recente concluída.";
        }
        return null;
    }

    public static String erroPublicacao(SituacaoRequisito situacao, UUID candidata, EstadoVersao estado,
                                        UUID versao, UUID ultimaSubmissao, UUID submissao) {
        if (situacao != SituacaoRequisito.ATIVO) {
            return "REQUISITO_CANCELADO";
        }
        if (candidata == null || !candidata.equals(versao)) {
            return "VERSAO_DIVERGENTE";
        }
        if (estado != EstadoVersao.APROVADA) {
            return "CANDIDATA_NAO_APROVADA";
        }
        if (ultimaSubmissao == null || !ultimaSubmissao.equals(submissao)) {
            return "VERSAO_DIVERGENTE";
        }
        return null;
    }
}
