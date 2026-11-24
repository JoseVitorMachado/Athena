package br.ufla.athena.requisitos.api;

import br.ufla.athena.requisitos.aplicacao.PublicacaoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/requisitos/{requisitoId}")
public class PublicacaoController {
    private final PublicacaoService publicacoes;

    public PublicacaoController(PublicacaoService publicacoes) {
        this.publicacoes = publicacoes;
    }

    @PostMapping("/confirmacoes-publicacao")
    public ResponseEntity<ConfirmacaoResposta> confirmar(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @Valid @RequestBody ConfirmacaoEntrada entrada) {
        return ResponseEntity.status(201).body(publicacoes.confirmar(projetoId, requisitoId, entrada));
    }

    @PostMapping("/publicacoes")
    public ResponseEntity<PublicacaoResposta> criar(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave,
            @Valid @RequestBody PublicacaoEntrada entrada) {
        PublicacaoResposta resposta = publicacoes.criar(projetoId, requisitoId, entrada, chave, ifMatch);
        URI local = URI.create("/api/v1/projetos/" + projetoId + "/requisitos/" + requisitoId
                + "/publicacoes/" + resposta.publicacaoId());
        return ResponseEntity.created(local).body(resposta);
    }

    @GetMapping("/publicacoes/{publicacaoId}")
    public PublicacaoResposta obter(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
                                   @PathVariable UUID publicacaoId) {
        return publicacoes.buscar(projetoId, requisitoId, publicacaoId);
    }
}
