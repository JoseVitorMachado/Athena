package br.ufla.athena.bff.gestao;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

/** Consulta o vínculo atual em Projetos e acesso, sem cache entre requisições. */
@Component
public class PapelAtual {

    public enum Papel {
        LEITOR, REPORTER, EDITOR, APROVADOR, MANTENEDOR, OWNER;

        public boolean permite(Papel minimo) {
            return ordinal() >= minimo.ordinal();
        }
    }

    public record Vinculo(Papel papel, boolean arquivado) {
    }

    private final ClienteDominio cliente;
    private final String url;

    public PapelAtual(ClienteDominio cliente, AthenaProperties propriedades) {
        this.cliente = cliente;
        this.url = propriedades.servicos().projetosAcesso();
    }

    public Vinculo consultar(String projetoId, String usuarioId) {
        ClienteDominio.Resposta resposta;
        try {
            // Endpoint interno não roteado pelo Gateway (livro §6.1).
            resposta = cliente.get("projetos e acesso", url, "/api/v1/internal/projetos/"
                    + projetoId + "/membros/" + usuarioId);
        } catch (RuntimeException e) {
            throw new FalhaAutorizacao(503, "IDENTIDADE_INDISPONIVEL", "Serviço de identidade indisponível.");
        }
        if (resposta.status().value() == 404) {
            throw new FalhaAutorizacao(403, "SEM_ACESSO_AO_PROJETO", "Acesso ao projeto negado.");
        }
        if (!resposta.sucesso()) {
            throw new FalhaAutorizacao(503, "IDENTIDADE_INDISPONIVEL", "Serviço de identidade indisponível.");
        }
        try {
            JsonNode corpo = resposta.corpo();
            if (corpo == null || !projetoId.equals(corpo.path("projetoId").asText())
                    || !usuarioId.equals(corpo.path("usuarioId").asText())
                    || !corpo.path("arquivado").isBoolean()) {
                throw new IllegalArgumentException();
            }
            return new Vinculo(Papel.valueOf(corpo.path("papel").asText()), corpo.path("arquivado").asBoolean());
        } catch (IllegalArgumentException e) {
            throw new FalhaAutorizacao(503, "IDENTIDADE_INDISPONIVEL", "Resposta de identidade inválida.");
        }
    }
}
