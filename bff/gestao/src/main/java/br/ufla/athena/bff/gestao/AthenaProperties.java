package br.ufla.athena.bff.gestao;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Endereços dos serviços de domínio e limites das chamadas HTTP internas. */
@ConfigurationProperties("athena")
public record AthenaProperties(Servicos servicos, Http http) {

    public record Servicos(String requisitos, String revisoes) {
    }

    public record Http(Duration timeoutConexao, Duration timeoutLeitura) {
    }
}
