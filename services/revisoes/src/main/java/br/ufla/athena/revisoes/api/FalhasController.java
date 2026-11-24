package br.ufla.athena.revisoes.api;

import br.ufla.athena.revisoes.aplicacao.InjetorFalhas;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** /api/v1/internal nunca é roteado pelo Gateway (livro §6.1). */
@RestController
@RequestMapping("/api/v1/internal/falhas")
public class FalhasController {
    private final InjetorFalhas injetor;

    public FalhasController(InjetorFalhas injetor) {
        this.injetor = injetor;
    }

    @GetMapping
    public ModoFalhas obter() {
        return new ModoFalhas(injetor.getAbrirLote());
    }

    @PostMapping
    public ModoFalhas alterar(@Valid @RequestBody ModoFalhas entrada) {
        injetor.definir(entrada.abrirLote());
        return obter();
    }

    public record ModoFalhas(@NotNull InjetorFalhas.Modo abrirLote) { }
}
