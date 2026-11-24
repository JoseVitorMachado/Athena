package br.ufla.athena.requisitos.api;

import br.ufla.athena.requisitos.aplicacao.BaselineService;
import br.ufla.athena.requisitos.aplicacao.ComparacaoBaseline;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Operações de baselines do projeto. */
@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/baselines")
public class BaselineController {
    private final BaselineService servico;
    public BaselineController(BaselineService servico) { this.servico = servico; }

    @GetMapping
    public Pagina<BaselineService.Resumo> listar(@PathVariable UUID projetoId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) throw new Problema(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Paginação inválida.");
        return Pagina.de(servico.listar(projetoId, PageRequest.of(page, size)), caminho(projetoId) + "?page=" + page + "&size=" + size);
    }
    @PostMapping
    public ResponseEntity<BaselineService.Resposta> criar(@PathVariable UUID projetoId, @RequestBody JsonNode corpo) {
        var baseline = servico.criar(projetoId, corpo);
        return ResponseEntity.created(URI.create(caminho(projetoId) + "/" + baseline.baselineId())).eTag("\"0\"").body(baseline);
    }
    @GetMapping("/{baselineId}")
    public ResponseEntity<BaselineService.Resposta> obter(@PathVariable UUID projetoId, @PathVariable UUID baselineId) {
        return ResponseEntity.ok().eTag("\"0\"").body(servico.obter(projetoId, baselineId));
    }
    /** Compara a baseline ao Livro corrente; operação adicional ao contrato de requisitos. */
    @GetMapping("/{baselineId}/comparacao")
    public ComparacaoBaseline.Resultado comparar(@PathVariable UUID projetoId, @PathVariable UUID baselineId) {
        return servico.comparar(projetoId, baselineId);
    }
    private static String caminho(UUID projeto) { return "/api/v1/projetos/" + projeto + "/baselines"; }
}
