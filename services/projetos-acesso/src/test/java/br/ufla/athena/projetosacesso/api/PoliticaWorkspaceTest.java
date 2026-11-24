package br.ufla.athena.projetosacesso.api;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PoliticaWorkspaceTest {
    private final UUID owner = UUID.randomUUID();
    private final UUID membro = UUID.randomUUID();

    @Test
    void visibilidadeDependeDoOwnerOuDeMembroDeProjeto() {
        assertTrue(PoliticaWorkspace.visivel(owner, owner, List.of()));
        assertTrue(PoliticaWorkspace.visivel(membro, owner, List.of("LEITOR")));
        assertFalse(PoliticaWorkspace.visivel(membro, owner, List.of()));
        assertFalse(PoliticaWorkspace.owner(membro, owner));
    }

    @Test
    void criacaoExigeOwnerDoWorkspaceOuMantenedorDeProjeto() {
        assertTrue(PoliticaWorkspace.podeCriarProjeto(owner, owner, List.of()));
        assertTrue(PoliticaWorkspace.podeCriarProjeto(membro, owner, List.of("LEITOR", "MANTENEDOR")));
        assertTrue(PoliticaWorkspace.podeCriarProjeto(membro, owner, List.of("OWNER")));
        for (String papel : List.of("LEITOR", "REPORTER", "EDITOR", "APROVADOR")) {
            assertFalse(PoliticaWorkspace.mantenedor(papel));
            assertFalse(PoliticaWorkspace.podeCriarProjeto(membro, owner, List.of(papel)));
        }
        assertFalse(PoliticaWorkspace.mantenedor(null));
    }

    @Test
    void confirmacaoComparaNomeExatoAposTrim() {
        assertTrue(PoliticaWorkspace.confirmacaoValida("Equipe ATHENA", "  Equipe ATHENA  "));
        assertFalse(PoliticaWorkspace.confirmacaoValida("Equipe ATHENA", "equipe ATHENA"));
        assertFalse(PoliticaWorkspace.confirmacaoValida("Equipe ATHENA", null));
    }
}
