package br.ufla.athena.rastreabilidade.aplicacao;

import br.ufla.athena.rastreabilidade.api.Problema;
import java.time.Duration;
import java.util.UUID;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

@Component
public class ClienteRequisitos {
    private final RestClient cliente;

    public ClienteRequisitos(@Value("${REQUISITOS_URL:http://localhost:8080}") String url) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(2));
        fabrica.setReadTimeout(Duration.ofSeconds(5));
        this.cliente = RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
    }

    public void validar(UUID projeto, UUID requisito) {
        try {
            cliente.get().uri("/api/v1/projetos/{projeto}/requisitos/{requisito}", projeto, requisito)
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404 || e.getStatusCode().value() == 400) {
                throw new Problema(HttpStatus.BAD_REQUEST, "RELACAO_ENTRE_PROJETOS", "Requisito não pertence ao projeto.");
            }
            throw new Problema(HttpStatus.SERVICE_UNAVAILABLE, "REQUISITOS_INDISPONIVEL", "Serviço de requisitos indisponível.");
        } catch (ResourceAccessException e) {
            throw new Problema(HttpStatus.SERVICE_UNAVAILABLE, "REQUISITOS_INDISPONIVEL", "Serviço de requisitos indisponível.");
        }
    }
    public List<Map<String, Object>> listar(UUID projeto) {
        List<Map<String, Object>> resultado = new ArrayList<>();
        try {
            for (String situacao : List.of("ATIVO", "CANCELADO")) {
                int pagina = 0;
                int total;
                do {
                    Map<String, Object> resposta = cliente.get()
                            .uri("/api/v1/projetos/{projeto}/requisitos?situacao={situacao}&page={pagina}&size=100", projeto, situacao, pagina)
                            .retrieve().body(new ParameterizedTypeReference<Map<String, Object>>() { });
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> itens = (List<Map<String, Object>>) resposta.get("items");
                    for (Map<String, Object> item : itens) {
                        UUID id = UUID.fromString(item.get("requisitoId").toString());
                        var detalhe = cliente.get().uri("/api/v1/projetos/{projeto}/requisitos/{id}", projeto, id)
                                .retrieve().toEntity(new ParameterizedTypeReference<Map<String, Object>>() { });
                        Map<String, Object> requisito = new java.util.HashMap<>(detalhe.getBody());
                        requisito.put("revisao", Long.parseLong(detalhe.getHeaders().getETag().replace("\"", "")));
                        resultado.add(requisito);
                    }
                    total = ((Number) resposta.get("totalPages")).intValue();
                    pagina++;
                } while (pagina < total);
            }
            return resultado;
        } catch (Exception e) {
            throw new Problema(HttpStatus.SERVICE_UNAVAILABLE, "REQUISITOS_INDISPONIVEL", "Serviço de requisitos indisponível.");
        }
    }
}
