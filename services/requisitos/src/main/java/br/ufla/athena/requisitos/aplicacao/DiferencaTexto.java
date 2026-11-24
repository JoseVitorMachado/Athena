package br.ufla.athena.requisitos.aplicacao;

import java.util.ArrayList;
import java.util.List;

/** Comparação de textos por linhas usando a maior subsequência comum. */
public final class DiferencaTexto {
    private DiferencaTexto() { }

    public enum Tipo { IGUAL, ADICIONADA, REMOVIDA }
    public record Linha(Tipo tipo, String texto) { }

    public static List<Linha> comparar(String anterior, String atual) {
        String[] a = linhas(anterior), b = linhas(atual);
        if (a.length > 2000 || b.length > 2000) throw new IllegalArgumentException("CONTEUDO_GRANDE_DEMAIS");
        int[][] lcs = new int[a.length + 1][b.length + 1];
        for (int i = a.length - 1; i >= 0; i--) {
            for (int j = b.length - 1; j >= 0; j--) {
                lcs[i][j] = a[i].equals(b[j]) ? 1 + lcs[i + 1][j + 1]
                        : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
            }
        }
        List<Linha> resultado = new ArrayList<>();
        int i = 0, j = 0;
        while (i < a.length || j < b.length) {
            if (i < a.length && j < b.length && a[i].equals(b[j])) {
                resultado.add(new Linha(Tipo.IGUAL, a[i++])); j++;
            } else if (i < a.length && (j == b.length || lcs[i + 1][j] >= lcs[i][j + 1])) {
                resultado.add(new Linha(Tipo.REMOVIDA, a[i++]));
            } else {
                resultado.add(new Linha(Tipo.ADICIONADA, b[j++]));
            }
        }
        return List.copyOf(resultado);
    }

    private static String[] linhas(String texto) {
        return texto.isEmpty() ? new String[0] : texto.split("\\R", -1);
    }
}
