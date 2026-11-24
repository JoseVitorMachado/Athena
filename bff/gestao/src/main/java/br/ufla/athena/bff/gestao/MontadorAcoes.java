package br.ufla.athena.bff.gestao;

import br.ufla.athena.bff.gestao.PapelAtual.Papel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Navegação e operações sugeridas; o domínio revalida cada chamada. */
public final class MontadorAcoes {

    public record Link(String href) {
    }

    public record Acao(String href, String method, String contentType,
                       List<String> campos, List<String> precondicoes) {
    }

    public record Resultado(Map<String, Link> links, Map<String, Acao> actions) {
    }

    public Resultado montar(String projetoId, String requisitoId, String situacao, String estado,
                            String loteId, String estadoLote, String submissaoId, boolean submissaoEmAndamento, Papel papel) {
        return montar(projetoId, requisitoId, situacao, estado, loteId, estadoLote, submissaoId,
                submissaoEmAndamento, papel, null, null);
    }

    public Resultado montar(String projetoId, String requisitoId, String situacao, String estado,
                            String loteId, String estadoLote, String submissaoId, boolean submissaoEmAndamento,
                            Papel papel, String publicadaId, String candidataId) {
        String colecao = Caminhos.GESTAO + projetoId + "/requisitos";
        String self = colecao + "/" + requisitoId;
        String revisao = loteId == null ? null : Caminhos.GESTAO + projetoId + "/revisoes/" + loteId;
        String submissao = submissaoId == null ? null : self + "/submissoes/" + submissaoId;
        Map<String, Link> links = new LinkedHashMap<>();
        Map<String, Acao> actions = new LinkedHashMap<>();
        links.put("self", new Link(self));
        links.put("colecao", new Link(colecao));
        links.put("historico", new Link(self + "/historico"));
        if (publicadaId != null && candidataId != null) {
            links.put("diferencas", new Link(self + "/versoes/" + candidataId + "/diferencas?com=" + publicadaId));
        }
        links.put("versoes", new Link(self + "/versoes"));
        links.put("comentarios", new Link(self + "/comentarios"));
        if (estado != null && !"PUBLICADA".equals(estado) && !"SEM_VERSAO".equals(estado)) {
            links.put("candidata", new Link(self + "/candidata"));
        }
        if (submissao != null) {
            links.put("submissao", new Link(submissao));
        }
        if (revisao != null) {
            links.put("revisao", new Link(revisao));
        }
        if ("ATIVO".equals(situacao) && papel.permite(Papel.REPORTER)) {
            actions.put("comentar", acao(self + "/comentarios", "POST",
                    List.of("versaoId", "texto", "ancora", "mencoes"),
                    List.of("Requisito ATIVO", "Papel Repórter ou superior")));
        }
        if (!papel.permite(Papel.EDITOR)) {
            return new Resultado(links, actions);
        }
        if ("CANCELADO".equals(situacao)) {
            actions.put("reativar", acao(self + "/reativacoes", "POST", List.of(),
                    List.of("If-Match", "Idempotency-Key", "Requisito CANCELADO", "Papel Editor ou superior")));
            return new Resultado(links, actions);
        }
        if (!"ATIVO".equals(situacao)) {
            return new Resultado(links, actions);
        }
        actions.put("editar-metadados", acao(self, "PATCH", List.of("responsavelId", "epicoId", "tipo"),
                List.of("If-Match", "Requisito ATIVO", "Papel Editor ou superior")));
        if (!submissaoEmAndamento && !"EM_SUBMISSAO".equals(estado) && !"AGUARDANDO_REVISAO".equals(estado)) {
            actions.put("cancelar", acao(self + "/cancelamentos", "POST", List.of("confirmacao"),
                    List.of("If-Match", "Idempotency-Key", "Confirmação verdadeira", "Requisito ATIVO",
                            "Sem submissão ou revisão em andamento", "Papel Editor ou superior")));
        }
        if (estado == null || "PUBLICADA".equals(estado) || "RASCUNHO".equals(estado) || "APROVADA".equals(estado)) {
            List<String> condicoes = "APROVADA".equals(estado)
                    ? List.of("If-Match", "Candidata em RASCUNHO ou APROVADA", "Papel Editor ou superior",
                            "Editar uma candidata APROVADA devolve-a a RASCUNHO")
                    : List.of("If-Match", "Candidata em RASCUNHO ou APROVADA", "Papel Editor ou superior");
            actions.put("editar-candidata", acao(self + "/candidata", "PATCH",
                    List.of("titulo", "conteudo", "criteriosAceite"), condicoes));
        }
        if ("RASCUNHO".equals(estado)) {
            actions.put("submeter", acao(self + "/submissoes", "POST", List.of("versaoId"),
                    List.of("If-Match", "Idempotency-Key", "Candidata em RASCUNHO", "Papel Editor ou superior")));
        }
        if ("EM_SUBMISSAO".equals(estado) && submissao != null) {
            actions.put("acompanhar-submissao", acao(submissao, "GET", List.of(), List.of()));
        }
        if ("AGUARDANDO_REVISAO".equals(estado) && revisao != null) {
            actions.put("consultar-revisao", acao(revisao, "GET", List.of(), List.of()));
            if ("ATIVO".equals(estadoLote) && papel.permite(Papel.APROVADOR)) {
                actions.put("decidir", acao(revisao + "/decisoes", "POST",
                        List.of("decisao", "justificativa", "submissaoId", "versaoId", "hashConteudo"),
                        List.of("If-Match do lote", "Idempotency-Key", "Lote ATIVO",
                                "Justificativa obrigatória para REJEITAR ou SOLICITAR_CORRECOES",
                                "Papel Aprovador ou superior")));
            }
        }
        if ("APROVADA".equals(estado) && papel.permite(Papel.APROVADOR)) {
            actions.put("publicar", acao(self + "/publicacoes", "POST",
                    List.of("versaoId", "submissaoId", "comprovanteHumano"),
                    List.of("If-Match", "Idempotency-Key", "Candidata APROVADA", "Papel Aprovador ou superior",
                            "MCP: confirmação humana específica; comprovante único válido por 5 minutos")));
        }
        return new Resultado(links, actions);
    }

    private static Acao acao(String href, String metodo, List<String> campos, List<String> precondicoes) {
        return new Acao(href, metodo, "application/json", campos, precondicoes);
    }
}
