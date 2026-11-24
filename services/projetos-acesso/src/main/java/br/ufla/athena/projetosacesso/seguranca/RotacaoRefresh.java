package br.ufla.athena.projetosacesso.seguranca;

import java.time.Instant;

/** Decisão de renovação; a persistência e o bloqueio são transacionais. */
public final class RotacaoRefresh {
    private RotacaoRefresh() {
    }

    public enum Resultado {
        ROTACIONAR, REUTILIZADO, ENCERRADO
    }

    public static Resultado avaliar(Instant consumido, Instant revogada, Instant fimSessao, Instant fimToken, Instant agora) {
        if (consumido != null) {
            return Resultado.REUTILIZADO;
        }
        if (revogada != null || !fimSessao.isAfter(agora) || !fimToken.isAfter(agora)) {
            return Resultado.ENCERRADO;
        }
        return Resultado.ROTACIONAR;
    }
}
