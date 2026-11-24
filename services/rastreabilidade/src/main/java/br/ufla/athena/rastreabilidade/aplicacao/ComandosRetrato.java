package br.ufla.athena.rastreabilidade.aplicacao;

import br.ufla.athena.rastreabilidade.mensageria.ManipuladorMensagem;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import static br.ufla.athena.rastreabilidade.aplicacao.ParticipanteSaga.id;
import static br.ufla.athena.rastreabilidade.aplicacao.ParticipanteSaga.payload;

@Configuration
public class ComandosRetrato {
    private final JdbcTemplate banco;
    private final ParticipanteSaga participante;

    public ComandosRetrato(JdbcTemplate banco, ParticipanteSaga participante) {
        this.banco = banco;
        this.participante = participante;
    }

    @Bean
    public ManipuladorMensagem registrarRetratoRastreabilidade() {
        return manipulador("RegistrarRetratoRastreabilidade", this::registrar);
    }

    @Bean
    public ManipuladorMensagem cancelarRetratoRastreabilidade() {
        return manipulador("CancelarRetratoRastreabilidade", this::cancelar);
    }

    private void registrar(Map<String, Object> envelope) {
        Map<String, Object> dados = payload(envelope);
        UUID submissao = id(dados, "submissaoId");
        UUID projeto = id(envelope, "projetoId");
        participante.travar(submissao, id(envelope, "commandId"));
        if (participante.repetir(envelope, false)) {
            return;
        }
        String recusa = banco.queryForObject("SELECT EXISTS (SELECT 1 FROM cancelamentos_submissao WHERE submissao_id = ?)", Boolean.class, submissao)
                ? "SUBMISSAO_CANCELADA" : !Instant.parse(dados.get("deadline").toString()).isAfter(Instant.now())
                ? "PRAZO_EXPIRADO" : null;
        if (recusa != null) {
            participante.concluir(envelope, "RetratoRastreabilidadeRecusado", Map.of(
                    "submissaoId", submissao.toString(), "code", recusa,
                    "detail", recusa.equals("PRAZO_EXPIRADO") ? "Prazo da submissão expirado." : "Submissão cancelada.",
                    "retryable", false), false);
            return;
        }
        banco.query("SELECT pg_advisory_xact_lock(hashtext(?))", rs -> { }, projeto.toString());
        UUID requisito = id(dados, "requisitoId");
        List<Map<String, Object>> relacoes = banco.query(
                "SELECT id, origem_id, destino_id, tipo, justificativa, atualizado_em FROM relacoes WHERE projeto_id = ? AND (origem_id = ? OR destino_id = ?) ORDER BY id",
                (rs, n) -> {
                    Map<String, Object> relacao = new LinkedHashMap<>();
                    relacao.put("relacaoId", rs.getObject("id").toString());
                    relacao.put("projetoId", projeto.toString());
                    relacao.put("origemId", rs.getObject("origem_id").toString());
                    relacao.put("destinoId", rs.getObject("destino_id").toString());
                    relacao.put("tipo", rs.getString("tipo"));
                    relacao.put("justificativa", rs.getString("justificativa"));
                    relacao.put("atualizadoEm", rs.getTimestamp("atualizado_em").toInstant().toString());
                    return relacao;
                }, projeto, requisito, requisito);
        List<Map<String, Object>> relacionados = relacoes.stream().map(relacao -> {
            boolean saida = requisito.toString().equals(relacao.get("origemId"));
            return Map.<String, Object>of("requisitoId", relacao.get(saida ? "destinoId" : "origemId"),
                    "tipo", relacao.get("tipo"), "sentido", saida ? "SAIDA" : "ENTRADA");
        }).toList();
        UUID retrato = UUID.randomUUID();
        banco.update("INSERT INTO retratos (id, projeto_id, requisito_id, submissao_id, versao_id, hash_conteudo, estado, relacoes) VALUES (?, ?, ?, ?, ?, ?, 'ATIVO', CAST(? AS jsonb))",
                retrato, projeto, requisito, submissao, id(dados, "versaoId"), dados.get("hashConteudo"), participante.escrever(relacoes));
        participante.concluir(envelope, "RetratoRastreabilidadeRegistrado", Map.of("submissaoId", submissao.toString(),
                "retratoId", retrato.toString(), "relacionados", relacionados), false);
    }

    private void cancelar(Map<String, Object> envelope) {
        UUID submissao = id(payload(envelope), "submissaoId");
        participante.travar(submissao, id(envelope, "commandId"));
        if (participante.repetir(envelope, false)) {
            return;
        }
        banco.update("INSERT INTO cancelamentos_submissao (submissao_id, projeto_id) VALUES (?, ?) ON CONFLICT DO NOTHING", submissao, id(envelope, "projetoId"));
        List<UUID> retratos = banco.query("UPDATE retratos SET estado = 'CANCELADO', atualizado_em = now() WHERE submissao_id = ? AND projeto_id = ? RETURNING id",
                (rs, n) -> rs.getObject(1, UUID.class), submissao, id(envelope, "projetoId"));
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("submissaoId", submissao.toString());
        resposta.put("retratoId", retratos.isEmpty() ? null : retratos.get(0).toString());
        participante.concluir(envelope, "CancelamentoRetratoConfirmado", resposta, false);
    }

    private static ManipuladorMensagem manipulador(String tipo, java.util.function.Consumer<Map<String, Object>> acao) {
        return new ManipuladorMensagem() {
            public String messageType() {
                return tipo;
            }
            public void tratar(Map<String, Object> envelope) {
                acao.accept(envelope);
            }
        };
    }
}
