package br.ufla.athena.bff.gestao;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gestao/projetos/{projetoId}")
public class RastreabilidadeGestaoController {

    private final ClienteDominio dominio;
    private final String rastreabilidadeUrl;

    public RastreabilidadeGestaoController(ClienteDominio dominio, AthenaProperties propriedades) {
        this.dominio = dominio;
        this.rastreabilidadeUrl = propriedades.servicos().rastreabilidade();
    }

    @GetMapping("/relacoes")
    public ResponseEntity<JsonNode> listar(@PathVariable UUID projetoId, HttpServletRequest pedido) {
        String query = pedido.getQueryString() == null ? "" : "?" + pedido.getQueryString();
        return RequisitosGestaoController.repassar(dominio.get("rastreabilidade", rastreabilidadeUrl,
                colecao(projetoId) + query));
    }

    @PostMapping("/relacoes")
    public ResponseEntity<JsonNode> criar(@PathVariable UUID projetoId, @RequestBody JsonNode corpo) {
        return RequisitosGestaoController.repassar(dominio.post("rastreabilidade", rastreabilidadeUrl,
                colecao(projetoId), corpo));
    }

    @GetMapping("/relacoes/{relacaoId}")
    public ResponseEntity<JsonNode> obter(@PathVariable UUID projetoId, @PathVariable UUID relacaoId) {
        return RequisitosGestaoController.repassar(dominio.get("rastreabilidade", rastreabilidadeUrl,
                relacao(projetoId, relacaoId)));
    }

    @PatchMapping("/relacoes/{relacaoId}")
    public ResponseEntity<JsonNode> editar(@PathVariable UUID projetoId, @PathVariable UUID relacaoId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestBody JsonNode corpo) {
        return RequisitosGestaoController.repassar(dominio.patch("rastreabilidade", rastreabilidadeUrl,
                relacao(projetoId, relacaoId), corpo, ifMatch));
    }

    @DeleteMapping("/relacoes/{relacaoId}")
    public ResponseEntity<JsonNode> remover(@PathVariable UUID projetoId, @PathVariable UUID relacaoId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch) {
        return RequisitosGestaoController.repassar(dominio.delete("rastreabilidade", rastreabilidadeUrl,
                relacao(projetoId, relacaoId), ifMatch));
    }

    @GetMapping("/mapa")
    public ResponseEntity<JsonNode> mapa(@PathVariable UUID projetoId) {
        return RequisitosGestaoController.repassar(dominio.get("rastreabilidade", rastreabilidadeUrl,
                Caminhos.DOMINIO + projetoId + "/mapa"));
    }

    private static String colecao(UUID projetoId) {
        return Caminhos.DOMINIO + projetoId + "/relacoes";
    }

    private static String relacao(UUID projetoId, UUID relacaoId) {
        return colecao(projetoId) + "/" + relacaoId;
    }
}
