package br.ufla.athena.requisitos.aplicacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ComparacaoBaselineTest {
    @Test
    void classificaEContaAsQuatroSituacoes() {
        UUID igual = UUID.randomUUID(), alterado = UUID.randomUUID(), fora = UUID.randomUUID(), novo = UUID.randomUUID();
        UUID versao = UUID.randomUUID();
        var resultado = ComparacaoBaseline.comparar(List.of(conteudo(igual, versao, 1), conteudo(alterado, versao, 1), conteudo(fora, versao, 1)),
                List.of(conteudo(igual, versao, 1), conteudo(alterado, UUID.randomUUID(), 2), conteudo(novo, versao, 1)));
        assertEquals(List.of(ComparacaoBaseline.Situacao.INALTERADO, ComparacaoBaseline.Situacao.ALTERADO,
                ComparacaoBaseline.Situacao.FORA_DO_LIVRO, ComparacaoBaseline.Situacao.NOVO), resultado.itens().stream().map(ComparacaoBaseline.Item::situacao).toList());
        for (var situacao : ComparacaoBaseline.Situacao.values()) assertEquals(1, resultado.contagens().get(situacao));
        assertEquals(1, resultado.itens().get(1).numeroBaseline());
        assertEquals(2, resultado.itens().get(1).numeroAtual());
        assertNull(resultado.itens().get(0).numeroAtual());
    }
    private static ComparacaoBaseline.Conteudo conteudo(UUID requisito, UUID versao, int numero) {
        return new ComparacaoBaseline.Conteudo(requisito, versao, "REQ-1", "Título", numero);
    }
}
