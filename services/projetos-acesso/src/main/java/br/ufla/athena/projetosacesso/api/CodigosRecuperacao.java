package br.ufla.athena.projetosacesso.api;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class CodigosRecuperacao {
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private CodigosRecuperacao() {}

    public static String gerar() {
        return String.format(Locale.ROOT, "%06d", ALEATORIO.nextInt(1_000_000));
    }

    public static String hash(String codigo, String segredo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(codigo.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("Não foi possível proteger o código de recuperação.", e);
        }
    }
}
