package br.ufla.athena.requisitos.aplicacao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MaquinaSagaTest {
    private final MaquinaSaga maquina = new MaquinaSaga();
    private final UUID comando = UUID.randomUUID();
    private final Instant agora = Instant.parse("2026-01-01T00:00:00Z");

    private MaquinaSaga.Estado estado(String estado, String etapa, int tentativas, Instant prazo) {
        return new MaquinaSaga.Estado(estado, etapa, comando, tentativas, prazo, agora);
    }

    @Test
    void caminhoFeliz() {
        assertEquals(MaquinaSaga.Acao.PROXIMO_LOTE, maquina.resposta(estado("EM_ANDAMENTO", "AGUARDANDO_RETRATO", 1, agora.plusSeconds(90)), "RetratoRastreabilidadeRegistrado", comando, false));
        assertEquals(MaquinaSaga.Acao.CONCLUIR, maquina.resposta(estado("EM_ANDAMENTO", "AGUARDANDO_LOTE", 1, agora.plusSeconds(90)), "LoteRevisaoAberto", comando, false));
    }

    @Test
    void recusaDefinitivaCompensaEmOrdem() {
        assertEquals(MaquinaSaga.Acao.INICIAR_COMPENSACAO, maquina.resposta(estado("EM_ANDAMENTO", "AGUARDANDO_LOTE", 1, agora.plusSeconds(90)), "LoteRevisaoRecusado", comando, false));
        assertEquals(MaquinaSaga.Acao.CANCELAR_RETRATO, maquina.resposta(estado("COMPENSANDO", "CANCELANDO_LOTE", 1, agora), "CancelamentoLoteConfirmado", comando, false));
        assertEquals(MaquinaSaga.Acao.COMPENSAR, maquina.resposta(estado("COMPENSANDO", "CANCELANDO_RETRATO", 1, agora), "CancelamentoRetratoConfirmado", comando, false));
        assertEquals(MaquinaSaga.Acao.AGUARDAR, maquina.resposta(estado("EM_ANDAMENTO", "AGUARDANDO_LOTE", 1, agora.plusSeconds(90)), "LoteRevisaoRecusado", comando, true));
    }

    @Test
    void esgotamentoEPrazoIniciamCompensacao() {
        assertEquals(MaquinaSaga.Acao.INICIAR_COMPENSACAO, maquina.tempo(estado("EM_ANDAMENTO", "AGUARDANDO_LOTE", 3, agora.plusSeconds(90)), agora, 3));
        assertEquals(MaquinaSaga.Acao.INICIAR_COMPENSACAO, maquina.tempo(estado("EM_ANDAMENTO", "AGUARDANDO_RETRATO", 1, agora), agora, 3));
        assertEquals(Duration.ofSeconds(12), MaquinaSaga.espera(Duration.ofSeconds(10), List.of(Duration.ofSeconds(2), Duration.ofSeconds(4)), 1));
        assertEquals(Duration.ofSeconds(14), MaquinaSaga.espera(Duration.ofSeconds(10), List.of(Duration.ofSeconds(2), Duration.ofSeconds(4)), 2));
    }

    @Test
    void cancelamentoPendenteContinuaReenviando() {
        assertEquals(MaquinaSaga.Acao.PENDENTE, maquina.tempo(estado("COMPENSANDO", "CANCELANDO_LOTE", 3, agora), agora, 3));
        MaquinaSaga.Estado pendente = new MaquinaSaga.Estado("COMPENSACAO_PENDENTE", "CANCELANDO_LOTE", comando, 3, agora, agora.plusSeconds(60));
        assertEquals(MaquinaSaga.Acao.AGUARDAR, maquina.tempo(pendente, agora.plusSeconds(59), 3));
        assertEquals(MaquinaSaga.Acao.REENVIAR, maquina.tempo(pendente, agora.plusSeconds(60), 3));
        assertEquals(MaquinaSaga.Acao.CANCELAR_RETRATO, maquina.resposta(pendente, "CancelamentoLoteConfirmado", comando, false));
    }

    @Test
    void respostasInesperadasSaoIgnoradas() {
        assertEquals(MaquinaSaga.Acao.IGNORAR, maquina.resposta(estado("EM_ANDAMENTO", "AGUARDANDO_LOTE", 1, agora.plusSeconds(90)), "LoteRevisaoAberto", UUID.randomUUID(), false));
        assertEquals(MaquinaSaga.Acao.IGNORAR, maquina.resposta(estado("COMPENSANDO", "CANCELANDO_LOTE", 1, agora), "LoteRevisaoAberto", comando, false));
    }
}
