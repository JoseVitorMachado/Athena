package br.ufla.athena.rastreabilidade.aplicacao;

import br.ufla.athena.rastreabilidade.mensageria.EmissorMensagens;
import br.ufla.athena.rastreabilidade.mensageria.ManipuladorMensagem;
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

    static boolean quiescente() { return true; }

    private void fechar(Map<String, Object> envelope) {
        UUID projeto = id(envelope, "projetoId");
        UUID exclusao = exclusao(envelope, projeto);
        bloquear(projeto, exclusao);
        if (!quiescente()) {
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
        banco.update("DELETE FROM resultados_comando WHERE projeto_id = ?", projeto);
        banco.update("DELETE FROM cancelamentos_submissao WHERE projeto_id = ?", projeto);
        banco.update("DELETE FROM retratos WHERE projeto_id = ?", projeto);
        banco.update("DELETE FROM relacoes WHERE projeto_id = ?", projeto);
        banco.update("DELETE FROM mapa_nos WHERE projeto_id = ?", projeto);
        banco.update("DELETE FROM mapa_arestas WHERE projeto_id = ?", projeto);
        banco.update("DELETE FROM mapa_estado WHERE projeto_id = ?", projeto);
        banco.update("DELETE FROM sequencias_escrita WHERE projeto_id = ?", projeto);
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
                        "projetoId", projeto.toString(), "servico", "rastreabilidade"));
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
