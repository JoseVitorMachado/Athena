package br.ufla.athena.gateway;

import java.util.List;
import java.util.Locale;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Remove identidade fornecida pelo cliente e estabelece a identidade interna do MCP. */
@Component
public class FiltroCabecalhosInternos implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var pedido = exchange.getRequest().mutate().headers(cabecalhos ->
                List.copyOf(cabecalhos.keySet()).stream()
                        .filter(nome -> nome.toLowerCase(Locale.ROOT).startsWith("x-athena-"))
                        .forEach(cabecalhos::remove)).build();
        var limpo = exchange.mutate().request(pedido).build();
        String caminho = pedido.getPath().value();
        if (!caminho.equals("/mcp") && !caminho.startsWith("/mcp/")) return chain.filter(limpo);
        return exchange.getPrincipal().ofType(JwtAuthenticationToken.class)
                .filter(JwtAuthenticationToken::isAuthenticated)
                .map(auth -> limpo.mutate().request(pedido.mutate()
                        .headers(h -> h.set("X-Athena-Usuario", auth.getToken().getSubject())).build()).build())
                .defaultIfEmpty(limpo).flatMap(chain::filter);
    }

    @Override
    public int getOrder() { return Ordered.HIGHEST_PRECEDENCE; }
}
