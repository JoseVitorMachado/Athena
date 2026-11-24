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
        if ("GET".equals(metodo)) {
            return Papel.LEITOR;
        }
        if (("PATCH".equals(metodo) && caminho.matches(BASE))
                || ("POST".equals(metodo) && (caminho.matches(BASE + "/arquivamentos")
                || caminho.matches(BASE + "/restauracoes")))) {
            return Papel.MANTENEDOR;
        }
        if ("POST".equals(metodo) && (caminho.matches(BASE + "/revisoes/[^/]+/decisoes")
                || caminho.matches(BASE + "/requisitos/[^/]+/publicacoes"))) {
            return Papel.APROVADOR;
        }
        if (("POST".equals(metodo) && (caminho.matches(BASE + "/requisitos")
                || caminho.matches(BASE + "/requisitos/[^/]+/submissoes")
                || caminho.matches(BASE + "/relacoes")))
                || ("PATCH".equals(metodo) && (caminho.matches(BASE + "/requisitos/[^/]+/candidata")
                || caminho.matches(BASE + "/relacoes/[^/]+")))
                || ("DELETE".equals(metodo) && caminho.matches(BASE + "/relacoes/[^/]+"))) {
            return Papel.EDITOR;
        }
        return null;
    }
}
