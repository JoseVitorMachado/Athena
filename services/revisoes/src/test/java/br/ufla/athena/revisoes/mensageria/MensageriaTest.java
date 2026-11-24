package br.ufla.athena.revisoes.mensageria;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MensageriaTest {
    @Test
    void agendaFalhasProgressivamenteSemDesistir() {
        Instant agora = Instant.parse("2026-01-01T00:00:00Z");
        MensagemOutbox mensagem = new MensagemOutbox(UUID.randomUUID(), "Evento", null,
                "athena.events", "servico.Evento", Map.of(), agora);
        for (long segundos : new long[] {2, 10, 30, 60, 60}) {
            mensagem.registrarFalha("x".repeat(1100), agora);
            assertEquals(agora.plusSeconds(segundos), mensagem.getProximaTentativa());
            assertEquals(1000, mensagem.getUltimoErro().length());
        }
    }

    @Test
    void montaEnvelopeVersionadoComCamposOpcionais() {
        UUID id = UUID.randomUUID();
        UUID projeto = UUID.randomUUID();
        UUID saga = UUID.randomUUID();
        Instant instante = Instant.parse("2026-01-01T00:00:00Z");
        Map<String, Object> payload = Map.of("valor", 1);
        Map<String, Object> envelope = Envelope.criar(id, "Evento", instante, id,
                null, projeto, saga, null, payload);
        assertEquals(1, envelope.get("schemaVersion"));
        assertEquals(id.toString(), envelope.get("messageId"));
        assertEquals("Evento", envelope.get("messageType"));
        assertEquals(instante.toString(), envelope.get("occurredAt"));
        assertEquals(id.toString(), envelope.get("correlationId"));
        assertEquals(projeto.toString(), envelope.get("projetoId"));
        assertEquals(saga.toString(), envelope.get("sagaId"));
        assertEquals(payload, envelope.get("payload"));
        assertFalse(envelope.containsKey("causationId"));
        assertFalse(envelope.containsKey("commandId"));
    }

    @Test
    void escolheRetentativaOuDlqAposCincoFalhas() {
        assertEquals(2000, PoliticaRetentativa.esperaMillis(1));
        assertEquals(10000, PoliticaRetentativa.esperaMillis(2));
        assertEquals(30000, PoliticaRetentativa.esperaMillis(3));
        assertEquals(60000, PoliticaRetentativa.esperaMillis(4));
        assertEquals(0, PoliticaRetentativa.esperaMillis(5));
    }
}
