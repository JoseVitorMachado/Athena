package br.ufla.athena.bff.gestao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/v1/gestao/projetos/{projetoId}/assistente")
public class AssistenteGestaoController {
    private static final Logger log = LoggerFactory.getLogger(AssistenteGestaoController.class);

    private final RestClient cliente;
    private final ObjectMapper json;

    public AssistenteGestaoController(@Value("${AGENTE_IA_URL:http://agente-ia:8090}") String url,
                                      ObjectMapper json) {
        this.json = json;
        var fabrica = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(3))
                .build());
        fabrica.setReadTimeout(Duration.ofSeconds(100));
        cliente = RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
    }

    @PostMapping("/perguntas")
    @Operation(summary = "Consultar assistente", description = "Papel mínimo: Leitor. Permitido em projetos arquivados; cada pergunta é independente.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(schema = @Schema(example = "{\"pergunta\": \"Qual é a meta de latência das consultas?\"}")))
    public ResponseEntity<?> perguntar(@PathVariable UUID projetoId, @RequestBody JsonNode corpo,
                                       @Parameter(hidden = true) @RequestHeader("Authorization") String authorization)
            throws JsonProcessingException {
        JsonNode pergunta = corpo == null ? null : corpo.get("pergunta");
        if (pergunta == null || !pergunta.isTextual() || pergunta.asText().isBlank() || pergunta.asText().length() > 8000) {
            return problema(400, "Informe uma pergunta de 1 a 8000 caracteres.");
        }
        // Em bytes, o pedido segue com Content-Length, que o agente exige.
        byte[] pedidoAgente = json.writeValueAsBytes(Map.of("projetoId", projetoId, "pergunta", pergunta.asText()));
        try {
            return cliente.post().uri("/perguntar").header("Authorization", authorization)
                    .contentType(MediaType.APPLICATION_JSON).body(pedidoAgente)
                    .exchange((pedido, resposta) -> {
                        int status = resposta.getStatusCode().value();
                        if (status == 200) return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(resposta.getBody().readAllBytes());
                        if (status == 401 || status == 403 || status == 429) return problema(status, "Não foi possível atender à consulta do assistente.");
                        log.warn("Agente de IA respondeu com status {}", status);
                        return problema(503, "O assistente está indisponível. Tente novamente em instantes.");
                    });
        } catch (ResourceAccessException e) {
            log.warn("Agente de IA inacessível: {}", e.getMostSpecificCause().getClass().getSimpleName());
            return problema(503, "O assistente está indisponível. Tente novamente em instantes.");
        }
    }

    private static ResponseEntity<?> problema(int status, String detalhe) {
        var resposta = ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON);
        if (status == 429 || status == 503) resposta.header(HttpHeaders.RETRY_AFTER, "15");
        return resposta.body(ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(status), detalhe));
    }
}
