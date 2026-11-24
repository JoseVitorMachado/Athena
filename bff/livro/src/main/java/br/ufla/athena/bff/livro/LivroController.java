package br.ufla.athena.bff.livro;

import br.ufla.athena.bff.livro.ClienteDominio.Resposta;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Livro de Requisitos: só leitura do conteúdo publicado (docs/api/bff-livro.yaml).
 *
 * O BFF não guarda cópia de nada (livro §4.3). Para cada requisito com versão
 * publicada, compõe:
 *   - de Requisitos: requisito, versão publicada e registro de publicação;
 *   - de Rastreabilidade: as relações, apresentadas como referências.
 * Sem Requisitos não há Livro (503). Sem Rastreabilidade o conteúdo sai com aviso.
 */
@RestController
@RequestMapping("/api/v1/livro/projetos/{projetoId}/livro/requisitos")
public class LivroController {

    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;
    private static final String DOMINIO = "/api/v1/projetos/";
    private static final String LIVRO = "/api/v1/livro/projetos/";
    /** Teto desta etapa: o Livro é montado a partir da primeira página de 100 requisitos. */
    private static final int LIMITE_REQUISITOS = 100;

    private final ClienteDominio dominio;
    private final String requisitosUrl;
    private final String rastreabilidadeUrl;

    public LivroController(ClienteDominio dominio, AthenaProperties propriedades) {
        this.dominio = dominio;
        this.requisitosUrl = propriedades.servicos().requisitos();
        this.rastreabilidadeUrl = propriedades.servicos().rastreabilidade();
    }

    /** listarRequisitosLivro: só requisitos ativos com versão publicada. */
    @GetMapping
    public ObjectNode listar(@PathVariable UUID projetoId,
                             @RequestParam(defaultValue = "0") @Min(0) int page,
                             @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        Map<String, JsonNode> ativos = requisitosAtivos(projetoId);
        List<JsonNode> publicados = ativos.values().stream()
                .filter(requisito -> requisito.hasNonNull("versaoPublicadaId"))
                .sorted((a, b) -> codigoNumerico(a) - codigoNumerico(b))
                .toList();

        int inicio = Math.min(page * size, publicados.size());
        int fim = Math.min(inicio + size, publicados.size());
        ArrayNode itens = JSON.arrayNode();
        for (JsonNode requisito : publicados.subList(inicio, fim)) {
            itens.add(compor(projetoId, requisito, ativos));
        }

        ObjectNode pagina = JSON.objectNode();
        pagina.set("items", itens);
        pagina.put("page", page);
        pagina.put("size", size);
        pagina.put("totalElements", publicados.size());
        pagina.put("totalPages", (publicados.size() + size - 1) / size);
        pagina.putObject("_links").putObject("self")
                .put("href", colecao(projetoId) + "?page=" + page + "&size=" + size);
        return pagina;
    }

    /** obterRequisitoLivro */
    @GetMapping("/{requisitoId}")
    public ObjectNode obter(@PathVariable UUID projetoId, @PathVariable UUID requisitoId) {
        Map<String, JsonNode> ativos = requisitosAtivos(projetoId);
        JsonNode requisito = ativos.get(requisitoId.toString());
        // Cancelado ou sem versão publicada não aparece no Livro corrente (livro §4.3).
        if (requisito == null || !requisito.hasNonNull("versaoPublicadaId")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisito não publicado no Livro");
        }
        return compor(projetoId, requisito, ativos);
    }

    private ObjectNode compor(UUID projetoId, JsonNode requisito, Map<String, JsonNode> ativos) {
        String requisitoId = requisito.get("requisitoId").asText();
        String base = DOMINIO + projetoId + "/requisitos/" + requisitoId;

        JsonNode versao = exigir(dominio.get("requisitos", requisitosUrl,
                base + "/versoes/" + requisito.get("versaoPublicadaId").asText()));
        JsonNode publicacao = exigir(dominio.get("requisitos", requisitosUrl, base + "/publicacoes?size=1"))
                .path("items").path(0);

        ObjectNode item = JSON.objectNode();
        item.put("requisitoId", requisitoId);
        item.put("projetoId", projetoId.toString());
        item.set("codigo", requisito.get("codigo"));
        item.set("titulo", versao.get("titulo"));
        item.set("versaoPublicada", versao.get("numero"));
        item.set("publicacaoId", publicacao.get("publicacaoId"));
        item.set("versaoId", versao.get("versaoId"));
        item.set("retratoId", publicacao.get("retratoId"));
        item.set("conteudo", versao.get("conteudo"));
        item.set("criteriosAceite", versao.get("criteriosAceite"));

        ArrayNode avisos = JSON.arrayNode();
        item.set("referencias", referencias(projetoId, requisitoId, publicacao.path("retratoId").asText(), ativos, avisos));
        item.set("avisos", avisos);

        ObjectNode links = item.putObject("_links");
        links.putObject("self").put("href", colecao(projetoId) + "/" + requisitoId);
        links.putObject("colecao").put("href", colecao(projetoId));
        return item;
    }

    /** Referências congeladas no retrato associado à publicação. */
    private ArrayNode referencias(UUID projetoId, String requisitoId, String retratoId, Map<String, JsonNode> ativos,
                                  ArrayNode avisos) {
        ArrayNode referencias = JSON.arrayNode();
        Resposta relacoes;
        try {
            relacoes = dominio.get("rastreabilidade", rastreabilidadeUrl,
                    DOMINIO + projetoId + "/retratos/" + retratoId);
        } catch (DominioIndisponivelException e) {
            avisos.add("Referências da publicação indisponíveis.");
            return referencias;
        }
        if (!relacoes.sucesso()) {
            avisos.add("Referências da publicação indisponíveis.");
            return referencias;
        }
        for (JsonNode relacao : relacoes.corpo().path("relacoes")) {
            String outro = relacao.get("origemId").asText().equals(requisitoId)
                    ? relacao.get("destinoId").asText()
                    : relacao.get("origemId").asText();
            JsonNode alvo = ativos.get(outro);
            ObjectNode referencia = referencias.addObject();
            if (alvo == null) {
                // Cancelado ou fora do alcance do leitor: não revela nada sobre o alvo.
                referencia.put("disponivel", false);
                continue;
            }
            referencia.put("disponivel", true);
            referencia.put("requisitoId", outro);
            referencia.set("codigo", alvo.get("codigo"));
            referencia.set("titulo", alvo.get("titulo"));
            referencia.put("cancelado", false);
            referencia.set("tipo", relacao.get("tipo"));
        }
        return referencias;
    }

    /** Requisitos ativos do projeto, por id. Cancelados ficam fora do Livro corrente. */
    private Map<String, JsonNode> requisitosAtivos(UUID projetoId) {
        JsonNode pagina = exigir(dominio.get("requisitos", requisitosUrl,
                DOMINIO + projetoId + "/requisitos?situacao=ATIVO&size=" + LIMITE_REQUISITOS));
        Map<String, JsonNode> porId = new HashMap<>();
        for (JsonNode requisito : pagina.path("items")) {
            porId.put(requisito.get("requisitoId").asText(), requisito);
        }
        return porId;
    }

    /** Conteúdo e situação são obrigatórios: sem eles o Livro não é exibido. */
    private static JsonNode exigir(Resposta resposta) {
        if (resposta.status().value() == 404) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Não encontrado");
        }
        if (!resposta.sucesso()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Requisitos respondeu " + resposta.status().value());
        }
        return resposta.corpo();
    }

    private static int codigoNumerico(JsonNode requisito) {
        String codigo = requisito.path("codigo").asText("REQ-0");
        try {
            return Integer.parseInt(codigo.substring(codigo.indexOf('-') + 1));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    private static String colecao(UUID projetoId) {
        return LIVRO + projetoId + "/livro/requisitos";
    }
}
