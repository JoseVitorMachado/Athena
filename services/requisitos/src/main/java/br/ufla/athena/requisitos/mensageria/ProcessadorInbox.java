package br.ufla.athena.requisitos.mensageria;

import java.sql.Types;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProcessadorInbox {
    private static final Logger log = LoggerFactory.getLogger(ProcessadorInbox.class);
    private final JdbcTemplate banco;

    public ProcessadorInbox(JdbcTemplate banco) { this.banco = banco; }

    public static boolean ignorar(String tipo, boolean projetoExcluido) {
        return projetoExcluido && !"FecharProjeto".equals(tipo) && !"LimparProjeto".equals(tipo);
    }

    @Transactional
    public void processar(Map<String, Object> envelope, ManipuladorMensagem manipulador) {
        UUID id = UUID.fromString(envelope.get("messageId").toString());
        Object comando = envelope.get("commandId");
        UUID commandId = comando == null ? null : UUID.fromString(comando.toString());
        int inseridos = banco.update("INSERT INTO inbox (message_id, tipo, command_id) "
                + "VALUES (?, ?, ?) ON CONFLICT (message_id) DO NOTHING", declaracao -> {
                    declaracao.setObject(1, id);
                    declaracao.setString(2, envelope.get("messageType").toString());
                    if (commandId == null) declaracao.setNull(3, Types.OTHER);
                    else declaracao.setObject(3, commandId);
                });
        if (inseridos == 0) {
            log.debug("Mensagem {} já processada", id);
            return;
        }
        Object projeto = envelope.get("projetoId");
        if (projeto != null) {
            UUID projetoId = UUID.fromString(projeto.toString());
            banco.query("SELECT pg_advisory_xact_lock(hashtext(?))", rs -> { }, "exclusao:" + projetoId);
            boolean excluido = Boolean.TRUE.equals(banco.queryForObject(
                    "SELECT EXISTS (SELECT 1 FROM projetos_excluidos WHERE projeto_id = ?)", Boolean.class, projetoId));
            if (ignorar(envelope.get("messageType").toString(), excluido)) {
                log.debug("Mensagem {} ignorada para projeto excluído", id);
                banco.update("UPDATE inbox SET processado_em = now() WHERE message_id = ?", id);
                return;
            }
        }
        if (manipulador == null) throw new MensagemInvalidaException("sem manipulador");
        manipulador.tratar(envelope);
        banco.update("UPDATE inbox SET processado_em = now() WHERE message_id = ?", id);
    }
}
