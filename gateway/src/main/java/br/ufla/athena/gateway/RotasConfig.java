package br.ufla.athena.gateway;

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

    @Bean
    public RouteLocator rotas(RouteLocatorBuilder rotas, RotasProperties destinos) {
        return rotas.routes()
                .route("bff-gestao", rota -> rota.path("/api/v1/gestao/**").uri(destinos.bffGestao()))
                .route("bff-livro", rota -> rota.path("/api/v1/livro/**").uri(destinos.bffLivro()))
                .route("impacto-mcp", rota -> rota.path("/mcp", "/mcp/**").uri(destinos.impactoMcp()))
                .build();
    }
}
