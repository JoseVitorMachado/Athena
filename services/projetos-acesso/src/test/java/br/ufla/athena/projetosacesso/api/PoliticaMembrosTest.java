package br.ufla.athena.projetosacesso.api;

import static org.junit.jupiter.api.Assertions.*;
import static br.ufla.athena.projetosacesso.api.PoliticaMembros.Papel.*;
import org.junit.jupiter.api.Test;

class PoliticaMembrosTest {
    @Test
    void gerenciaSomenteDentroDosLimitesDaHierarquia() {
        for (var ator : PoliticaMembros.Papel.values()) {
            for (var alvo : PoliticaMembros.Papel.values()) {
                for (var novo : PoliticaMembros.Papel.values()) {
                    boolean esperado = alvo != OWNER && novo != OWNER && (ator == OWNER
                            || ator == MANTENEDOR && alvo.ordinal() <= APROVADOR.ordinal() && novo.ordinal() <= APROVADOR.ordinal());
                    assertEquals(esperado, PoliticaMembros.podeGerenciar(ator, alvo, novo), ator + " " + alvo + " " + novo);
                }
            }
        }
        assertFalse(PoliticaMembros.podeGerenciar(null, LEITOR, EDITOR));
        assertFalse(PoliticaMembros.podeGerenciar(OWNER, null, EDITOR));
        assertFalse(PoliticaMembros.podeGerenciar(OWNER, LEITOR, null));
    }
    @Test
    void remocaoPermiteSaidaExcetoOwnerEObedeceHierarquiaParaTerceiros() {
        for (var ator : PoliticaMembros.Papel.values()) {
            for (var alvo : PoliticaMembros.Papel.values()) {
                assertEquals(alvo != OWNER, PoliticaMembros.podeRemover(ator, alvo, true));
                assertEquals(PoliticaMembros.podeGerenciar(ator, alvo, alvo), PoliticaMembros.podeRemover(ator, alvo, false));
            }
        }
        assertFalse(PoliticaMembros.podeRemover(null, LEITOR, true));
        assertFalse(PoliticaMembros.podeRemover(OWNER, null, false));
    }
    @Test
    void convitesRespeitamLimites() {
        for (var ator : PoliticaMembros.Papel.values()) {
            for (var convite : PoliticaMembros.Papel.values()) {
                boolean esperado = convite != OWNER && (ator == OWNER || ator == MANTENEDOR && convite.ordinal() <= APROVADOR.ordinal());
                assertEquals(esperado, PoliticaMembros.podeConvidar(ator, convite));
            }
        }
        assertFalse(PoliticaMembros.podeConvidar(null, LEITOR));
        assertFalse(PoliticaMembros.podeConvidar(OWNER, null));
    }
    @Test
    void convertePapeisDoContratoEDoBanco() {
        String[] nomes = {"Leitor", "Repórter", "Editor", "Aprovador", "Mantenedor", "Owner"};
        for (var papel : PoliticaMembros.Papel.values()) {
            assertEquals(nomes[papel.ordinal()], PoliticaMembros.paraContrato(papel));
            assertEquals(papel, PoliticaMembros.deContrato(nomes[papel.ordinal()]));
            assertEquals(papel, PoliticaMembros.deContrato(papel.name()));
        }
        assertEquals(REPORTER, PoliticaMembros.deContrato("Reporter"));
        assertNull(PoliticaMembros.paraContrato(null));
        assertThrows(IllegalArgumentException.class, () -> PoliticaMembros.deContrato(null));
        assertThrows(IllegalArgumentException.class, () -> PoliticaMembros.deContrato("outro"));
    }
}
