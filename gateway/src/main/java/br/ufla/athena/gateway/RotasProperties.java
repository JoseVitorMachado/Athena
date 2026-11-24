package br.ufla.athena.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Destino interno de cada rota do Gateway. */
@ConfigurationProperties("athena.rotas")
public record RotasProperties(String bffGestao, String bffLivro, String impactoMcp, String projetosAcesso) {
}
