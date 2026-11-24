package br.ufla.athena.projetosacesso.seguranca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class RotacaoRefreshTest {
    @Test
    void decideRotacaoReusoEExpiracao() {
        Instant agora = Instant.parse("2026-01-01T00:00:00Z");
        Instant fim = agora.plusSeconds(60);
        assertEquals(RotacaoRefresh.Resultado.ROTACIONAR, RotacaoRefresh.avaliar(null, null, fim, fim, agora));
        assertEquals(RotacaoRefresh.Resultado.REUTILIZADO, RotacaoRefresh.avaliar(agora, null, fim, fim, agora));
        assertEquals(RotacaoRefresh.Resultado.ENCERRADO, RotacaoRefresh.avaliar(null, null, agora, fim, agora));
        assertEquals(RotacaoRefresh.Resultado.ENCERRADO, RotacaoRefresh.avaliar(null, agora, fim, fim, agora));
    }
}
