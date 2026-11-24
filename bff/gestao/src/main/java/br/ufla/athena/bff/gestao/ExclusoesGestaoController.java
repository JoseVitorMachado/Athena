package br.ufla.athena.bff.gestao;

import br.ufla.athena.bff.gestao.docs.ConfirmacaoNomeDoc;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gestao/projetos/{projetoId}/exclusoes")
@Tag(name = "Exclusões")
public class ExclusoesGestaoController {
    private final ClienteDominio dominio;
    private final String url;
    public ExclusoesGestaoController(ClienteDominio dominio, AthenaProperties propriedades) {
        this.dominio = dominio; this.url = propriedades.servicos().projetosAcesso();
    }
    @Operation(summary = "Solicitar exclusão de projeto", parameters = {
        @Parameter(name = "If-Match", in = ParameterIn.HEADER, required = true),
        @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER, required = true)})
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = ConfirmacaoNomeDoc.class)))
    @ApiResponse(responseCode = "202", description = "Exclusão iniciada")
    @PostMapping
    public ResponseEntity<JsonNode> criar(@PathVariable UUID projetoId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, @RequestBody JsonNode corpo) {
        return RequisitosGestaoController.repassar(dominio.post("projetos e acesso", url, caminho(projetoId), corpo, ifMatch, chave, authorization));
    }
    @Operation(summary = "Acompanhar exclusão de projeto")
    @ApiResponse(responseCode = "200", description = "Estado da exclusão")
    @GetMapping("/{exclusaoId}")
    public ResponseEntity<JsonNode> obter(@PathVariable UUID projetoId, @PathVariable UUID exclusaoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return RequisitosGestaoController.repassar(dominio.get("projetos e acesso", url, caminho(projetoId) + "/" + exclusaoId, authorization));
    }
    private String caminho(UUID projetoId) { return "/api/v1/projetos/" + projetoId + "/exclusoes"; }
}
