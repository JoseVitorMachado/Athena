package br.ufla.athena.gateway;

import java.time.Duration;
import org.springframework.cloud.gateway.config.HttpClientCustomizer;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Roteamento externo do ATHENA (livro de arquitetura §6.1).
 *
 * <pre>
 *   /api/v1/gestao/**  ->  BFF Gestão
 *   /api/v1/livro/**   ->  BFF Livro
 *   /mcp               ->  Análise de Impacto/MCP
 * </pre>
 *
 * O caminho é preservado. Qualquer outra entrada não tem rota e recebe 404: os
 * serviços de domínio nunca são alcançados diretamente por aqui.
 */
@Configuration
public class RotasConfig {

    /**
     * Cache de DNS curto no cliente HTTP do Gateway. O resolvedor do Reactor Netty
     * guarda o endereço pelo TTL que o DNS informa (no Docker, 10 minutos): se um
     * BFF fosse recriado com outro IP, o Gateway continuaria mandando pedidos para
     * o IP antigo, que pode já pertencer a outro container.
     */
    @Bean
    public HttpClientCustomizer dnsComCacheCurto() {
        return cliente -> cliente.resolver(resolvedor -> resolvedor
                .cacheMaxTimeToLive(Duration.ofSeconds(5))
                .cacheNegativeTimeToLive(Duration.ofSeconds(1)));
    }

    @Bean
    public RouteLocator rotas(RouteLocatorBuilder rotas, RotasProperties destinos) {
        return rotas.routes()
                .route("identidade", rota -> rota.path("/api/v1/autenticacao/**", "/api/v1/usuarios/**", "/oauth2/jwks").uri(destinos.projetosAcesso()))
                .route("assistente-gestao", rota -> rota.path("/api/v1/gestao/projetos/*/assistente/perguntas")
                        .metadata("response-timeout", 105000).uri(destinos.bffGestao()))
                .route("bff-gestao", rota -> rota.path("/api/v1/gestao/**").uri(destinos.bffGestao()))
                .route("bff-livro", rota -> rota.path("/api/v1/livro/**").uri(destinos.bffLivro()))
                .route("impacto-mcp", rota -> rota.path("/mcp", "/mcp/**").uri(destinos.impactoMcp()))
                .build();
    }
}
