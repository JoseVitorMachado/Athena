package br.ufla.athena.integracoes.mensageria;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class PublicadorOutbox {
    private static final Logger log = LoggerFactory.getLogger(PublicadorOutbox.class);
    private final OutboxRepository outbox;
    private final RabbitTemplate rabbit;
    private final ObjectMapper json;
    private final TransactionTemplate transacao;
    private final int limite;

    public PublicadorOutbox(OutboxRepository outbox, RabbitTemplate rabbit, ObjectMapper json,
                            TransactionTemplate transacao,
                            @Value("${athena.mensageria.lote-publicacao:50}") int limite) {
        this.outbox = outbox;
        this.rabbit = rabbit;
        this.json = json;
        this.transacao = transacao;
        this.limite = limite;
    }

    @Scheduled(fixedDelayString = "${athena.mensageria.intervalo-publicacao:500ms}")
    public void publicar() {
        transacao.executeWithoutResult(status -> outbox.buscarPendentes(limite).forEach(this::publicar));
    }

    private void publicar(MensagemOutbox registro) {
        try {
            MessageProperties propriedades = new MessageProperties();
            propriedades.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            propriedades.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            propriedades.setMessageId(registro.getId().toString());
            propriedades.setType(registro.getTipo());
            Object correlacao = registro.getPayload().get("correlationId");
            if (correlacao != null) propriedades.setCorrelationId(correlacao.toString());
            if (registro.getSagaId() != null) propriedades.setHeader("sagaId", registro.getSagaId().toString());
            Message mensagem = new Message(json.writeValueAsBytes(registro.getPayload()), propriedades);
            rabbit.invoke(ops -> {
                ops.send(registro.getExchange(), registro.getRoutingKey(), mensagem);
                ops.waitForConfirmsOrDie(5000);
                return null;
            });
            // Uma queda antes do commit pode repetir o envio; a inbox trata a repetição (livro §6.8).
            registro.marcarPublicada(Instant.now());
        } catch (Exception erro) {
            registro.registrarFalha(erro.getClass().getSimpleName() + ": " + erro.getMessage(), Instant.now());
            log.warn("Falha ao publicar mensagem {}: {}", registro.getId(), erro.getClass().getSimpleName());
        }
    }
}
