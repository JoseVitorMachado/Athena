package br.ufla.athena.bff.gestao;

import br.ufla.athena.bff.gestao.ClienteDominio.Resposta;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Requisitos na visão do cliente Gestão (docs/api/bff-gestao.yaml).
 *
 * Listagem e criação são repasses ao serviço Requisitos. A consulta de um
 * requisito é uma composição: dados do requisito e da candidata vêm de
 * Requisitos; lote e pendências abertas vêm de Revisões.
 */
@RestController
@RequestMapping("/api/v1/gestao/projetos/{projetoId}/requisitos")
public class RequisitosGestaoController {

    private static final JsonNodeFactory JSON = JsonNodeFactory.instance;

    private final ClienteDominio dominio;
    private final String requisitosUrl;
    private final String revisoesUrl;
    private final MontadorAcoes montador = new MontadorAcoes();

    public RequisitosGestaoController(ClienteDominio dominio, AthenaProperties propriedades) {
        this.dominio = dominio;
        this.requisitosUrl = propriedades.servicos().requisitos();
        this.revisoesUrl = propriedades.servicos().revisoes();
    }

    /** gestaoListarRequisitos */
    @GetMapping
    public ResponseEntity<JsonNode> listar(@PathVariable UUID projetoId, HttpServletRequest pedido) {
        String query = pedido.getQueryString() == null ? "" : "?" + pedido.getQueryString();
        Resposta resposta = dominio.get("requisitos", requisitosUrl, colecao(projetoId) + query);
        if (resposta.sucesso()) {
            Caminhos.reescreverLinks(resposta.corpo());
        }
        return repassar(resposta);
    }

    /** gestaoCriarRequisito */
    @PostMapping
    public ResponseEntity<JsonNode> criar(@PathVariable UUID projetoId, @RequestBody JsonNode corpo) {
        return repassar(dominio.post("requisitos", requisitosUrl, colecao(projetoId), corpo));
    }

    @GetMapping("/{requisitoId}/candidata")
    public ResponseEntity<JsonNode> obterCandidata(@PathVariable UUID projetoId, @PathVariable UUID requisitoId) {
        return repassar(dominio.get("requisitos", requisitosUrl, caminhoRequisito(projetoId, requisitoId) + "/candidata"));
    }

    @PatchMapping("/{requisitoId}/candidata")
    public ResponseEntity<JsonNode> editarCandidata(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch, @RequestBody JsonNode corpo) {
        return repassar(dominio.patch("requisitos", requisitosUrl,
                caminhoRequisito(projetoId, requisitoId) + "/candidata", corpo, ifMatch));
    }

    @PostMapping("/{requisitoId}/submissoes")
    public ResponseEntity<JsonNode> criarSubmissao(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, @RequestBody JsonNode corpo) {
        return repassar(dominio.post("requisitos", requisitosUrl,
                caminhoRequisito(projetoId, requisitoId) + "/submissoes", corpo, ifMatch, chave));
    }

    @GetMapping("/{requisitoId}/submissoes")
    public ResponseEntity<JsonNode> listarSubmissoes(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            HttpServletRequest pedido) {
        String query = pedido.getQueryString() == null ? "" : "?" + pedido.getQueryString();
        return repassar(dominio.get("requisitos", requisitosUrl,
                caminhoRequisito(projetoId, requisitoId) + "/submissoes" + query));
    }

    @GetMapping("/{requisitoId}/submissoes/{submissaoId}")
    public ResponseEntity<JsonNode> obterSubmissao(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
                                                    @PathVariable UUID submissaoId) {
        return repassar(dominio.get("requisitos", requisitosUrl,
                caminhoRequisito(projetoId, requisitoId) + "/submissoes/" + submissaoId));
    }

    @GetMapping("/{requisitoId}/versoes")
    public ResponseEntity<JsonNode> listarVersoes(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
                                                   HttpServletRequest pedido) {
        String query = pedido.getQueryString() == null ? "" : "?" + pedido.getQueryString();
        return repassar(dominio.get("requisitos", requisitosUrl,
                caminhoRequisito(projetoId, requisitoId) + "/versoes" + query));
    }

    @GetMapping("/{requisitoId}/versoes/{versaoId}")
    public ResponseEntity<JsonNode> obterVersao(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
                                                @PathVariable UUID versaoId) {
        return repassar(dominio.get("requisitos", requisitosUrl,
                caminhoRequisito(projetoId, requisitoId) + "/versoes/" + versaoId));
    }

    @GetMapping("/{requisitoId}/publicacoes")
    public ResponseEntity<JsonNode> listarPublicacoes(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
                                                       HttpServletRequest pedido) {
        String query = pedido.getQueryString() == null ? "" : "?" + pedido.getQueryString();
        return repassar(dominio.get("requisitos", requisitosUrl,
                caminhoRequisito(projetoId, requisitoId) + "/publicacoes" + query));
    }

    @PostMapping("/{requisitoId}/publicacoes")
    public ResponseEntity<JsonNode> criarPublicacao(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, @RequestBody JsonNode corpo) {
        return repassar(dominio.post("requisitos", requisitosUrl,
                caminhoRequisito(projetoId, requisitoId) + "/publicacoes", corpo, ifMatch, chave));
    }

    private static String caminhoRequisito(UUID projetoId, UUID requisitoId) {
        return colecao(projetoId) + "/" + requisitoId;
    }

    /** gestaoObterRequisito: composição Requisitos + Revisões. */
    @GetMapping("/{requisitoId}")
    public ResponseEntity<JsonNode> obter(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
                                        HttpServletRequest pedido) {
        Resposta requisito = dominio.get("requisitos", requisitosUrl, colecao(projetoId) + "/" + requisitoId);
        if (!requisito.sucesso()) {
            return repassar(requisito);
        }

        JsonNode origem = requisito.corpo();
        ObjectNode visao = JSON.objectNode();
        ArrayNode avisos = JSON.arrayNode();
        copiar(origem, visao, "requisitoId", "projetoId", "codigo", "titulo", "situacao");

        JsonNode candidata = origem.get("candidata");
        if (candidata != null && candidata.isObject()) {
            visao.set("versaoEmEdicao", candidata.get("numero"));
            visao.set("versaoId", candidata.get("versaoId"));
            visao.set("estadoVersao", candidata.get("estadoVersao"));
        }

        Resposta submissoes = dominio.get("requisitos", requisitosUrl,
                caminhoRequisito(projetoId, requisitoId) + "/submissoes?size=1");
        if (!submissoes.sucesso()) {
            return repassar(submissoes);
        }
        JsonNode submissao = submissoes.corpo().path("items").path(0);
        JsonNode lote = acrescentarRevisao(projetoId, requisitoId, submissao, visao, avisos);

        visao.set("atualizadoEm", origem.get("atualizadoEm"));

        String estado = texto(candidata, "estadoVersao");
        if (estado == null && !origem.hasNonNull("versaoPublicadaId")) {
            estado = "SEM_VERSAO";
        }
        MontadorAcoes.Resultado navegacao = montador.montar(projetoId.toString(), requisitoId.toString(),
                texto(origem, "situacao"), estado, texto(lote, "loteId"),
                texto(lote, "estado"), texto(submissao, "submissaoId"), ((PapelAtual.Vinculo) pedido.getAttribute(AutorizacaoProjeto.VINCULO)).papel());
        visao.set("_links", JSON.pojoNode(navegacao.links()));
        visao.set("_actions", JSON.pojoNode(navegacao.actions()));
        visao.set("avisos", avisos);

        // O ETag é o do requisito no serviço proprietário; o BFF não calcula outro.
        ResponseEntity.BodyBuilder resposta = ResponseEntity.ok();
        String etag = requisito.cabecalhos().getETag();
        if (etag != null) {
            resposta.eTag(etag);
        }
        return resposta.body(visao);
    }

    /**
     * Revisões é informação auxiliar nesta tela: se o serviço estiver fora do ar, o
     * requisito é entregue mesmo assim, com um aviso, e sem presumir nada sobre a
     * revisão (docs/api/README.md, "Mesmo requisito nos dois clientes").
     */
    private JsonNode acrescentarRevisao(UUID projetoId, UUID requisitoId, JsonNode submissao,
                                        ObjectNode visao, ArrayNode avisos) {
        try {
            String loteId = texto(submissao, "loteId");
            String caminho = Caminhos.DOMINIO + projetoId + "/lotes";
            Resposta lotes = dominio.get("revisoes", revisoesUrl, loteId == null
                    ? caminho + "?requisitoId=" + requisitoId + "&size=1"
                    : caminho + "/" + loteId);
            if (!lotes.sucesso()) {
                avisos.add("Não foi possível consultar a revisão deste requisito.");
                return null;
            }
            JsonNode lote = loteId == null ? lotes.corpo().path("items").path(0) : lotes.corpo();
            if (lote.isMissingNode()) {
                return null;
            }
            if (texto(submissao, "submissaoId") == null
                    || !texto(submissao, "submissaoId").equals(texto(lote, "submissaoId"))) {
                return null;
            }
            visao.set("loteId", lote.get("loteId"));
            int abertas = 0;
            for (JsonNode pendencia : lote.path("pendencias")) {
                if (!pendencia.path("resolvida").asBoolean(false)) {
                    abertas++;
                }
            }
            visao.put("pendenciasAbertas", abertas);
            return lote;
        } catch (DominioIndisponivelException e) {
            avisos.add("Revisões indisponível: lote e pendências não foram consultados.");
            return null;
        }
    }

    private static String texto(JsonNode origem, String campo) {
        return origem != null && origem.hasNonNull(campo) ? origem.get(campo).asText() : null;
    }

    private static void copiar(JsonNode origem, ObjectNode destino, String... campos) {
        for (String campo : campos) {
            if (origem.hasNonNull(campo)) {
                destino.set(campo, origem.get(campo));
            }
        }
    }

    private static String colecao(UUID projetoId) {
        return Caminhos.DOMINIO + projetoId + "/requisitos";
    }

    /** Devolve ao cliente o que o domínio respondeu, trocando os caminhos pelos do BFF. */
    static ResponseEntity<JsonNode> repassar(Resposta resposta) {
        Caminhos.reescreverLinks(resposta.corpo());
        ResponseEntity.BodyBuilder saida = ResponseEntity.status(resposta.status());
        HttpHeaders origem = resposta.cabecalhos();
        MediaType tipo = origem.getContentType();
        if (tipo != null) {
            saida.contentType(tipo);
        }
        if (origem.getLocation() != null) {
            saida.header(HttpHeaders.LOCATION, Caminhos.paraGestao(origem.getLocation().toString()));
        }
        if (origem.getETag() != null) {
            saida.eTag(origem.getETag());
        }
        if (origem.getFirst(HttpHeaders.RETRY_AFTER) != null) {
            saida.header(HttpHeaders.RETRY_AFTER, origem.getFirst(HttpHeaders.RETRY_AFTER));
        }
        return saida.body(resposta.corpo());
    }
}
