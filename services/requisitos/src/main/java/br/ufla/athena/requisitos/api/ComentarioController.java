package br.ufla.athena.requisitos.api;

import br.ufla.athena.requisitos.aplicacao.ComentarioService;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Operações de comentários de um requisito. */
@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/requisitos/{requisitoId}/comentarios")
public class ComentarioController {
    private final ComentarioService servico;

    public ComentarioController(ComentarioService servico) { this.servico = servico; }

    /** Lista comentários em ordem de criação. */
    @GetMapping
    public Pagina<ComentarioResposta> listar(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean resolvido) {
        if (page < 0 || size < 1 || size > 100) throw new Problema(org.springframework.http.HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Paginação inválida.");
        String caminho = caminho(projetoId, requisitoId) + "?page=" + page + "&size=" + size
                + (resolvido == null ? "" : "&resolvido=" + resolvido);
        return Pagina.de(servico.listar(projetoId, requisitoId, resolvido, PageRequest.of(page, size)), caminho);
    }

    /** Cria um comentário na versão informada. */
    @PostMapping
    public ResponseEntity<ComentarioResposta> criar(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestBody JsonNode entrada) {
        ComentarioResposta criado = servico.criar(projetoId, requisitoId, entrada);
        return ResponseEntity.created(URI.create(caminho(projetoId, requisitoId) + "/" + criado.comentarioId())).body(criado);
    }

    /** Altera texto ou resolução de um comentário. */
    @PatchMapping("/{comentarioId}")
    public ComentarioResposta alterar(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @PathVariable UUID comentarioId, @RequestBody JsonNode entrada) {
        return servico.alterar(projetoId, requisitoId, comentarioId, entrada);
    }

    private static String caminho(UUID projeto, UUID requisito) {
        return "/api/v1/projetos/" + projeto + "/requisitos/" + requisito + "/comentarios";
    }
}
