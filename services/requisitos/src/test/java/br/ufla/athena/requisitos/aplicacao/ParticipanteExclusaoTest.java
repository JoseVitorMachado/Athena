package br.ufla.athena.requisitos.aplicacao;

import br.ufla.athena.requisitos.mensageria.ProcessadorInbox;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParticipanteExclusaoTest {
    @Test
    void quiescenciaDependeDeSubmissaoAtiva() {
        assertTrue(ParticipanteExclusao.quiescente(false));
        assertFalse(ParticipanteExclusao.quiescente(true));
    }

    @Test
    void guardaPermiteComandosDeExclusao() {
        assertFalse(ProcessadorInbox.ignorar("Evento", false));
        assertTrue(ProcessadorInbox.ignorar("Evento", true));
        assertFalse(ProcessadorInbox.ignorar("FecharProjeto", true));
        assertFalse(ProcessadorInbox.ignorar("LimparProjeto", true));
    }
}
