package br.ufla.athena.bff.gestao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.ufla.athena.bff.gestao.PapelAtual.Papel;
import java.util.List;
import org.junit.jupiter.api.Test;

class MontadorAcoesTest {

    private final MontadorAcoes montador = new MontadorAcoes();

    @Test
    void rascunhoDependeDoPapel() {
        var editor = montar("ATIVO", "RASCUNHO", null, Papel.EDITOR);
        assertEquals(5, editor.actions().size());
        assertTrue(editor.actions().containsKey("editar-metadados"));
        assertTrue(editor.actions().containsKey("cancelar"));
        assertTrue(editor.actions().containsKey("editar-candidata"));
        assertTrue(editor.actions().containsKey("submeter"));
        assertTrue(montar("ATIVO", "RASCUNHO", null, Papel.LEITOR).actions().isEmpty());
    }

    @Test
    void comentariosDependemDaSituacaoEDoPapel() {
        var reporter = montar("ATIVO", "RASCUNHO", null, Papel.REPORTER);
        assertEquals("/api/v1/gestao/projetos/p/requisitos/r/comentarios", reporter.links().get("comentarios").href());
        var acao = reporter.actions().get("comentar");
        assertEquals("POST", acao.method());
        assertEquals(List.of("versaoId", "texto", "ancora", "mencoes"), acao.campos());
        assertFalse(montar("ATIVO", "RASCUNHO", null, Papel.LEITOR).actions().containsKey("comentar"));
        var cancelado = montar("CANCELADO", "RASCUNHO", null, Papel.REPORTER);
        assertTrue(cancelado.links().containsKey("comentarios"));
        assertFalse(cancelado.actions().containsKey("comentar"));
    }

    @Test
    void decisaoExigeLoteAtivoEAprovador() {
        assertTrue(montar("ATIVO", "AGUARDANDO_REVISAO", "ATIVO", Papel.APROVADOR).actions().containsKey("decidir"));
        assertFalse(montar("ATIVO", "AGUARDANDO_REVISAO", "ATIVO", Papel.EDITOR).actions().containsKey("decidir"));
        assertFalse(montar("ATIVO", "AGUARDANDO_REVISAO", null, Papel.APROVADOR).actions().containsKey("decidir"));
    }

    @Test
    void publicacaoSegueExemploDoContrato() {
        var acao = montar("ATIVO", "APROVADA", "ATIVO", Papel.APROVADOR).actions().get("publicar");
        assertEquals("/api/v1/gestao/projetos/p/requisitos/r/publicacoes", acao.href());
        assertEquals("POST", acao.method());
        assertEquals("application/json", acao.contentType());
        assertEquals(List.of("versaoId", "submissaoId", "comprovanteHumano"), acao.campos());
        assertEquals(List.of("If-Match", "Idempotency-Key", "Candidata APROVADA", "Papel Aprovador ou superior",
                "MCP: confirmação humana específica; comprovante único válido por 5 minutos"), acao.precondicoes());
    }

    @Test
    void canceladoOfereceApenasReativacaoParaEditor() {
        var actions = montar("CANCELADO", "APROVADA", "ATIVO", Papel.OWNER).actions();
        assertEquals(1, actions.size());
        assertEquals("POST", actions.get("reativar").method());
        assertTrue(montar("CANCELADO", "RASCUNHO", null, Papel.LEITOR).actions().isEmpty());
    }

    @Test
    void cancelamentoExigeAusenciaDeSubmissaoERevisao() {
        assertFalse(montar("ATIVO", "EM_SUBMISSAO", null, Papel.EDITOR).actions().containsKey("cancelar"));
        assertFalse(montar("ATIVO", "AGUARDANDO_REVISAO", "ATIVO", Papel.EDITOR).actions().containsKey("cancelar"));
        var emAndamento = montador.montar("p", "r", "ATIVO", "RASCUNHO", null, null, "s", true, Papel.EDITOR);
        assertFalse(emAndamento.actions().containsKey("cancelar"));
        var acao = montar("ATIVO", "RASCUNHO", null, Papel.EDITOR).actions().get("cancelar");
        assertEquals("/api/v1/gestao/projetos/p/requisitos/r/cancelamentos", acao.href());
        assertEquals(List.of("confirmacao"), acao.campos());
    }

    private MontadorAcoes.Resultado montar(String situacao, String estado, String lote, Papel papel) {
        return montador.montar("p", "r", situacao, estado, "l", lote, "s", false, papel);
    }
}
