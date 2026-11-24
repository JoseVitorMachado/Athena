package br.ufla.athena.requisitos.aplicacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import br.ufla.athena.requisitos.dominio.EstadoVersao;
import br.ufla.athena.requisitos.dominio.SituacaoRequisito;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegrasRevisaoTest {
    private final UUID versao = UUID.randomUUID();
    private final UUID submissao = UUID.randomUUID();

    @Test
    void aceitaDecisaoDaCandidataEUltimaSubmissao() {
        assertNull(RegrasRevisao.motivoDescarte(versao, EstadoVersao.AGUARDANDO_REVISAO, "hash",
                submissao, versao, "hash", submissao));
    }

    @Test
    void descartaVersaoEstadoHashOuSubmissaoDivergente() {
        assertNotNull(RegrasRevisao.motivoDescarte(null, null, null, submissao, versao, "hash", submissao));
        assertNotNull(RegrasRevisao.motivoDescarte(UUID.randomUUID(), EstadoVersao.AGUARDANDO_REVISAO,
                "hash", submissao, versao, "hash", submissao));
        assertNotNull(RegrasRevisao.motivoDescarte(versao, EstadoVersao.RASCUNHO, "hash",
                submissao, versao, "hash", submissao));
        assertNotNull(RegrasRevisao.motivoDescarte(versao, EstadoVersao.AGUARDANDO_REVISAO, "outro",
                submissao, versao, "hash", submissao));
        assertNotNull(RegrasRevisao.motivoDescarte(versao, EstadoVersao.AGUARDANDO_REVISAO, "hash",
                UUID.randomUUID(), versao, "hash", submissao));
        assertNotNull(RegrasRevisao.motivoDescarte(versao, EstadoVersao.AGUARDANDO_REVISAO, "hash",
                null, versao, "hash", submissao));
    }

    @Test
    void validaCondicoesDaPublicacao() {
        assertNull(RegrasRevisao.erroPublicacao(SituacaoRequisito.ATIVO, versao, EstadoVersao.APROVADA,
                versao, submissao, submissao));
        assertEquals("REQUISITO_CANCELADO", RegrasRevisao.erroPublicacao(SituacaoRequisito.CANCELADO,
                versao, EstadoVersao.APROVADA, versao, submissao, submissao));
        assertEquals("CANDIDATA_NAO_APROVADA", RegrasRevisao.erroPublicacao(SituacaoRequisito.ATIVO,
                versao, EstadoVersao.RASCUNHO, versao, submissao, submissao));
        assertEquals("VERSAO_DIVERGENTE", RegrasRevisao.erroPublicacao(SituacaoRequisito.ATIVO,
                versao, EstadoVersao.APROVADA, UUID.randomUUID(), submissao, submissao));
        assertEquals("VERSAO_DIVERGENTE", RegrasRevisao.erroPublicacao(SituacaoRequisito.ATIVO,
                versao, EstadoVersao.APROVADA, versao, UUID.randomUUID(), submissao));
    }
}
