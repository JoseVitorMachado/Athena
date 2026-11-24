package br.ufla.athena.gateway;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SegurancaConfig {

    /** Origens aceitas em renovação e logout, além da própria origem do Host (livro §7.3). */
    private final List<String> origensPermitidas;

    public SegurancaConfig(
            @org.springframework.beans.factory.annotation.Value("${athena.origens-permitidas:}") List<String> origens) {
        this.origensPermitidas = origens.stream()
                .map(String::trim)
                .filter(origem -> !origem.isEmpty())
                .map(origem -> origem.toLowerCase(java.util.Locale.ROOT))
                .toList();
    }

    /**
     * Aceita o Origin quando ele está na lista configurada ou quando aponta para o
     * mesmo host:porta que o cliente usou (Host ou X-Forwarded-Host), o que cobre o
     * acesso direto ao Gateway e o Ingress. Sem Origin, recusa.
     */
    boolean origemPermitida(HttpHeaders cabecalhos) {
        String origem = cabecalhos.getOrigin();
        if (origem == null || origem.isBlank() || "null".equals(origem)) {
            return false;
        }
        String normalizada = origem.trim().toLowerCase(java.util.Locale.ROOT);
        if (origensPermitidas.contains(normalizada)) {
            return true;
        }
        java.net.URI uri;
        try {
            uri = java.net.URI.create(normalizada);
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (uri.getHost() == null || uri.getRawPath() != null && !uri.getRawPath().isEmpty()) {
            return false;
        }
        String autoridade = uri.getPort() < 0 ? uri.getHost() : uri.getHost() + ":" + uri.getPort();
        String host = cabecalhos.getFirst("X-Forwarded-Host");
        if (host == null || host.isBlank()) {
            host = cabecalhos.getFirst(HttpHeaders.HOST);
        }
        return host != null && autoridade.equals(host.trim().toLowerCase(java.util.Locale.ROOT));
    }

    @Bean
    ReactiveJwtDecoder jwtDecoder(RotasProperties destinos) {
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(destinos.projetosAcesso() + "/oauth2/jwks").build();
        OAuth2TokenValidator<Jwt> audiencia = jwt -> jwt.getAudience().contains("athena-api")
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer("urn:athena"), audiencia));
        return decoder;
    }

    @Bean
    SecurityWebFilterChain seguranca(ServerHttpSecurity http, RotasProperties destinos) {
        WebClient cliente = WebClient.builder().baseUrl(destinos.projetosAcesso()).build();
        // A autenticação é lida do contexto reativo: nesta posição da cadeia o
        // exchange.getPrincipal() ainda não está preenchido.
        WebFilter sessao = (exchange, chain) -> org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                .map(org.springframework.security.core.context.SecurityContext::getAuthentication)
                .ofType(org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken.class)
                .map(java.util.Optional::of)
                .defaultIfEmpty(java.util.Optional.empty())
                .flatMap(opcional -> {
                    if (opcional.isEmpty()) return chain.filter(exchange);
                    var auth = opcional.get();
                    String sid = auth.getToken().getClaimAsString("sid");
                    try {
                        UUID.fromString(sid);
                    } catch (Exception e) {
                        return problema(exchange, HttpStatus.UNAUTHORIZED, "SESSAO_ENCERRADA");
                    }
                    return cliente.get().uri("/api/v1/internal/sessoes/{id}", sid).exchangeToMono(resposta -> {
                        if (resposta.statusCode().value() == 404) return Mono.just("SESSAO_ENCERRADA");
                        if (!resposta.statusCode().is2xxSuccessful()) return Mono.just("IDENTIDADE_INDISPONIVEL");
                        return resposta.bodyToMono(SessaoInterna.class)
                                .map(s -> s.ativa() && auth.getToken().getSubject().equals(s.usuarioId()) ? "OK" : "SESSAO_ENCERRADA")
                                .defaultIfEmpty("IDENTIDADE_INDISPONIVEL");
                    }).timeout(Duration.ofSeconds(2)).onErrorReturn("IDENTIDADE_INDISPONIVEL")
                            .flatMap(resultado -> {
                                if ("OK".equals(resultado)) return chain.filter(exchange);
                                return problema(exchange, "SESSAO_ENCERRADA".equals(resultado) ? HttpStatus.UNAUTHORIZED : HttpStatus.SERVICE_UNAVAILABLE, resultado);
                            });
                });
        return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(org.springframework.security.web.server.context.NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(reg -> reg.pathMatchers(HttpMethod.POST, "/api/v1/autenticacao/cadastro", "/api/v1/autenticacao/login", "/api/v1/autenticacao/renovacao", "/api/v1/autenticacao/logout",
                        "/api/v1/autenticacao/recuperacao", "/api/v1/autenticacao/recuperacao/conclusao").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/gestao/docs", "/api/v1/gestao/docs/**",
                                "/api/v1/livro/docs", "/api/v1/livro/docs/**",
                                "/api/v1/autenticacao/docs", "/api/v1/autenticacao/docs/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/oauth2/jwks", "/actuator", "/actuator/**").permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> {}).authenticationEntryPoint((exchange, ex) -> problema(exchange, HttpStatus.UNAUTHORIZED, "NAO_AUTENTICADO")))
                .exceptionHandling(ex -> ex.authenticationEntryPoint((exchange, erro) -> problema(exchange, HttpStatus.UNAUTHORIZED, "NAO_AUTENTICADO")))
                .addFilterAfter(sessao, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    WebFilter validarOrigem() {
        return (exchange, chain) -> {
            String caminho = exchange.getRequest().getPath().value();
            if (exchange.getRequest().getMethod() != HttpMethod.POST
                    || !(caminho.equals("/api/v1/autenticacao/renovacao") || caminho.equals("/api/v1/autenticacao/logout"))) {
                return chain.filter(exchange);
            }
            if (origemPermitida(exchange.getRequest().getHeaders())) {
                return chain.filter(exchange);
            }
            // Origin ausente ou fora da política não autoriza o uso do cookie (livro §7.3).
            return problema(exchange, HttpStatus.FORBIDDEN, "ORIGEM_INVALIDA");
        };
    }

    private static Mono<Void> problema(ServerWebExchange exchange, HttpStatus status, String codigo) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        if (status == HttpStatus.UNAUTHORIZED) {
            exchange.getResponse().getHeaders().set(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        }
        String corpo = "{\"type\":\"about:blank\",\"title\":\"" + status.getReasonPhrase() + "\",\"status\":" + status.value() + ",\"codigo\":\"" + codigo + "\"}";
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(corpo.getBytes(java.nio.charset.StandardCharsets.UTF_8))));
    }

    record SessaoInterna(String sessaoId, String usuarioId, boolean ativa) {}
}
