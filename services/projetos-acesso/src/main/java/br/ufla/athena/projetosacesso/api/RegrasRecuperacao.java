package br.ufla.athena.projetosacesso.api;

import java.time.Instant;

public final class RegrasRecuperacao {
    private RegrasRecuperacao() {}

    public static boolean valido(Instant expira, Instant usado, Instant invalidado, int tentativas, Instant agora) {
        return expira.isAfter(agora) && usado == null && invalidado == null && tentativas < 5;
    }

    public static boolean invalidarAposFalha(int tentativasAnteriores) {
        return tentativasAnteriores + 1 >= 5;
    }
}
