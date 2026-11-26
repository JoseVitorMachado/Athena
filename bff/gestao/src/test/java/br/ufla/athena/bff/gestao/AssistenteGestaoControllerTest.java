package br.ufla.athena.bff.gestao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class AssistenteGestaoControllerTest {

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void encaminhaTokenECorpoComTamanhoDeclarado() throws IOException {
        AtomicReference<String> tamanho = new AtomicReference<>();
        AtomicReference<String> token = new AtomicReference<>();
        HttpServer agente = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        agente.createContext("/perguntar", troca -> {
            tamanho.set(troca.getRequestHeaders().getFirst("Content-Length"));
            token.set(troca.getRequestHeaders().getFirst("Authorization"));
            troca.getRequestBody().readAllBytes();
            troca.sendResponseHeaders(200, 2);
            troca.getResponseBody().write("{}".getBytes());
            troca.close();
        });
        agente.start();
        try {
            var controller = new AssistenteGestaoController(
                    "http://127.0.0.1:" + agente.getAddress().getPort(), json);
            var resposta = controller.perguntar(UUID.randomUUID(),
                    json.readTree("{\"pergunta\":\"Qual é a meta?\"}"), "Bearer usuario");

            assertEquals(200, resposta.getStatusCode().value());
            assertEquals("Bearer usuario", token.get());
            assertNotNull(tamanho.get());
        } finally {
            agente.stop(0);
        }
    }
}
