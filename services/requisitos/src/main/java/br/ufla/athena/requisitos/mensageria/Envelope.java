package br.ufla.athena.requisitos.mensageria;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Envelope versionado (livro §6.6). */
public final class Envelope {
    private Envelope() { }

    public static Map<String, Object> criar(UUID messageId, String messageType, Instant occurredAt,
            UUID correlationId, UUID causationId, UUID projetoId, UUID sagaId, UUID commandId,
            Map<String, Object> payload) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("schemaVersion", 1);
        envelope.put("messageId", messageId.toString());
        envelope.put("messageType", messageType);
        envelope.put("occurredAt", occurredAt.toString());
        envelope.put("correlationId", correlationId.toString());
        if (causationId != null) envelope.put("causationId", causationId.toString());
        envelope.put("projetoId", projetoId.toString());
        if (sagaId != null) envelope.put("sagaId", sagaId.toString());
        if (commandId != null) envelope.put("commandId", commandId.toString());
        envelope.put("payload", payload);
        return envelope;
    }
}
