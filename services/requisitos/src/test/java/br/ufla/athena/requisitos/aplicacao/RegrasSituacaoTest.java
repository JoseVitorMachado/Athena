package br.ufla.athena.requisitos.aplicacao;

import static org.junit.jupiter.api.Assertions.*;
import br.ufla.athena.requisitos.dominio.EstadoVersao;
import br.ufla.athena.requisitos.dominio.SituacaoRequisito;
import java.util.List;
import org.junit.jupiter.api.Test;

class RegrasSituacaoTest {
    @Test
    void cancelarExigeAusenciaDeRevisaoESagaAtiva() {
        assertEquals("REQUISITO_JA_CANCELADO", RegrasSituacao.erroCancelamento(SituacaoRequisito.CANCELADO, List.of(), null));
        for (String saga : List.of("EM_ANDAMENTO", "COMPENSANDO", "COMPENSACAO_PENDENTE", "DESCONHECIDA"))
            assertEquals("REQUISITO_EM_REVISAO", RegrasSituacao.erroCancelamento(SituacaoRequisito.ATIVO, List.of(saga), EstadoVersao.RASCUNHO));
        for (EstadoVersao estado : List.of(EstadoVersao.EM_SUBMISSAO, EstadoVersao.AGUARDANDO_REVISAO))
            assertEquals("REQUISITO_EM_REVISAO", RegrasSituacao.erroCancelamento(SituacaoRequisito.ATIVO, List.of("CONCLUIDA"), estado));
        assertNull(RegrasSituacao.erroCancelamento(SituacaoRequisito.ATIVO, List.of("CONCLUIDA", "COMPENSADA"), EstadoVersao.APROVADA));
    }

    @Test
    void reativarExigeCancelamento() {
        assertEquals("REQUISITO_JA_ATIVO", RegrasSituacao.erroReativacao(SituacaoRequisito.ATIVO));
        assertNull(RegrasSituacao.erroReativacao(SituacaoRequisito.CANCELADO));
    }

    @Test
    void livroExigeAtivoPublicadoEAposUltimaPublicacao() {
        assertTrue(RegrasSituacao.noLivro(SituacaoRequisito.ATIVO, true, false));
        assertFalse(RegrasSituacao.noLivro(SituacaoRequisito.CANCELADO, true, false));
        assertFalse(RegrasSituacao.noLivro(SituacaoRequisito.ATIVO, false, false));
        assertFalse(RegrasSituacao.noLivro(SituacaoRequisito.ATIVO, true, true));
    }
    @Test
    void reativacaoPreservaConteudoEExigeNovaPublicacao() {
        var requisito = br.ufla.athena.requisitos.dominio.Requisito.novo(java.util.UUID.randomUUID(), 1,
                java.util.UUID.randomUUID(), null, null, "FUNCIONAL", "Título", "Conteúdo", List.of("Critério"));
        var candidata = requisito.candidata().orElseThrow();
        candidata.alterarEstado(EstadoVersao.PUBLICADA);
        assertTrue(br.ufla.athena.requisitos.api.RequisitoResposta.de(requisito).noLivro());
        requisito.cancelar();
        assertFalse(br.ufla.athena.requisitos.api.RequisitoResposta.de(requisito).noLivro());
        var reaberta = requisito.reativar();
        assertEquals("Conteúdo", reaberta.getConteudo());
        assertEquals(EstadoVersao.RASCUNHO, reaberta.getEstadoVersao());
        assertFalse(br.ufla.athena.requisitos.api.RequisitoResposta.de(requisito).noLivro());
        reaberta.alterarEstado(EstadoVersao.APROVADA);
        requisito.cancelar();
        assertSame(reaberta, requisito.reativar());
        assertEquals(EstadoVersao.RASCUNHO, reaberta.getEstadoVersao());
        reaberta.alterarEstado(EstadoVersao.PUBLICADA);
        requisito.registrarPublicacao();
        assertTrue(br.ufla.athena.requisitos.api.RequisitoResposta.de(requisito).noLivro());
    }
}
