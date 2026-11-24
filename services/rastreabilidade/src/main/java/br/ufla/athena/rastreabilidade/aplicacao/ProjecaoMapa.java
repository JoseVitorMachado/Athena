package br.ufla.athena.rastreabilidade.aplicacao;

import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Atualiza a projeção dentro da transação da inbox. */
@Service
public class ProjecaoMapa {
    private final JdbcTemplate banco;

    public ProjecaoMapa(JdbcTemplate banco) {
        this.banco = banco;
    }

    public void aplicar(String tipo, Map<String, Object> dados) {
        UUID projeto = id(dados, "projetoId");
        long revisao = numero(dados, "revisao");
        boolean requisito = tipo.equals("RequisitoAlterado");
        long posicao = requisito ? 0 : numero(dados, "posicao");
        banco.query("SELECT pg_advisory_xact_lock(hashtext(?))", rs -> { }, projeto.toString());
        banco.update("INSERT INTO mapa_estado (projeto_id, geracao, situacao, posicao_aplicada, disponivel, ultima_atualizacao) "
                + "VALUES (?, ?, 'ATUALIZADO', ?, true, now()) ON CONFLICT (projeto_id) "
                + "DO UPDATE SET disponivel = true, posicao_aplicada = greatest(mapa_estado.posicao_aplicada, EXCLUDED.posicao_aplicada), "
                + "ultima_atualizacao = now()", projeto, UUID.randomUUID(), posicao);
        UUID entidade = id(dados, requisito ? "requisitoId" : "relacaoId");
        String consulta = requisito
                ? "SELECT revisao_origem FROM mapa_nos WHERE projeto_id = ? AND requisito_id = ?"
                : "SELECT revisao_origem FROM mapa_arestas WHERE projeto_id = ? AND relacao_id = ?";
        var revisoes = banco.query(consulta, (rs, n) -> rs.getLong(1), projeto, entidade);
        if (!deveAplicar(revisoes.isEmpty() ? null : revisoes.get(0), revisao)) {
            return;
        }
        if (tipo.equals("RequisitoAlterado")) {
            banco.update("INSERT INTO mapa_nos (projeto_id, requisito_id, codigo, titulo, situacao, revisao_origem, atualizado_em) "
                    + "VALUES (?, ?, ?, ?, ?, ?, now()) ON CONFLICT (projeto_id, requisito_id) DO UPDATE SET "
                    + "codigo = EXCLUDED.codigo, titulo = EXCLUDED.titulo, situacao = EXCLUDED.situacao, "
                    + "revisao_origem = EXCLUDED.revisao_origem, atualizado_em = now() "
                    + "WHERE mapa_nos.revisao_origem < EXCLUDED.revisao_origem", projeto, id(dados, "requisitoId"),
                    dados.get("codigo"), dados.get("titulo"), dados.get("situacao"), revisao);
        } else {
            boolean removida = tipo.equals("RelacaoRemovida");
            banco.update("INSERT INTO mapa_arestas (relacao_id, projeto_id, origem_id, destino_id, tipo, justificativa, revisao_origem, atualizado_em, removida) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, now(), ?) ON CONFLICT (relacao_id) DO UPDATE SET "
                    + "origem_id = EXCLUDED.origem_id, destino_id = EXCLUDED.destino_id, tipo = EXCLUDED.tipo, "
                    + "justificativa = EXCLUDED.justificativa, revisao_origem = EXCLUDED.revisao_origem, "
                    + "atualizado_em = now(), removida = EXCLUDED.removida WHERE mapa_arestas.revisao_origem < EXCLUDED.revisao_origem",
                    id(dados, "relacaoId"), projeto, removida ? null : id(dados, "origemId"),
                    removida ? null : id(dados, "destinoId"), dados.get("tipo"), dados.get("justificativa"), revisao, removida);
            banco.update("UPDATE mapa_estado SET posicao_aplicada = greatest(posicao_aplicada, ?), ultima_atualizacao = now() WHERE projeto_id = ?",
                    numero(dados, "posicao"), projeto);
        }
        banco.update("UPDATE mapa_estado SET ultima_atualizacao = now() WHERE projeto_id = ?", projeto);
    }

    static boolean deveAplicar(Long revisaoAtual, long revisaoRecebida) {
        return revisaoAtual == null || revisaoRecebida > revisaoAtual;
    }

    private static UUID id(Map<String, Object> dados, String campo) {
        return UUID.fromString(dados.get(campo).toString());
    }

    private static long numero(Map<String, Object> dados, String campo) {
        return ((Number) dados.get(campo)).longValue();
    }
}
