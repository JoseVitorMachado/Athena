package br.ufla.athena.rastreabilidade.aplicacao;

import br.ufla.athena.rastreabilidade.mensageria.ManipuladorMensagem;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ManipuladoresMapa {
    @Bean
    ManipuladorMensagem relacaoCriada(ProjecaoMapa projecao, AtrasoProjecao atraso) {
        return manipulador("RelacaoCriada", projecao, atraso);
    }

    @Bean
    ManipuladorMensagem relacaoAlterada(ProjecaoMapa projecao, AtrasoProjecao atraso) {
        return manipulador("RelacaoAlterada", projecao, atraso);
    }

    @Bean
    ManipuladorMensagem relacaoRemovida(ProjecaoMapa projecao, AtrasoProjecao atraso) {
        return manipulador("RelacaoRemovida", projecao, atraso);
    }

    @Bean
    ManipuladorMensagem requisitoAlterado(ProjecaoMapa projecao, AtrasoProjecao atraso) {
        return manipulador("RequisitoAlterado", projecao, atraso);
    }

    private ManipuladorMensagem manipulador(String tipo, ProjecaoMapa projecao, AtrasoProjecao atraso) {
        return new ManipuladorMensagem() {
            @Override
            public String messageType() {
                return tipo;
            }

            @Override
            public void tratar(Map<String, Object> envelope) {
                if (!tipo.equals("RequisitoAlterado")) {
                    atraso.aguardar();
                }
                projecao.aplicar(tipo, ParticipanteSaga.payload(envelope));
            }
        };
    }
}
