package br.ufla.athena.rastreabilidade.aplicacao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MapaServiceTest {
    @Test
    void calculaSituacaoDaProjecao() {
        assertEquals("RECONSTRUINDO", MapaService.situacao("RECONSTRUINDO", 2, 3));
        assertEquals("ATUALIZACAO_PENDENTE", MapaService.situacao("ATUALIZADO", 2, 3));
        assertEquals("ATUALIZADO", MapaService.situacao("ATUALIZADO", 3, 3));
    }
}
