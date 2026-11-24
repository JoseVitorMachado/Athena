package br.ufla.athena.projetosacesso.api;

import java.util.Locale;

/** Regras de hierarquia e conversão dos papéis de projeto. */
final class PoliticaMembros {
    private PoliticaMembros() { }

    enum Papel { LEITOR, REPORTER, EDITOR, APROVADOR, MANTENEDOR, OWNER }

    static Papel deContrato(String valor) {
        if (valor == null) throw new IllegalArgumentException("Papel obrigatório.");
        return Papel.valueOf(valor.trim().toUpperCase(Locale.ROOT).replace("Ó", "O"));
    }
    static String paraContrato(Papel papel) {
        if (papel == null) return null;
        return switch (papel) {
            case LEITOR -> "Leitor"; case REPORTER -> "Repórter"; case EDITOR -> "Editor";
            case APROVADOR -> "Aprovador"; case MANTENEDOR -> "Mantenedor"; case OWNER -> "Owner";
        };
    }
    static boolean podeGerenciar(Papel ator, Papel atual, Papel novo) {
        if (ator == null || atual == null || novo == null || atual == Papel.OWNER || novo == Papel.OWNER) return false;
        return ator == Papel.OWNER || ator == Papel.MANTENEDOR && atual.ordinal() <= Papel.APROVADOR.ordinal() && novo.ordinal() <= Papel.APROVADOR.ordinal();
    }
    static boolean podeRemover(Papel ator, Papel alvo, boolean proprio) {
        if (ator == null || alvo == null || alvo == Papel.OWNER) return false;
        return proprio || podeGerenciar(ator, alvo, alvo);
    }
    static boolean podeConvidar(Papel ator, Papel convite) {
        if (ator == null || convite == null || convite == Papel.OWNER) return false;
        return ator == Papel.OWNER || ator == Papel.MANTENEDOR && convite.ordinal() <= Papel.APROVADOR.ordinal();
    }
}
