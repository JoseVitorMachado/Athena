package br.ufla.athena.projetosacesso.api;

import br.ufla.athena.projetosacesso.dominio.*;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
class AcessoRecursos {
    final WorkspaceRepository workspaces;
    final ProjetoRepository projetos;
    final JdbcTemplate banco;
    AcessoRecursos(WorkspaceRepository workspaces, ProjetoRepository projetos, JdbcTemplate banco) {
        this.workspaces = workspaces; this.projetos = projetos; this.banco = banco;
    }
    List<String> papeis(UUID workspace, UUID usuario) {
        return banco.queryForList("SELECT m.papel FROM membros m JOIN projetos p ON p.id=m.projeto_id WHERE p.workspace_id=? AND p.excluido_em IS NULL AND NOT p.em_exclusao AND m.usuario_id=?", String.class, workspace, usuario);
    }
    Workspace workspace(UUID id, UUID usuario, boolean bloquear) {
        Workspace w = (bloquear ? workspaces.buscarParaAlteracao(id) : workspaces.findById(id))
                .filter(x -> x.getExcluidoEm() == null)
                .orElseThrow(AcessoRecursos::workspaceNaoEncontrado);
        if (!PoliticaWorkspace.visivel(usuario, w.getOwnerId(), papeis(id, usuario))) throw workspaceNaoEncontrado();
        return w;
    }
    Projeto projeto(UUID id, UUID usuario, boolean escrita) {
        Projeto p = projetos.findById(id).filter(x -> x.getExcluidoEm() == null)
                .orElseThrow(AcessoRecursos::projetoNaoEncontrado);
        List<String> papeis = banco.queryForList("SELECT m.papel FROM membros m JOIN projetos p ON p.id=m.projeto_id WHERE m.projeto_id=? AND m.usuario_id=? AND NOT p.em_exclusao", String.class, id, usuario);
        if (papeis.isEmpty()) throw projetoNaoEncontrado();
        if (escrita && !PoliticaWorkspace.mantenedor(papeis.get(0))) throw insuficiente();
        return p;
    }
    static void revisao(String ifMatch, long revisao) {
        if (ifMatch == null || ifMatch.isBlank()) throw new Problema(HttpStatus.PRECONDITION_REQUIRED, "PRECONDICAO_OBRIGATORIA", "Informe If-Match.");
        if (!ifMatch.trim().equals(etag(revisao))) throw new Problema(HttpStatus.PRECONDITION_FAILED, "REVISAO_DIVERGENTE", "A revisão foi alterada.");
    }
    static String etag(long revisao) { return "\"" + revisao + "\""; }
    static Problema workspaceNaoEncontrado() { return new Problema(HttpStatus.NOT_FOUND, "WORKSPACE_NAO_ENCONTRADO", "Workspace não encontrado."); }
    static Problema projetoNaoEncontrado() { return new Problema(HttpStatus.NOT_FOUND, "PROJETO_NAO_ENCONTRADO", "Projeto não encontrado."); }
    static Problema insuficiente() { return new Problema(HttpStatus.FORBIDDEN, "PAPEL_INSUFICIENTE", "Papel insuficiente."); }
    static String ordem(String sort, String alias) {
        Ordenacao.de(sort);
        return switch (sort) {
            case "atualizadoEm,desc" -> alias + ".atualizado_em DESC, " + alias + ".id ASC";
            case "atualizadoEm,asc" -> alias + ".atualizado_em ASC, " + alias + ".id ASC";
            case "id,desc" -> alias + ".id DESC";
            default -> alias + ".id ASC";
        };
    }
}
