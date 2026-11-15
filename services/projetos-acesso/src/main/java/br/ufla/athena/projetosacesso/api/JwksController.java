package br.ufla.athena.projetosacesso.api;

import br.ufla.athena.projetosacesso.seguranca.ChavesJwt;
import java.math.BigInteger;
import java.util.Base64;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Chaves públicas do emissor (operação /oauth2/jwks de docs/api/projetos-acesso.yaml). */
@RestController
public class JwksController {

    private final ChavesJwt chaves;

    public JwksController(ChavesJwt chaves) {
        this.chaves = chaves;
    }

    @GetMapping("/oauth2/jwks")
    public Jwks jwks() {
        return new Jwks(List.of(new Jwk("RSA", chaves.kid(), "sig", "RS256",
                base64Url(chaves.chavePublica().getModulus()),
                base64Url(chaves.chavePublica().getPublicExponent()))));
    }

    /** Inteiro sem sinal em base64url, como pede a RFC 7518. */
    private static String base64Url(BigInteger valor) {
        byte[] bytes = valor.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            bytes = java.util.Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record Jwks(List<Jwk> keys) {
    }

    public record Jwk(String kty, String kid, String use, String alg, String n, String e) {
    }
}
