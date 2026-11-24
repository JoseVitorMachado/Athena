package br.ufla.athena.revisoes.aplicacao;

import br.ufla.athena.revisoes.dominio.Lote;
import br.ufla.athena.revisoes.dominio.Pendencia;
import br.ufla.athena.revisoes.mensageria.ManipuladorMensagem;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import static br.ufla.athena.revisoes.aplicacao.ParticipanteSaga.id;
import static br.ufla.athena.revisoes.aplicacao.ParticipanteSaga.payload;

@Configuration
public class ComandosLote {
    private static final Logger log = LoggerFactory.getLogger(ComandosLote.class);
    private final JdbcTemplate banco;
    private final EntityManager entidades;
    private final ParticipanteSaga participante;
    private final InjetorFalhas falhas;

    public ComandosLote(JdbcTemplate banco, EntityManager entidades, ParticipanteSaga participante,
                        InjetorFalhas falhas) {
        this.banco = banco;
        this.entidades = entidades;
        this.participante = participante;
        this.falhas = falhas;
    }

    @Bean
    public ManipuladorMensagem abrirLoteRevisao() {
        return manipulador("AbrirLoteRevisao", this::abrir);
    }

    @Bean
    public ManipuladorMensagem cancelarLoteRevisao() {
        return manipulador("CancelarLoteRevisao", this::cancelar);
    }

    @Bean
    public ManipuladorMensagem ativarLoteRevisao() {
        return manipulador("AtivarLoteRevisao", this::ativar);
    }

    private void abrir(Map<String, Object> envelope) {
        Map<String, Object> dados = payload(envelope);
        UUID submissao = id(dados, "submissaoId");
        participante.travar(submissao, id(envelope, "commandId"));
        InjetorFalhas.Modo modo = falhas.getAbrirLote();
        boolean silencioso = modo == InjetorFalhas.Modo.SILENCIAR;
        if (modo != InjetorFalhas.Modo.NENHUMA) {
            log.warn("Modo de falha {} na abertura do lote da submissão {}", modo, submissao);
        }
        if (participante.repetir(envelope, silencioso)) {
            return;
        }
        String recusa = banco.queryForObject("SELECT EXISTS (SELECT 1 FROM cancelamentos_submissao WHERE submissao_id = ?)", Boolean.class, submissao)
                ? "SUBMISSAO_CANCELADA" : !Instant.parse(dados.get("deadline").toString()).isAfter(Instant.now())
                ? "PRAZO_EXPIRADO" : modo == InjetorFalhas.Modo.RECUSAR ? "FALHA_INJETADA" : null;
        if (recusa != null) {
            participante.concluir(envelope, "LoteRevisaoRecusado", Map.of("submissaoId", submissao.toString(),
                    "code", recusa, "detail", switch (recusa) {
                        case "PRAZO_EXPIRADO" -> "Prazo da submissão expirado.";
                        case "SUBMISSAO_CANCELADA" -> "Submissão cancelada.";
                        default -> "Falha de abertura injetada.";
                    }, "retryable", false), false);
            return;
        }
        Lote lote = Lote.novo(id(envelope, "projetoId"), id(dados, "requisitoId"), submissao,
                id(dados, "versaoId"), dados.get("hashConteudo").toString());
        entidades.persist(lote);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> relacionados = (List<Map<String, Object>>) dados.get("relacionados");
        for (Map<String, Object> relacionado : relacionados) {
            String descricao = "Verificar impacto em " + relacionado.get("requisitoId") + " ("
                    + relacionado.get("tipo") + ", " + relacionado.get("sentido") + ")";
            entidades.persist(Pendencia.nova(lote, descricao));
        }
        entidades.persist(Pendencia.nova(lote, "Revisar o conteúdo submetido"));
        participante.concluir(envelope, "LoteRevisaoAberto", Map.of("submissaoId", submissao.toString(),
                "loteId", lote.getId().toString()), silencioso);
    }

    private void cancelar(Map<String, Object> envelope) {
        UUID submissao = id(payload(envelope), "submissaoId");
        participante.travar(submissao, id(envelope, "commandId"));
        if (participante.repetir(envelope, false)) {
            return;
        }
        banco.update("INSERT INTO cancelamentos_submissao (submissao_id, projeto_id) VALUES (?, ?) ON CONFLICT DO NOTHING", submissao, id(envelope, "projetoId"));
        List<UUID> lotes = banco.query("SELECT id FROM lotes WHERE submissao_id = ? AND projeto_id = ? FOR UPDATE",
                (rs, n) -> rs.getObject(1, UUID.class), submissao, id(envelope, "projetoId"));
        banco.update("UPDATE lotes SET estado = 'CANCELADO', atualizado_em = now(), revisao = revisao + 1 WHERE submissao_id = ? AND projeto_id = ? AND estado NOT IN ('ENCERRADO', 'CANCELADO')",
                submissao, id(envelope, "projetoId"));
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("submissaoId", submissao.toString());
        resposta.put("loteId", lotes.isEmpty() ? null : lotes.get(0).toString());
        participante.concluir(envelope, "CancelamentoLoteConfirmado", resposta, false);
    }

    private void ativar(Map<String, Object> envelope) {
        Map<String, Object> dados = payload(envelope);
        UUID submissao = id(dados, "submissaoId");
        UUID lote = id(dados, "loteId");
        participante.travar(submissao, id(envelope, "commandId"));
        if (participante.repetir(envelope, false)) {
            return;
        }
        int alterados = banco.update("UPDATE lotes SET estado = 'ATIVO', atualizado_em = now(), revisao = revisao + 1 WHERE id = ? AND submissao_id = ? AND projeto_id = ? AND estado = 'PREPARADO'",
                lote, submissao, id(envelope, "projetoId"));
        if (alterados == 0 && !banco.queryForObject("SELECT EXISTS (SELECT 1 FROM lotes WHERE id = ? AND submissao_id = ? AND projeto_id = ? AND estado = 'ATIVO')",
                Boolean.class, lote, submissao, id(envelope, "projetoId"))) {
            throw new IllegalStateException("Lote não está preparado ou ativo para esta submissão");
        }
        participante.concluir(envelope, "LoteRevisaoAtivado", Map.of("submissaoId", submissao.toString(),
                "loteId", lote.toString()), false);
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
