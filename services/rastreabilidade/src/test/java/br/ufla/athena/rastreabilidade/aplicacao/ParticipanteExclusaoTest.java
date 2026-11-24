package br.ufla.athena.rastreabilidade.aplicacao;

import br.ufla.athena.rastreabilidade.mensageria.ProcessadorInbox;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParticipanteExclusaoTest {
    @Test
    void sempreQuiescente() {
        assertTrue(ParticipanteExclusao.quiescente());
    }

    @Test
    void guardaPermiteComandosDeExclusao() {
        assertFalse(ProcessadorInbox.ignorar("Evento", false));
        assertTrue(ProcessadorInbox.ignorar("Evento", true));
        assertFalse(ProcessadorInbox.ignorar("FecharProjeto", true));
        assertFalse(ProcessadorInbox.ignorar("LimparProjeto", true));
    }
}
