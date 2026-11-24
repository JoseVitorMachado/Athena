package br.ufla.athena.projetosacesso.api;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class RecuperacaoTest {
    @Test
    void geraCodigoNumericoEProtegeComSegredo() {
        for (int i = 0; i < 1000; i++) {
            String codigo = CodigosRecuperacao.gerar();
            assertTrue(codigo.matches("[0-9]{6}"));
            assertNotEquals(codigo, CodigosRecuperacao.hash(codigo, "segredo-de-teste"));
            assertEquals(64, CodigosRecuperacao.hash(codigo, "segredo-de-teste").length());
        }
    }

    @Test
    void verificaExpiracaoTentativasEUsoUnico() {
        Instant agora = Instant.parse("2026-01-01T00:00:00Z");
        Instant fim = agora.plusSeconds(600);
        assertTrue(RegrasRecuperacao.valido(fim, null, null, 4, agora));
        assertFalse(RegrasRecuperacao.valido(fim, null, null, 0, fim));
        assertFalse(RegrasRecuperacao.valido(fim, agora, null, 0, agora));
        assertFalse(RegrasRecuperacao.valido(fim, null, agora, 0, agora));
        assertFalse(RegrasRecuperacao.valido(fim, null, null, 5, agora));
        assertFalse(RegrasRecuperacao.invalidarAposFalha(3));
        assertTrue(RegrasRecuperacao.invalidarAposFalha(4));
    }
}
