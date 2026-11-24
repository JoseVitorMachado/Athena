package br.ufla.athena.integracoes.mensageria;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConsumidorComandos {
    private final DespachanteMensagens despachante;
    private final String fila;

    public ConsumidorComandos(DespachanteMensagens despachante,
                              @Value("${athena.mensageria.servico}") String servico) {
        this.despachante = despachante;
        this.fila = servico + ".comandos";
    }

    @RabbitListener(queues = "${athena.mensageria.servico}.comandos", ackMode = "AUTO")
    public void consumir(Message mensagem) {
        despachante.consumir(mensagem, fila);
    }
}
