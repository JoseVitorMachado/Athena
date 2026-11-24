package br.ufla.athena.requisitos.aplicacao;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/** Decisões sem efeitos externos (livro §6.5–6.7). */
public final class MaquinaSaga {
    public enum Acao { IGNORAR, AGUARDAR, REENVIAR, INICIAR_COMPENSACAO, PROXIMO_LOTE,
        CONCLUIR, CANCELAR_RETRATO, COMPENSAR, PENDENTE }

    public record Estado(String estado, String etapa, UUID comando, int tentativas,
                         Instant prazo, Instant proximaAcao) { }

    public Acao resposta(Estado atual, String tipo, UUID comando, boolean retryable) {
        if (!comando.equals(atual.comando())) {
            return Acao.IGNORAR;
        }
        boolean andamento = "EM_ANDAMENTO".equals(atual.estado());
        boolean compensando = "COMPENSANDO".equals(atual.estado())
                || "COMPENSACAO_PENDENTE".equals(atual.estado());
        if (andamento && (("AGUARDANDO_RETRATO".equals(atual.etapa())
                && "RetratoRastreabilidadeRecusado".equals(tipo))
                || ("AGUARDANDO_LOTE".equals(atual.etapa()) && "LoteRevisaoRecusado".equals(tipo)))) {
            return retryable ? Acao.AGUARDAR : Acao.INICIAR_COMPENSACAO;
        }
        if (andamento && "AGUARDANDO_RETRATO".equals(atual.etapa())
                && "RetratoRastreabilidadeRegistrado".equals(tipo)) return Acao.PROXIMO_LOTE;
        if (andamento && "AGUARDANDO_LOTE".equals(atual.etapa())
                && "LoteRevisaoAberto".equals(tipo)) return Acao.CONCLUIR;
        if (compensando && "CANCELANDO_LOTE".equals(atual.etapa())
                && "CancelamentoLoteConfirmado".equals(tipo)) return Acao.CANCELAR_RETRATO;
        if (compensando && "CANCELANDO_RETRATO".equals(atual.etapa())
                && "CancelamentoRetratoConfirmado".equals(tipo)) return Acao.COMPENSAR;
        return Acao.IGNORAR;
    }

    public Acao tempo(Estado atual, Instant agora, int maximo) {
        if ("EM_ANDAMENTO".equals(atual.estado()) && !agora.isBefore(atual.prazo())) {
            return Acao.INICIAR_COMPENSACAO;
        }
        if (atual.proximaAcao() == null || agora.isBefore(atual.proximaAcao())) return Acao.AGUARDAR;
        if ("COMPENSACAO_PENDENTE".equals(atual.estado())) return Acao.REENVIAR;
        if (atual.tentativas() >= maximo) {
            return "EM_ANDAMENTO".equals(atual.estado()) ? Acao.INICIAR_COMPENSACAO : Acao.PENDENTE;
        }
        return Acao.REENVIAR;
    }

    public static Duration espera(Duration espera, java.util.List<Duration> intervalos, int tentativa) {
        return espera.plus(tentativa < intervalos.size() + 1 ? intervalos.get(tentativa - 1) : Duration.ZERO);
    }
}
