package br.ufla.athena.rastreabilidade.aplicacao;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ComandosRetratoTest {
    @Test
    void prazoVencidoERecusado() {
        JdbcTemplate banco = mock(JdbcTemplate.class);
        ParticipanteSaga participante = mock(ParticipanteSaga.class);
        when(banco.queryForObject(anyString(), eq(Boolean.class), any(UUID.class))).thenReturn(false);
        ComandosRetrato comandos = new ComandosRetrato(banco, participante);
        Map<String, Object> envelope = Map.of("commandId", UUID.randomUUID().toString(),
                "projetoId", UUID.randomUUID().toString(), "payload", Map.of(
                        "submissaoId", UUID.randomUUID().toString(), "deadline", Instant.now().minusSeconds(1).toString()));

        comandos.registrarRetratoRastreabilidade().tratar(envelope);

        verify(participante).concluir(eq(envelope), eq("RetratoRastreabilidadeRecusado"), argThat(
                resposta -> "PRAZO_EXPIRADO".equals(resposta.get("code"))), eq(false));
    }

    @Test
    void criacaoAposCancelamentoERecusada() {
        JdbcTemplate banco = mock(JdbcTemplate.class);
        ParticipanteSaga participante = mock(ParticipanteSaga.class);
        when(banco.queryForObject(anyString(), eq(Boolean.class), any(UUID.class))).thenReturn(true);
        ComandosRetrato comandos = new ComandosRetrato(banco, participante);
        Map<String, Object> envelope = Map.of("commandId", UUID.randomUUID().toString(),
                "projetoId", UUID.randomUUID().toString(), "payload", Map.of(
                        "submissaoId", UUID.randomUUID().toString(), "deadline", Instant.now().plusSeconds(60).toString()));

        comandos.registrarRetratoRastreabilidade().tratar(envelope);

        verify(participante).concluir(eq(envelope), eq("RetratoRastreabilidadeRecusado"), argThat(
                resposta -> "SUBMISSAO_CANCELADA".equals(resposta.get("code"))
                        && Boolean.FALSE.equals(resposta.get("retryable"))), eq(false));
        verify(banco, never()).update(anyString(), any(Object[].class));
    }
}
