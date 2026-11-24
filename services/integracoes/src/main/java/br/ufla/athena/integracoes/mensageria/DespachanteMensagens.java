package br.ufla.athena.integracoes.mensageria;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.ImmediateRequeueAmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageBuilderSupport;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class DespachanteMensagens {
    private static final Logger log = LoggerFactory.getLogger(DespachanteMensagens.class);
    private final Map<String, ManipuladorMensagem> manipuladores;
    private final ProcessadorInbox inbox;
    private final RabbitTemplate rabbit;
    private final ObjectMapper json;

    public DespachanteMensagens(List<ManipuladorMensagem> manipuladores, ProcessadorInbox inbox,
                                RabbitTemplate rabbit, ObjectMapper json) {
        this.manipuladores = manipuladores.stream().collect(Collectors.toUnmodifiableMap(
                ManipuladorMensagem::messageType, m -> m));
        this.inbox = inbox;
        this.rabbit = rabbit;
        this.json = json;
    }

    public void consumir(Message mensagem, String fila) {
        int anteriores = tentativas(mensagem);
        try {
            Map<String, Object> envelope = lerEnvelope(mensagem);
            ManipuladorMensagem manipulador = manipuladores.get(envelope.get("messageType"));
            if (manipulador == null) throw new MensagemInvalidaException("sem manipulador");
            inbox.processar(envelope, manipulador);
        } catch (MensagemInvalidaException erro) {
            // Sem retentativa: vai direto para a DLQ, mas fica registrado para diagnóstico.
            log.warn("Mensagem {} inválida, enviada para a DLQ: {}", mensagem.getMessageProperties().getMessageId(),
                    erro.getMessage());
            encaminhar(mensagem, fila + ".dlq", anteriores, erro.getMessage(), 0);
        } catch (Exception erro) {
            int falhas = anteriores + 1;
            long espera = PoliticaRetentativa.esperaMillis(falhas);
            String destino = fila + (espera == 0 ? ".dlq" : ".retentativa");
            log.warn("Falha ao processar mensagem {}: {}", mensagem.getMessageProperties().getMessageId(),
                    erro.getClass().getSimpleName());
            encaminhar(mensagem, destino, falhas, "falha de processamento: " + erro.getClass().getSimpleName(), espera);
        }
    }

    private Map<String, Object> lerEnvelope(Message mensagem) {
        try {
            Map<String, Object> envelope = json.readValue(mensagem.getBody(), new TypeReference<>() { });
            if (envelope == null || !(envelope.get("messageId") instanceof String id)
                    || !(envelope.get("messageType") instanceof String tipo) || tipo.isBlank()
                    || !(envelope.get("schemaVersion") instanceof Number versao)
                    || versao.doubleValue() != 1 || !(envelope.get("payload") instanceof Map)) {
                throw new MensagemInvalidaException("envelope inválido ou versão desconhecida");
            }
            UUID.fromString(id);
            if (envelope.get("commandId") != null) UUID.fromString(envelope.get("commandId").toString());
            return envelope;
        } catch (MensagemInvalidaException erro) {
            throw erro;
        } catch (Exception erro) {
            throw new MensagemInvalidaException("envelope inválido");
        }
    }

    private int tentativas(Message mensagem) {
        Object valor = mensagem.getMessageProperties().getHeaders().get("x-athena-tentativas");
        return valor instanceof Number numero ? Math.max(0, numero.intValue()) : 0;
    }

    private void encaminhar(Message original, String destino, int tentativas, String motivo, long espera) {
        MessageBuilderSupport<Message> copia = MessageBuilder.fromMessage(original)
                .setHeader("x-athena-motivo", motivo)
                .setHeader("x-athena-tentativas", tentativas)
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        // A expiração anterior não pode acompanhar a mensagem para a DLQ.
        copia.setExpiration(espera == 0 ? null : Long.toString(espera));
        try {
            rabbit.invoke(ops -> {
                ops.send("", destino, copia.build());
                ops.waitForConfirmsOrDie(5000);
                return null;
            });
        } catch (Exception erro) {
            throw new ImmediateRequeueAmqpException("Falha ao encaminhar mensagem", erro);
        }
    }
}
