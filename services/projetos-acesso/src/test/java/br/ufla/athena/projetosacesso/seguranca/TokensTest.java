package br.ufla.athena.projetosacesso.seguranca;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TokensTest {
    @Test
    void emiteEValidaClaims() throws Exception {
        Tokens tokens = new Tokens(new ChavesJwt(""));
        UUID usuario = UUID.randomUUID();
        UUID sessao = UUID.randomUUID();
        Instant agora = Instant.now();
        String jwt = tokens.emitir(usuario, sessao, "Nome", "nome@example.org", agora, agora.plusSeconds(900));
        var claims = tokens.validar("Bearer " + jwt);
        assertEquals(usuario.toString(), claims.getSubject());
        assertEquals(sessao.toString(), claims.getStringClaim("sid"));
        assertEquals("urn:athena", claims.getIssuer());
        assertEquals("athena-api", claims.getAudience().get(0));
        assertEquals("Nome", claims.getStringClaim("nome"));
        assertEquals("nome@example.org", claims.getStringClaim("email"));
        assertThrows(RuntimeException.class, () -> tokens.validar("Bearer " + jwt + "x"));
    }

    @Test
    void hashEOpacos() {
        Tokens tokens = new Tokens(new ChavesJwt(""));
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", Tokens.hash("abc"));
        assertEquals(43, tokens.opaco().length());
        assertNotEquals(tokens.opaco(), tokens.opaco());
    }
}
