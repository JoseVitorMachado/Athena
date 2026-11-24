package br.ufla.athena.requisitos.aplicacao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

public final class RegrasComprovante {
    private RegrasComprovante() { }

    public static String hash(String valor) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String hashAcao(String acao, UUID alvo, UUID versao, String decisao) {
        return hash(acao + ":" + alvo + ":" + versao + (decisao == null ? "" : ":" + decisao));
    }

    public record Vinculos(UUID usuario, UUID projeto, String acao, UUID alvo, UUID versao, String hashAcao) { }

    public static boolean valido(Vinculos registrado, Vinculos esperado, Instant expira, Instant consumido, Instant agora) {
        return Objects.equals(registrado, esperado) && expira != null && expira.isAfter(agora) && consumido == null;
    }
}
