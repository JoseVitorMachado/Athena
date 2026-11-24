package br.ufla.athena.requisitos.aplicacao;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AgendadorSaga {
    private final CoordenadorSaga coordenador;

    public AgendadorSaga(CoordenadorSaga coordenador) {
        this.coordenador = coordenador;
    }

    @Scheduled(fixedDelay = 1000)
    public void executar() {
        coordenador.processarAtrasadas();
    }
}
