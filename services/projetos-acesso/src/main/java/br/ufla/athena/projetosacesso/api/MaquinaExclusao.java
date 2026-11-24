package br.ufla.athena.projetosacesso.api;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** Regras puras das fases e das retentativas. */
public final class MaquinaExclusao {
    private MaquinaExclusao() { }
    public record Confirmacao(String servico, Instant quiescencia, Instant limpeza, Instant ultimoEnvio) { }
    public static String proximo(String estado, List<Confirmacao> confirmacoes) {
        if (confirmacoes.isEmpty()) return estado;
        if (estado.equals("FECHANDO") && confirmacoes.stream().allMatch(c -> c.quiescencia() != null)) return "EXCLUINDO";
        if (estado.equals("EXCLUINDO") && confirmacoes.stream().allMatch(c -> c.limpeza() != null)) return "CONCLUIDA";
        return estado;
    }
    public static List<String> reenviar(String estado, List<Confirmacao> confirmacoes, Instant agora, Duration intervalo) {
        return confirmacoes.stream().filter(c -> (estado.equals("FECHANDO") && c.quiescencia() == null)
                || (estado.equals("EXCLUINDO") && c.limpeza() == null))
                .filter(c -> c.ultimoEnvio() == null || !c.ultimoEnvio().plus(intervalo).isAfter(agora))
                .map(Confirmacao::servico).toList();
    }
}
