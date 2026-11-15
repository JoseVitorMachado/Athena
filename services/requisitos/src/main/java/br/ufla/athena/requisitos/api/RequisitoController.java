package br.ufla.athena.requisitos.api;

import br.ufla.athena.requisitos.aplicacao.RequisitoService;
import br.ufla.athena.requisitos.aplicacao.RequisitoService.Encontrado;
import br.ufla.athena.requisitos.dominio.SituacaoRequisito;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Operações de docs/api/requisitos.yaml implementadas nesta etapa. */
@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/requisitos")
public class RequisitoController {

    private final RequisitoService servico;

    public RequisitoController(RequisitoService servico) {
        this.servico = servico;
    }

    /** listarRequisitos: cancelados ficam fora por padrão. */
    @GetMapping
    public Pagina<RequisitoResposta> listar(
            @PathVariable UUID projetoId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = Ordenacao.PADRAO) String sort,
            @RequestParam(defaultValue = "ATIVO") SituacaoRequisito situacao) {
        String self = caminhoColecao(projetoId) + "?page=" + page + "&size=" + size + "&sort=" + sort;
        return servico.listar(projetoId, situacao, PageRequest.of(page, size, Ordenacao.de(sort)), self);
    }

    /** criarRequisito: 201 com Location para o recurso criado. */
    @PostMapping
    public ResponseEntity<RequisitoResposta> criar(@PathVariable UUID projetoId,
                                                   @Valid @RequestBody RequisitoEntrada entrada) {
        Encontrado criado = servico.criar(projetoId, entrada);
        URI location = URI.create(caminhoColecao(projetoId) + "/" + criado.requisito().requisitoId());
        return ResponseEntity.created(location).eTag(etag(criado)).body(criado.requisito());
    }

    /** obterRequisito: o ETag é a revisão técnica do requisito. */
    @GetMapping("/{requisitoId}")
    public ResponseEntity<RequisitoResposta> obter(@PathVariable UUID projetoId, @PathVariable UUID requisitoId) {
        Encontrado encontrado = servico.buscar(projetoId, requisitoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisito não encontrado"));
        return ResponseEntity.ok().eTag(etag(encontrado)).body(encontrado.requisito());
    }

    private static String caminhoColecao(UUID projetoId) {
        return "/api/v1/projetos/" + projetoId + "/requisitos";
    }

    private static String etag(Encontrado encontrado) {
        return "\"" + encontrado.revisao() + "\"";
    }
}
