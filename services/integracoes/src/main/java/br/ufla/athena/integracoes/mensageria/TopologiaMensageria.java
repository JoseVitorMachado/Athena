package br.ufla.athena.integracoes.mensageria;

import java.util.ArrayList;
import java.util.List;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopologiaMensageria {
    private final String servico;

    public TopologiaMensageria(@Value("${athena.mensageria.servico}") String servico) {
        this.servico = servico;
    }

    @Bean
    public TopicExchange exchangeComandos() { return new TopicExchange("athena.commands", true, false); }

    @Bean
    public TopicExchange exchangeEventos() { return new TopicExchange("athena.events", true, false); }

    @Bean
    public Declarables comandos(TopicExchange exchangeComandos) {
        return filas(servico + ".comandos", exchangeComandos, List.of(servico + ".#"));
    }

    // Pode ser registrado como bean quando houver assinaturas de eventos (livro §6.8).
    public Declarables filasEventos(List<String> routingKeys) {
        return filas(servico + ".eventos", exchangeEventos(), routingKeys);
    }

    private Declarables filas(String nome, TopicExchange exchange, List<String> routingKeys) {
        Queue principal = QueueBuilder.durable(nome).deadLetterExchange("")
                .deadLetterRoutingKey(nome + ".dlq").build();
        Queue retentativa = QueueBuilder.durable(nome + ".retentativa").deadLetterExchange("")
                .deadLetterRoutingKey(nome).build();
        Queue dlq = QueueBuilder.durable(nome + ".dlq").build();
        List<Declarable> declaracoes = new ArrayList<>(List.of(principal, retentativa, dlq));
        routingKeys.forEach(chave -> declaracoes.add(BindingBuilder.bind(principal).to(exchange).with(chave)));
        return new Declarables(declaracoes);
    }
}
