package br.ufla.athena.requisitos.aplicacao;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegrasComprovanteTest {
    @Test void validaPrazoConsumoEVinculos() {
        var agora = Instant.parse("2026-01-01T00:00:00Z");
        var vinculos = new RegrasComprovante.Vinculos(UUID.randomUUID(), UUID.randomUUID(), "DECIDIR", UUID.randomUUID(), UUID.randomUUID(), "hash");
        assertTrue(RegrasComprovante.valido(vinculos, vinculos, agora.plusSeconds(1), null, agora));
        assertFalse(RegrasComprovante.valido(vinculos, vinculos, agora, null, agora));
        assertFalse(RegrasComprovante.valido(vinculos, vinculos, agora.plusSeconds(1), agora, agora));
        var outro = new RegrasComprovante.Vinculos(UUID.randomUUID(), vinculos.projeto(), vinculos.acao(), vinculos.alvo(), vinculos.versao(), vinculos.hashAcao());
        assertFalse(RegrasComprovante.valido(vinculos, outro, agora.plusSeconds(1), null, agora));
    }

    @Test void calculaHashDaAcao() {
        var alvo = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var versao = UUID.fromString("00000000-0000-0000-0000-000000000002");
        assertEquals("a544cf3d367b5db6e2a24753d44e00a4f4aefc474035e5c2d596517d1f1eedc5", RegrasComprovante.hashAcao("publicar", alvo, versao, null));
        assertNotEquals(RegrasComprovante.hashAcao("decidir", alvo, versao, "APROVAR"), RegrasComprovante.hashAcao("decidir", alvo, versao, "REJEITAR"));
    }
}
