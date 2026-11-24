package br.ufla.athena.rastreabilidade.aplicacao;

import br.ufla.athena.rastreabilidade.dominio.GrafoPrecedencias;
import br.ufla.athena.rastreabilidade.dominio.TipoRelacao;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrafoPrecedenciasTest {
    private final UUID a = UUID.randomUUID();
    private final UUID b = UUID.randomUUID();
    private final UUID c = UUID.randomUUID();

    @Test
    void detectaCicloDireto() {
        GrafoPrecedencias grafo = new GrafoPrecedencias();
        grafo.adicionar(a, b, TipoRelacao.BLOQUEIA);
        assertTrue(grafo.criaCiclo(b, a, TipoRelacao.BLOQUEIA));
    }

    @Test
    void detectaCicloCombinado() {
        GrafoPrecedencias grafo = new GrafoPrecedencias();
        grafo.adicionar(a, b, TipoRelacao.BLOQUEIA);
        grafo.adicionar(c, b, TipoRelacao.DEPENDE_DE);
        assertTrue(grafo.criaCiclo(c, a, TipoRelacao.BLOQUEIA));
    }

    @Test
    void permiteCircularidadeNaoDirecional() {
        GrafoPrecedencias grafo = new GrafoPrecedencias();
        grafo.adicionar(a, b, TipoRelacao.RELACIONADO_A);
        grafo.adicionar(b, c, TipoRelacao.RELACIONADO_A);
        assertFalse(grafo.criaCiclo(c, a, TipoRelacao.RELACIONADO_A));
    }

    @Test
    void detectaCicloNaAlteracao() {
        GrafoPrecedencias grafo = new GrafoPrecedencias();
        grafo.adicionar(a, b, TipoRelacao.BLOQUEIA);
        grafo.adicionar(b, c, TipoRelacao.BLOQUEIA);
        assertTrue(grafo.criaCiclo(c, a, TipoRelacao.BLOQUEIA));
        assertFalse(grafo.criaCiclo(a, c, TipoRelacao.BLOQUEIA));
    }
}
