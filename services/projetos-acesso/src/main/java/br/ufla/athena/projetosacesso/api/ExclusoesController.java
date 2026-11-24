package br.ufla.athena.projetosacesso.api;

import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/exclusoes")
public class ExclusoesController {
    private final IdentidadeController identidade;
    private final ServicoExclusao exclusoes;
    public ExclusoesController(IdentidadeController identidade, ServicoExclusao exclusoes) {
        this.identidade = identidade; this.exclusoes = exclusoes;
    }
    record ConfirmacaoNome(String confirmacaoNome) { }
    @PostMapping
    public ResponseEntity<ServicoExclusao.Exclusao> criar(@PathVariable UUID projetoId,
            @RequestHeader(value = "Authorization", required = false) String bearer,
            @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave,
            @RequestBody ConfirmacaoNome entrada) {
        var exclusao = exclusoes.criar(projetoId, identidade.usuarioAutenticado(bearer), ifMatch, chave, entrada.confirmacaoNome());
        return ResponseEntity.accepted().location(URI.create("/api/v1/projetos/" + projetoId + "/exclusoes/" + exclusao.exclusaoId())).body(exclusao);
    }
    @GetMapping("/{exclusaoId}")
    public ServicoExclusao.Exclusao obter(@PathVariable UUID projetoId, @PathVariable UUID exclusaoId,
            @RequestHeader(value = "Authorization", required = false) String bearer) {
        return exclusoes.obter(projetoId, exclusaoId, identidade.usuarioAutenticado(bearer));
    }
}
