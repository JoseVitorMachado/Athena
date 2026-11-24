package br.ufla.athena.projetosacesso.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class PoliticaSenhaTest {
    @Test
    void exigeTodosOsRequisitos() {
        assertTrue(IdentidadeController.senhaForte("Athena@2026"));
        assertFalse(IdentidadeController.senhaForte("Ab1!"));
        assertFalse(IdentidadeController.senhaForte("abcdefgh1"));
        assertFalse(IdentidadeController.senhaForte("abcdefgh!"));
        assertFalse(IdentidadeController.senhaForte("12345678!"));
    }
}
