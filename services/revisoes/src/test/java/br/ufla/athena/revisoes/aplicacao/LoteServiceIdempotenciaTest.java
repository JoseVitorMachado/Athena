package br.ufla.athena.revisoes.aplicacao;

import br.ufla.athena.revisoes.api.DecisaoEntrada;
import br.ufla.athena.revisoes.api.Problema;
import br.ufla.athena.revisoes.dominio.DecisaoRepository;
import br.ufla.athena.revisoes.dominio.Decisao;
import br.ufla.athena.revisoes.dominio.Lote;
import br.ufla.athena.revisoes.dominio.LoteRepository;
import br.ufla.athena.revisoes.dominio.TipoDecisao;
import br.ufla.athena.revisoes.mensageria.EmissorMensagens;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoteServiceIdempotenciaTest {
    @Test
    void chaveAusenteRetornaPrecondicao() {
        JdbcTemplate banco = mock(JdbcTemplate.class);
        LoteService servico = new LoteService(mock(LoteRepository.class), mock(DecisaoRepository.class),
                mock(EmissorMensagens.class), UUID.randomUUID(), banco, new ObjectMapper());
        Problema erro = assertThrows(Problema.class, () -> servico.decidir(UUID.randomUUID(), UUID.randomUUID(),
                null, null, entrada()));
        assertEquals("IDEMPOTENCY_KEY_AUSENTE", erro.getBody().getProperties().get("codigo"));
        verifyNoInteractions(banco);
    }

    @Test
    void chaveUsadaComOutroPedidoRetornaConflitoAntesDoIfMatch() {
        JdbcTemplate banco = mock(JdbcTemplate.class);
        LoteRepository lotes = mock(LoteRepository.class);
        when(banco.query(anyString(), any(RowMapper.class), any(), any(), any())).thenAnswer(chamada -> {
            RowMapper<?> mapper = chamada.getArgument(1);
            java.sql.ResultSet linha = mock(java.sql.ResultSet.class);
            when(linha.getString(1)).thenReturn("outra-impressao");
            when(linha.getObject(2, UUID.class)).thenReturn(UUID.randomUUID());
            return List.of(mapper.mapRow(linha, 0));
        });
        LoteService servico = new LoteService(lotes, mock(DecisaoRepository.class), mock(EmissorMensagens.class),
                UUID.randomUUID(), banco, new ObjectMapper());
        Problema erro = assertThrows(Problema.class, () -> servico.decidir(UUID.randomUUID(), UUID.randomUUID(),
                null, "chave", entrada()));
        assertEquals("IDEMPOTENCY_KEY_REUTILIZADA", erro.getBody().getProperties().get("codigo"));
        verifyNoInteractions(lotes);
    }

    @Test
    void repeticaoRetornaDecisaoMesmoSemIfMatch() throws Exception {
        JdbcTemplate banco = mock(JdbcTemplate.class);
        LoteRepository lotes = mock(LoteRepository.class);
        DecisaoRepository decisoes = mock(DecisaoRepository.class);
        ObjectMapper json = new ObjectMapper();
        DecisaoEntrada entrada = entrada();
        UUID projeto = UUID.randomUUID();
        Lote lote = Lote.novo(projeto, UUID.randomUUID(), entrada.submissaoId(), entrada.versaoId(),
                entrada.hashConteudo());
        Decisao decisao = Decisao.registrar(lote, entrada.decisao(), null, UUID.randomUUID());
        var digest = java.security.MessageDigest.getInstance("SHA-256");
        digest.update(lote.getId().toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String impressao = java.util.HexFormat.of().formatHex(digest.digest(json.writeValueAsBytes(entrada)));
        when(banco.query(anyString(), any(RowMapper.class), any(), any(), any())).thenAnswer(chamada -> {
            RowMapper<?> mapper = chamada.getArgument(1);
            java.sql.ResultSet linha = mock(java.sql.ResultSet.class);
            when(linha.getString(1)).thenReturn(impressao);
            when(linha.getObject(2, UUID.class)).thenReturn(decisao.getId());
            return List.of(mapper.mapRow(linha, 0));
        });
        when(decisoes.findById(decisao.getId())).thenReturn(Optional.of(decisao));
        LoteService servico = new LoteService(lotes, decisoes, mock(EmissorMensagens.class),
                UUID.randomUUID(), banco, json);

        assertEquals(decisao.getId(), servico.decidir(projeto, lote.getId(), null, "chave", entrada).decisaoId());
        verifyNoInteractions(lotes);
    }

    private static DecisaoEntrada entrada() {
        return new DecisaoEntrada(TipoDecisao.APROVAR, null, UUID.randomUUID(), UUID.randomUUID(), "a".repeat(64));
    }
}
