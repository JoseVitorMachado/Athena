package br.ufla.athena.requisitos.aplicacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.List;
import org.junit.jupiter.api.Test;

class DiferencaTextoTest {
    private static DiferencaTexto.Linha linha(DiferencaTexto.Tipo tipo, String texto) {
        return new DiferencaTexto.Linha(tipo, texto);
    }
    @Test void comparaLinhas() {
        var igual = DiferencaTexto.Tipo.IGUAL;
        var adicionada = DiferencaTexto.Tipo.ADICIONADA;
        var removida = DiferencaTexto.Tipo.REMOVIDA;
        assertEquals(List.of(linha(igual, "a")), DiferencaTexto.comparar("a", "a"));
        assertEquals(List.of(linha(igual, "a"), linha(adicionada, "b")), DiferencaTexto.comparar("a", "a\nb"));
        assertEquals(List.of(linha(removida, "a"), linha(igual, "b")), DiferencaTexto.comparar("a\nb", "b"));
        assertEquals(List.of(linha(removida, "a"), linha(adicionada, "b")), DiferencaTexto.comparar("a", "b"));
        assertEquals(List.of(), DiferencaTexto.comparar("", ""));
        assertThrows(IllegalArgumentException.class, () -> DiferencaTexto.comparar("a\n".repeat(2000), ""));
    }
}
