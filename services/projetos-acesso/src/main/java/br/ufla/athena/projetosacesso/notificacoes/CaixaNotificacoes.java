package br.ufla.athena.projetosacesso.notificacoes;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Armazena notificações na transação da operação que as produziu. */
@Component
public class CaixaNotificacoes {
    private final JdbcTemplate banco;
    public CaixaNotificacoes(JdbcTemplate banco) { this.banco = banco; }

    /** Registra uma notificação, ignorando uma origem já recebida pelo destinatário. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void notificar(UUID destinatario, UUID projeto, String tipo, String mensagem, String link, String origem) {
        banco.update("INSERT INTO notificacoes(id,destinatario_id,projeto_id,tipo,mensagem,link,origem) VALUES (?,?,?,?,?,?,?) ON CONFLICT DO NOTHING",
                UUID.randomUUID(), destinatario, projeto, tipo, mensagem, link, origem);
    }
}
