package br.ufla.athena.revisoes.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Intenção de publicar uma mensagem (livro §6.8). É gravada na mesma transação
 * do efeito de negócio; o publicador que a envia ao RabbitMQ entra na Parte 4.
 */
@Entity
@Table(name = "outbox")
public class MensagemOutbox {

    @Id
    private UUID id;

    private String tipo;

    private UUID sagaId;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> payload;

    private Instant criadoEm;

    private int tentativas;

    private Instant proximaTentativa;

    private Instant publicadoEm;

    protected MensagemOutbox() {
        // exigido pelo JPA
    }

    public MensagemOutbox(UUID id, String tipo, Map<String, Object> payload, Instant agora) {
        this.id = id;
        this.tipo = tipo;
        this.payload = payload;
        this.criadoEm = agora;
        this.tentativas = 0;
        this.proximaTentativa = agora;
    }

    public UUID getId() {
        return id;
    }
}
