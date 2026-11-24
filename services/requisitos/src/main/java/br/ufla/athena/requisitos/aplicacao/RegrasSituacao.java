package br.ufla.athena.requisitos.aplicacao;

import br.ufla.athena.requisitos.dominio.EstadoVersao;
import br.ufla.athena.requisitos.dominio.SituacaoRequisito;
import java.util.Collection;

/** Regras puras para transições de situação. */
public final class RegrasSituacao {
    private RegrasSituacao() { }

    public static String erroCancelamento(SituacaoRequisito situacao, Collection<String> sagas, EstadoVersao candidata) {
        if (situacao == SituacaoRequisito.CANCELADO) return "REQUISITO_JA_CANCELADO";
        if (sagas.stream().anyMatch(s -> !"CONCLUIDA".equals(s) && !"COMPENSADA".equals(s))
                || candidata == EstadoVersao.EM_SUBMISSAO || candidata == EstadoVersao.AGUARDANDO_REVISAO)
            return "REQUISITO_EM_REVISAO";
        return null;
    }

    public static String erroReativacao(SituacaoRequisito situacao) {
        return situacao == SituacaoRequisito.ATIVO ? "REQUISITO_JA_ATIVO" : null;
    }

    public static boolean noLivro(SituacaoRequisito situacao, boolean publicada, boolean foraDoLivro) {
        return situacao == SituacaoRequisito.ATIVO && publicada && !foraDoLivro;
    }
}
