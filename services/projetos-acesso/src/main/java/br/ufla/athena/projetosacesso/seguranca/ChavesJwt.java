package br.ufla.athena.projetosacesso.seguranca;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Par de chaves RSA do emissor de tokens (livro §7.1: RS256, chave privada só no
 * emissor, chaves públicas distribuídas por JWKS).
 *
 * A chave privada vem de um arquivo PEM (PKCS#8) apontado por configuração; no
 * Kubernetes esse arquivo é um Secret montado como volume. Sem arquivo, gera um
 * par efêmero para desenvolvimento.
 */
@Component
public class ChavesJwt {

    private static final Logger LOG = LoggerFactory.getLogger(ChavesJwt.class);

    private final RSAPublicKey chavePublica;
    private final String kid;

    public ChavesJwt(@Value("${athena.jwt.chave-privada-arquivo:}") String arquivo) {
        try {
            RSAPrivateCrtKey privada = arquivo.isBlank() ? gerarEfemera() : ler(Path.of(arquivo));
            KeyFactory fabrica = KeyFactory.getInstance("RSA");
            this.chavePublica = (RSAPublicKey) fabrica.generatePublic(
                    new RSAPublicKeySpec(privada.getModulus(), privada.getPublicExponent()));
            this.kid = identificador(chavePublica);
            LOG.info("Chave JWT carregada de {} (kid={})",
                    arquivo.isBlank() ? "par efêmero de desenvolvimento" : arquivo, kid);
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("Não foi possível carregar a chave JWT", e);
        }
    }

    public RSAPublicKey chavePublica() {
        return chavePublica;
    }

    public String kid() {
        return kid;
    }

    private static RSAPrivateCrtKey ler(Path arquivo) throws IOException, GeneralSecurityException {
        String pem = Files.readString(arquivo, StandardCharsets.US_ASCII)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(pem);
        return (RSAPrivateCrtKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    private static RSAPrivateCrtKey gerarEfemera() throws GeneralSecurityException {
        KeyPairGenerator gerador = KeyPairGenerator.getInstance("RSA");
        gerador.initialize(2048);
        return (RSAPrivateCrtKey) gerador.generateKeyPair().getPrivate();
    }

    /** kid estável: prefixo do SHA-256 da chave pública codificada. */
    private static String identificador(RSAPublicKey chave) throws GeneralSecurityException {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(chave.getEncoded());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash).substring(0, 16);
    }
}
