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

/** Repasse das operações de membros e acesso ao projeto. */
@RestController
@RequestMapping("/api/v1/gestao/projetos/{projetoId}")
@Tag(name = "Membros")
public class MembrosGestaoController {
    private final ClienteDominio dominio;
    private final String url;

    /** Configura o cliente do serviço de projetos e acesso. */
    public MembrosGestaoController(ClienteDominio dominio, AthenaProperties propriedades) {
        this.dominio = dominio;
        this.url = propriedades.servicos().projetosAcesso();
    }

    private static String query(HttpServletRequest pedido) {
        return pedido.getQueryString() == null ? "" : "?" + pedido.getQueryString();
    }

    /** Listar membros. */
    @Operation(summary = "Listar membros", description = "Papel mínimo: Leitor. Autorização revalidada no domínio.")
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
    @GetMapping("/membros")
    public ResponseEntity<JsonNode> listarMembros(@PathVariable UUID projetoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, HttpServletRequest pedido) {
        return RequisitosGestaoController.repassar(dominio.get("projetos e acesso", url,
                "/api/v1/projetos/" + projetoId + "/membros" + query(pedido), ifMatch, chave, authorization));
    }

    /** Editar membro. */
    @Operation(summary = "Editar membro", description = "Papel mínimo: Mantenedor. Autorização revalidada no domínio.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = MembroPatchDoc.class)))
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
    @PatchMapping("/membros/{membroId}")
    public ResponseEntity<JsonNode> editarMembro(@PathVariable UUID projetoId,
            @PathVariable UUID membroId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave,
            @RequestBody JsonNode corpo, HttpServletRequest pedido) {
        return RequisitosGestaoController.repassar(dominio.patch("projetos e acesso", url,
                "/api/v1/projetos/" + projetoId + "/membros/" + membroId + query(pedido), corpo, ifMatch, chave, authorization));
    }

    /** Remover membro. */
    @Operation(summary = "Remover membro", description = "Papel mínimo: Leitor. Autorização revalidada no domínio.")
    @ApiResponse(responseCode = "204", description = "Operação concluída")
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
    @DeleteMapping("/membros/{membroId}")
    public ResponseEntity<JsonNode> removerMembro(@PathVariable UUID projetoId,
            @PathVariable UUID membroId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, HttpServletRequest pedido) {
        return RequisitosGestaoController.repassar(dominio.delete("projetos e acesso", url,
                "/api/v1/projetos/" + projetoId + "/membros/" + membroId + query(pedido), ifMatch, chave, authorization));
    }

    /** Listar histórico de membros. */
    @Operation(summary = "Listar histórico de membros", description = "Papel mínimo: Leitor. Autorização revalidada no domínio.")
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
    @GetMapping("/historico-membros")
    public ResponseEntity<JsonNode> listarHistorico(@PathVariable UUID projetoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, HttpServletRequest pedido) {
        return RequisitosGestaoController.repassar(dominio.get("projetos e acesso", url,
                "/api/v1/projetos/" + projetoId + "/historico-membros" + query(pedido), ifMatch, chave, authorization));
    }

    /** Listar convites do projeto. */
    @Operation(summary = "Listar convites do projeto", description = "Papel mínimo: Mantenedor. Autorização revalidada no domínio.")
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
    public ResponseEntity<JsonNode> listarConvites(@PathVariable UUID projetoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, HttpServletRequest pedido) {
        return RequisitosGestaoController.repassar(dominio.get("projetos e acesso", url,
                "/api/v1/projetos/" + projetoId + "/convites" + query(pedido), ifMatch, chave, authorization));
    }

    /** Criar convite. */
    @Operation(summary = "Criar convite", description = "Papel mínimo: Mantenedor. Autorização revalidada no domínio.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = ConviteEntradaDoc.class)))
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
    @PostMapping("/convites")
    public ResponseEntity<JsonNode> criarConvite(@PathVariable UUID projetoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave,
            @RequestBody JsonNode corpo, HttpServletRequest pedido) {
        return RequisitosGestaoController.repassar(dominio.post("projetos e acesso", url,
                "/api/v1/projetos/" + projetoId + "/convites" + query(pedido), corpo, ifMatch, chave, authorization));
    }

    /** Remover convite. */
    @Operation(summary = "Remover convite", description = "Papel mínimo: Mantenedor. Autorização revalidada no domínio.")
    @ApiResponse(responseCode = "204", description = "Operação concluída")
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
    @DeleteMapping("/convites/{conviteId}")
    public ResponseEntity<JsonNode> removerConvite(@PathVariable UUID projetoId,
            @PathVariable UUID conviteId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, HttpServletRequest pedido) {
        return RequisitosGestaoController.repassar(dominio.delete("projetos e acesso", url,
                "/api/v1/projetos/" + projetoId + "/convites/" + conviteId + query(pedido), ifMatch, chave, authorization));
    }

    /** Criar transferência de propriedade. */
    @Operation(summary = "Criar transferência de propriedade", description = "Papel mínimo: Owner. Autorização revalidada no domínio.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = TransferenciaEntradaDoc.class)))
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
    @PostMapping("/transferencias-propriedade")
    public ResponseEntity<JsonNode> criarTransferencia(@PathVariable UUID projetoId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave,
            @RequestBody JsonNode corpo, HttpServletRequest pedido) {
        return RequisitosGestaoController.repassar(dominio.post("projetos e acesso", url,
                "/api/v1/projetos/" + projetoId + "/transferencias-propriedade" + query(pedido), corpo, ifMatch, chave, authorization));
    }

}
