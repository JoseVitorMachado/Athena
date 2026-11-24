package br.ufla.athena.requisitos.api;

import br.ufla.athena.requisitos.aplicacao.CoordenadorSaga;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/requisitos/{requisitoId}/submissoes")
public class SubmissaoController {
    private final CoordenadorSaga coordenador;

    public SubmissaoController(CoordenadorSaga coordenador) {
        this.coordenador = coordenador;
    }

    @PostMapping
    public ResponseEntity<SubmissaoResposta> criar(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave,
            @Valid @RequestBody SubmissaoEntrada entrada) {
        SubmissaoResposta resposta = coordenador.criar(projetoId, requisitoId, entrada.versaoId(), chave, ifMatch);
        return ResponseEntity.accepted().location(URI.create(caminho(projetoId, requisitoId) + "/" + resposta.submissaoId())).body(resposta);
    }

    @GetMapping
    public Pagina<SubmissaoResposta> listar(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return Pagina.de(coordenador.listar(projetoId, requisitoId, PageRequest.of(page, size)),
                caminho(projetoId, requisitoId) + "?page=" + page + "&size=" + size);
    }

    @GetMapping("/{submissaoId}")
    public SubmissaoResposta obter(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
                                   @PathVariable UUID submissaoId) {
        return coordenador.buscar(projetoId, requisitoId, submissaoId);
    }

    private static String caminho(UUID projeto, UUID requisito) {
        return "/api/v1/projetos/" + projeto + "/requisitos/" + requisito + "/submissoes";
    }
}
