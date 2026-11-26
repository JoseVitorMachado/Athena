package br.ufla.athena.bff.gestao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.ufla.athena.bff.gestao.PapelAtual.Papel;
import org.junit.jupiter.api.Test;

class PoliticaProjetoTest {

    private static final String BASE = "/api/v1/gestao/projetos/p";

    @Test
    void identificaPapelMinimoPorOperacao() {
        assertEquals(Papel.LEITOR, PoliticaProjeto.papelMinimo("POST", BASE + "/assistente/perguntas"));
        assertEquals(Papel.LEITOR, PoliticaProjeto.papelMinimo("GET", BASE));
        assertEquals(Papel.MANTENEDOR, PoliticaProjeto.papelMinimo("PATCH", BASE));
        assertEquals(Papel.MANTENEDOR, PoliticaProjeto.papelMinimo("POST", BASE + "/arquivamentos"));
        assertEquals(Papel.MANTENEDOR, PoliticaProjeto.papelMinimo("POST", BASE + "/restauracoes"));
        assertEquals(Papel.LEITOR, PoliticaProjeto.papelMinimo("GET", BASE + "/requisitos/r"));
        assertEquals(Papel.REPORTER, PoliticaProjeto.papelMinimo("GET", BASE + "/requisitos/r/comentarios"));
        assertEquals(Papel.REPORTER, PoliticaProjeto.papelMinimo("POST", BASE + "/requisitos/r/comentarios"));
        assertEquals(Papel.REPORTER, PoliticaProjeto.papelMinimo("PATCH", BASE + "/requisitos/r/comentarios/c"));
        assertNull(PoliticaProjeto.papelMinimo("POST", BASE + "/requisitos/r/comentarios/c"));
        assertEquals(Papel.LEITOR, PoliticaProjeto.papelMinimo("GET", BASE + "/mapa"));
        assertEquals(Papel.LEITOR, PoliticaProjeto.papelMinimo("GET", BASE + "/membros"));
        assertEquals(Papel.LEITOR, PoliticaProjeto.papelMinimo("GET", BASE + "/historico-membros"));
        assertEquals(Papel.MANTENEDOR, PoliticaProjeto.papelMinimo("GET", BASE + "/convites"));
        assertEquals(Papel.MANTENEDOR, PoliticaProjeto.papelMinimo("PATCH", BASE + "/membros/m"));
        assertEquals(Papel.LEITOR, PoliticaProjeto.papelMinimo("DELETE", BASE + "/membros/m"));
        assertEquals(Papel.MANTENEDOR, PoliticaProjeto.papelMinimo("POST", BASE + "/convites"));
        assertEquals(Papel.MANTENEDOR, PoliticaProjeto.papelMinimo("DELETE", BASE + "/convites/c"));
        assertEquals(Papel.OWNER, PoliticaProjeto.papelMinimo("POST", BASE + "/transferencias-propriedade"));
        assertEquals(Papel.OWNER, PoliticaProjeto.papelMinimo("POST", BASE + "/exclusoes"));
        assertNull(PoliticaProjeto.papelMinimo("PATCH", BASE + "/membros"));
        assertNull(PoliticaProjeto.papelMinimo("DELETE", BASE + "/convites"));
        assertEquals(Papel.LEITOR, PoliticaProjeto.papelMinimo("GET", BASE + "/baselines"));
        assertEquals(Papel.LEITOR, PoliticaProjeto.papelMinimo("GET", BASE + "/baselines/b"));
        assertEquals(Papel.LEITOR, PoliticaProjeto.papelMinimo("GET", BASE + "/baselines/b/comparacao"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("POST", BASE + "/baselines"));
        assertNull(PoliticaProjeto.papelMinimo("DELETE", BASE + "/baselines/b"));
        assertEquals(Papel.LEITOR, PoliticaProjeto.papelMinimo("GET", BASE + "/epicos"));
        assertEquals(Papel.LEITOR, PoliticaProjeto.papelMinimo("GET", BASE + "/epicos/e"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("POST", BASE + "/epicos"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("PATCH", BASE + "/epicos/e"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("DELETE", BASE + "/epicos/e"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("PATCH", BASE + "/requisitos/r"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("POST", BASE + "/requisitos/r/cancelamentos"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("POST", BASE + "/requisitos/r/reativacoes"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("POST", BASE + "/requisitos"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("PATCH", BASE + "/requisitos/r/candidata"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("POST", BASE + "/requisitos/r/submissoes"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("POST", BASE + "/relacoes"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("PATCH", BASE + "/relacoes/r"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("DELETE", BASE + "/relacoes/r"));
        assertEquals(Papel.APROVADOR, PoliticaProjeto.papelMinimo("POST", BASE + "/revisoes/r/decisoes"));
        assertEquals(Papel.APROVADOR, PoliticaProjeto.papelMinimo("POST", BASE + "/requisitos/r/publicacoes"));
        assertEquals(Papel.APROVADOR, PoliticaProjeto.papelMinimo("POST", BASE + "/revisoes/r/confirmacoes-decisao"));
        assertEquals(Papel.APROVADOR, PoliticaProjeto.papelMinimo("POST", BASE + "/requisitos/r/confirmacoes-publicacao"));
        assertNull(PoliticaProjeto.papelMinimo("POST", BASE + "/outra-operacao"));
    }
    @Test
    void colecaoNaoExigeProjeto() {
        assertFalse(PoliticaProjeto.exigeProjeto("GET", "/api/v1/gestao/projetos"));
        assertFalse(PoliticaProjeto.exigeProjeto("POST", "/api/v1/gestao/projetos"));
        assertTrue(PoliticaProjeto.exigeProjeto("PATCH", "/api/v1/gestao/projetos"));
        assertTrue(PoliticaProjeto.exigeProjeto("GET", BASE));
        assertFalse(PoliticaProjeto.exigeProjeto("GET", BASE + "/exclusoes/e"));
        assertTrue(PoliticaProjeto.exigeProjeto("POST", BASE + "/exclusoes"));
    }

    @Test
    void projetoArquivadoPermiteLeituraERestauracao() {
        assertTrue(PoliticaProjeto.permiteArquivado("POST", BASE + "/assistente/perguntas"));
        assertTrue(PoliticaProjeto.permiteArquivado("GET", BASE));
        assertTrue(PoliticaProjeto.permiteArquivado("POST", BASE + "/restauracoes"));
        assertTrue(PoliticaProjeto.permiteArquivado("POST", BASE + "/exclusoes"));
        assertFalse(PoliticaProjeto.permiteArquivado("PATCH", BASE));
        assertFalse(PoliticaProjeto.permiteArquivado("POST", BASE + "/arquivamentos"));
        assertFalse(PoliticaProjeto.permiteArquivado("PATCH", BASE + "/restauracoes"));
        assertFalse(PoliticaProjeto.permiteArquivado("POST", BASE + "/restauracoes/outra"));
    }
}
