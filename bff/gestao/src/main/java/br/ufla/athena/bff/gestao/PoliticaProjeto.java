package br.ufla.athena.bff.gestao;

import br.ufla.athena.bff.gestao.PapelAtual.Papel;

/** Operações conhecidas da fachada; escritas desconhecidas são negadas. */
public final class PoliticaProjeto {
    private static final String BASE = "/api/v1/gestao/projetos/[^/]+";

    private PoliticaProjeto() {
    }

    public static boolean exigeProjeto(String metodo, String caminho) {
        return !(caminho.equals("/api/v1/gestao/projetos")
                && ("GET".equals(metodo) || "POST".equals(metodo)));
    }

    public static boolean permiteArquivado(String metodo, String caminho) {
        return "GET".equals(metodo)
                || ("POST".equals(metodo) && caminho.matches(BASE + "/restauracoes"));
    }

    public static Papel papelMinimo(String metodo, String caminho) {
        if ("GET".equals(metodo) && caminho.matches(BASE + "/convites")) {
            return Papel.MANTENEDOR;
        }
        if ("DELETE".equals(metodo) && caminho.matches(BASE + "/membros/[^/]+")) {
            return Papel.LEITOR;
        }
        if ("POST".equals(metodo) && caminho.matches(BASE + "/transferencias-propriedade")) {
            return Papel.OWNER;
        }
        if ((("GET".equals(metodo) || "POST".equals(metodo))
                && caminho.matches(BASE + "/requisitos/[^/]+/comentarios"))
                || ("PATCH".equals(metodo)
                && caminho.matches(BASE + "/requisitos/[^/]+/comentarios/[^/]+"))) {
            return Papel.REPORTER;
        }
        if ("GET".equals(metodo)) {
            return Papel.LEITOR;
        }
        if (("PATCH".equals(metodo) && caminho.matches(BASE + "/membros/[^/]+"))
                || ("POST".equals(metodo) && caminho.matches(BASE + "/convites"))
                || ("DELETE".equals(metodo) && caminho.matches(BASE + "/convites/[^/]+"))
                || ("PATCH".equals(metodo) && caminho.matches(BASE))
                || ("POST".equals(metodo) && (caminho.matches(BASE + "/arquivamentos")
                || caminho.matches(BASE + "/restauracoes")))) {
            return Papel.MANTENEDOR;
        }
        if ("POST".equals(metodo) && (caminho.matches(BASE + "/revisoes/[^/]+/decisoes")
                || caminho.matches(BASE + "/requisitos/[^/]+/publicacoes"))) {
            return Papel.APROVADOR;
        }
        if (("POST".equals(metodo) && (caminho.matches(BASE + "/requisitos")
                || caminho.matches(BASE + "/epicos")
                || caminho.matches(BASE + "/requisitos/[^/]+/(cancelamentos|reativacoes)")
                || caminho.matches(BASE + "/requisitos/[^/]+/submissoes")
                || caminho.matches(BASE + "/relacoes")))
                || ("PATCH".equals(metodo) && (caminho.matches(BASE + "/requisitos/[^/]+")
                || caminho.matches(BASE + "/epicos/[^/]+")
                || caminho.matches(BASE + "/requisitos/[^/]+/candidata")
                || caminho.matches(BASE + "/relacoes/[^/]+")))
                || ("DELETE".equals(metodo) && (caminho.matches(BASE + "/relacoes/[^/]+")
                || caminho.matches(BASE + "/epicos/[^/]+")))) {
            return Papel.EDITOR;
        }
        return null;
    }
}
