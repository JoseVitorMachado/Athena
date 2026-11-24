package br.ufla.athena.revisoes.aplicacao;

import br.ufla.athena.revisoes.dominio.Lote;
import br.ufla.athena.revisoes.dominio.Pendencia;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ComandosLoteTest {
    @Test
    void silenciarCriaLoteEPendenciasSemEmitirResposta() {
        JdbcTemplate banco = mock(JdbcTemplate.class);
        EntityManager entidades = mock(EntityManager.class);
        ParticipanteSaga participante = mock(ParticipanteSaga.class);
        InjetorFalhas falhas = new InjetorFalhas(InjetorFalhas.Modo.SILENCIAR);
        when(banco.queryForObject(anyString(), eq(Boolean.class), any(UUID.class))).thenReturn(false);
        ComandosLote comandos = new ComandosLote(banco, entidades, participante, falhas);
        Map<String, Object> envelope = envelope();

        comandos.abrirLoteRevisao().tratar(envelope);

        verify(entidades).persist(any(Lote.class));
        verify(entidades, times(2)).persist(any(Pendencia.class));
        verify(participante).concluir(eq(envelope), eq("LoteRevisaoAberto"), anyMap(), eq(true));
    }

    @Test
    void recusaInjetadaNaoCriaLote() {
        JdbcTemplate banco = mock(JdbcTemplate.class);
        EntityManager entidades = mock(EntityManager.class);
        ParticipanteSaga participante = mock(ParticipanteSaga.class);
        when(banco.queryForObject(anyString(), eq(Boolean.class), any(UUID.class))).thenReturn(false);
        ComandosLote comandos = new ComandosLote(banco, entidades, participante,
                new InjetorFalhas(InjetorFalhas.Modo.RECUSAR));
        Map<String, Object> envelope = envelope();

        comandos.abrirLoteRevisao().tratar(envelope);

        verifyNoInteractions(entidades);
        verify(participante).concluir(eq(envelope), eq("LoteRevisaoRecusado"), argThat(
                resposta -> "FALHA_INJETADA".equals(resposta.get("code"))), eq(false));
    }

    @Test
    void reenvioSilenciosoNaoRepeteCriacao() {
        JdbcTemplate banco = mock(JdbcTemplate.class);
        EntityManager entidades = mock(EntityManager.class);
        ParticipanteSaga participante = mock(ParticipanteSaga.class);
        when(participante.repetir(anyMap(), eq(true))).thenReturn(true);
        ComandosLote comandos = new ComandosLote(banco, entidades, participante,
                new InjetorFalhas(InjetorFalhas.Modo.SILENCIAR));

        comandos.abrirLoteRevisao().tratar(envelope());

        verifyNoInteractions(entidades, banco);
        verify(participante, never()).concluir(anyMap(), anyString(), anyMap(), anyBoolean());
    }

    private static Map<String, Object> envelope() {
        return Map.of("commandId", UUID.randomUUID().toString(), "projetoId", UUID.randomUUID().toString(),
                "payload", Map.of("submissaoId", UUID.randomUUID().toString(), "requisitoId", UUID.randomUUID().toString(),
                        "versaoId", UUID.randomUUID().toString(), "hashConteudo", "a".repeat(64),
                        "deadline", Instant.now().plusSeconds(60).toString(), "relacionados", List.of(
                                Map.of("requisitoId", UUID.randomUUID().toString(), "tipo", "DEPENDE_DE", "sentido", "SAIDA"))));
    }
}
