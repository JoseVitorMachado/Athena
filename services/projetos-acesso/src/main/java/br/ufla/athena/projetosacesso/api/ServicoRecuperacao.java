package br.ufla.athena.projetosacesso.api;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class ServicoRecuperacao {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ServicoRecuperacao.class);
    private final JdbcTemplate banco;
    private final EnvioEmail envio;
    private final TransactionTemplate transacao;
    private final String segredo;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Argon2PasswordEncoder senhas = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    public ServicoRecuperacao(JdbcTemplate banco, EnvioEmail envio, TransactionTemplate transacao,
                              @Value("${athena.recuperacao.segredo}") String segredo) {
        this.banco = banco;
        this.envio = envio;
        this.transacao = transacao;
        this.segredo = segredo;
    }

    public void solicitar(String email) {
        executor.execute(() -> {
            try {
            String codigo = CodigosRecuperacao.gerar();
            Boolean criado = transacao.execute(status -> {
                var contas = banco.query("SELECT id FROM usuarios WHERE email=? FOR UPDATE", (rs, n) -> rs.getObject(1, UUID.class), email);
                if (contas.isEmpty()) return false;
                UUID usuario = contas.get(0);
                Instant agora = Instant.now();
                banco.update("UPDATE codigos_recuperacao SET invalidado_em=? WHERE usuario_id=? AND usado_em IS NULL AND invalidado_em IS NULL", Timestamp.from(agora), usuario);
                banco.update("INSERT INTO codigos_recuperacao(id,usuario_id,hash,criado_em,expira_em) VALUES (?,?,?,?,?)", UUID.randomUUID(), usuario, CodigosRecuperacao.hash(codigo, segredo), Timestamp.from(agora), Timestamp.from(agora.plusSeconds(600)));
                return true;
            });
            if (Boolean.TRUE.equals(criado)) envio.enviar(email, codigo);
            } catch (RuntimeException e) {
                LOG.warn("Não foi possível processar o pedido de recuperação.");
            }
        });
    }

    record Codigo(UUID id, String hash, Instant expira, Instant usado, Instant invalidado, int tentativas) {}

    @Transactional(noRollbackFor = Problema.class)
    public void concluir(String email, String codigo, String novaSenha) {
        var contas = banco.query("SELECT id FROM usuarios WHERE email=? FOR UPDATE", (rs, n) -> rs.getObject(1, UUID.class), email);
        if (contas.isEmpty()) throw invalido();
        UUID usuario = contas.get(0);
        var codigos = banco.query("SELECT id,hash,expira_em,usado_em,invalidado_em,tentativas FROM codigos_recuperacao WHERE usuario_id=? ORDER BY criado_em DESC LIMIT 1 FOR UPDATE", (rs, n) -> new Codigo(rs.getObject(1, UUID.class), rs.getString(2), rs.getTimestamp(3).toInstant(), rs.getTimestamp(4) == null ? null : rs.getTimestamp(4).toInstant(), rs.getTimestamp(5) == null ? null : rs.getTimestamp(5).toInstant(), rs.getInt(6)), usuario);
        Instant agora = Instant.now();
        if (codigos.isEmpty()) throw invalido();
        Codigo atual = codigos.get(0);
        if (!RegrasRecuperacao.valido(atual.expira(), atual.usado(), atual.invalidado(), atual.tentativas(), agora)) throw invalido();
        if (codigo == null || !codigo.matches("[0-9]{6}") || !MessageDigest.isEqual(atual.hash().getBytes(StandardCharsets.US_ASCII), CodigosRecuperacao.hash(codigo, segredo).getBytes(StandardCharsets.US_ASCII))) {
            banco.update("UPDATE codigos_recuperacao SET tentativas=tentativas+1, invalidado_em=? WHERE id=?", RegrasRecuperacao.invalidarAposFalha(atual.tentativas()) ? Timestamp.from(agora) : null, atual.id());
            throw invalido();
        }
        if (!IdentidadeController.senhaForte(novaSenha)) throw new Problema(HttpStatus.BAD_REQUEST, "SENHA_FRACA", "A senha deve conter ao menos oito caracteres, letra, número e caractere especial.");
        banco.update("UPDATE usuarios SET senha_hash=?, atualizado_em=? WHERE id=?", senhas.encode(novaSenha), Timestamp.from(agora), usuario);
        banco.update("UPDATE codigos_recuperacao SET usado_em=? WHERE id=?", Timestamp.from(agora), atual.id());
        banco.update("UPDATE sessoes SET revogada_em=? WHERE usuario_id=? AND revogada_em IS NULL", Timestamp.from(agora), usuario);
        banco.update("UPDATE tokens_renovacao SET consumido_em=? WHERE consumido_em IS NULL AND sessao_id IN (SELECT id FROM sessoes WHERE usuario_id=?)", Timestamp.from(agora), usuario);
    }

    private Problema invalido() {
        return new Problema(HttpStatus.BAD_REQUEST, "CODIGO_INVALIDO", "Código inválido ou expirado.");
    }

    @PreDestroy
    public void encerrar() { executor.shutdown(); }
}
