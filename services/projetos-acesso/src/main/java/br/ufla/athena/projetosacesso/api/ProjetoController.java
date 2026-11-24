package br.ufla.athena.projetosacesso.api;

import br.ufla.athena.projetosacesso.dominio.*;
import br.ufla.athena.projetosacesso.mensageria.EmissorMensagens;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.net.URI;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projetos")
@Validated
public class ProjetoController {
    private static final String CAMINHO = "/api/v1/projetos";
    private final AcessoRecursos acesso;
    private final IdentidadeController identidade;
    private final EmissorMensagens emissor;
    private final EntityManager entidades;
    public ProjetoController(AcessoRecursos acesso, IdentidadeController identidade, EmissorMensagens emissor, EntityManager entidades) {
        this.acesso = acesso; this.identidade = identidade; this.emissor = emissor; this.entidades = entidades;
    }
    @GetMapping
    @Transactional(readOnly = true)
    public Pagina<ProjetoResposta> listar(@RequestHeader(value = "Authorization", required = false) String bearer,
            @RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = Ordenacao.PADRAO) String sort, @RequestParam(required = false) UUID workspaceId,
            @RequestParam(defaultValue = "false") boolean arquivado) {
        UUID usuario = identidade.usuarioAutenticado(bearer);
        String ordem = AcessoRecursos.ordem(sort, "p");
        String filtro = " FROM projetos p JOIN membros m ON m.projeto_id=p.id WHERE m.usuario_id=? AND p.excluido_em IS NULL AND p.arquivado=?" + (workspaceId == null ? "" : " AND p.workspace_id=?");
        List<Object> argumentos = new ArrayList<>(List.of(usuario, arquivado));
        if (workspaceId != null) argumentos.add(workspaceId);
        Long total = acesso.banco.queryForObject("SELECT count(*)" + filtro, Long.class, argumentos.toArray());
        argumentos.add(size); argumentos.add((long) page * size);
        List<UUID> ids = acesso.banco.queryForList("SELECT p.id" + filtro + " ORDER BY " + ordem + " LIMIT ? OFFSET ?", UUID.class, argumentos.toArray());
        List<ProjetoResposta> itens = ids.stream().map(id -> ProjetoResposta.de(acesso.projetos.findById(id).orElseThrow())).toList();
        String self = CAMINHO + "?page=" + page + "&size=" + size + "&sort=" + sort + "&arquivado=" + arquivado + (workspaceId == null ? "" : "&workspaceId=" + workspaceId);
        return Pagina.de(new PageImpl<>(itens, PageRequest.of(page, size), total == null ? 0 : total), self);
    }
    @PostMapping
    @Transactional
    public ResponseEntity<ProjetoResposta> criar(@RequestHeader(value = "Authorization", required = false) String bearer, @Valid @RequestBody ProjetoEntrada entrada) {
        UUID usuario = identidade.usuarioAutenticado(bearer);
        Workspace w = acesso.workspace(entrada.workspaceId(), usuario, true);
        if (!PoliticaWorkspace.podeCriarProjeto(usuario, w.getOwnerId(), acesso.papeis(w.getId(), usuario))) throw AcessoRecursos.insuficiente();
        Projeto p = acesso.projetos.saveAndFlush(Projeto.novo(w.getId(), entrada.nome().trim(), entrada.descricao(), usuario));
        acesso.banco.update("INSERT INTO membros(projeto_id, usuario_id, papel) VALUES (?, ?, ?)", p.getId(), usuario, "OWNER");
        evento("ProjetoCriado", p, usuario);
        return ResponseEntity.created(URI.create(CAMINHO + "/" + p.getId())).eTag(AcessoRecursos.etag(p.getRevisao())).body(ProjetoResposta.de(p));
    }
    @GetMapping("/{projetoId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ProjetoResposta> obter(@RequestHeader(value = "Authorization", required = false) String bearer, @PathVariable UUID projetoId) {
        Projeto p = acesso.projeto(projetoId, identidade.usuarioAutenticado(bearer), false);
        return resposta(p);
    }
    @PatchMapping("/{projetoId}")
    @Transactional
    public ResponseEntity<ProjetoResposta> alterar(@RequestHeader(value = "Authorization", required = false) String bearer,
            @RequestHeader(value = "If-Match", required = false) String ifMatch, @PathVariable UUID projetoId, @RequestBody JsonNode entrada) {
        UUID usuario = identidade.usuarioAutenticado(bearer);
        Projeto p = projetoParaEscrita(projetoId, usuario);
        AcessoRecursos.revisao(ifMatch, p.getRevisao());
        if (p.isArquivado()) throw new Problema(HttpStatus.CONFLICT, "PROJETO_ARQUIVADO", "Projeto arquivado.");
        if (!entrada.isObject() || entrada.isEmpty() || camposDesconhecidos(entrada)) throw invalida();
        JsonNode nome = entrada.get("nome"), descricao = entrada.get("descricao");
        if (nome != null && (!nome.isTextual() || nome.asText().isBlank() || nome.asText().length() > 120)) throw invalida();
        if (descricao != null && !descricao.isNull() && (!descricao.isTextual() || descricao.asText().length() > 1000)) throw invalida();
        p.alterar(nome == null ? p.getNome() : nome.asText().trim(), descricao == null ? p.getDescricao() : descricao.isNull() ? null : descricao.asText());
        acesso.projetos.flush();
        evento("ProjetoAlterado", p, usuario);
        return resposta(p);
    }
    @PostMapping("/{projetoId}/arquivamentos")
    @Transactional
    public ResponseEntity<ProjetoResposta> arquivar(@RequestHeader(value = "Authorization", required = false) String bearer, @PathVariable UUID projetoId) {
        UUID usuario = identidade.usuarioAutenticado(bearer);
        Projeto p = projetoParaEscrita(projetoId, usuario);
        if (p.isArquivado()) throw new Problema(HttpStatus.CONFLICT, "PROJETO_JA_ARQUIVADO", "Projeto já arquivado.");
        p.arquivar(); acesso.projetos.flush(); evento("ProjetoArquivado", p, usuario);
        return criada(p);
    }
    @PostMapping("/{projetoId}/restauracoes")
    @Transactional
    public ResponseEntity<ProjetoResposta> restaurar(@RequestHeader(value = "Authorization", required = false) String bearer, @PathVariable UUID projetoId) {
        UUID usuario = identidade.usuarioAutenticado(bearer);
        Projeto p = projetoParaEscrita(projetoId, usuario);
        if (!p.isArquivado()) throw new Problema(HttpStatus.CONFLICT, "PROJETO_NAO_ARQUIVADO", "Projeto não arquivado.");
        p.restaurar(); acesso.projetos.flush(); evento("ProjetoRestaurado", p, usuario);
        return criada(p);
    }
    private Projeto projetoParaEscrita(UUID id, UUID usuario) {
        Projeto p = acesso.projeto(id, usuario, true);
        acesso.workspaces.buscarParaAlteracao(p.getWorkspaceId()).orElseThrow(AcessoRecursos::projetoNaoEncontrado);
        entidades.refresh(p, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return acesso.projeto(id, usuario, true);
    }
    private void evento(String tipo, Projeto p, UUID usuario) {
        emissor.evento(tipo, p.getId(), null, null, UUID.randomUUID(), null, Map.of("projetoId", p.getId(), "workspaceId", p.getWorkspaceId(), "nome", p.getNome(), "arquivado", p.isArquivado(), "revisao", p.getRevisao(), "autorId", usuario));
    }
    private ResponseEntity<ProjetoResposta> resposta(Projeto p) { return ResponseEntity.ok().eTag(AcessoRecursos.etag(p.getRevisao())).body(ProjetoResposta.de(p)); }
    private ResponseEntity<ProjetoResposta> criada(Projeto p) { return ResponseEntity.created(URI.create(CAMINHO + "/" + p.getId())).eTag(AcessoRecursos.etag(p.getRevisao())).body(ProjetoResposta.de(p)); }
    private boolean camposDesconhecidos(JsonNode entrada) {
        var campos = entrada.fieldNames();
        while (campos.hasNext()) {
            String campo = campos.next();
            if (!campo.equals("nome") && !campo.equals("descricao")) return true;
        }
        return false;
    }
    private Problema invalida() { return new Problema(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Informe campos válidos para alteração."); }
}
