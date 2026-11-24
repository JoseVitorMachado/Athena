package br.ufla.athena.requisitos.api;

import br.ufla.athena.requisitos.aplicacao.RequisitoService;
import br.ufla.athena.requisitos.aplicacao.RequisitoService.Encontrado;
import br.ufla.athena.requisitos.dominio.EstadoVersao;
import br.ufla.athena.requisitos.dominio.FiltroRequisitos;
import br.ufla.athena.requisitos.dominio.SituacaoRequisito;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

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
            @RequestParam(defaultValue = "ATIVO") SituacaoRequisito situacao,
            @RequestParam(required = false) EstadoVersao estadoVersao,
            @RequestParam(required = false) @Size(max = 40) String tipo,
            @RequestParam(required = false) UUID epicoId,
            @RequestParam(required = false) UUID responsavelId,
            @RequestParam(required = false) @Size(max = 200) String q) {
        // O link self repete os filtros usados, para a navegação manter o mesmo recorte.
        UriComponentsBuilder self = UriComponentsBuilder.fromPath(caminhoColecao(projetoId))
                .queryParam("page", page).queryParam("size", size).queryParam("sort", sort)
                .queryParam("situacao", situacao);
        if (estadoVersao != null) self.queryParam("estadoVersao", estadoVersao);
        if (tipo != null && !tipo.isBlank()) self.queryParam("tipo", tipo);
        if (epicoId != null) self.queryParam("epicoId", epicoId);
        if (responsavelId != null) self.queryParam("responsavelId", responsavelId);
        if (q != null && !q.isBlank()) self.queryParam("q", q);
        FiltroRequisitos filtro = new FiltroRequisitos(projetoId, situacao, estadoVersao, tipo, epicoId,
                responsavelId, q);
        return servico.listar(filtro, PageRequest.of(page, size, Ordenacao.de(sort)),
                self.encode().build().toUriString());
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

    @GetMapping("/{requisitoId}/candidata")
    public ResponseEntity<VersaoResposta> obterCandidata(@PathVariable UUID projetoId, @PathVariable UUID requisitoId) {
        var encontrada = servico.obterCandidata(projetoId, requisitoId);
        return ResponseEntity.ok().eTag("\"" + encontrada.revisao() + "\"").body(encontrada.versao());
    }

    @PatchMapping("/{requisitoId}/candidata")
    public ResponseEntity<VersaoResposta> editarCandidata(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch,
            @RequestBody CandidataPatch patch) {
        var encontrada = servico.editarCandidata(projetoId, requisitoId, ifMatch, patch);
        return ResponseEntity.ok().eTag("\"" + encontrada.revisao() + "\"").body(encontrada.versao());
    }

    /** Atualiza os metadados do requisito. */
    @PatchMapping("/{requisitoId}")
    public ResponseEntity<RequisitoResposta> alterarMetadados(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @RequestBody com.fasterxml.jackson.databind.JsonNode corpo) {
        Encontrado r = servico.alterarMetadados(projetoId, requisitoId, ifMatch, corpo);
        return ResponseEntity.ok().eTag(etag(r)).body(r.requisito());
    }

    /** Cancela o requisito mediante confirmação explícita. */
    @PostMapping("/{requisitoId}/cancelamentos")
    public ResponseEntity<RequisitoService.Alteracao> cancelar(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave,
            @RequestBody(required = false) com.fasterxml.jackson.databind.JsonNode corpo) {
        boolean confirmado = corpo != null && corpo.isObject() && corpo.size() == 1 && corpo.has("confirmacao")
                && corpo.get("confirmacao").isBoolean() && corpo.get("confirmacao").booleanValue();
        return respostaSituacao(projetoId, requisitoId, "cancelamentos", servico.alterarSituacao(
                projetoId, requisitoId, ifMatch, chave, SituacaoRequisito.CANCELADO, confirmado));
    }

    /** Reativa o requisito e abre uma candidata em rascunho. */
    @PostMapping("/{requisitoId}/reativacoes")
    public ResponseEntity<RequisitoService.Alteracao> reativar(@PathVariable UUID projetoId, @PathVariable UUID requisitoId,
            @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @RequestHeader(value = "Idempotency-Key", required = false) String chave) {
        return respostaSituacao(projetoId, requisitoId, "reativacoes", servico.alterarSituacao(
                projetoId, requisitoId, ifMatch, chave, SituacaoRequisito.ATIVO, true));
    }

    private static ResponseEntity<RequisitoService.Alteracao> respostaSituacao(UUID projeto, UUID requisito,
            String operacao, RequisitoService.AlteracaoEncontrada resultado) {
        URI local = URI.create(caminhoColecao(projeto) + "/" + requisito + "/" + operacao + "/" + resultado.alteracao().operacaoId());
        return ResponseEntity.created(local).eTag("\"" + resultado.revisao() + "\"").body(resultado.alteracao());
    }

    private static String caminhoColecao(UUID projetoId) {
        return "/api/v1/projetos/" + projetoId + "/requisitos";
    }

    private static String etag(Encontrado encontrado) {
        return "\"" + encontrado.revisao() + "\"";
    }
}
