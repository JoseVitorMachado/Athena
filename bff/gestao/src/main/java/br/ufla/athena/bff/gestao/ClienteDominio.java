package br.ufla.athena.bff.gestao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Chamadas REST aos serviços de domínio. Devolve status, cabeçalhos e corpo como
 * vieram, para o BFF decidir se repassa ou compõe. Falha de conexão ou timeout
 * vira {@link DominioIndisponivelException}.
 */
@Component
public class ClienteDominio {

    private final RestClient http;
    private final ObjectMapper json;

    public ClienteDominio(RestClient.Builder builder, ObjectMapper json, AthenaProperties propriedades) {
        HttpClient cliente = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(propriedades.http().timeoutConexao())
                .build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(cliente);
        fabrica.setReadTimeout(propriedades.http().timeoutLeitura());
        this.http = builder.requestFactory(fabrica).build();
        this.json = json;
    }

    public Resposta get(String servico, String urlBase, String caminhoComQuery) {
        return chamar(servico, HttpMethod.GET, urlBase, caminhoComQuery, null, null, null);
    }

    public Resposta post(String servico, String urlBase, String caminho, JsonNode corpo) {
        return chamar(servico, HttpMethod.POST, urlBase, caminho, corpo, null, null);
    }

    public Resposta post(String servico, String urlBase, String caminho, JsonNode corpo,
                         String ifMatch, String idempotencyKey) {
        return chamar(servico, HttpMethod.POST, urlBase, caminho, corpo, ifMatch, idempotencyKey);
    }

    public Resposta patch(String servico, String urlBase, String caminho, JsonNode corpo, String ifMatch) {
        return chamar(servico, HttpMethod.PATCH, urlBase, caminho, corpo, ifMatch, null);
    }

    public Resposta delete(String servico, String urlBase, String caminho, String ifMatch) {
        return chamar(servico, HttpMethod.DELETE, urlBase, caminho, null, ifMatch, null);
    }

    public Resposta get(String servico, String urlBase, String caminho, String authorization) {
        return chamar(servico, HttpMethod.GET, urlBase, caminho, null, null, null, authorization);
    }

    /** Encaminha os cabeçalhos condicionais de uma consulta autenticada. */
    public Resposta get(String servico, String urlBase, String caminho, String ifMatch,
                        String idempotencyKey, String authorization) {
        return chamar(servico, HttpMethod.GET, urlBase, caminho, null, ifMatch, idempotencyKey, authorization);
    }

    public Resposta post(String servico, String urlBase, String caminho, JsonNode corpo,
                         String ifMatch, String idempotencyKey, String authorization) {
        return chamar(servico, HttpMethod.POST, urlBase, caminho, corpo, ifMatch, idempotencyKey, authorization);
    }

    public Resposta patch(String servico, String urlBase, String caminho, JsonNode corpo,
                          String ifMatch, String idempotencyKey, String authorization) {
        return chamar(servico, HttpMethod.PATCH, urlBase, caminho, corpo, ifMatch, idempotencyKey, authorization);
    }

    /** Encaminha uma exclusão autenticada ao domínio. */
    public Resposta delete(String servico, String urlBase, String caminho, String ifMatch, String authorization) {
        return delete(servico, urlBase, caminho, ifMatch, null, authorization);
    }

    /** Encaminha também a chave de idempotência, quando informada. */
    public Resposta delete(String servico, String urlBase, String caminho, String ifMatch,
                           String idempotencyKey, String authorization) {
        return chamar(servico, HttpMethod.DELETE, urlBase, caminho, null, ifMatch, idempotencyKey, authorization);
    }

    private Resposta chamar(String servico, HttpMethod metodo, String urlBase, String caminho, JsonNode corpo,
                            String ifMatch, String idempotencyKey) {
        return chamar(servico, metodo, urlBase, caminho, corpo, ifMatch, idempotencyKey, null);
    }

    private Resposta chamar(String servico, HttpMethod metodo, String urlBase, String caminho, JsonNode corpo,
                            String ifMatch, String idempotencyKey, String authorization) {
        RestClient.RequestBodySpec requisicao = http.method(metodo)
                .uri(URI.create(urlBase + caminho))
                .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_PROBLEM_JSON);
        var autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao instanceof JwtAuthenticationToken jwt && jwt.isAuthenticated()
                && jwt.getToken().getSubject() != null) {
            requisicao.header("X-Athena-Usuario", jwt.getToken().getSubject());
        }
        if (authorization != null) {
            requisicao.header(HttpHeaders.AUTHORIZATION, authorization);
        }
        if (ifMatch != null) {
            requisicao.header(HttpHeaders.IF_MATCH, ifMatch);
        }
        if (idempotencyKey != null) {
            requisicao.header("Idempotency-Key", idempotencyKey);
        }
        if (corpo != null) {
            requisicao.contentType(MediaType.APPLICATION_JSON).body(corpo);
        }
        try {
            return requisicao.exchange((pedido, resposta) -> {
                byte[] bytes = resposta.getBody().readAllBytes();
                JsonNode conteudo = bytes.length == 0 ? null : json.readTree(bytes);
                return new Resposta(resposta.getStatusCode(), resposta.getHeaders(), conteudo);
            });
        } catch (ResourceAccessException e) {
            throw new DominioIndisponivelException(servico, e);
        }
    }

    public record Resposta(HttpStatusCode status, HttpHeaders cabecalhos, JsonNode corpo) {

        public boolean sucesso() {
            return status.is2xxSuccessful();
        }
    }
}
