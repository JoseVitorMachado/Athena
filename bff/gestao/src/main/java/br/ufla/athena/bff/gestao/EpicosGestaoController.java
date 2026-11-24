package br.ufla.athena.bff.gestao;

import br.ufla.athena.bff.gestao.docs.EpicoEntradaDoc;
import br.ufla.athena.bff.gestao.docs.EpicoPatchDoc;
import br.ufla.athena.bff.gestao.docs.ProblemaDoc;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Repasse das operações de épicos ao serviço de requisitos. */
@RestController
@RequestMapping("/api/v1/gestao/projetos/{projetoId}/epicos")
@Tag(name = "Épicos")
public class EpicosGestaoController {
    private final ClienteDominio dominio;
    private final String url;

    /** Configura o serviço de requisitos. */
    public EpicosGestaoController(ClienteDominio dominio, AthenaProperties propriedades) {
        this.dominio = dominio;
        this.url = propriedades.servicos().requisitos();
    }

    /** Lista os épicos do projeto. */
    @Operation(summary = "Listar épicos", description = "Papel mínimo: Leitor.")
    @ApiResponse(responseCode = "200", description = "Operação concluída")
    @GetMapping
    public ResponseEntity<JsonNode> listar(@PathVariable UUID projetoId, HttpServletRequest pedido) {
        String query = pedido.getQueryString() == null ? "" : "?" + pedido.getQueryString();
        return RequisitosGestaoController.repassar(dominio.get("requisitos", url, colecao(projetoId) + query));
    }

    /** Cria um épico. */
    @Operation(summary = "Criar épico", description = "Papel mínimo: Editor.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = EpicoEntradaDoc.class)))
    @ApiResponse(responseCode = "201", description = "Operação concluída")
    @ApiResponse(responseCode = "400", description = "Erro 400", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @PostMapping
    public ResponseEntity<JsonNode> criar(@PathVariable UUID projetoId, @RequestBody JsonNode corpo) {
        return RequisitosGestaoController.repassar(dominio.post("requisitos", url, colecao(projetoId), corpo));
    }

    /** Consulta um épico. */
    @Operation(summary = "Obter épico", description = "Papel mínimo: Leitor.")
    @ApiResponse(responseCode = "200", description = "Operação concluída")
    @GetMapping("/{epicoId}")
    public ResponseEntity<JsonNode> obter(@PathVariable UUID projetoId, @PathVariable UUID epicoId) {
        return RequisitosGestaoController.repassar(dominio.get("requisitos", url, colecao(projetoId) + "/" + epicoId));
    }

    /** Atualiza um épico. */
    @Operation(summary = "Editar épico", description = "Papel mínimo: Editor.",
            parameters = @Parameter(name = "If-Match", in = ParameterIn.HEADER, required = true))
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = EpicoPatchDoc.class)))
    @ApiResponse(responseCode = "200", description = "Operação concluída")
    @PatchMapping("/{epicoId}")
    public ResponseEntity<JsonNode> editar(@PathVariable UUID projetoId, @PathVariable UUID epicoId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch, @RequestBody JsonNode corpo) {
        return RequisitosGestaoController.repassar(dominio.patch("requisitos", url,
                colecao(projetoId) + "/" + epicoId, corpo, ifMatch));
    }

    /** Exclui um épico. */
    @Operation(summary = "Excluir épico", description = "Papel mínimo: Editor.",
            parameters = @Parameter(name = "If-Match", in = ParameterIn.HEADER, required = true))
    @ApiResponse(responseCode = "204", description = "Operação concluída")
    @DeleteMapping("/{epicoId}")
    public ResponseEntity<JsonNode> excluir(@PathVariable UUID projetoId, @PathVariable UUID epicoId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch) {
        return RequisitosGestaoController.repassar(dominio.delete("requisitos", url,
                colecao(projetoId) + "/" + epicoId, ifMatch));
    }

    private static String colecao(UUID projetoId) {
        return Caminhos.DOMINIO + projetoId + "/epicos";
    }
}
