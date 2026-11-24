package br.ufla.athena.projetosacesso.notificacoes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class MensagensNotificacaoTest {
    @Test void montaMensagens() {
        assertEquals("Você foi convidado para o projeto Atlas como Repórter.", MensagensNotificacao.convite("Atlas", "Repórter"));
        assertEquals("Ana quer transferir a propriedade do projeto Atlas para você.", MensagensNotificacao.transferencia("Ana", "Atlas"));
        assertEquals("Seu papel no projeto Atlas agora é Editor.", MensagensNotificacao.papel("Atlas", "Editor"));
        assertEquals("Você foi removido do projeto Atlas.", MensagensNotificacao.removido("Atlas"));
        assertEquals("Ana aceitou o convite para o projeto Atlas.", MensagensNotificacao.conviteAceito("Ana", "Atlas"));
        assertEquals("Ana aceitou a transferência de propriedade do projeto Atlas.", MensagensNotificacao.propriedadeTransferida("Ana", "Atlas"));
        assertEquals("Ana mencionou você em REQ-1: texto", MensagensNotificacao.mencao("Ana", "REQ-1", "texto"));
        assertEquals("Revisão disponível: REQ-1 — Título.", MensagensNotificacao.revisao("REQ-1", "Título"));
        assertEquals("Sua alteração em REQ-1 foi aprovada e pode ser publicada.", MensagensNotificacao.decisao("REQ-1", "APROVAR"));
        assertEquals("Sua alteração em REQ-1 foi rejeitada e voltou para rascunho.", MensagensNotificacao.decisao("REQ-1", "REJEITAR"));
        assertEquals("A revisão de REQ-1 foi concluída.", MensagensNotificacao.decisao("REQ-1", null));
    }
}
