package br.ufla.athena.requisitos.api;

import br.ufla.athena.requisitos.dominio.PublicacaoRepository;
import br.ufla.athena.requisitos.dominio.RequisitoRepository;
import br.ufla.athena.requisitos.dominio.VersaoRepository;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import java.util.List;
import java.util.Objects;
import br.ufla.athena.requisitos.dominio.Versao;
import br.ufla.athena.requisitos.aplicacao.DiferencaTexto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<VersaoResposta> versao(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
                                                 @PathVariable UUID versaoId) {
        // O ETag da versão publicada é o If-Match usado para abrir uma nova candidata a partir dela.
        return versoes.findByIdAndRequisito_IdAndRequisito_ProjetoId(versaoId, requisitoId, projetoId)
                .map(versao -> ResponseEntity.ok().eTag("\"" + versao.getRevisao() + "\"").body(VersaoResposta.de(versao)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Versão não encontrada"));
    }

    public record ResumoVersao(UUID versaoId, int numero, String estadoVersao) {
        static ResumoVersao de(Versao v) { return new ResumoVersao(v.getId(), v.getNumero(), v.getEstadoVersao().name()); }
    }
    public record Titulo(String anterior, String atual, boolean alterado) { }
    public record Criterios(List<String> adicionados, List<String> removidos, List<String> mantidos) { }
    public record DiferencaVersoes(ResumoVersao de, ResumoVersao para, Titulo titulo,
            List<DiferencaTexto.Linha> conteudo, Criterios criterios) { }

    @GetMapping("/versoes/{versaoId}/diferencas")
    @Transactional(readOnly = true)
    public ResponseEntity<DiferencaVersoes> diferencas(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @PathVariable UUID versaoId, @RequestParam(required = false) UUID com) {
        Versao atual = versoes.findByIdAndRequisito_IdAndRequisito_ProjetoId(versaoId, requisitoId, projetoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Versão não encontrada"));
        Versao anterior = com == null
                ? versoes.findByRequisito_IdAndNumero(requisitoId, atual.getNumero() - 1)
                    .orElseThrow(() -> new Problema(HttpStatus.NOT_FOUND, "SEM_VERSAO_ANTERIOR", "Não existe versão anterior."))
                : versoes.findByIdAndRequisito_IdAndRequisito_ProjetoId(com, requisitoId, projetoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Versão não encontrada"));
        List<DiferencaTexto.Linha> linhas;
        try { linhas = DiferencaTexto.comparar(anterior.getConteudo(), atual.getConteudo()); }
        catch (IllegalArgumentException e) {
            throw new Problema(HttpStatus.UNPROCESSABLE_ENTITY, "CONTEUDO_GRANDE_DEMAIS", "O conteúdo excede 2000 linhas.");
        }
        List<String> a = anterior.getCriteriosAceite(), b = atual.getCriteriosAceite();
        DiferencaVersoes resultado = new DiferencaVersoes(ResumoVersao.de(anterior), ResumoVersao.de(atual),
                new Titulo(anterior.getTitulo(), atual.getTitulo(), !Objects.equals(anterior.getTitulo(), atual.getTitulo())),
                linhas, new Criterios(b.stream().filter(c -> !a.contains(c)).toList(),
                a.stream().filter(c -> !b.contains(c)).toList(), a.stream().filter(b::contains).toList()));
        return ResponseEntity.ok().eTag("\"" + anterior.getId() + "-" + anterior.getRevisao() + "-" + atual.getId() + "-" + atual.getRevisao() + "\"").body(resultado);
    }

    /** listarVersoes: por padrão, da mais recente para a mais antiga. */
    @GetMapping("/versoes")
    @Transactional(readOnly = true)
    public Pagina<VersaoResposta> versoes(
            @PathVariable UUID projetoId,
            @PathVariable UUID requisitoId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = Ordenacao.PADRAO) String sort) {
        requisitos.findByIdAndProjetoId(requisitoId, projetoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requisito não encontrado"));
        PageRequest paginacao = PageRequest.of(page, size, Ordenacao.de(sort));
        String self = "/api/v1/projetos/" + projetoId + "/requisitos/" + requisitoId + "/versoes?page=" + page
                + "&size=" + size + "&sort=" + sort;
        return Pagina.de(versoes.findByRequisito_Id(requisitoId, paginacao).map(VersaoResposta::de), self);
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
