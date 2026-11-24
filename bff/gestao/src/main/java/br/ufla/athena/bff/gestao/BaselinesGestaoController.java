package br.ufla.athena.bff.gestao;

import br.ufla.athena.bff.gestao.docs.BaselineEntradaDoc;
import br.ufla.athena.bff.gestao.docs.ProblemaDoc;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Repasse das baselines ao serviço de requisitos. */
@RestController
@RequestMapping("/api/v1/gestao/projetos/{projetoId}/baselines")
@Tag(name = "Baselines")
public class BaselinesGestaoController {
    private final ClienteDominio dominio;
    private final String url;
    public BaselinesGestaoController(ClienteDominio dominio, AthenaProperties propriedades) {
        this.dominio = dominio; this.url = propriedades.servicos().requisitos();
    }
    @Operation(summary = "Listar baselines", description = "Papel mínimo: Leitor.")
    @ApiResponse(responseCode = "200", description = "Operação concluída")
    @GetMapping
    public ResponseEntity<JsonNode> listar(@PathVariable UUID projetoId, HttpServletRequest pedido) {
        String query = pedido.getQueryString() == null ? "" : "?" + pedido.getQueryString();
        return RequisitosGestaoController.repassar(dominio.get("requisitos", url, colecao(projetoId) + query));
    }
    @Operation(summary = "Criar baseline", description = "Papel mínimo: Editor.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = BaselineEntradaDoc.class)))
    @ApiResponse(responseCode = "201", description = "Baseline criada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @PostMapping
    public ResponseEntity<JsonNode> criar(@PathVariable UUID projetoId, @RequestBody JsonNode corpo) {
        return RequisitosGestaoController.repassar(dominio.post("requisitos", url, colecao(projetoId), corpo));
    }
    @Operation(summary = "Obter baseline", description = "Papel mínimo: Leitor.")
    @ApiResponse(responseCode = "200", description = "Operação concluída")
    @GetMapping("/{baselineId}")
    public ResponseEntity<JsonNode> obter(@PathVariable UUID projetoId, @PathVariable UUID baselineId) {
        return RequisitosGestaoController.repassar(dominio.get("requisitos", url, colecao(projetoId) + "/" + baselineId));
    }
    /** Consulta adicional ao contrato de requisitos: comparação com o Livro corrente. */
    @Operation(summary = "Comparar baseline com o Livro", description = "Papel mínimo: Leitor.")
    @ApiResponse(responseCode = "200", description = "Comparação concluída")
    @GetMapping("/{baselineId}/comparacao")
    public ResponseEntity<JsonNode> comparar(@PathVariable UUID projetoId, @PathVariable UUID baselineId) {
        return RequisitosGestaoController.repassar(dominio.get("requisitos", url, colecao(projetoId) + "/" + baselineId + "/comparacao"));
    }
    private static String colecao(UUID projetoId) { return Caminhos.DOMINIO + projetoId + "/baselines"; }
}
