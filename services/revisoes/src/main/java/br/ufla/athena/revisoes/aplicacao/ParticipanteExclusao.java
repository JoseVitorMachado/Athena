package br.ufla.athena.revisoes.aplicacao;

import br.ufla.athena.revisoes.mensageria.EmissorMensagens;
import br.ufla.athena.revisoes.mensageria.ManipuladorMensagem;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/** Participante da exclusão física, executado na transação da inbox. */
@Configuration
public class ParticipanteExclusao {
    private static final Logger log = LoggerFactory.getLogger(ParticipanteExclusao.class);
    private final JdbcTemplate banco;
    private final EmissorMensagens emissor;

    public ParticipanteExclusao(JdbcTemplate banco, EmissorMensagens emissor) {
        this.banco = banco;
        this.emissor = emissor;
    }

    @Bean
    public ManipuladorMensagem fecharProjeto() { return manipulador("FecharProjeto", this::fechar); }

    @Bean
    public ManipuladorMensagem limparProjeto() { return manipulador("LimparProjeto", this::limpar); }

    private void fechar(Map<String, Object> envelope) {
        UUID projetoId = id(envelope, "projetoId");
        UUID exclusaoId = exclusao(envelope, projetoId);
        bloquear(projetoId, exclusaoId);
        if (quiescente(projetoId)) emitir(envelope, exclusaoId, projetoId, "ProjetoFechado");
        else log.info("aguardando quiescência: {}", projetoId);
    }

    private boolean quiescente(UUID projetoId) {
        banco.update("UPDATE lotes SET estado = 'CANCELADO', atualizado_em = now(), revisao = revisao + 1 WHERE projeto_id = ? AND estado = 'ATIVO'", projetoId);
        return !Boolean.TRUE.equals(banco.queryForObject("SELECT EXISTS (SELECT 1 FROM lotes WHERE projeto_id = ? AND estado = 'PREPARADO')", Boolean.class, projetoId));
    }

    private void limpar(Map<String, Object> envelope) {
        UUID projetoId = id(envelope, "projetoId");
        UUID exclusaoId = exclusao(envelope, projetoId);
        bloquear(projetoId, exclusaoId);
        boolean excluido = Boolean.TRUE.equals(banco.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM projetos_excluidos WHERE projeto_id = ?)", Boolean.class, projetoId));
        if (!excluido) {
        banco.update("DELETE FROM resultados_comando WHERE projeto_id = ? OR resposta_payload ->> 'submissaoId' IN (SELECT submissao_id::text FROM lotes WHERE projeto_id = ?)", projetoId, projetoId);
        banco.update("DELETE FROM cancelamentos_submissao WHERE projeto_id = ? OR submissao_id IN (SELECT submissao_id FROM lotes WHERE projeto_id = ?)", projetoId, projetoId);
        banco.update("DELETE FROM chaves_idempotencia WHERE projeto_id = ?", projetoId);
        banco.update("DELETE FROM decisoes WHERE lote_id IN (SELECT id FROM lotes WHERE projeto_id = ?)", projetoId);
        banco.update("DELETE FROM pendencias WHERE lote_id IN (SELECT id FROM lotes WHERE projeto_id = ?)", projetoId);
        banco.update("DELETE FROM lotes WHERE projeto_id = ?", projetoId);
            banco.update("DELETE FROM outbox WHERE payload ->> 'projetoId' = ?", projetoId.toString());
            banco.update("DELETE FROM inbox WHERE projeto_id = ? AND message_id <> ?", projetoId, id(envelope, "messageId"));
            banco.update("INSERT INTO projetos_excluidos (projeto_id) VALUES (?) ON CONFLICT DO NOTHING", projetoId);
        }
        emitir(envelope, exclusaoId, projetoId, "ProjetoLimpo");
    }

    private void bloquear(UUID projetoId, UUID exclusaoId) {
        banco.update("INSERT INTO projetos_bloqueados (projeto_id, exclusao_id) VALUES (?, ?) ON CONFLICT (projeto_id) DO NOTHING", projetoId, exclusaoId);
    }

    private void emitir(Map<String, Object> envelope, UUID exclusaoId, UUID projetoId, String tipo) {
        emissor.evento(tipo, projetoId, null, null, id(envelope, "correlationId"), id(envelope, "messageId"),
                Map.of("exclusaoId", exclusaoId.toString(), "projetoId", projetoId.toString(), "servico", "revisoes"));
    }

    private static UUID exclusao(Map<String, Object> envelope, UUID projetoId) {
        @SuppressWarnings("unchecked")
        Map<String, Object> dados = (Map<String, Object>) envelope.get("payload");
        if (!projetoId.equals(id(dados, "projetoId"))) throw new IllegalArgumentException("Projeto divergente");
        return id(dados, "exclusaoId");
    }

    private static UUID id(Map<String, Object> dados, String campo) {
        return UUID.fromString(dados.get(campo).toString());
    }

    private static ManipuladorMensagem manipulador(String tipo, Consumer<Map<String, Object>> acao) {
        return new ManipuladorMensagem() {
            public String messageType() { return tipo; }
            public void tratar(Map<String, Object> envelope) { acao.accept(envelope); }
        };
    }
}
