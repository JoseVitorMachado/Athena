package br.ufla.athena.requisitos.aplicacao;

import static org.junit.jupiter.api.Assertions.*;

import br.ufla.athena.requisitos.dominio.Requisito;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegrasComentarioTest {
    @Test
    void respeitaHierarquiaEAutoria() {
        UUID autor = UUID.randomUUID();
        UUID outro = UUID.randomUUID();
        assertFalse(RegrasComentario.podeCriar(null));
        assertFalse(RegrasComentario.podeCriar(RegrasComentario.Papel.LEITOR));
        assertTrue(RegrasComentario.podeCriar(RegrasComentario.Papel.REPORTER));
        assertTrue(RegrasComentario.podeEditarTexto(autor, autor));
        assertFalse(RegrasComentario.podeEditarTexto(autor, outro));
        assertFalse(RegrasComentario.podeResolver(RegrasComentario.Papel.LEITOR, autor, autor));
        assertFalse(RegrasComentario.podeResolver(RegrasComentario.Papel.REPORTER, autor, outro));
        assertTrue(RegrasComentario.podeResolver(RegrasComentario.Papel.REPORTER, autor, autor));
        assertTrue(RegrasComentario.podeResolver(RegrasComentario.Papel.EDITOR, autor, outro));
        assertTrue(RegrasComentario.podeResolver(RegrasComentario.Papel.OWNER, autor, outro));
    }
    @Test
    void localizaSelecaoEmCadaParteDaVersao() {
        Requisito requisito = Requisito.novo(UUID.randomUUID(), 1, UUID.randomUUID(), null, null,
                "FUNCIONAL", "Título original", "Conteúdo original", List.of("Critério original"));
        var versao = requisito.candidata().orElseThrow();
        assertTrue(ComentarioService.localizada(versao, "Título"));
        assertTrue(ComentarioService.localizada(versao, "Conteúdo"));
        assertTrue(ComentarioService.localizada(versao, "Critério"));
        assertFalse(ComentarioService.localizada(versao, "Ausente"));
    }
}
