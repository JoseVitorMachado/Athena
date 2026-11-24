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
        assertEquals(Papel.LEITOR, PoliticaProjeto.papelMinimo("GET", BASE));
        assertEquals(Papel.MANTENEDOR, PoliticaProjeto.papelMinimo("PATCH", BASE));
        assertEquals(Papel.MANTENEDOR, PoliticaProjeto.papelMinimo("POST", BASE + "/arquivamentos"));
        assertEquals(Papel.MANTENEDOR, PoliticaProjeto.papelMinimo("POST", BASE + "/restauracoes"));
        assertEquals(Papel.LEITOR, PoliticaProjeto.papelMinimo("GET", BASE + "/requisitos/r"));
        assertEquals(Papel.LEITOR, PoliticaProjeto.papelMinimo("GET", BASE + "/mapa"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("POST", BASE + "/requisitos"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("PATCH", BASE + "/requisitos/r/candidata"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("POST", BASE + "/requisitos/r/submissoes"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("POST", BASE + "/relacoes"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("PATCH", BASE + "/relacoes/r"));
        assertEquals(Papel.EDITOR, PoliticaProjeto.papelMinimo("DELETE", BASE + "/relacoes/r"));
        assertEquals(Papel.APROVADOR, PoliticaProjeto.papelMinimo("POST", BASE + "/revisoes/r/decisoes"));
        assertEquals(Papel.APROVADOR, PoliticaProjeto.papelMinimo("POST", BASE + "/requisitos/r/publicacoes"));
        assertNull(PoliticaProjeto.papelMinimo("POST", BASE + "/outra-operacao"));
    }
    @Test
    void colecaoNaoExigeProjeto() {
        assertFalse(PoliticaProjeto.exigeProjeto("GET", "/api/v1/gestao/projetos"));
        assertFalse(PoliticaProjeto.exigeProjeto("POST", "/api/v1/gestao/projetos"));
        assertTrue(PoliticaProjeto.exigeProjeto("PATCH", "/api/v1/gestao/projetos"));
        assertTrue(PoliticaProjeto.exigeProjeto("GET", BASE));
    }

    @Test
    void projetoArquivadoPermiteLeituraERestauracao() {
        assertTrue(PoliticaProjeto.permiteArquivado("GET", BASE));
        assertTrue(PoliticaProjeto.permiteArquivado("POST", BASE + "/restauracoes"));
        assertFalse(PoliticaProjeto.permiteArquivado("PATCH", BASE));
        assertFalse(PoliticaProjeto.permiteArquivado("POST", BASE + "/arquivamentos"));
        assertFalse(PoliticaProjeto.permiteArquivado("PATCH", BASE + "/restauracoes"));
        assertFalse(PoliticaProjeto.permiteArquivado("POST", BASE + "/restauracoes/outra"));
    }
}
