package br.ufla.athena.requisitos.api;

import br.ufla.athena.requisitos.dominio.PublicacaoRepository;
import br.ufla.athena.requisitos.dominio.RequisitoRepository;
import br.ufla.athena.requisitos.dominio.VersaoRepository;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Leitura de versões e publicações (obterVersao e listarPublicacoes do contrato). */
@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/requisitos/{requisitoId}")
public class VersaoPublicacaoController {

    private final RequisitoRepository requisitos;
    private final VersaoRepository versoes;
    private final PublicacaoRepository publicacoes;

    public VersaoPublicacaoController(RequisitoRepository requisitos, VersaoRepository versoes,
                                      PublicacaoRepository publicacoes) {
        this.requisitos = requisitos;
        this.versoes = versoes;
        this.publicacoes = publicacoes;
    }

    /** obterVersao */
    @GetMapping("/versoes/{versaoId}")
    @Transactional(readOnly = true)
    public VersaoResposta versao(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
                                 @PathVariable UUID versaoId) {
        return versoes.findByIdAndRequisito_IdAndRequisito_ProjetoId(versaoId, requisitoId, projetoId)
                .map(VersaoResposta::de)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Versão não encontrada"));
    }

    /** listarPublicacoes: da mais recente para a mais antiga. */
    @GetMapping("/publicacoes")
    @Transactional(readOnly = true)
    public Pagina<PublicacaoResposta> publicacoes(
            @PathVariable UUID projetoId,
            @PathVariable UUID requisitoId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        requisitos.findByIdAndProjetoId(requisitoId, projetoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisito não encontrado"));
        PageRequest paginacao = PageRequest.of(page, size, Sort.by(Sort.Order.desc("data"), Sort.Order.asc("id")));
        String self = "/api/v1/projetos/" + projetoId + "/requisitos/" + requisitoId + "/publicacoes?page=" + page
                + "&size=" + size;
        return Pagina.de(publicacoes.findByRequisitoId(requisitoId, paginacao).map(PublicacaoResposta::de), self);
    }
}
