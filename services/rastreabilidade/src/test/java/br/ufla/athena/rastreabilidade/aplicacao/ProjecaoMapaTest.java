package br.ufla.athena.rastreabilidade.aplicacao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjecaoMapaTest {
    @Test
    void aceitaSomenteRevisaoMaisRecente() {
        assertTrue(ProjecaoMapa.deveAplicar(null, 0));
        assertFalse(ProjecaoMapa.deveAplicar(5L, 4));
        assertFalse(ProjecaoMapa.deveAplicar(5L, 5));
        assertTrue(ProjecaoMapa.deveAplicar(5L, 6));
    }

    @Test
    void criacaoAtrasadaNaoRessuscitaRelacaoRemovida() {
        boolean removida = true;
        if (ProjecaoMapa.deveAplicar(5L, 3)) {
            removida = false;
        }
        assertTrue(removida);
    }

    @Test
    void requisitoAntigoNaoSobrescreveTituloNovo() {
        String titulo = "Título novo";
        if (ProjecaoMapa.deveAplicar(8L, 7)) {
            titulo = "Título antigo";
        }
        assertEquals("Título novo", titulo);
    }
}
