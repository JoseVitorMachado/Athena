package br.ufla.athena.requisitos.aplicacao;

import br.ufla.athena.requisitos.mensageria.ManipuladorMensagem;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ManipuladoresSaga {
    private ManipuladorMensagem manipulador(String tipo, CoordenadorSaga coordenador) {
        return new ManipuladorMensagem() {
            @Override
            public String messageType() {
                return tipo;
            }

            @Override
            public void tratar(Map<String, Object> envelope) {
                coordenador.receber(envelope);
            }
        };
    }

    @Bean
    ManipuladorMensagem retratoRegistrado(CoordenadorSaga coordenador) {
        return manipulador("RetratoRastreabilidadeRegistrado", coordenador);
    }
    @Bean
    ManipuladorMensagem retratoRecusado(CoordenadorSaga coordenador) {
        return manipulador("RetratoRastreabilidadeRecusado", coordenador);
    }
    @Bean
    ManipuladorMensagem loteAberto(CoordenadorSaga coordenador) {
        return manipulador("LoteRevisaoAberto", coordenador);
    }
    @Bean
    ManipuladorMensagem loteRecusado(CoordenadorSaga coordenador) {
        return manipulador("LoteRevisaoRecusado", coordenador);
    }
    @Bean
    ManipuladorMensagem loteCancelado(CoordenadorSaga coordenador) {
        return manipulador("CancelamentoLoteConfirmado", coordenador);
    }
    @Bean
    ManipuladorMensagem retratoCancelado(CoordenadorSaga coordenador) {
        return manipulador("CancelamentoRetratoConfirmado", coordenador);
    }
    @Bean
    ManipuladorMensagem loteAtivado(CoordenadorSaga coordenador) {
        return manipulador("LoteRevisaoAtivado", coordenador);
    }
}
