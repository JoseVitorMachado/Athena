package br.ufla.athena.integracoes.api;

import br.ufla.athena.integracoes.dominio.Conexao;
import br.ufla.athena.integracoes.dominio.ConexaoRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Operações de docs/api/integracoes.yaml implementadas nesta etapa. */
@RestController
@RequestMapping("/api/v1/projetos/{projetoId}/conexoes")
public class ConexaoController {

    private final ConexaoRepository repositorio;

    public ConexaoController(ConexaoRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** listarConexoes */
    @GetMapping
    public Pagina<ConexaoResposta> listar(
            @PathVariable UUID projetoId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = Ordenacao.PADRAO) String sort) {
        String self = colecao(projetoId) + "?page=" + page + "&size=" + size + "&sort=" + sort;
        return Pagina.de(
                repositorio.findByProjetoId(projetoId, PageRequest.of(page, size, Ordenacao.de(sort)))
                        .map(ConexaoResposta::de),
                self);
    }

    /**
     * criarConexao: registra provedor, nome e configuração. O segredo enviado em
     * "credenciais" é validado mas ainda não é guardado: o armazenamento cifrado
     * entra junto com os conectores, e gravar o segredo em claro não é opção.
     */
    @PostMapping
    public ResponseEntity<ConexaoResposta> criar(@PathVariable UUID projetoId,
                                                 @Valid @RequestBody ConexaoEntrada entrada) {
        ConexaoEntrada.Configuracao configuracao = entrada.configuracao();
        Conexao conexao = repositorio.saveAndFlush(Conexao.nova(projetoId, entrada.provedor(), entrada.nome().trim(),
                configuracao == null ? null : configuracao.urlBase(),
                configuracao == null ? null : configuracao.projetoExternoId()));
        return ResponseEntity.created(URI.create(colecao(projetoId) + "/" + conexao.getId()))
                .eTag(etag(conexao))
                .body(ConexaoResposta.de(conexao));
    }

    /** obterConexao */
    @GetMapping("/{conexaoId}")
    public ResponseEntity<ConexaoResposta> obter(@PathVariable UUID projetoId, @PathVariable UUID conexaoId) {
        Conexao conexao = repositorio.findByIdAndProjetoId(conexaoId, projetoId).orElseThrow(() ->
                new Problema(HttpStatus.NOT_FOUND, "CONEXAO_NAO_ENCONTRADA", "Conexão não encontrada."));
        return ResponseEntity.ok().eTag(etag(conexao)).body(ConexaoResposta.de(conexao));
    }

    private static String colecao(UUID projetoId) {
        return "/api/v1/projetos/" + projetoId + "/conexoes";
    }

    private static String etag(Conexao conexao) {
        return "\"" + conexao.getRevisao() + "\"";
    }
}
