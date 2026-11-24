package br.ufla.athena.projetosacesso.api;

import br.ufla.athena.projetosacesso.dominio.Projeto;
import br.ufla.athena.projetosacesso.dominio.ProjetoRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Operações de projeto de docs/api/projetos-acesso.yaml implementadas nesta etapa. */
@RestController
@RequestMapping("/api/v1/projetos")
public class ProjetoController {

    private static final String CAMINHO = "/api/v1/projetos";

    private final ProjetoRepository repositorio;
    private final IdentidadeController identidade;
    private final org.springframework.jdbc.core.JdbcTemplate banco;

    public ProjetoController(ProjetoRepository repositorio, IdentidadeController identidade, org.springframework.jdbc.core.JdbcTemplate banco) {
        this.repositorio = repositorio;
        this.identidade = identidade;
        this.banco = banco;
    }

    /** listarProjetos */
    @GetMapping
    public Pagina<ProjetoResposta> listar(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = Ordenacao.PADRAO) String sort,
            @RequestParam(required = false) UUID workspaceId) {
        Pageable paginacao = PageRequest.of(page, size, Ordenacao.de(sort));
        Page<Projeto> projetos = workspaceId == null
                ? repositorio.findByArquivadoFalse(paginacao)
                : repositorio.findByWorkspaceIdAndArquivadoFalse(workspaceId, paginacao);
        String self = CAMINHO + "?page=" + page + "&size=" + size + "&sort=" + sort;
        return Pagina.de(projetos.map(ProjetoResposta::de), self);
    }

    /** criarProjeto: 201 com Location. O criador passa a ser o Owner. */
    @PostMapping
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<ProjetoResposta> criar(@Valid @RequestBody ProjetoEntrada entrada, @org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String bearer) {
        UUID usuario = identidade.usuarioAutenticado(bearer);
        Projeto projeto = repositorio.saveAndFlush(
                Projeto.novo(entrada.workspaceId(), entrada.nome().trim(), entrada.descricao(), usuario));
        banco.update("INSERT INTO membros(projeto_id, usuario_id, papel) VALUES (?, ?, ?)", projeto.getId(), usuario, "OWNER");
        return ResponseEntity.created(URI.create(CAMINHO + "/" + projeto.getId()))
                .eTag(etag(projeto))
                .body(ProjetoResposta.de(projeto));
    }

    /** obterProjeto */
    @GetMapping("/{projetoId}")
    public ResponseEntity<ProjetoResposta> obter(@PathVariable UUID projetoId) {
        Projeto projeto = repositorio.findById(projetoId).orElseThrow(() ->
                new Problema(HttpStatus.NOT_FOUND, "PROJETO_NAO_ENCONTRADO", "Projeto não encontrado."));
        return ResponseEntity.ok().eTag(etag(projeto)).body(ProjetoResposta.de(projeto));
    }

    private static String etag(Projeto projeto) {
        return "\"" + projeto.getRevisao() + "\"";
    }
}
