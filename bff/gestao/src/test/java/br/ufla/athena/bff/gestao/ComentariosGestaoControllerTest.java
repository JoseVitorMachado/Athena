package br.ufla.athena.bff.gestao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

class ComentariosGestaoControllerTest {
    private final ClienteDominio dominio = mock(ClienteDominio.class);
    private final PapelAtual papelAtual = mock(PapelAtual.class);
    private final AthenaProperties propriedades = new AthenaProperties(
            new AthenaProperties.Servicos("", "http://requisitos", "", ""), null);
    private final ComentariosGestaoController controller = new ComentariosGestaoController(dominio, propriedades, papelAtual);
    private final ObjectMapper json = new ObjectMapper();
    private final UUID projeto = UUID.randomUUID();
    private final UUID requisito = UUID.randomUUID();

    @Test
    void validaMembroERepassaComentario() {
        UUID usuario = UUID.randomUUID();
        ObjectNode corpo = json.createObjectNode();
        corpo.putArray("mencoes").add(usuario.toString());
        String caminho = "/api/v1/projetos/" + projeto + "/requisitos/" + requisito + "/comentarios";
        when(dominio.post("requisitos", "http://requisitos", caminho, corpo, null, null))
                .thenReturn(new ClienteDominio.Resposta(HttpStatus.CREATED, HttpHeaders.EMPTY, corpo));
        assertEquals(201, controller.criar(projeto, requisito, null, null, corpo).getStatusCode().value());
        verify(papelAtual).consultar(projeto.toString(), usuario.toString());
    }

    @Test
    void rejeitaIdentificadorInvalidoELimiteExcedido() {
        ObjectNode corpo = json.createObjectNode();
        corpo.putArray("mencoes").add("inválido");
        FalhaAutorizacao erro = assertThrows(FalhaAutorizacao.class, () -> controller.criar(projeto, requisito, null, null, corpo));
        assertEquals(400, erro.status());
        assertEquals("MENCAO_INVALIDA", erro.codigo());
        assertEquals("Só é possível mencionar membros do projeto.", erro.getMessage());
        ArrayNode mencoes = corpo.putArray("mencoes");
        for (int i = 0; i < 21; i++) mencoes.add(UUID.randomUUID().toString());
        assertEquals(400, assertThrows(FalhaAutorizacao.class, () -> controller.criar(projeto, requisito, null, null, corpo)).status());
        verifyNoInteractions(dominio, papelAtual);
    }

    @Test
    void naoMembroRetorna400EIndisponibilidadeContinua503() {
        UUID usuario = UUID.randomUUID();
        ObjectNode corpo = json.createObjectNode();
        corpo.putArray("mencoes").add(usuario.toString());
        when(papelAtual.consultar(projeto.toString(), usuario.toString()))
                .thenThrow(new FalhaAutorizacao(403, "SEM_ACESSO_AO_PROJETO", "Acesso negado."))
                .thenThrow(new FalhaAutorizacao(503, "IDENTIDADE_INDISPONIVEL", "Serviço indisponível."));
        FalhaAutorizacao naoMembro = assertThrows(FalhaAutorizacao.class, () -> controller.criar(projeto, requisito, null, null, corpo));
        assertEquals(400, naoMembro.status());
        assertEquals("MENCAO_INVALIDA", naoMembro.codigo());
        FalhaAutorizacao indisponivel = assertThrows(FalhaAutorizacao.class, () -> controller.criar(projeto, requisito, null, null, corpo));
        assertEquals(503, indisponivel.status());
        assertEquals("IDENTIDADE_INDISPONIVEL", indisponivel.codigo());
        verifyNoInteractions(dominio);
    }
}
