package br.ufla.athena.revisoes.mensageria;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(ExisteAssinaturaEventos.class)
public class ConsumidorEventos {
    private final DespachanteMensagens despachante;
    private final String fila;

    public ConsumidorEventos(DespachanteMensagens despachante,
                             @Value("${athena.mensageria.servico}") String servico) {
        this.despachante = despachante;
        this.fila = servico + ".eventos";
    }

    @RabbitListener(queues = "${athena.mensageria.servico}.eventos", ackMode = "AUTO")
    public void consumir(Message mensagem) {
        despachante.consumir(mensagem, fila);
    }
}
