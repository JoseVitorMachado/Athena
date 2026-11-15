package br.ufla.athena.revisoes.api;

import br.ufla.athena.revisoes.aplicacao.LoteService;
import br.ufla.athena.revisoes.aplicacao.LoteService.LoteComRevisao;
import br.ufla.athena.revisoes.dominio.EstadoLote;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Operações de docs/api/revisoes.yaml implementadas nesta etapa. */
@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/lotes")
public class LoteController {

    private final LoteService servico;

    public LoteController(LoteService servico) {
        this.servico = servico;
    }

    /** listarLotes */
    @GetMapping
    public Pagina<LoteResposta> listar(
            @PathVariable UUID projetoId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = Ordenacao.PADRAO) String sort,
            @RequestParam(required = false) UUID requisitoId,
            @RequestParam(required = false) EstadoLote estado) {
        String self = colecao(projetoId) + "?page=" + page + "&size=" + size + "&sort=" + sort;
        return Pagina.de(
                servico.listar(projetoId, requisitoId, estado, PageRequest.of(page, size, Ordenacao.de(sort))),
                self);
    }

    /** obterLote: o ETag é a revisão técnica do lote, usada no If-Match da decisão. */
    @GetMapping("/{loteId}")
    public ResponseEntity<LoteResposta> obter(@PathVariable UUID projetoId, @PathVariable UUID loteId) {
        LoteComRevisao encontrado = servico.buscar(projetoId, loteId).orElseThrow(() ->
                new Problema(HttpStatus.NOT_FOUND, "LOTE_NAO_ENCONTRADO", "Lote não encontrado."));
        return ResponseEntity.ok().eTag(LoteService.etag(encontrado.revisao())).body(encontrado.lote());
    }

    /** criarDecisao: 201 com Location; exige If-Match (428 sem ele, 412 se desatualizado). */
    @PostMapping("/{loteId}/decisoes")
    public ResponseEntity<DecisaoResposta> decidir(
            @PathVariable UUID projetoId,
            @PathVariable UUID loteId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @Valid @RequestBody DecisaoEntrada entrada) {
        DecisaoResposta decisao = servico.decidir(projetoId, loteId, ifMatch, entrada);
        URI location = URI.create(colecao(projetoId) + "/" + loteId + "/decisoes/" + decisao.decisaoId());
        return ResponseEntity.created(location).body(decisao);
    }

    /** listarDecisoes: um lote tem no máximo uma decisão final. */
    @GetMapping("/{loteId}/decisoes")
    public Pagina<DecisaoResposta> decisoes(@PathVariable UUID projetoId, @PathVariable UUID loteId) {
        List<DecisaoResposta> itens = servico.decisaoDoLote(projetoId, loteId).map(List::of).orElse(List.of());
        return Pagina.de(new PageImpl<>(itens, PageRequest.of(0, 20), itens.size()),
                colecao(projetoId) + "/" + loteId + "/decisoes");
    }

    private static String colecao(UUID projetoId) {
        return "/api/v1/projetos/" + projetoId + "/lotes";
    }
}
