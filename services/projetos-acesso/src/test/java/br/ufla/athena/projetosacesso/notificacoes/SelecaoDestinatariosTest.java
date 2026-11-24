package br.ufla.athena.projetosacesso.notificacoes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SelecaoDestinatariosTest {
    @Test void selecionaSomenteMembrosMencionadosEExcluiAutor() {
        UUID autor=UUID.randomUUID(), membro=UUID.randomUUID(), estranho=UUID.randomUUID();
        var membros=List.of(new SelecaoDestinatarios.Membro(autor,"OWNER"),new SelecaoDestinatarios.Membro(membro,"LEITOR"));
        assertEquals(List.of(membro),SelecaoDestinatarios.mencoes(membros,List.of(autor,membro,membro,estranho),autor));
    }
    @Test void selecionaAprovadoresESuperioresExcetoSubmissor() {
        UUID editor=UUID.randomUUID(), aprovador=UUID.randomUUID(), mantenedor=UUID.randomUUID(), owner=UUID.randomUUID();
        var membros=List.of(new SelecaoDestinatarios.Membro(editor,"EDITOR"),new SelecaoDestinatarios.Membro(aprovador,"APROVADOR"),new SelecaoDestinatarios.Membro(mantenedor,"MANTENEDOR"),new SelecaoDestinatarios.Membro(owner,"OWNER"));
        assertEquals(List.of(aprovador,owner),SelecaoDestinatarios.revisores(membros,mantenedor));
    }
}
