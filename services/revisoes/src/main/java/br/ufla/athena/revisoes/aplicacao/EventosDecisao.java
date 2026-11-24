package br.ufla.athena.revisoes.aplicacao;

import br.ufla.athena.revisoes.mensageria.ManipuladorMensagem;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class EventosDecisao {
    @Bean
    public ManipuladorMensagem decisaoRevisaoAplicada(JdbcTemplate banco) {
        return new ManipuladorMensagem() {
            @Override
            public String messageType() {
                return "DecisaoRevisaoAplicada";
            }

            @Override
            public void tratar(Map<String, Object> envelope) {
                Map<String, Object> dados = ParticipanteSaga.payload(envelope);
                String resultado = dados.get("resultado").toString();
                if (!resultado.equals("APLICADA") && !resultado.equals("DESCARTADA")) {
                    throw new IllegalArgumentException("Resultado de aplicação inválido");
                }
                int alterados = banco.update("UPDATE decisoes SET aplicacao_em_requisitos = ? WHERE id = ? AND lote_id = ? AND submissao_id = ? AND EXISTS (SELECT 1 FROM lotes WHERE lotes.id = decisoes.lote_id AND lotes.projeto_id = ?)",
                        resultado, ParticipanteSaga.id(dados, "decisaoId"), ParticipanteSaga.id(dados, "loteId"),
                        ParticipanteSaga.id(dados, "submissaoId"), ParticipanteSaga.id(envelope, "projetoId"));
                if (alterados == 0) {
                    throw new IllegalStateException("Decisão não encontrada para a aplicação recebida");
                }
            }
        };
    }
}
