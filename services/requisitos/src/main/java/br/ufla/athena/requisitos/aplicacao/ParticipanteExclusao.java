package br.ufla.athena.requisitos.aplicacao;

import br.ufla.athena.requisitos.mensageria.EmissorMensagens;
import br.ufla.athena.requisitos.mensageria.ManipuladorMensagem;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

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
    ManipuladorMensagem fecharProjeto() { return manipulador("FecharProjeto", this::fechar); }

    @Bean
    ManipuladorMensagem limparProjeto() { return manipulador("LimparProjeto", this::limpar); }

    static boolean quiescente(boolean possuiSubmissaoAtiva) { return !possuiSubmissaoAtiva; }

    private void fechar(Map<String, Object> envelope) {
        UUID projeto = id(envelope, "projetoId");
        UUID exclusao = exclusao(envelope, projeto);
        bloquear(projeto, exclusao);
        if (!quiescente(Boolean.TRUE.equals(banco.queryForObject("SELECT EXISTS (SELECT 1 FROM submissoes WHERE projeto_id = ? AND estado_saga IN ('EM_ANDAMENTO','COMPENSANDO','COMPENSACAO_PENDENTE'))", Boolean.class, projeto)))) {
            log.info("Projeto {} aguardando quiescência", projeto);
            return;
        }
        emitir(envelope, projeto, exclusao, "ProjetoFechado");
    }

    private void limpar(Map<String, Object> envelope) {
        UUID projeto = id(envelope, "projetoId");
        UUID exclusao = exclusao(envelope, projeto);
        bloquear(projeto, exclusao);
        boolean excluido = Boolean.TRUE.equals(banco.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM projetos_excluidos WHERE projeto_id = ?)", Boolean.class, projeto));
        if (!excluido) {
        banco.update("DELETE FROM historico_requisitos WHERE projeto_id = ?", projeto);
        banco.update("DELETE FROM comentarios_historico WHERE comentario_id IN (SELECT id FROM comentarios WHERE projeto_id = ?)", projeto);
        banco.update("DELETE FROM comentarios WHERE projeto_id = ?", projeto);
        banco.update("DELETE FROM baseline_itens WHERE baseline_id IN (SELECT id FROM baselines WHERE projeto_id = ?) OR requisito_id IN (SELECT id FROM requisitos WHERE projeto_id = ?)", projeto, projeto);
        banco.update("DELETE FROM baselines WHERE projeto_id = ?", projeto);
        banco.update("DELETE FROM alteracoes_situacao WHERE projeto_id = ?", projeto);
        banco.update("DELETE FROM publicacoes WHERE requisito_id IN (SELECT id FROM requisitos WHERE projeto_id = ?)", projeto);
        banco.update("DELETE FROM decisoes_aplicadas WHERE versao_id IN (SELECT v.id FROM versoes v JOIN requisitos r ON r.id = v.requisito_id WHERE r.projeto_id = ?)", projeto);
        banco.update("DELETE FROM submissoes WHERE projeto_id = ?", projeto);
        banco.update("DELETE FROM versoes WHERE requisito_id IN (SELECT id FROM requisitos WHERE projeto_id = ?)", projeto);
        banco.update("DELETE FROM requisitos WHERE projeto_id = ?", projeto);
        banco.update("DELETE FROM epicos WHERE projeto_id = ?", projeto);
        banco.update("DELETE FROM sequencias_codigo WHERE projeto_id = ?", projeto);
        banco.update("DELETE FROM comprovantes_humanos WHERE projeto_id = ?", projeto);
        banco.update("DELETE FROM chaves_idempotencia WHERE projeto_id = ?", projeto);
            banco.update("DELETE FROM outbox WHERE payload->>'projetoId' = ?", projeto.toString());
            banco.update("INSERT INTO projetos_excluidos (projeto_id) VALUES (?) ON CONFLICT DO NOTHING", projeto);
        }
        emitir(envelope, projeto, exclusao, "ProjetoLimpo");
    }

    private void bloquear(UUID projeto, UUID exclusao) {
        banco.update("INSERT INTO projetos_bloqueados (projeto_id, exclusao_id) VALUES (?, ?) ON CONFLICT DO NOTHING", projeto, exclusao);
    }

    private void emitir(Map<String, Object> envelope, UUID projeto, UUID exclusao, String tipo) {
        emissor.evento(tipo, projeto, null, id(envelope, "commandId"), id(envelope, "correlationId"),
                id(envelope, "messageId"), Map.of("exclusaoId", exclusao.toString(),
                        "projetoId", projeto.toString(), "servico", "requisitos"));
    }

    private static UUID exclusao(Map<String, Object> envelope, UUID projeto) {
        Object valor = envelope.get("payload");
        if (!(valor instanceof Map<?, ?> dados) || !projeto.equals(UUID.fromString(dados.get("projetoId").toString())))
            throw new IllegalArgumentException("Projeto do comando inválido");
        return UUID.fromString(dados.get("exclusaoId").toString());
    }

    private static UUID id(Map<String, Object> mapa, String campo) { return UUID.fromString(mapa.get(campo).toString()); }

    private static ManipuladorMensagem manipulador(String tipo, Consumer<Map<String, Object>> acao) {
        return new ManipuladorMensagem() {
            public String messageType() { return tipo; }
            public void tratar(Map<String, Object> envelope) { acao.accept(envelope); }
        };
    }
}
