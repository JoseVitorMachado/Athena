package br.ufla.athena.rastreabilidade.api;

import br.ufla.athena.rastreabilidade.aplicacao.AtrasoProjecao;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** /api/v1/internal nunca é roteado pelo Gateway. */
@RestController
@RequestMapping("/api/v1/internal/projecao")
public class ProjecaoInternaController {
    private final AtrasoProjecao atraso;

    public ProjecaoInternaController(AtrasoProjecao atraso) {
        this.atraso = atraso;
    }

    @GetMapping
    public Configuracao obter() {
        return new Configuracao(atraso.consultar());
    }

    @PostMapping
    public Configuracao configurar(@RequestBody Configuracao configuracao) {
        try {
            atraso.definir(configuracao.atrasoSegundos());
        } catch (IllegalArgumentException e) {
            throw new Problema(HttpStatus.BAD_REQUEST, "ATRASO_INVALIDO", e.getMessage());
        }
        return obter();
    }

    public record Configuracao(long atrasoSegundos) { }
}
