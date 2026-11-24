package br.ufla.athena.rastreabilidade.api;

import br.ufla.athena.rastreabilidade.aplicacao.MapaService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/mapa")
public class MapaController {
    private final MapaService servico;

    public MapaController(MapaService servico) {
        this.servico = servico;
    }

    @GetMapping
    public ResponseEntity<MapaService.Mapa> obter(@PathVariable UUID projetoId) {
        MapaService.Resultado resultado = servico.obter(projetoId);
        return ResponseEntity.ok().eTag(resultado.etag()).body(resultado.mapa());
    }

    @PostMapping("/reconstrucoes")
    public ResponseEntity<Void> reconstruir(@PathVariable UUID projetoId) {
        servico.reconstruir(projetoId);
        return ResponseEntity.accepted().build();
    }
}
