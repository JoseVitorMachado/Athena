package br.ufla.athena.requisitos.aplicacao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

public final class IdempotenciaSubmissao {
    private IdempotenciaSubmissao() { }

    public static String impressao(UUID requisitoId, UUID versaoId) {
        try {
            String pedido = requisitoId + ":" + versaoId;
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(pedido.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String impressaoPublicacao(UUID requisitoId, UUID versaoId, UUID submissaoId,
                                              String comprovanteHumano) {
        try {
            String pedido = requisitoId + ":" + versaoId + ":" + submissaoId + ":"
                    + (comprovanteHumano == null ? "-" : comprovanteHumano.length() + ":" + comprovanteHumano);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(pedido.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean mesmoPedido(String anterior, String atual) {
        return anterior.equals(atual);
    }
}
