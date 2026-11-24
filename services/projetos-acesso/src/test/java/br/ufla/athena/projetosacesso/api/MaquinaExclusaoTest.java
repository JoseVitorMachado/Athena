package br.ufla.athena.projetosacesso.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class MaquinaExclusaoTest {
    private final Instant agora = Instant.parse("2026-01-01T00:00:30Z");
    @Test void avancarSomenteComTodasAsConfirmacoesDaFase() {
        var pendente = new MaquinaExclusao.Confirmacao("a", null, null, agora);
        var fechada = new MaquinaExclusao.Confirmacao("a", agora, null, agora);
        var limpa = new MaquinaExclusao.Confirmacao("a", agora, agora, agora);
        assertEquals("FECHANDO", MaquinaExclusao.proximo("FECHANDO", List.of()));
        assertEquals("FECHANDO", MaquinaExclusao.proximo("FECHANDO", List.of(fechada, pendente)));
        assertEquals("EXCLUINDO", MaquinaExclusao.proximo("FECHANDO", List.of(fechada)));
        assertEquals("EXCLUINDO", MaquinaExclusao.proximo("EXCLUINDO", List.of(fechada)));
        assertEquals("CONCLUIDA", MaquinaExclusao.proximo("EXCLUINDO", List.of(limpa)));
    }
    @Test void reenviarSomentePendentesAposIntervalo() {
        var antiga = new MaquinaExclusao.Confirmacao("a", null, null, agora.minusSeconds(15));
        var recente = new MaquinaExclusao.Confirmacao("b", null, null, agora.minusSeconds(14));
        var fechada = new MaquinaExclusao.Confirmacao("c", agora, null, null);
        assertEquals(List.of("a"), MaquinaExclusao.reenviar("FECHANDO", List.of(antiga, recente, fechada), agora, Duration.ofSeconds(15)));
        assertEquals(List.of("a", "c"), MaquinaExclusao.reenviar("EXCLUINDO", List.of(antiga, recente, fechada), agora, Duration.ofSeconds(15)));
        assertEquals(List.of(), MaquinaExclusao.reenviar("CONCLUIDA", List.of(antiga), agora, Duration.ofSeconds(15)));
    }
}
