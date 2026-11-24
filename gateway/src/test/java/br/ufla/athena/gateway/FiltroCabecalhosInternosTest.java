package br.ufla.athena.gateway;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class FiltroCabecalhosInternosTest {
    private final FiltroCabecalhosInternos filtro = new FiltroCabecalhosInternos();

    @Test
    void removeTodosOsCabecalhosInternosEmOutrasRotas() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/gestao/projetos")
                .header("x-aThEnA-usuario", "falso").header("X-Athena-Papel", "OWNER")
                .header("Outro", "preservado"));
        AtomicReference<ServerWebExchange> recebido = new AtomicReference<>();
        filtro.filter(exchange, e -> { recebido.set(e); return Mono.empty(); }).block();
        assertNull(recebido.get().getRequest().getHeaders().getFirst("X-Athena-Usuario"));
        assertNull(recebido.get().getRequest().getHeaders().getFirst("X-Athena-Papel"));
        assertEquals("preservado", recebido.get().getRequest().getHeaders().getFirst("Outro"));
    }

    @Test
    void injetaSomenteUsuarioAutenticadoNaRotaMcp() {
        String sujeito = "11111111-1111-4111-8111-111111111111";
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "RS256").subject(sujeito)
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/mcp")
                .header("X-Athena-Usuario", "falso").header("x-athena-canal", "MCP"));
        var autenticado = exchange.mutate().principal(Mono.just(new JwtAuthenticationToken(jwt, java.util.List.of()))).build();
        AtomicReference<ServerWebExchange> recebido = new AtomicReference<>();
        filtro.filter(autenticado, e -> { recebido.set(e); return Mono.empty(); }).block();
        assertEquals(sujeito, recebido.get().getRequest().getHeaders().getFirst("X-Athena-Usuario"));
        assertNull(recebido.get().getRequest().getHeaders().getFirst("X-Athena-Canal"));
    }
}
