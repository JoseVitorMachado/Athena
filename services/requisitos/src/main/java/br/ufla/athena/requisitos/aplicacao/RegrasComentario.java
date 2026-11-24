package br.ufla.athena.requisitos.aplicacao;

import java.util.UUID;

/** Permissões para criar e alterar comentários. */
public final class RegrasComentario {
    private RegrasComentario() { }

    public enum Papel { LEITOR, REPORTER, EDITOR, APROVADOR, MANTENEDOR, OWNER }

    /** Verifica o papel mínimo para registrar comentários. */
    public static boolean podeCriar(Papel papel) {
        return papel != null && papel.ordinal() >= Papel.REPORTER.ordinal();
    }

    /** Verifica a autoria necessária para alterar o texto. */
    public static boolean podeEditarTexto(UUID autor, UUID usuario) {
        return autor != null && autor.equals(usuario);
    }

    /** Verifica a permissão para resolver ou reabrir. */
    public static boolean podeResolver(Papel papel, UUID autor, UUID usuario) {
        return papel != null && (papel.ordinal() >= Papel.EDITOR.ordinal()
                || (podeCriar(papel) && podeEditarTexto(autor, usuario)));
    }
}
