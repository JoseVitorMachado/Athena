package br.ufla.athena.bff.gestao;

import br.ufla.athena.bff.gestao.docs.NotificacaoPatchDoc;
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

/** Repasse das notificações do destinatário autenticado. */
@RestController
@RequestMapping("/api/v1/gestao/notificacoes")
@Tag(name = "Notificações")
public class NotificacoesGestaoController {
    private final ClienteDominio dominio;
    private final String url;

    /** Configura o serviço de projetos e acesso. */
    public NotificacoesGestaoController(ClienteDominio dominio, AthenaProperties propriedades) {
        this.dominio = dominio;
        this.url = propriedades.servicos().projetosAcesso();
    }

    /** Lista as notificações do destinatário. */
    @Operation(summary = "Listar notificações", parameters = {
            @Parameter(name = "page", in = ParameterIn.QUERY),
            @Parameter(name = "size", in = ParameterIn.QUERY),
            @Parameter(name = "lida", in = ParameterIn.QUERY)})
    @ApiResponse(responseCode = "200", description = "Operação concluída")
    @ApiResponse(responseCode = "503", description = "Serviço indisponível", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @GetMapping
    public ResponseEntity<JsonNode> listar(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, HttpServletRequest pedido) {
        String query = pedido.getQueryString() == null ? "" : "?" + pedido.getQueryString();
        return RequisitosGestaoController.repassar(dominio.get("projetos e acesso", url,
                "/api/v1/notificacoes" + query, ifMatch, chave, authorization));
    }

    /** Atualiza a leitura de uma notificação do destinatário. */
    @Operation(summary = "Editar notificação", description = "Somente o destinatário pode alterar a leitura.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = NotificacaoPatchDoc.class)))
    @ApiResponse(responseCode = "200", description = "Notificação atualizada")
    @ApiResponse(responseCode = "503", description = "Serviço indisponível", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @PatchMapping("/{notificacaoId}")
    public ResponseEntity<JsonNode> editar(@PathVariable UUID notificacaoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, @RequestBody JsonNode corpo) {
        return RequisitosGestaoController.repassar(dominio.patch("projetos e acesso", url,
                "/api/v1/notificacoes/" + notificacaoId, corpo, ifMatch, chave, authorization));
    }
}
