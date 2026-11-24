package br.ufla.athena.projetosacesso.api;

import br.ufla.athena.projetosacesso.dominio.*;
import br.ufla.athena.projetosacesso.mensageria.EmissorMensagens;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** Workspaces (livro §5.6 e §7.4): a propriedade do workspace não concede acesso aos projetos. */
@RestController
@RequestMapping("/api/v1/workspaces")
@Validated
public class WorkspaceController {
    private static final String CAMINHO = "/api/v1/workspaces";
    private final IdentidadeController identidade;
    private final AcessoRecursos acesso;
    private final EmissorMensagens emissor;
    public WorkspaceController(IdentidadeController identidade, AcessoRecursos acesso, EmissorMensagens emissor) {
        this.identidade = identidade; this.acesso = acesso; this.emissor = emissor;
    }
    record Entrada(@NotBlank @Size(max = 120) String nome) { }
    record Confirmacao(String confirmacaoNome) { }
    record Resposta(UUID workspaceId, String nome, UUID ownerId, Instant atualizadoEm) {
        static Resposta de(Workspace w) { return new Resposta(w.getId(), w.getNome(), w.getOwnerId(), w.getAtualizadoEm()); }
    }
    record ExclusaoWorkspace(UUID exclusaoId, UUID workspaceId, Instant excluidoEm) { }

    @GetMapping
    @Transactional(readOnly = true)
    public Pagina<Resposta> listar(@RequestHeader(value = "Authorization", required = false) String bearer,
            @RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = Ordenacao.PADRAO) String sort) {
        UUID usuario = identidade.usuarioAutenticado(bearer);
        String ordem = AcessoRecursos.ordem(sort, "w");
        String filtro = " FROM workspaces w WHERE w.excluido_em IS NULL AND (w.owner_id=? OR EXISTS (SELECT 1 FROM projetos p JOIN membros m ON m.projeto_id=p.id WHERE p.workspace_id=w.id AND p.excluido_em IS NULL AND NOT p.em_exclusao AND m.usuario_id=?))";
        Long total = acesso.banco.queryForObject("SELECT count(*)" + filtro, Long.class, usuario, usuario);
        List<UUID> ids = acesso.banco.queryForList("SELECT w.id" + filtro + " ORDER BY " + ordem + " LIMIT ? OFFSET ?", UUID.class, usuario, usuario, size, (long) page * size);
        List<Resposta> itens = ids.stream().map(id -> Resposta.de(acesso.workspaces.findById(id).orElseThrow())).toList();
        return Pagina.de(new PageImpl<>(itens, PageRequest.of(page, size), total == null ? 0 : total), CAMINHO + "?page=" + page + "&size=" + size + "&sort=" + sort);
    }
    @PostMapping
    @Transactional
    public ResponseEntity<Resposta> criar(@RequestHeader(value = "Authorization", required = false) String bearer, @Valid @RequestBody Entrada entrada) {
        UUID usuario = identidade.usuarioAutenticado(bearer);
        Workspace w = acesso.workspaces.saveAndFlush(Workspace.novo(entrada.nome().trim(), usuario));
        evento("WorkspaceCriado", w, Map.of("workspaceId", w.getId(), "nome", w.getNome(), "ownerId", usuario));
        return ResponseEntity.created(URI.create(CAMINHO + "/" + w.getId())).eTag(AcessoRecursos.etag(w.getRevisao())).body(Resposta.de(w));
    }
    @GetMapping("/{workspaceId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Resposta> obter(@RequestHeader(value = "Authorization", required = false) String bearer, @PathVariable UUID workspaceId) {
        Workspace w = acesso.workspace(workspaceId, identidade.usuarioAutenticado(bearer), false);
        return ResponseEntity.ok().eTag(AcessoRecursos.etag(w.getRevisao())).body(Resposta.de(w));
    }
    @PatchMapping("/{workspaceId}")
    @Transactional
    public ResponseEntity<Resposta> alterar(@RequestHeader(value = "Authorization", required = false) String bearer, @RequestHeader(value = "If-Match", required = false) String ifMatch, @PathVariable UUID workspaceId, @Valid @RequestBody Entrada entrada) {
        UUID usuario = identidade.usuarioAutenticado(bearer);
        Workspace w = acesso.workspace(workspaceId, usuario, true);
        if (!PoliticaWorkspace.owner(usuario, w.getOwnerId())) throw AcessoRecursos.insuficiente();
        AcessoRecursos.revisao(ifMatch, w.getRevisao());
        w.renomear(entrada.nome().trim());
        acesso.workspaces.saveAndFlush(w);
        evento("WorkspaceAlterado", w, Map.of("workspaceId", w.getId(), "nome", w.getNome(), "ownerId", usuario));
        return ResponseEntity.ok().eTag(AcessoRecursos.etag(w.getRevisao())).body(Resposta.de(w));
    }
    @PostMapping("/{workspaceId}/exclusoes")
    @Transactional
    public ResponseEntity<ExclusaoWorkspace> excluir(@RequestHeader(value = "Authorization", required = false) String bearer, @RequestHeader(value = "If-Match", required = false) String ifMatch, @PathVariable UUID workspaceId, @RequestBody Confirmacao entrada) {
        UUID usuario = identidade.usuarioAutenticado(bearer);
        Workspace w = acesso.workspace(workspaceId, usuario, true);
        if (!PoliticaWorkspace.owner(usuario, w.getOwnerId())) throw AcessoRecursos.insuficiente();
        AcessoRecursos.revisao(ifMatch, w.getRevisao());
        if (!PoliticaWorkspace.confirmacaoValida(w.getNome(), entrada.confirmacaoNome())) throw new Problema(HttpStatus.BAD_REQUEST, "CONFIRMACAO_INVALIDA", "Confirmação do nome inválida.");
        UUID exclusaoId = UUID.randomUUID();
        Instant agora = Instant.now();
        w.excluir(exclusaoId, agora);
        List<Projeto> projetos = acesso.projetos.findByWorkspaceIdAndExcluidoEmIsNull(workspaceId);
        projetos.forEach(p -> p.excluirLogicamente(agora));
        acesso.projetos.flush();
        acesso.workspaces.flush();
        List<UUID> ids = projetos.stream().map(Projeto::getId).toList();
        evento("WorkspaceExcluido", w, Map.of("workspaceId", workspaceId, "exclusaoId", exclusaoId, "projetoIds", ids));
        for (Projeto p : projetos) emissor.evento("ProjetoExcluidoLogicamente", p.getId(), null, null, UUID.randomUUID(), null, Map.of("projetoId", p.getId()));
        return ResponseEntity.created(URI.create(CAMINHO + "/" + workspaceId + "/exclusoes/" + exclusaoId)).body(new ExclusaoWorkspace(exclusaoId, workspaceId, agora));
    }
    // O envelope exige projetoId; eventos de workspace levam o próprio workspaceId nesse campo.
    private void evento(String tipo, Workspace w, Map<String, Object> payload) {
        emissor.evento(tipo, w.getId(), null, null, UUID.randomUUID(), null, payload);
    }
}
