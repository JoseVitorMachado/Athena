package br.ufla.athena.requisitos.aplicacao;

import br.ufla.athena.requisitos.api.ConfirmacaoResposta;
import br.ufla.athena.requisitos.api.Problema;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

public final class ComprovantesHumanos {
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private ComprovantesHumanos() { }

    public static void autorizarEmissao() {
        if (!"INTERFACE".equals(CanalAtual.cabecalho("X-Athena-Canal")))
            throw new Problema(HttpStatus.FORBIDDEN, "CONFIRMACAO_EXIGE_INTERFACE", "A confirmação exige a interface.");
        if (!Set.of("APROVADOR", "MANTENEDOR", "OWNER").contains(
                java.util.Objects.toString(CanalAtual.cabecalho("X-Athena-Papel"), "")))
            throw new Problema(HttpStatus.FORBIDDEN, "PAPEL_INSUFICIENTE", "Papel insuficiente.");
    }

    public static UUID usuarioInterno() {
        String valor = CanalAtual.cabecalho("X-Athena-Usuario");
        try {
            UUID id = UUID.fromString(valor);
            if (id.toString().equalsIgnoreCase(valor)) return id;
        } catch (IllegalArgumentException | NullPointerException ignorada) {
            // Cabeçalho ausente ou inválido não identifica o usuário.
        }
        throw invalido();
    }

    public static ConfirmacaoResposta emitir(JdbcTemplate banco, UUID usuario, UUID projeto, String acao,
                                              UUID alvo, UUID versao, String hashAcao) {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        UUID id = UUID.randomUUID();
        Instant expira = Instant.now().plusSeconds(300);
        banco.update("INSERT INTO comprovantes_humanos (id, hash_token, usuario_id, projeto_id, acao, alvo_id, versao_id, hash_acao, expira_em) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id, RegrasComprovante.hash(token), usuario, projeto, acao, alvo, versao, hashAcao, Timestamp.from(expira));
        return new ConfirmacaoResposta(id, token, expira);
    }

    public static void consumir(JdbcTemplate banco, String token, RegrasComprovante.Vinculos esperado) {
        if (!"MCP".equals(CanalAtual.cabecalho("X-Athena-Canal"))) return;
        if (token == null || token.isBlank() || !usuarioInterno().equals(esperado.usuario())) throw invalido();
        var encontrados = banco.query("SELECT usuario_id, projeto_id, acao, alvo_id, versao_id, hash_acao, expira_em, consumido_em FROM comprovantes_humanos WHERE hash_token = ? FOR UPDATE",
                (rs, n) -> new Registro(new RegrasComprovante.Vinculos(rs.getObject(1, UUID.class), rs.getObject(2, UUID.class),
                        rs.getString(3), rs.getObject(4, UUID.class), rs.getObject(5, UUID.class), rs.getString(6)),
                        rs.getTimestamp(7).toInstant(), rs.getTimestamp(8) == null ? null : rs.getTimestamp(8).toInstant()),
                RegrasComprovante.hash(token));
        Instant agora = Instant.now();
        if (encontrados.isEmpty() || !RegrasComprovante.valido(encontrados.get(0).vinculos(), esperado,
                encontrados.get(0).expira(), encontrados.get(0).consumido(), agora)) throw invalido();
        banco.update("UPDATE comprovantes_humanos SET consumido_em = ? WHERE hash_token = ?", Timestamp.from(agora), RegrasComprovante.hash(token));
    }

    private static Problema invalido() {
        return new Problema(HttpStatus.FORBIDDEN, "COMPROVANTE_INVALIDO", "Confirmação humana ausente, expirada ou não corresponde à ação.");
    }

    private record Registro(RegrasComprovante.Vinculos vinculos, Instant expira, Instant consumido) { }
}
