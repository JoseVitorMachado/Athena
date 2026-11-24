package br.ufla.athena.rastreabilidade.mensageria;

/** Cinco tentativas no total (livro §6.7). */
public final class PoliticaRetentativa {
    private PoliticaRetentativa() { }

    public static long esperaMillis(int falhas) {
        return switch (falhas) {
            case 1 -> 2000;
            case 2 -> 10000;
            case 3 -> 30000;
            case 4 -> 60000;
            default -> 0;
        };
    }
}
