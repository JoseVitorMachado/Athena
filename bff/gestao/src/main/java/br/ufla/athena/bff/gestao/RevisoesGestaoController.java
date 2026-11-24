package br.ufla.athena.bff.gestao;

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
    private final String revisoesUrl;

    public RevisoesGestaoController(ClienteDominio dominio, AthenaProperties propriedades) {
        this.dominio = dominio;
        this.revisoesUrl = propriedades.servicos().revisoes();
    }

    @GetMapping
    public ResponseEntity<JsonNode> listar(@PathVariable UUID projetoId, HttpServletRequest pedido) {
        String query = pedido.getQueryString() == null ? "" : "?" + pedido.getQueryString();
        return RequisitosGestaoController.repassar(dominio.get("revisoes", revisoesUrl, colecao(projetoId) + query));
    }

    @GetMapping("/{loteId}")
    public ResponseEntity<JsonNode> obter(@PathVariable UUID projetoId, @PathVariable UUID loteId) {
        return RequisitosGestaoController.repassar(dominio.get("revisoes", revisoesUrl,
                colecao(projetoId) + "/" + loteId));
    }

    @GetMapping("/{loteId}/decisoes")
    public ResponseEntity<JsonNode> listarDecisoes(@PathVariable UUID projetoId, @PathVariable UUID loteId,
                                                   HttpServletRequest pedido) {
        String query = pedido.getQueryString() == null ? "" : "?" + pedido.getQueryString();
        return RequisitosGestaoController.repassar(dominio.get("revisoes", revisoesUrl,
                colecao(projetoId) + "/" + loteId + "/decisoes" + query));
    }

    @PostMapping("/{loteId}/decisoes")
    public ResponseEntity<JsonNode> decidir(@PathVariable UUID projetoId, @PathVariable UUID loteId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave, @RequestBody JsonNode corpo) {
        return RequisitosGestaoController.repassar(dominio.post("revisoes", revisoesUrl,
                colecao(projetoId) + "/" + loteId + "/decisoes", corpo, ifMatch, chave));
    }

    private static String colecao(UUID projetoId) {
        return Caminhos.DOMINIO + projetoId + "/lotes";
    }
}
