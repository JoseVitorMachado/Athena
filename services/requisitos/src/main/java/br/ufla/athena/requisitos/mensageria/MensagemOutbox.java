package br.ufla.athena.requisitos.mensageria;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "outbox")
public class MensagemOutbox {
    @Id
    private UUID id;
    private String tipo;
    private UUID sagaId;
    private String exchange;
    private String routingKey;
    private String ultimoErro;
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> payload;
    private Instant criadoEm;
    private int tentativas;
    private Instant proximaTentativa;
    private Instant publicadoEm;

    protected MensagemOutbox() { }

    public MensagemOutbox(UUID id, String tipo, UUID sagaId, String exchange, String routingKey,
                          Map<String, Object> envelope, Instant agora) {
        this.id = id;
        this.tipo = tipo;
        this.sagaId = sagaId;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.payload = envelope;
        this.criadoEm = agora;
        this.proximaTentativa = agora;
    }

    public void marcarPublicada(Instant agora) { this.publicadoEm = agora; }

    // A intenção permanece na outbox enquanto o broker estiver indisponível (livro §6.8).
    public void registrarFalha(String erro, Instant agora) {
        tentativas++;
        ultimoErro = erro == null ? null : erro.substring(0, Math.min(erro.length(), 1000));
        long espera = switch (tentativas) { case 1 -> 2; case 2 -> 10; case 3 -> 30; default -> 60; };
        proximaTentativa = agora.plusSeconds(espera);
    }

    public UUID getId() { return id; }
    public String getTipo() { return tipo; }
    public UUID getSagaId() { return sagaId; }
    public String getExchange() { return exchange; }
    public String getRoutingKey() { return routingKey; }
    public Map<String, Object> getPayload() { return payload; }
    public Instant getProximaTentativa() { return proximaTentativa; }
    public String getUltimoErro() { return ultimoErro; }
}
