package br.ufla.athena.rastreabilidade.aplicacao;

import br.ufla.athena.rastreabilidade.api.Problema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class MapaService {
    private final JdbcTemplate banco;
    private final ClienteRequisitos requisitos;
    private final ProjecaoMapa projecao;
    private final TransactionTemplate transacao;

    public MapaService(JdbcTemplate banco, ClienteRequisitos requisitos, ProjecaoMapa projecao, TransactionTemplate transacao) {
        this.banco = banco;
        this.requisitos = requisitos;
        this.projecao = projecao;
        this.transacao = transacao;
    }

    public Resultado obter(UUID projeto) {
        return transacao.execute(status -> {
            var estados = banco.query("SELECT e.geracao, e.situacao, e.posicao_aplicada, e.ultima_atualizacao, "
                    + "coalesce(s.posicao, 0) FROM mapa_estado e LEFT JOIN sequencias_escrita s ON s.projeto_id = e.projeto_id WHERE e.projeto_id = ? AND e.disponivel",
                    (rs, n) -> new Estado(rs.getObject(1, UUID.class), rs.getString(2), rs.getLong(3),
                            rs.getTimestamp(4).toInstant(), rs.getLong(5)), projeto);
            if (estados.isEmpty()) {
                throw new Problema(HttpStatus.SERVICE_UNAVAILABLE, "MAPA_INDISPONIVEL", "Projeção do mapa indisponível.");
            }
            Estado estado = estados.get(0);
            List<No> nos = banco.query("SELECT requisito_id, titulo, situacao, codigo, tipo, epico_id, responsavel_id FROM mapa_nos "
                    + "WHERE projeto_id = ? ORDER BY requisito_id LIMIT 501",
                    (rs, n) -> new No(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3), rs.getString(4),
                            rs.getString(5), rs.getObject(6, UUID.class), rs.getObject(7, UUID.class)), projeto);
            List<Aresta> arestas = banco.query("SELECT relacao_id, projeto_id, origem_id, destino_id, tipo, justificativa, atualizado_em "
                    + "FROM mapa_arestas WHERE projeto_id = ? AND NOT removida ORDER BY relacao_id LIMIT 1001",
                    (rs, n) -> new Aresta(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class), rs.getObject(3, UUID.class),
                            rs.getObject(4, UUID.class), rs.getString(5), rs.getString(6), rs.getTimestamp(7).toInstant()), projeto);
            boolean truncado = nos.size() > 500 || arestas.size() > 1000;
            Mapa mapa = new Mapa(nos.subList(0, Math.min(500, nos.size())), arestas.subList(0, Math.min(1000, arestas.size())),
                    truncado, estado.atualizacao(), situacao(estado.situacao(), estado.posicao(), estado.escrita()), estado.geracao());
            return new Resultado(mapa, "\"" + estado.geracao() + "-" + estado.posicao() + "\"");
        });
    }

    public static String situacao(String estado, long aplicada, long escrita) {
        if (estado.equals("RECONSTRUINDO")) {
            return "RECONSTRUINDO";
        }
        if (aplicada < escrita) {
            return "ATUALIZACAO_PENDENTE";
        }
        return "ATUALIZADO";
    }

    public synchronized void reconstruir(UUID projeto) {
        UUID geracao = UUID.randomUUID();
        Long posicao = transacao.execute(status -> {
            banco.query("SELECT pg_advisory_xact_lock(hashtext(?))", rs -> { }, projeto.toString());
            banco.update("INSERT INTO mapa_estado (projeto_id, geracao, situacao, posicao_aplicada, disponivel, ultima_atualizacao) "
                    + "VALUES (?, ?, 'RECONSTRUINDO', 0, false, now()) ON CONFLICT (projeto_id) "
                    + "DO UPDATE SET situacao = 'RECONSTRUINDO'", projeto, geracao);
            return banco.queryForObject("SELECT coalesce((SELECT posicao FROM sequencias_escrita WHERE projeto_id = ?), 0)", Long.class, projeto);
        });
        try {
            List<Map<String, Object>> nosAnteriores = banco.queryForList(
                    "SELECT requisito_id, revisao_origem FROM mapa_nos WHERE projeto_id = ?", projeto);
            List<Map<String, Object>> nos = requisitos.listar(projeto);
            Set<UUID> idsNos = nos.stream()
                    .map(no -> UUID.fromString(no.get("requisitoId").toString()))
                    .collect(Collectors.toSet());
            transacao.executeWithoutResult(status -> {
                banco.query("SELECT pg_advisory_xact_lock(hashtext(?))", rs -> { }, projeto.toString());
                List<Map<String, Object>> arestas = banco.queryForList("SELECT id AS \"relacaoId\", projeto_id AS \"projetoId\", origem_id AS \"origemId\", destino_id AS \"destinoId\", tipo, justificativa, revisao FROM relacoes WHERE projeto_id = ?", projeto);
                for (Map<String, Object> anterior : nosAnteriores) {
                    UUID requisito = (UUID) anterior.get("requisito_id");
                    if (!idsNos.contains(requisito)) {
                        banco.update("DELETE FROM mapa_nos WHERE projeto_id = ? AND requisito_id = ? AND revisao_origem = ?",
                                projeto, requisito, anterior.get("revisao_origem"));
                    }
                }
                banco.update("UPDATE mapa_arestas a SET removida = true, revisao_origem = greatest(a.revisao_origem, "
                        + "coalesce((SELECT max((o.payload->'payload'->>'revisao')::bigint) FROM outbox o "
                        + "WHERE o.tipo = 'RelacaoRemovida' AND o.payload->'payload'->>'relacaoId' = a.relacao_id::text), "
                        + "9223372036854775807)), atualizado_em = now() WHERE a.projeto_id = ? AND NOT a.removida "
                        + "AND NOT EXISTS (SELECT 1 FROM relacoes r WHERE r.id = a.relacao_id AND r.projeto_id = a.projeto_id)", projeto);
                for (Map<String, Object> no : nos) {
                    projecao.aplicar("RequisitoAlterado", no);
                }
                for (Map<String, Object> aresta : arestas) {
                    aresta.put("posicao", posicao);
                    projecao.aplicar("RelacaoCriada", aresta);
                }
                banco.update("INSERT INTO mapa_estado (projeto_id, geracao, situacao, posicao_aplicada, ultima_atualizacao) VALUES (?, ?, 'ATUALIZADO', ?, now()) "
                        + "ON CONFLICT (projeto_id) DO UPDATE SET geracao = EXCLUDED.geracao, situacao = 'ATUALIZADO', disponivel = true, "
                        + "posicao_aplicada = greatest(mapa_estado.posicao_aplicada, EXCLUDED.posicao_aplicada), ultima_atualizacao = now()",
                        projeto, geracao, posicao);
            });
        } catch (RuntimeException e) {
            transacao.executeWithoutResult(status -> banco.update("UPDATE mapa_estado SET situacao = 'ATUALIZADO' WHERE projeto_id = ?", projeto));
            throw e;
        }
    }

    /** Nó do mapa; tipo, épico e responsável ficam ausentes quando não definidos. */
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    public record No(UUID requisitoId, String titulo, String situacao, String codigo, String tipo, UUID epicoId,
                     UUID responsavelId) { }
    public record Aresta(UUID relacaoId, UUID projetoId, UUID origemId, UUID destinoId, String tipo, String justificativa, Instant atualizadoEm) { }
    public record Mapa(List<No> nos, List<Aresta> arestas, boolean truncado, Instant ultimaAtualizacao, String situacaoProjecao, UUID geracao) { }
    public record Resultado(Mapa mapa, String etag) { }
    private record Estado(UUID geracao, String situacao, long posicao, Instant atualizacao, long escrita) { }
}
