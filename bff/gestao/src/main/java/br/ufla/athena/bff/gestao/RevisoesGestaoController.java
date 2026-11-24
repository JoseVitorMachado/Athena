package br.ufla.athena.bff.gestao;

import br.ufla.athena.bff.gestao.docs.ProblemaDoc;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.ufla.athena.bff.gestao.docs.DecisaoEntradaDoc;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gestao/projetos/{projetoId}/revisoes")
public class RevisoesGestaoController {
    private final ClienteDominio dominio;
    private final String projetosAcessoUrl;
    private final String revisoesUrl;

    public RevisoesGestaoController(ClienteDominio dominio, AthenaProperties propriedades) {
        this.dominio = dominio;
        this.projetosAcessoUrl = propriedades.servicos().projetosAcesso();
        this.revisoesUrl = propriedades.servicos().revisoes();
    }

    @Tag(name = "Revisões")
    @Operation(summary = "Listar lotes", description = "Papel mínimo: Leitor. Lista os itens disponíveis.")
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
        return RequisitosGestaoController.repassar(dominio.get("revisoes", revisoesUrl, colecao(projetoId) + query));
    }

    @Tag(name = "Revisões")
    @Operation(summary = "Obter lote", description = "Papel mínimo: Leitor. Consulta o recurso.")
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
    @GetMapping("/{loteId}")
    public ResponseEntity<JsonNode> obter(@PathVariable UUID projetoId, @PathVariable UUID loteId) {
        return RequisitosGestaoController.repassar(dominio.get("revisoes", revisoesUrl,
                colecao(projetoId) + "/" + loteId));
    }

    @Tag(name = "Revisões")
    @Operation(summary = "Listar decisoes", description = "Papel mínimo: Leitor. Lista as decisões do lote.")
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
    @GetMapping("/{loteId}/decisoes")
    public ResponseEntity<JsonNode> listarDecisoes(@PathVariable UUID projetoId, @PathVariable UUID loteId,
                                                   HttpServletRequest pedido) {
        String query = pedido.getQueryString() == null ? "" : "?" + pedido.getQueryString();
        return RequisitosGestaoController.repassar(dominio.get("revisoes", revisoesUrl,
                colecao(projetoId) + "/" + loteId + "/decisoes" + query));
    }

    @Tag(name = "Revisões")
    @Operation(summary = "Criar decisao", description = "Papel mínimo: Aprovador. Registra uma decisão para o lote.",
            parameters = {@Parameter(name = "If-Match", in = ParameterIn.HEADER, description = "ETag obtido no GET da lote de revisão.", required = true), @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER, description = "Qualquer string única por tentativa; repetir a mesma chave com o mesmo pedido devolve o mesmo resultado.", required = true)})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = DecisaoEntradaDoc.class)))
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
    @PostMapping("/{loteId}/decisoes")
    public ResponseEntity<JsonNode> decidir(@PathVariable UUID projetoId, @PathVariable UUID loteId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, @RequestBody JsonNode corpo) {
        return RequisitosGestaoController.repassar(dominio.post("revisoes", revisoesUrl,
                colecao(projetoId) + "/" + loteId + "/decisoes", corpo, ifMatch, chave));
    }

    @Tag(name = "Revisões")
    @Operation(summary = "Criar confirmação humana", description = "Papel mínimo: Aprovador. SHA-256 em hexadecimal minúsculo sobre UTF-8, sem espaços: decidir:<loteId>:<versaoId>:<decisao>, com decisão APROVAR, REJEITAR ou SOLICITAR_CORRECOES.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = br.ufla.athena.bff.gestao.docs.ConfirmacaoHumanaDoc.class)))
    @ApiResponse(responseCode = "201", description = "Comprovante criado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "401", description = "Senha inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "429", description = "Muitas tentativas", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Serviço indisponível", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @PostMapping("/{loteId}/confirmacoes-decisao")
    public ResponseEntity<JsonNode> confirmarAcao(@PathVariable UUID projetoId, @PathVariable UUID loteId, @RequestBody(required = false) JsonNode corpo) {
        return ConfirmacaoHumana.emitir(dominio, projetosAcessoUrl, "revisoes", revisoesUrl,
                colecao(projetoId) + "/" + loteId + "/confirmacoes-decisao", corpo);
    }

    private static String colecao(UUID projetoId) {
        return Caminhos.DOMINIO + projetoId + "/lotes";
    }
}
