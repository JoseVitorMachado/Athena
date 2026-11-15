package br.ufla.athena.integracoes.api;

import br.ufla.athena.integracoes.dominio.Conexao;
import br.ufla.athena.integracoes.dominio.Provedor;
import java.time.Instant;
import java.util.UUID;

/** Schema Conexao de docs/api/integracoes.yaml. */
public record ConexaoResposta(
        UUID conexaoId,
        UUID projetoId,
        Provedor provedor,
        String nome,
        Configuracao configuracao,
        Instant atualizadoEm) {

    public record Configuracao(String urlBase, String projetoExternoId) {
    }

    public static ConexaoResposta de(Conexao conexao) {
        Configuracao configuracao = conexao.getUrlBase() == null && conexao.getProjetoExternoId() == null
                ? null
                : new Configuracao(conexao.getUrlBase(), conexao.getProjetoExternoId());
        return new ConexaoResposta(conexao.getId(), conexao.getProjetoId(), conexao.getProvedor(),
                conexao.getNome(), configuracao, conexao.getAtualizadoEm());
    }
}
