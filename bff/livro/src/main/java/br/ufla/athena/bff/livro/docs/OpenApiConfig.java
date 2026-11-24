package br.ufla.athena.bff.livro.docs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApi() {
        OpenAPI documento = new OpenAPI()
                .info(new Info().title("ATHENA — BFF Livro").version("1")
                        .description("Lista somente as rotas implementadas. O contrato completo do projeto está em docs/api."))
                .servers(List.of(new Server().url("/")))
                .components(new Components().addSecuritySchemes("bearerJwt", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                        .description("Obtenha o token em POST /api/v1/autenticacao/login. Na demonstração, use owner, mantenedor, aprovador, editor, reporter ou leitor @athena.dev, com senha Athena@2026.")));
        documento.addSecurityItem(new SecurityRequirement().addList("bearerJwt"));
        return documento;
    }
}
