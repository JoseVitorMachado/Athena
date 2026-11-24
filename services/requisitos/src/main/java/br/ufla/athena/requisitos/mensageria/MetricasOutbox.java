package br.ufla.athena.requisitos.mensageria;

import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MetricasOutbox {
    private final JdbcTemplate banco;
    private final AtomicLong pendentes = new AtomicLong();

    public MetricasOutbox(JdbcTemplate banco, MeterRegistry registro) {
        this.banco = banco;
        registro.gauge("athena.outbox.pendentes", pendentes);
    }

    @Scheduled(fixedDelay = 15000)
    public void atualizar() {
        pendentes.set(banco.queryForObject("SELECT count(*) FROM outbox WHERE publicado_em IS NULL", Long.class));
    }
}
