package br.ufla.athena.bff.gestao;

import br.ufla.athena.bff.gestao.docs.*;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Repasse das operações de convites e transferências do destinatário. */
@RestController
@RequestMapping("/api/v1/gestao")
@Tag(name = "Convites")
public class ConvitesGestaoController {
    private final ClienteDominio dominio;
    private final String url;

    /** Configura o cliente do serviço de projetos e acesso. */
    public ConvitesGestaoController(ClienteDominio dominio, AthenaProperties propriedades) {
        this.dominio = dominio;
        this.url = propriedades.servicos().projetosAcesso();
    }

    private static String query(HttpServletRequest pedido) {
        return pedido.getQueryString() == null ? "" : "?" + pedido.getQueryString();
    }

    /** Listar meus convites. */
    @Operation(summary = "Listar meus convites", description = "Papel mínimo: destinatário autenticado. Autorização revalidada no domínio.")
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
    @ApiResponse(responseCode = "412", description = "Erro 412",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "428", description = "Erro 428",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "429", description = "Erro 429",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Erro 503",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @GetMapping("/convites")
    public ResponseEntity<JsonNode> listarConvites(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, HttpServletRequest pedido) {
        return RequisitosGestaoController.repassar(dominio.get("projetos e acesso", url,
                "/api/v1/convites" + query(pedido), ifMatch, chave, authorization));
    }

    /** Aceitar convite. */
    @Operation(summary = "Aceitar convite", description = "Papel mínimo: destinatário autenticado. Autorização revalidada no domínio.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = false,
            content = @Content(schema = @Schema(type = "object")))
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
    @ApiResponse(responseCode = "412", description = "Erro 412",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "428", description = "Erro 428",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "429", description = "Erro 429",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Erro 503",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @PostMapping("/convites/{conviteId}/aceite")
    public ResponseEntity<JsonNode> aceitarConvite(@PathVariable UUID conviteId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave,
            @RequestBody(required = false) JsonNode corpo, HttpServletRequest pedido) {
        return RequisitosGestaoController.repassar(dominio.post("projetos e acesso", url,
                "/api/v1/convites/" + conviteId + "/aceite" + query(pedido), corpo, ifMatch, chave, authorization));
    }

    /** Listar minhas transferências de propriedade. */
    @Operation(summary = "Listar minhas transferências de propriedade", description = "Papel mínimo: destinatário autenticado. Autorização revalidada no domínio.")
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
    @ApiResponse(responseCode = "412", description = "Erro 412",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "428", description = "Erro 428",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "429", description = "Erro 429",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Erro 503",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @GetMapping("/transferencias-propriedade")
    public ResponseEntity<JsonNode> listarTransferencias(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, HttpServletRequest pedido) {
        return RequisitosGestaoController.repassar(dominio.get("projetos e acesso", url,
                "/api/v1/transferencias-propriedade" + query(pedido), ifMatch, chave, authorization));
    }

    /** Aceitar transferência de propriedade. */
    @Operation(summary = "Aceitar transferência de propriedade", description = "Papel mínimo: destinatário autenticado. Autorização revalidada no domínio.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = false,
            content = @Content(schema = @Schema(type = "object")))
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
    @ApiResponse(responseCode = "412", description = "Erro 412",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "428", description = "Erro 428",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "429", description = "Erro 429",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Erro 503",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @PostMapping("/transferencias-propriedade/{transferenciaId}/aceite")
    public ResponseEntity<JsonNode> aceitarTransferencia(@PathVariable UUID transferenciaId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave,
            @RequestBody(required = false) JsonNode corpo, HttpServletRequest pedido) {
        return RequisitosGestaoController.repassar(dominio.post("projetos e acesso", url,
                "/api/v1/transferencias-propriedade/" + transferenciaId + "/aceite" + query(pedido), corpo, ifMatch, chave, authorization));
    }

}
