package br.ufla.athena.revisoes.aplicacao;

import br.ufla.athena.revisoes.mensageria.EmissorMensagens;
import br.ufla.athena.revisoes.mensageria.ManipuladorMensagem;
import br.ufla.athena.revisoes.mensageria.ProcessadorInbox;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ParticipanteExclusaoTest {
    private final JdbcTemplate banco = mock(JdbcTemplate.class);
    private final EmissorMensagens emissor = mock(EmissorMensagens.class);
    private final ParticipanteExclusao participante = new ParticipanteExclusao(banco, emissor);
    private final UUID projeto = UUID.randomUUID();

    @Test
    void aguardaLotePreparadoDepoisDeCancelarAtivos() {
        when(banco.queryForObject(contains("estado = 'PREPARADO'"), eq(Boolean.class), eq(projeto))).thenReturn(true);
        participante.fecharProjeto().tratar(envelope("FecharProjeto"));
        verify(banco).update(contains("estado = 'CANCELADO'"), eq(projeto));
        verifyNoInteractions(emissor);
    }

    @Test
    void respondeQuandoNaoHaLotePreparado() {
        when(banco.queryForObject(contains("estado = 'PREPARADO'"), eq(Boolean.class), eq(projeto))).thenReturn(false);
        participante.fecharProjeto().tratar(envelope("FecharProjeto"));
        verify(emissor).evento(eq("ProjetoFechado"), eq(projeto), isNull(), isNull(), any(), any(), anyMap());
    }

    @Test
    void ignoraMensagemAtrasadaDeProjetoExcluido() {
        when(banco.queryForObject(contains("projetos_excluidos"), eq(Boolean.class), eq(projeto))).thenReturn(true);
        ManipuladorMensagem manipulador = mock(ManipuladorMensagem.class);
        new ProcessadorInbox(banco).processar(envelope("OutroEvento"), manipulador);
        verifyNoInteractions(manipulador);
        verify(banco, never()).update(contains("INSERT INTO inbox"), any(org.springframework.jdbc.core.PreparedStatementSetter.class));
    }

    @Test
    void limpezaRepetidaReemiteResposta() {
        when(banco.queryForObject(contains("projetos_excluidos"), eq(Boolean.class), eq(projeto))).thenReturn(true);
        participante.limparProjeto().tratar(envelope("LimparProjeto"));
        verify(emissor).evento(eq("ProjetoLimpo"), eq(projeto), isNull(), isNull(), any(), any(), anyMap());
        verify(banco, never()).update(contains("DELETE FROM outbox"), eq(projeto.toString()));
    }

    private Map<String, Object> envelope(String tipo) {
        return Map.of("messageType", tipo, "messageId", UUID.randomUUID().toString(),
                "correlationId", UUID.randomUUID().toString(), "projetoId", projeto.toString(),
                "payload", Map.of("projetoId", projeto.toString(), "exclusaoId", UUID.randomUUID().toString()));
    }
}
