package br.ufla.athena.requisitos.aplicacao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.ufla.athena.requisitos.dominio.Requisito;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RequisitoAlteradoTest {

    @Test
    void montaPayloadComDadosERevisaoDoRequisito() {
        UUID projetoId = UUID.randomUUID();
        Requisito requisito = Requisito.novo(projetoId, 7, UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), "FUNCIONAL", "Título", "Conteúdo", List.of("Critério"));

        Map<String, Object> payload = RequisitoService.payloadRequisitoAlterado(requisito);

        assertEquals(Map.of(
                "requisitoId", requisito.getId(),
                "projetoId", projetoId,
                "codigo", "REQ-7",
                "titulo", "Título",
                "situacao", "ATIVO",
                "revisao", requisito.getRevisao()), payload);
    }
}
