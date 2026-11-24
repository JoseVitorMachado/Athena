package br.ufla.athena.projetosacesso.seguranca;

import br.ufla.athena.projetosacesso.api.Problema;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class Tokens {
    private final ChavesJwt chaves;
    private final SecureRandom aleatorio = new SecureRandom();

    public Tokens(ChavesJwt chaves) {
        this.chaves = chaves;
    }

    public String opaco() {
        byte[] bytes = new byte[32];
        aleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String valor) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public String emitir(UUID usuario, UUID sessao, String nome, String email, Instant agora, Instant expira) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder().issuer("urn:athena").audience("athena-api")
                    .subject(usuario.toString()).claim("sid", sessao.toString()).claim("nome", nome).claim("email", email)
                    .issueTime(Date.from(agora)).expirationTime(Date.from(expira)).build();
            SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(chaves.kid()).build(), claims);
            jwt.sign(new RSASSASigner(chaves.chavePrivada()));
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao assinar token", e);
        }
    }

    public JWTClaimsSet validar(String authorization) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) throw new IllegalArgumentException();
            SignedJWT jwt = SignedJWT.parse(authorization.substring(7));
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (!JWSAlgorithm.RS256.equals(jwt.getHeader().getAlgorithm()) || !chaves.kid().equals(jwt.getHeader().getKeyID())
                    || !jwt.verify(new RSASSAVerifier(chaves.chavePublica())) || !"urn:athena".equals(claims.getIssuer())
                    || !claims.getAudience().contains("athena-api") || claims.getExpirationTime() == null
                    || !claims.getExpirationTime().toInstant().isAfter(Instant.now())) throw new IllegalArgumentException();
            UUID.fromString(claims.getSubject());
            UUID.fromString(claims.getStringClaim("sid"));
            return claims;
        } catch (Exception e) {
            throw new Problema(HttpStatus.UNAUTHORIZED, "TOKEN_INVALIDO", "Credencial inválida.");
        }
    }
}
