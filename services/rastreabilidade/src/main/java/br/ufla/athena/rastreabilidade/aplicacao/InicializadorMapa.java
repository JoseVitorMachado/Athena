package br.ufla.athena.rastreabilidade.aplicacao;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class InicializadorMapa {
    private static final Logger log = LoggerFactory.getLogger(InicializadorMapa.class);
    private final JdbcTemplate banco;
    private final MapaService mapa;

    public InicializadorMapa(JdbcTemplate banco, MapaService mapa) {
        this.banco = banco;
        this.mapa = mapa;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void iniciar() {
        Thread thread = new Thread(this::reconstruir, "inicializacao-mapa");
        thread.setDaemon(true);
        thread.start();
    }

    private void reconstruir() {
        for (int tentativa = 1; tentativa <= 5; tentativa++) {
            try {
                var projetos = banco.queryForList("SELECT DISTINCT r.projeto_id FROM relacoes r LEFT JOIN mapa_estado e ON e.projeto_id = r.projeto_id WHERE e.projeto_id IS NULL OR NOT e.disponivel", UUID.class);
                for (UUID projeto : projetos) {
                    mapa.reconstruir(projeto);
                }
                return;
            } catch (RuntimeException e) {
                log.warn("Falha na inicialização do mapa, tentativa {}", tentativa, e);
                if (tentativa < 5) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException interrupcao) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }
}
