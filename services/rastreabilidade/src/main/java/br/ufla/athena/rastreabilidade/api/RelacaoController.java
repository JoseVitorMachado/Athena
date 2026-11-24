package br.ufla.athena.rastreabilidade.api;

import br.ufla.athena.rastreabilidade.aplicacao.RelacaoService;
import br.ufla.athena.rastreabilidade.dominio.Relacao;
import br.ufla.athena.rastreabilidade.dominio.RelacaoRepository;
import br.ufla.athena.rastreabilidade.dominio.TipoRelacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Operações de docs/api/rastreabilidade.yaml implementadas nesta etapa. */
@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/relacoes")
public class RelacaoController {

    private final RelacaoService servico;
    private final RelacaoRepository repositorio;

    public RelacaoController(RelacaoService servico, RelacaoRepository repositorio) {
        this.servico = servico;
        this.repositorio = repositorio;
    }

    /** listarRelacoes: requisitoId casa com qualquer um dos extremos. */
    @GetMapping
    public Pagina<RelacaoResposta> listar(
            @PathVariable UUID projetoId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = Ordenacao.PADRAO) String sort,
            @RequestParam(required = false) UUID requisitoId,
            @RequestParam(required = false) TipoRelacao tipo) {
        String self = colecao(projetoId) + "?page=" + page + "&size=" + size + "&sort=" + sort;
        return Pagina.de(
                servico.listar(projetoId, requisitoId, tipo, PageRequest.of(page, size, Ordenacao.de(sort)))
                        .map(RelacaoResposta::de),
                self);
    }

    /** criarRelacao: 201 com Location; duplicata responde 409 RELACAO_DUPLICADA. */
    @PostMapping
    public ResponseEntity<RelacaoResposta> criar(@PathVariable UUID projetoId,
                                                 @Valid @RequestBody RelacaoEntrada entrada) {
        Relacao relacao = servico.criar(projetoId, entrada);
        return ResponseEntity.created(URI.create(colecao(projetoId) + "/" + relacao.getId()))
                .eTag(etag(relacao))
                .body(RelacaoResposta.de(relacao));
    }

    /** obterRelacao */
    @GetMapping("/{relacaoId}")
    public ResponseEntity<RelacaoResposta> obter(@PathVariable UUID projetoId, @PathVariable UUID relacaoId) {
        Relacao relacao = repositorio.findByIdAndProjetoId(relacaoId, projetoId).orElseThrow(() ->
                new Problema(HttpStatus.NOT_FOUND, "RELACAO_NAO_ENCONTRADA", "Relação não encontrada."));
        return ResponseEntity.ok().eTag(etag(relacao)).body(RelacaoResposta.de(relacao));
    }

    @PatchMapping("/{relacaoId}")
    public ResponseEntity<RelacaoResposta> editar(@PathVariable UUID projetoId, @PathVariable UUID relacaoId,
            @RequestHeader(value = "If-Match", required = false) String ifMatch, @RequestBody Map<String, Object> patch) {
        Relacao relacao = servico.editar(projetoId, relacaoId, ifMatch, patch);
        return ResponseEntity.ok().eTag(etag(relacao)).body(RelacaoResposta.de(relacao));
    }

    @DeleteMapping("/{relacaoId}")
    public ResponseEntity<Void> remover(@PathVariable UUID projetoId, @PathVariable UUID relacaoId,
            @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        servico.remover(projetoId, relacaoId, ifMatch);
        return ResponseEntity.noContent().build();
    }

    private static String colecao(UUID projetoId) {
        return "/api/v1/projetos/" + projetoId + "/relacoes";
    }

    private static String etag(Relacao relacao) {
        return "\"" + relacao.getRevisao() + "\"";
    }
}
