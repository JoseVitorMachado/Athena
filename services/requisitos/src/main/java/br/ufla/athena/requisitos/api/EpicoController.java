package br.ufla.athena.requisitos.api;

import br.ufla.athena.requisitos.aplicacao.EpicoService;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Operações de épicos do projeto. */
@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/epicos")
public class EpicoController {
    private final EpicoService servico;
    public EpicoController(EpicoService servico) { this.servico = servico; }

    @GetMapping
    public Pagina<EpicoService.Resposta> listar(@PathVariable UUID projetoId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = Ordenacao.PADRAO) String sort) {
        if (page < 0 || size < 1 || size > 100) throw new Problema(org.springframework.http.HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Paginação inválida.");
        return servico.listar(projetoId, PageRequest.of(page, size, Ordenacao.de(sort)), caminho(projetoId) + "?page=" + page + "&size=" + size + "&sort=" + sort);
    }
    @PostMapping
    public ResponseEntity<EpicoService.Resposta> criar(@PathVariable UUID projetoId, @RequestBody JsonNode corpo) {
        var e = servico.criar(projetoId, corpo);
        return ResponseEntity.created(URI.create(caminho(projetoId) + "/" + e.resposta().epicoId())).eTag(etag(e)).body(e.resposta());
    }
    @GetMapping("/{epicoId}")
    public ResponseEntity<EpicoService.Resposta> obter(@PathVariable UUID projetoId, @PathVariable UUID epicoId) {
        var e = servico.obter(projetoId, epicoId);
        return ResponseEntity.ok().eTag(etag(e)).body(e.resposta());
    }
    @PatchMapping("/{epicoId}")
    public ResponseEntity<EpicoService.Resposta> alterar(@PathVariable UUID projetoId, @PathVariable UUID epicoId,
            @RequestHeader(value = "If-Match", required = false) String ifMatch, @RequestBody JsonNode corpo) {
        var e = servico.alterar(projetoId, epicoId, ifMatch, corpo);
        return ResponseEntity.ok().eTag(etag(e)).body(e.resposta());
    }
    @DeleteMapping("/{epicoId}")
    public ResponseEntity<Void> excluir(@PathVariable UUID projetoId, @PathVariable UUID epicoId,
            @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        servico.excluir(projetoId, epicoId, ifMatch);
        return ResponseEntity.noContent().build();
    }
    private static String caminho(UUID projeto) { return "/api/v1/projetos/" + projeto + "/epicos"; }
    private static String etag(EpicoService.Encontrado e) { return "\"" + e.revisao() + "\""; }
}
