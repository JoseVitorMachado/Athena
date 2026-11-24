package br.ufla.athena.rastreabilidade.aplicacao;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AtrasoProjecao {
    private static final Logger log = LoggerFactory.getLogger(AtrasoProjecao.class);
    private volatile long segundos;

    public AtrasoProjecao(@Value("${athena.mapa.atraso-projecao:0s}") Duration atraso) {
        definir(atraso.getSeconds());
    }

    public long consultar() {
        return segundos;
    }

    public void definir(long valor) {
        if (valor < 0 || valor > 86400) {
            throw new IllegalArgumentException("Atraso deve estar entre 0 e 86400 segundos.");
        }
        segundos = valor;
        if (valor > 0) {
            log.warn("Atraso artificial da projeção definido para {} segundos", valor);
        }
    }

    public void aguardar() {
        try {
            Thread.sleep(Duration.ofSeconds(segundos).toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Espera da projeção interrompida", e);
        }
    }
}
