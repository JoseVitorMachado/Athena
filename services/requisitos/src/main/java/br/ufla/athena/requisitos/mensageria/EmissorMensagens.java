package br.ufla.athena.requisitos.mensageria;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EmissorMensagens {
    private final OutboxRepository outbox;
    private final String servico;

    public EmissorMensagens(OutboxRepository outbox, @Value("${athena.mensageria.servico}") String servico) {
        this.outbox = outbox;
        this.servico = servico;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void comando(String destino, String messageType, UUID projetoId, UUID sagaId, UUID commandId,
                        UUID correlationId, UUID causationId, Map<String, Object> payload) {
        gravar("athena.commands", destino + "." + messageType, messageType, projetoId, sagaId,
                commandId, correlationId, causationId, payload);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void evento(String messageType, UUID projetoId, UUID sagaId, UUID commandId,
                       UUID correlationId, UUID causationId, Map<String, Object> payload) {
        gravar("athena.events", servico + "." + messageType, messageType, projetoId, sagaId,
                commandId, correlationId, causationId, payload);
    }

    private void gravar(String exchange, String routingKey, String tipo, UUID projetoId, UUID sagaId,
                        UUID commandId, UUID correlationId, UUID causationId, Map<String, Object> payload) {
        UUID messageId = UUID.randomUUID();
        Instant agora = Instant.now();
        Map<String, Object> envelope = Envelope.criar(messageId, tipo, agora, correlationId,
                causationId, projetoId, sagaId, commandId, payload);
        outbox.save(new MensagemOutbox(messageId, tipo, sagaId, exchange, routingKey, envelope, agora));
    }
}
