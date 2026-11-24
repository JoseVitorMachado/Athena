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
        assertEquals(2, editor.actions().size());
        assertTrue(editor.actions().containsKey("editar-candidata"));
        assertTrue(editor.actions().containsKey("submeter"));
        assertTrue(montar("ATIVO", "RASCUNHO", null, Papel.LEITOR).actions().isEmpty());
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
    void canceladoNaoOfereceEscrita() {
        assertTrue(montar("CANCELADO", "APROVADA", "ATIVO", Papel.OWNER).actions().isEmpty());
    }

    private MontadorAcoes.Resultado montar(String situacao, String estado, String lote, Papel papel) {
        return montador.montar("p", "r", situacao, estado, "l", lote, "s", papel);
    }
}
