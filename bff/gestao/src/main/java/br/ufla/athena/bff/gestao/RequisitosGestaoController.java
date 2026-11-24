package br.ufla.athena.bff.gestao;

import br.ufla.athena.bff.gestao.docs.ProblemaDoc;
import br.ufla.athena.bff.gestao.docs.MetadadosPatchDoc;
import br.ufla.athena.bff.gestao.docs.ConfirmacaoDoc;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.ufla.athena.bff.gestao.docs.RequisitoEntradaDoc;
import br.ufla.athena.bff.gestao.docs.CandidataPatchDoc;
import br.ufla.athena.bff.gestao.docs.SubmissaoEntradaDoc;
import br.ufla.athena.bff.gestao.docs.PublicacaoEntradaDoc;
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
    private final PapelAtual papelAtual;
    private final String revisoesUrl;
    private final MontadorAcoes montador = new MontadorAcoes();

    public RequisitosGestaoController(ClienteDominio dominio, AthenaProperties propriedades, PapelAtual papelAtual) {
        this.dominio = dominio;
        this.papelAtual = papelAtual;
        this.requisitosUrl = propriedades.servicos().requisitos();
        this.revisoesUrl = propriedades.servicos().revisoes();
    }

    /** gestaoListarRequisitos */
    @Tag(name = "Requisitos")
    @Operation(summary = "Listar requisitos", description = "Papel mínimo: Leitor. Lista os itens disponíveis.")
    @ApiResponse(responseCode = "200", description = "Operação concluída")
    @ApiResponse(responseCode = "400", description = "Erro 400",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "401", description = "Erro 401",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "403", description = "Erro 403",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "404", description = "Erro 404",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "409", description = "Erro 409",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "429", description = "Erro 429",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Erro 503",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
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
    @Tag(name = "Requisitos")
    @Operation(summary = "Criar requisito", description = "Papel mínimo: Editor. Cria o recurso.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = RequisitoEntradaDoc.class)))
    @ApiResponse(responseCode = "201", description = "Operação concluída")
    @ApiResponse(responseCode = "400", description = "Erro 400",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "401", description = "Erro 401",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "403", description = "Erro 403",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "404", description = "Erro 404",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "409", description = "Erro 409",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "429", description = "Erro 429",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Erro 503",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @PostMapping
    public ResponseEntity<JsonNode> criar(@PathVariable UUID projetoId, @RequestBody JsonNode corpo) {
        return repassar(dominio.post("requisitos", requisitosUrl, colecao(projetoId), corpo));
    }

    /** Atualiza os metadados do requisito. */
    @Tag(name = "Requisitos")
    @Operation(summary = "Editar metadados", description = "Papel mínimo: Editor.", parameters = {
            @Parameter(name = "If-Match", in = ParameterIn.HEADER, required = true),
            @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER)})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = MetadadosPatchDoc.class)))
    @ApiResponse(responseCode = "200", description = "Operação concluída")
    @ApiResponse(responseCode = "400", description = "Responsável inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Serviço indisponível", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "412", description = "ETag divergente", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "428", description = "If-Match ausente", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @PatchMapping("/{requisitoId}")
    public ResponseEntity<JsonNode> editarMetadados(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, @RequestBody JsonNode corpo) {
        if (corpo.hasNonNull("responsavelId")) {
            String responsavel;
            try {
                if (!corpo.get("responsavelId").isTextual()) throw new IllegalArgumentException();
                responsavel = UUID.fromString(corpo.get("responsavelId").asText()).toString();
            } catch (IllegalArgumentException e) {
                throw new FalhaAutorizacao(400, "RESPONSAVEL_INVALIDO", "O responsável deve ser membro do projeto.");
            }
            try {
                papelAtual.consultar(projetoId.toString(), responsavel);
            } catch (FalhaAutorizacao e) {
                if (e.status() != 403) throw e;
                throw new FalhaAutorizacao(400, "RESPONSAVEL_INVALIDO", "O responsável deve ser membro do projeto.");
            }
        }
        return repassar(dominio.patch("requisitos", requisitosUrl, caminhoRequisito(projetoId, requisitoId),
                corpo, ifMatch, chave, null));
    }

    /** Cancela o requisito mediante confirmação. */
    @Tag(name = "Requisitos")
    @Operation(summary = "Criar cancelamento", description = "Papel mínimo: Editor.", parameters = {
            @Parameter(name = "If-Match", in = ParameterIn.HEADER, required = true),
            @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER)})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = ConfirmacaoDoc.class)))
    @ApiResponse(responseCode = "201", description = "Operação concluída")
    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "409", description = "Conflito de estado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "412", description = "ETag divergente", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "428", description = "If-Match ausente", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Serviço indisponível", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @PostMapping("/{requisitoId}/cancelamentos")
    public ResponseEntity<JsonNode> cancelar(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, @RequestBody JsonNode corpo) {
        return repassar(dominio.post("requisitos", requisitosUrl, caminhoRequisito(projetoId, requisitoId)
                + "/cancelamentos", corpo, ifMatch, chave));
    }

    /** Reativa o requisito cancelado. */
    @Tag(name = "Requisitos")
    @Operation(summary = "Criar reativação", description = "Papel mínimo: Editor.", parameters = {
            @Parameter(name = "If-Match", in = ParameterIn.HEADER, required = true),
            @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER)})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(type = "object")))
    @ApiResponse(responseCode = "201", description = "Operação concluída")
    @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "409", description = "Conflito de estado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "412", description = "ETag divergente", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "428", description = "If-Match ausente", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Serviço indisponível", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @PostMapping("/{requisitoId}/reativacoes")
    public ResponseEntity<JsonNode> reativar(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave,
            @RequestBody(required = false) JsonNode corpo) {
        return repassar(dominio.post("requisitos", requisitosUrl, caminhoRequisito(projetoId, requisitoId)
                + "/reativacoes", corpo, ifMatch, chave));
    }

    /** SAGA ainda não terminou (inclui compensação em curso): cancelamento seria recusado. */
    static boolean sagaEmAndamento(String estadoSaga) {
        return estadoSaga != null && !"CONCLUIDA".equals(estadoSaga) && !"COMPENSADA".equals(estadoSaga);
    }

    @Tag(name = "Candidata")
    @Operation(summary = "Obter candidata", description = "Papel mínimo: Leitor. Consulta a candidata e retorna seu ETag.")
    @ApiResponse(responseCode = "200", description = "Operação concluída")
    @ApiResponse(responseCode = "400", description = "Erro 400",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "401", description = "Erro 401",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "403", description = "Erro 403",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "404", description = "Erro 404",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "409", description = "Erro 409",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "429", description = "Erro 429",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Erro 503",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @GetMapping("/{requisitoId}/candidata")
    public ResponseEntity<JsonNode> obterCandidata(@PathVariable UUID projetoId, @PathVariable UUID requisitoId) {
        return repassar(dominio.get("requisitos", requisitosUrl, caminhoRequisito(projetoId, requisitoId) + "/candidata"));
    }

    @Tag(name = "Candidata")
    @Operation(summary = "Editar candidata", description = "Papel mínimo: Editor. Atualiza a candidata.",
            parameters = {@Parameter(name = "If-Match", in = ParameterIn.HEADER, description = "ETag obtido no GET da candidata.", required = true)})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = CandidataPatchDoc.class)))
    @ApiResponse(responseCode = "200", description = "Operação concluída")
    @ApiResponse(responseCode = "400", description = "Erro 400",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "401", description = "Erro 401",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "403", description = "Erro 403",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "404", description = "Erro 404",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "409", description = "Erro 409",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "429", description = "Erro 429",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Erro 503",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "412", description = "Erro 412",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "428", description = "Erro 428",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @PatchMapping("/{requisitoId}/candidata")
    public ResponseEntity<JsonNode> editarCandidata(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch, @RequestBody JsonNode corpo) {
        return repassar(dominio.patch("requisitos", requisitosUrl,
                caminhoRequisito(projetoId, requisitoId) + "/candidata", corpo, ifMatch));
    }

    @Tag(name = "Submissões")
    @Operation(summary = "Criar submissao", description = "Papel mínimo: Editor. Responde 202; a SAGA continua em segundo plano. Acompanhe a submissão pelo Location.",
            parameters = {@Parameter(name = "If-Match", in = ParameterIn.HEADER, description = "ETag obtido no GET da candidata.", required = true), @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER, description = "Qualquer string única por tentativa; repetir a mesma chave com o mesmo pedido devolve o mesmo resultado.", required = true)})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = SubmissaoEntradaDoc.class)))
    @ApiResponse(responseCode = "202", description = "Operação concluída")
    @ApiResponse(responseCode = "400", description = "Erro 400",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "401", description = "Erro 401",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "403", description = "Erro 403",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "404", description = "Erro 404",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "409", description = "Erro 409",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "429", description = "Erro 429",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Erro 503",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "412", description = "Erro 412",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "428", description = "Erro 428",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @PostMapping("/{requisitoId}/submissoes")
    public ResponseEntity<JsonNode> criarSubmissao(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, @RequestBody JsonNode corpo) {
        return repassar(dominio.post("requisitos", requisitosUrl,
                caminhoRequisito(projetoId, requisitoId) + "/submissoes", corpo, ifMatch, chave));
    }

    @Tag(name = "Submissões")
    @Operation(summary = "Listar submissoes", description = "Papel mínimo: Leitor. Lista as submissões.")
    @ApiResponse(responseCode = "200", description = "Operação concluída")
    @ApiResponse(responseCode = "400", description = "Erro 400",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "401", description = "Erro 401",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "403", description = "Erro 403",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "404", description = "Erro 404",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "409", description = "Erro 409",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "429", description = "Erro 429",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Erro 503",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @GetMapping("/{requisitoId}/submissoes")
    public ResponseEntity<JsonNode> listarSubmissoes(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            HttpServletRequest pedido) {
        String query = pedido.getQueryString() == null ? "" : "?" + pedido.getQueryString();
        return repassar(dominio.get("requisitos", requisitosUrl,
                caminhoRequisito(projetoId, requisitoId) + "/submissoes" + query));
    }

    @Tag(name = "Submissões")
    @Operation(summary = "Obter submissao", description = "Papel mínimo: Leitor. Consulta o andamento da submissão.")
    @ApiResponse(responseCode = "200", description = "Operação concluída")
    @ApiResponse(responseCode = "400", description = "Erro 400",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "401", description = "Erro 401",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "403", description = "Erro 403",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "404", description = "Erro 404",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "409", description = "Erro 409",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "429", description = "Erro 429",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Erro 503",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @GetMapping("/{requisitoId}/submissoes/{submissaoId}")
    public ResponseEntity<JsonNode> obterSubmissao(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
                                                    @PathVariable UUID submissaoId) {
        return repassar(dominio.get("requisitos", requisitosUrl,
                caminhoRequisito(projetoId, requisitoId) + "/submissoes/" + submissaoId));
    }

    @Tag(name = "Versões")
    @Operation(summary = "Listar versoes", description = "Papel mínimo: Leitor. Lista as versões.")
    @ApiResponse(responseCode = "200", description = "Operação concluída")
    @ApiResponse(responseCode = "400", description = "Erro 400",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "401", description = "Erro 401",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "403", description = "Erro 403",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "404", description = "Erro 404",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "409", description = "Erro 409",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "429", description = "Erro 429",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Erro 503",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @GetMapping("/{requisitoId}/versoes")
    public ResponseEntity<JsonNode> listarVersoes(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
                                                   HttpServletRequest pedido) {
        String query = pedido.getQueryString() == null ? "" : "?" + pedido.getQueryString();
        return repassar(dominio.get("requisitos", requisitosUrl,
                caminhoRequisito(projetoId, requisitoId) + "/versoes" + query));
    }

    @Tag(name = "Versões")
    @Operation(summary = "Obter versao", description = "Papel mínimo: Leitor. Consulta uma versão.")
    @ApiResponse(responseCode = "200", description = "Operação concluída")
    @ApiResponse(responseCode = "400", description = "Erro 400",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "401", description = "Erro 401",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "403", description = "Erro 403",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "404", description = "Erro 404",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "409", description = "Erro 409",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "429", description = "Erro 429",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Erro 503",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @GetMapping("/{requisitoId}/versoes/{versaoId}")
    public ResponseEntity<JsonNode> obterVersao(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
                                                @PathVariable UUID versaoId) {
        return repassar(dominio.get("requisitos", requisitosUrl,
                caminhoRequisito(projetoId, requisitoId) + "/versoes/" + versaoId));
    }

    @Tag(name = "Publicações")
    @Operation(summary = "Listar publicacoes", description = "Papel mínimo: Leitor. Lista as publicações.")
    @ApiResponse(responseCode = "200", description = "Operação concluída")
    @ApiResponse(responseCode = "400", description = "Erro 400",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "401", description = "Erro 401",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "403", description = "Erro 403",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "404", description = "Erro 404",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "409", description = "Erro 409",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "429", description = "Erro 429",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Erro 503",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @GetMapping("/{requisitoId}/publicacoes")
    public ResponseEntity<JsonNode> listarPublicacoes(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
                                                       HttpServletRequest pedido) {
        String query = pedido.getQueryString() == null ? "" : "?" + pedido.getQueryString();
        return repassar(dominio.get("requisitos", requisitosUrl,
                caminhoRequisito(projetoId, requisitoId) + "/publicacoes" + query));
    }

    @Tag(name = "Publicações")
    @Operation(summary = "Criar publicacao", description = "Papel mínimo: Aprovador. Publica a versão aprovada.",
            parameters = {@Parameter(name = "If-Match", in = ParameterIn.HEADER, description = "ETag obtido no GET da candidata.", required = true), @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER, description = "Qualquer string única por tentativa; repetir a mesma chave com o mesmo pedido devolve o mesmo resultado.", required = true)})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = PublicacaoEntradaDoc.class)))
    @ApiResponse(responseCode = "201", description = "Operação concluída")
    @ApiResponse(responseCode = "400", description = "Erro 400",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "401", description = "Erro 401",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "403", description = "Erro 403",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "404", description = "Erro 404",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "409", description = "Erro 409",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "429", description = "Erro 429",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Erro 503",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "412", description = "Erro 412",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "428", description = "Erro 428",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
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
    @Tag(name = "Requisitos")
    @Operation(summary = "Obter requisito", description = "Papel mínimo: Leitor. Consulta o recurso. Compõe dados de Requisitos e Revisões; indisponibilidade de Revisões gera aviso. O ETag é do requisito, não da candidata.")
    @ApiResponse(responseCode = "200", description = "Operação concluída", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"requisitoId\":\"11111111-1111-4111-8111-111111111111\",\"projetoId\":\"22222222-2222-4222-8222-222222222222\",\"codigo\":\"REQ-42\",\"titulo\":\"Exportar documento de requisitos.\",\"situacao\":\"ATIVO\",\"versaoEmEdicao\":4,\"versaoId\":\"33333333-3333-4333-8333-333333333333\",\"estadoVersao\":\"RASCUNHO\",\"atualizadoEm\":\"2026-10-21T15:00:00Z\",\"_links\":{\"self\":{\"href\":\"/api/v1/gestao/projetos/22222222-2222-4222-8222-222222222222/requisitos/11111111-1111-4111-8111-111111111111\"},\"colecao\":{\"href\":\"/api/v1/gestao/projetos/22222222-2222-4222-8222-222222222222/requisitos\"},\"versoes\":{\"href\":\"/api/v1/gestao/projetos/22222222-2222-4222-8222-222222222222/requisitos/11111111-1111-4111-8111-111111111111/versoes\"},\"candidata\":{\"href\":\"/api/v1/gestao/projetos/22222222-2222-4222-8222-222222222222/requisitos/11111111-1111-4111-8111-111111111111/candidata\"}},\"_actions\":{\"editar-candidata\":{\"href\":\"/api/v1/gestao/projetos/22222222-2222-4222-8222-222222222222/requisitos/11111111-1111-4111-8111-111111111111/candidata\",\"method\":\"PATCH\",\"contentType\":\"application/json\",\"campos\":[\"titulo\",\"conteudo\",\"criteriosAceite\"],\"precondicoes\":[\"If-Match\",\"Candidata em RASCUNHO ou APROVADA\",\"Papel Editor ou superior\"]},\"submeter\":{\"href\":\"/api/v1/gestao/projetos/22222222-2222-4222-8222-222222222222/requisitos/11111111-1111-4111-8111-111111111111/submissoes\",\"method\":\"POST\",\"contentType\":\"application/json\",\"campos\":[\"versaoId\"],\"precondicoes\":[\"If-Match\",\"Idempotency-Key\",\"Candidata em RASCUNHO\",\"Papel Editor ou superior\"]}},\"avisos\":[]}")))
    @ApiResponse(responseCode = "400", description = "Erro 400",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "401", description = "Erro 401",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "403", description = "Erro 403",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "404", description = "Erro 404",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "409", description = "Erro 409",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "429", description = "Erro 429",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Erro 503",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
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
        copiar(origem, visao, "requisitoId", "projetoId", "codigo", "titulo", "situacao", "noLivro", "responsavelId", "epicoId", "tipo", "versaoPublicadaId");

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
                texto(lote, "estado"), texto(submissao, "submissaoId"),
                sagaEmAndamento(texto(submissao, "estadoSaga")), ((PapelAtual.Vinculo) pedido.getAttribute(AutorizacaoProjeto.VINCULO)).papel());
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
