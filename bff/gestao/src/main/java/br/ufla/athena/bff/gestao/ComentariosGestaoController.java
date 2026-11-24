package br.ufla.athena.bff.gestao;

import br.ufla.athena.bff.gestao.docs.ComentarioEntradaDoc;
import br.ufla.athena.bff.gestao.docs.ComentarioPatchDoc;
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

/** Repasse dos comentários de requisitos. */
@RestController
@RequestMapping("/api/v1/gestao/projetos/{projetoId}/requisitos/{requisitoId}/comentarios")
@Tag(name = "Comentários")
public class ComentariosGestaoController {
    private final ClienteDominio dominio;
    private final PapelAtual papelAtual;
    private final String url;

    /** Configura os serviços de requisitos e de consulta de membros. */
    public ComentariosGestaoController(ClienteDominio dominio, AthenaProperties propriedades, PapelAtual papelAtual) {
        this.dominio = dominio;
        this.papelAtual = papelAtual;
        this.url = propriedades.servicos().requisitos();
    }

    /** Lista os comentários do requisito. */
    @Operation(summary = "Listar comentários", description = "Papel mínimo: Repórter.", parameters = {
            @Parameter(name = "page", in = ParameterIn.QUERY),
            @Parameter(name = "size", in = ParameterIn.QUERY),
            @Parameter(name = "resolvido", in = ParameterIn.QUERY)})
    @ApiResponse(responseCode = "200", description = "Operação concluída")
    @ApiResponse(responseCode = "503", description = "Serviço indisponível", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @GetMapping
    public ResponseEntity<JsonNode> listar(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            HttpServletRequest pedido) {
        String query = pedido.getQueryString() == null ? "" : "?" + pedido.getQueryString();
        return RequisitosGestaoController.repassar(dominio.get("requisitos", url, caminho(projetoId, requisitoId) + query));
    }

    /** Cria um comentário após validar os membros mencionados. */
    @Operation(summary = "Criar comentário", description = "Papel mínimo: Repórter; até 20 menções a membros do projeto.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = ComentarioEntradaDoc.class)))
    @ApiResponse(responseCode = "201", description = "Comentário criado")
    @ApiResponse(responseCode = "400", description = "Menção inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @ApiResponse(responseCode = "503", description = "Serviço indisponível", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @PostMapping
    public ResponseEntity<JsonNode> criar(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, @RequestBody JsonNode corpo) {
        if (corpo.has("mencoes")) {
            JsonNode mencoes = corpo.get("mencoes");
            if (!mencoes.isArray() || mencoes.size() > 20) {
                throw mencaoInvalida();
            }
            for (JsonNode mencao : mencoes) {
                String usuarioId;
                try {
                    if (!mencao.isTextual()) throw new IllegalArgumentException();
                    String valor = mencao.asText();
                    usuarioId = UUID.fromString(valor).toString();
                    if (!usuarioId.equalsIgnoreCase(valor)) throw new IllegalArgumentException();
                } catch (IllegalArgumentException e) {
                    throw mencaoInvalida();
                }
                try {
                    papelAtual.consultar(projetoId.toString(), usuarioId);
                } catch (FalhaAutorizacao e) {
                    if (e.status() != 403) throw e;
                    throw mencaoInvalida();
                }
            }
        }
        return RequisitosGestaoController.repassar(dominio.post("requisitos", url, caminho(projetoId, requisitoId), corpo, ifMatch, chave));
    }

    /** Edita ou resolve um comentário. */
    @Operation(summary = "Editar comentário", description = "Papel mínimo: Repórter; o domínio valida a autoria e a operação.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(schema = @Schema(implementation = ComentarioPatchDoc.class)))
    @ApiResponse(responseCode = "200", description = "Comentário atualizado")
    @ApiResponse(responseCode = "503", description = "Serviço indisponível", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemaDoc.class)))
    @PatchMapping("/{comentarioId}")
    public ResponseEntity<JsonNode> editar(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @PathVariable UUID comentarioId, @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, @RequestBody JsonNode corpo) {
        return RequisitosGestaoController.repassar(dominio.patch("requisitos", url,
                caminho(projetoId, requisitoId) + "/" + comentarioId, corpo, ifMatch, chave, null));
    }

    private static String caminho(UUID projetoId, UUID requisitoId) {
        return Caminhos.DOMINIO + projetoId + "/requisitos/" + requisitoId + "/comentarios";
    }

    private static FalhaAutorizacao mencaoInvalida() {
        return new FalhaAutorizacao(400, "MENCAO_INVALIDA", "Só é possível mencionar membros do projeto.");
    }
}
