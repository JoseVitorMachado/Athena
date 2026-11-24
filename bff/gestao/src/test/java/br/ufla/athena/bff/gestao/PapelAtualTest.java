package br.ufla.athena.bff.gestao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

class PapelAtualTest {

    @Test
    void naoMembroRecebe403SemDetalhesDoProjeto() {
        ClienteDominio cliente = mock(ClienteDominio.class);
        AthenaProperties propriedades = new AthenaProperties(
                new AthenaProperties.Servicos("http://identidade", "", "", ""), null);
        when(cliente.get("projetos e acesso", "http://identidade",
                "/api/v1/internal/projetos/p/membros/u"))
                .thenReturn(new ClienteDominio.Resposta(HttpStatus.NOT_FOUND, HttpHeaders.EMPTY, null));

        FalhaAutorizacao erro = assertThrows(FalhaAutorizacao.class,
                () -> new PapelAtual(cliente, propriedades).consultar("p", "u"));
        assertEquals(403, erro.status());
        assertEquals("SEM_ACESSO_AO_PROJETO", erro.codigo());
        assertEquals("Acesso ao projeto negado.", erro.getMessage());
    }
}
