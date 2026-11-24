package br.ufla.athena.bff.livro;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class Seguranca {

    @Bean
    SecurityFilterChain segurancaHttp(HttpSecurity http, ObjectMapper json) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(regras -> regras
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint((pedido, resposta, erro) -> {
                            resposta.setHeader("WWW-Authenticate", "Bearer");
                            problema(resposta, json, HttpStatus.UNAUTHORIZED, "NAO_AUTENTICADO", "Autenticação necessária.");
                        })
                        .accessDeniedHandler((pedido, resposta, erro) ->
                                problema(resposta, json, HttpStatus.FORBIDDEN, "ACESSO_NEGADO", "Acesso negado.")))
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults())
                        .authenticationEntryPoint((pedido, resposta, erro) -> {
                            resposta.setHeader("WWW-Authenticate", "Bearer");
                            problema(resposta, json, HttpStatus.UNAUTHORIZED, "NAO_AUTENTICADO", "Credencial ausente ou inválida.");
                        }));
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwks) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwks)
                .jwsAlgorithm(SignatureAlgorithm.RS256).build();
        OAuth2TokenValidator<Jwt> destinatario = jwt -> jwt.getAudience() != null && jwt.getAudience().contains("athena-api")
                ? OAuth2TokenValidatorResult.success()
                : invalido();
        OAuth2TokenValidator<Jwt> sujeito = jwt -> {
            try {
                UUID.fromString(jwt.getSubject());
                return jwt.getExpiresAt() == null ? invalido() : OAuth2TokenValidatorResult.success();
            } catch (IllegalArgumentException | NullPointerException e) {
                return invalido();
            }
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer("urn:athena"), destinatario, sujeito));
        return decoder;
    }

    private static OAuth2TokenValidatorResult invalido() {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
    }

    static void problema(HttpServletResponse resposta, ObjectMapper json, HttpStatus status,
                         String codigo, String detalhe) throws IOException {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setProperty("codigo", codigo);
        resposta.setStatus(status.value());
        resposta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        json.writeValue(resposta.getOutputStream(), problema);
    }
}
