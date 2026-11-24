package br.ufla.athena.projetosacesso.api;

import br.ufla.athena.projetosacesso.mensageria.ManipuladorMensagem;
import br.ufla.athena.projetosacesso.mensageria.MensagemInvalidaException;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ManipuladoresExclusao {
    private final ServicoExclusao exclusoes;
    public ManipuladoresExclusao(ServicoExclusao exclusoes) { this.exclusoes = exclusoes; }
    @Bean ManipuladorMensagem projetoFechado() { return manipulador("ProjetoFechado", false); }
    @Bean ManipuladorMensagem projetoLimpo() { return manipulador("ProjetoLimpo", true); }
    private ManipuladorMensagem manipulador(String tipo, boolean limpeza) {
        return new ManipuladorMensagem() {
            public String messageType() { return tipo; }
            public void tratar(Map<String,Object> envelope) {
                if (!(envelope.get("payload") instanceof Map<?,?> dados) || !(dados.get("servico") instanceof String servico) || servico.isBlank()) throw new MensagemInvalidaException("payload inválido");
                try {
                    UUID projeto = UUID.fromString(String.valueOf(dados.get("projetoId")));
                    if (!projeto.equals(UUID.fromString(String.valueOf(envelope.get("projetoId"))))) throw new IllegalArgumentException();
                    exclusoes.confirmar(UUID.fromString(String.valueOf(dados.get("exclusaoId"))), projeto, servico, limpeza);
                } catch (IllegalArgumentException erro) { throw new MensagemInvalidaException("identificador inválido"); }
            }
        };
    }
}
