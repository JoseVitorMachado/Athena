package br.ufla.athena.projetosacesso.api;

import br.ufla.athena.projetosacesso.seguranca.Tokens;
import br.ufla.athena.projetosacesso.seguranca.RotacaoRefresh;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
public class IdentidadeController {
    private final JdbcTemplate banco;
    private final Tokens tokens;
    private final boolean cookieSeguro;
    private final Argon2PasswordEncoder senhas = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    private final String hashFalso = senhas.encode(UUID.randomUUID().toString());
    // Limite em memória por instância; não é compartilhado entre réplicas.
    private final Map<String, List<Instant>> falhas = new ConcurrentHashMap<>();

    public IdentidadeController(JdbcTemplate banco, Tokens tokens, @Value("${athena.cookie.seguro:true}") boolean cookieSeguro) {
        this.banco = banco;
        this.tokens = tokens;
        this.cookieSeguro = cookieSeguro;
    }

    record Cadastro(@NotBlank @Size(max = 120) String nome, @NotBlank @Email @Size(max = 254) String email, @NotBlank String senha) {}
    record Login(@NotBlank @Email String email, @NotBlank String senha, boolean lembrarDispositivo) {}
    record Usuario(UUID usuarioId, String nome, String email) {}
    record Sessao(String accessToken, String tokenType, Instant expiraEm, String csrfToken) {}
    record Conta(UUID id, String nome, String email, String hash) {}
    record Estado(UUID id, UUID usuario, Instant expira, boolean lembrar, Instant revogada, String csrf, UUID familia, Instant consumido, Instant tokenExpira) {}

    @PostMapping("/api/v1/autenticacao/cadastro")
    public ResponseEntity<Usuario> cadastro(@Valid @RequestBody Cadastro entrada) {
        if (!senhaForte(entrada.senha())) throw new Problema(HttpStatus.BAD_REQUEST, "SENHA_FRACA", "A senha deve conter ao menos oito caracteres, letra, número e caractere especial.");
        UUID id = UUID.randomUUID();
        String email = entrada.email().trim().toLowerCase(Locale.ROOT);
        try {
            banco.update("INSERT INTO usuarios(id,nome,email,senha_hash) VALUES (?,?,?,?)", id, entrada.nome().trim(), email, senhas.encode(entrada.senha()));
        } catch (DataIntegrityViolationException e) {
            throw new Problema(HttpStatus.CONFLICT, "EMAIL_JA_CADASTRADO", "E-mail já cadastrado.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new Usuario(id, entrada.nome().trim(), email));
    }

    static boolean senhaForte(String senha) {
        return senha != null && senha.length() >= 8 && senha.codePoints().anyMatch(Character::isLetter)
                && senha.codePoints().anyMatch(Character::isDigit)
                && senha.codePoints().anyMatch(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c));
    }

    @PostMapping("/api/v1/autenticacao/login")
    @Transactional
    public ResponseEntity<?> login(@Valid @RequestBody Login entrada) {
        String email = entrada.email().trim().toLowerCase(Locale.ROOT);
        Instant agora = Instant.now();
        synchronized (falhas) {
            List<Instant> recentes = falhas.getOrDefault(email, List.of()).stream().filter(i -> i.plusSeconds(900).isAfter(agora)).toList();
            falhas.put(email, recentes);
            if (recentes.size() >= 5) {
                long espera = Math.max(1, Duration.between(agora, recentes.get(0).plusSeconds(900)).toSeconds());
                return ResponseEntity.status(429).header(HttpHeaders.RETRY_AFTER, Long.toString(espera)).contentType(org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON)
                        .body(Map.of("type", "about:blank", "title", "Too Many Requests", "status", 429, "codigo", "MUITAS_TENTATIVAS"));
            }
        }
        List<Conta> contas = banco.query("SELECT id,nome,email,senha_hash FROM usuarios WHERE email=?", (rs, n) -> new Conta(rs.getObject(1, UUID.class), rs.getString(2), rs.getString(3), rs.getString(4)), email);
        Conta conta = contas.isEmpty() ? null : contas.get(0);
        if (!senhas.matches(entrada.senha(), conta == null ? hashFalso : conta.hash())) {
            synchronized (falhas) {
                java.util.ArrayList<Instant> lista = new java.util.ArrayList<>(falhas.getOrDefault(email, List.of()));
                lista.add(agora);
                falhas.put(email, lista);
            }
            throw new Problema(HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS", "Credenciais inválidas.");
        }
        falhas.remove(email);
        UUID id = UUID.randomUUID();
        Instant fim = agora.plus(Duration.ofDays(entrada.lembrarDispositivo() ? 30 : 1));
        String csrf = tokens.opaco();
        String refresh = tokens.opaco();
        banco.update("INSERT INTO sessoes(id,usuario_id,criada_em,expira_em,lembrar,csrf_hash) VALUES (?,?,?,?,?,?)", id, conta.id(), java.sql.Timestamp.from(agora), java.sql.Timestamp.from(fim), entrada.lembrarDispositivo(), Tokens.hash(csrf));
        inserirRefresh(refresh, id, UUID.randomUUID(), csrf, agora, fim);
        return resposta(conta, id, agora, fim, csrf, refresh, entrada.lembrarDispositivo());
    }

    @PostMapping("/api/v1/autenticacao/renovacao")
    @Transactional(noRollbackFor = Problema.class)
    public ResponseEntity<Sessao> renovar(@CookieValue(value = "athenaRefresh", required = false) String refresh, @RequestHeader(value = "X-CSRF-Token", required = false) String csrf) {
        Estado estado = estado(refresh);
        Instant agora = Instant.now();
        RotacaoRefresh.Resultado resultado = RotacaoRefresh.avaliar(estado.consumido(), estado.revogada(), estado.expira(), estado.tokenExpira(), agora);
        // O reuso é avaliado antes do CSRF: um refresh já consumido indica cópia
        // vazada, e a sessão inteira cai mesmo que o CSRF não confira (livro §7.2).
        if (resultado == RotacaoRefresh.Resultado.REUTILIZADO) {
            banco.update("UPDATE sessoes SET revogada_em=? WHERE id=? AND revogada_em IS NULL", java.sql.Timestamp.from(agora), estado.id());
            throw new Problema(HttpStatus.UNAUTHORIZED, "REFRESH_REUTILIZADO", "Credencial de renovação reutilizada.");
        }
        if (resultado == RotacaoRefresh.Resultado.ENCERRADO) throw new Problema(HttpStatus.UNAUTHORIZED, "SESSAO_ENCERRADA", "Sessão encerrada.");
        validarCsrf(estado, csrf);
        banco.update("UPDATE tokens_renovacao SET consumido_em=? WHERE hash=?", java.sql.Timestamp.from(agora), Tokens.hash(refresh));
        String novo = tokens.opaco();
        String novoCsrf = tokens.opaco();
        banco.update("UPDATE sessoes SET csrf_hash=? WHERE id=?", Tokens.hash(novoCsrf), estado.id());
        inserirRefresh(novo, estado.id(), estado.familia(), novoCsrf, agora, estado.expira());
        return resposta(conta(estado.usuario()), estado.id(), agora, estado.expira(), novoCsrf, novo, estado.lembrar());
    }

    @PostMapping("/api/v1/autenticacao/logout")
    @Transactional
    public ResponseEntity<Void> logout(@CookieValue(value = "athenaRefresh", required = false) String refresh, @RequestHeader(value = "X-CSRF-Token", required = false) String csrf) {
        Estado estado = estado(refresh);
        validarCsrf(estado, csrf);
        if (estado.consumido() != null) throw new Problema(HttpStatus.UNAUTHORIZED, "REFRESH_INVALIDO", "Sessão inválida.");
        validarAtiva(estado, Instant.now());
        banco.update("UPDATE sessoes SET revogada_em=now() WHERE id=?", estado.id());
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie("", false, Duration.ZERO)).build();
    }

    @GetMapping("/api/v1/usuarios/me")
    public Usuario eu(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String bearer) {
        UUID usuario = usuarioAutenticado(bearer);
        Conta conta = conta(usuario);
        return new Usuario(conta.id(), conta.nome(), conta.email());
    }

    public UUID usuarioAutenticado(String bearer) {
        var claims = tokens.validar(bearer);
        UUID id = UUID.fromString(claims.getSubject());
        try {
            UUID sid = UUID.fromString(claims.getStringClaim("sid"));
            Integer numero = banco.queryForObject("SELECT count(*) FROM sessoes WHERE id=? AND usuario_id=? AND revogada_em IS NULL AND expira_em>now()", Integer.class, sid, id);
            if (numero == null || numero == 0) throw new IllegalArgumentException();
            return id;
        } catch (Exception e) {
            throw new Problema(HttpStatus.UNAUTHORIZED, "SESSAO_ENCERRADA", "Sessão encerrada.");
        }
    }

    // Endpoints internos não são roteados pelo Gateway (livro §6.1).
    @GetMapping("/api/v1/internal/sessoes/{id}")
    public Map<String, Object> sessao(@PathVariable UUID id) {
        return banco.query("SELECT usuario_id FROM sessoes WHERE id=? AND revogada_em IS NULL AND expira_em>now()", (rs, n) -> Map.<String, Object>of("sessaoId", id, "usuarioId", rs.getObject(1, UUID.class), "ativa", true), id).stream().findFirst().orElseThrow(() -> new Problema(HttpStatus.NOT_FOUND, "SESSAO_NAO_ENCONTRADA", "Sessão não encontrada."));
    }

    @GetMapping("/api/v1/internal/projetos/{projetoId}/membros/{usuarioId}")
    public Map<String, Object> membro(@PathVariable UUID projetoId, @PathVariable UUID usuarioId) {
        return banco.query("SELECT m.papel,p.arquivado FROM membros m JOIN projetos p ON p.id=m.projeto_id WHERE m.projeto_id=? AND m.usuario_id=?", (rs, n) -> Map.<String, Object>of("projetoId", projetoId, "usuarioId", usuarioId, "papel", rs.getString(1), "arquivado", rs.getBoolean(2)), projetoId, usuarioId).stream().findFirst().orElseThrow(() -> new Problema(HttpStatus.NOT_FOUND, "MEMBRO_NAO_ENCONTRADO", "Membro não encontrado."));
    }

    private Estado estado(String refresh) {
        if (refresh == null) throw new Problema(HttpStatus.UNAUTHORIZED, "REFRESH_INVALIDO", "Sessão inválida.");
        return banco.query("SELECT s.id,s.usuario_id,s.expira_em,s.lembrar,s.revogada_em,t.csrf_hash,t.familia,t.consumido_em,t.expira_em FROM tokens_renovacao t JOIN sessoes s ON s.id=t.sessao_id WHERE t.hash=? FOR UPDATE OF s,t", (rs,n) -> new Estado(rs.getObject(1,UUID.class), rs.getObject(2,UUID.class), rs.getTimestamp(3).toInstant(), rs.getBoolean(4), rs.getTimestamp(5) == null ? null : rs.getTimestamp(5).toInstant(), rs.getString(6), rs.getObject(7,UUID.class), rs.getTimestamp(8) == null ? null : rs.getTimestamp(8).toInstant(), rs.getTimestamp(9).toInstant()), Tokens.hash(refresh)).stream().findFirst().orElseThrow(() -> new Problema(HttpStatus.UNAUTHORIZED, "REFRESH_INVALIDO", "Sessão inválida."));
    }
    private void validarCsrf(Estado s, String csrf) {
        if (csrf == null || !java.security.MessageDigest.isEqual(s.csrf().getBytes(java.nio.charset.StandardCharsets.US_ASCII), Tokens.hash(csrf).getBytes(java.nio.charset.StandardCharsets.US_ASCII))) throw new Problema(HttpStatus.FORBIDDEN, "CSRF_INVALIDO", "Token CSRF inválido.");
    }
    private void validarAtiva(Estado s, Instant agora) {
        if (s.revogada() != null || !s.expira().isAfter(agora) || !s.tokenExpira().isAfter(agora)) throw new Problema(HttpStatus.UNAUTHORIZED, "SESSAO_ENCERRADA", "Sessão encerrada.");
    }
    private Conta conta(UUID id) {
        return banco.queryForObject("SELECT id,nome,email,senha_hash FROM usuarios WHERE id=?", (rs,n) -> new Conta(rs.getObject(1,UUID.class),rs.getString(2),rs.getString(3),rs.getString(4)), id);
    }
    private void inserirRefresh(String token, UUID sessao, UUID familia, String csrf, Instant agora, Instant fim) {
        banco.update("INSERT INTO tokens_renovacao(hash,sessao_id,familia,csrf_hash,criado_em,expira_em) VALUES (?,?,?,?,?,?)", Tokens.hash(token), sessao, familia, Tokens.hash(csrf), java.sql.Timestamp.from(agora), java.sql.Timestamp.from(fim));
    }
    private ResponseEntity<Sessao> resposta(Conta conta, UUID id, Instant agora, Instant fim, String csrf, String refresh, boolean lembrar) {
        Instant expiracao = agora.plusSeconds(900).isBefore(fim) ? agora.plusSeconds(900) : fim;
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie(refresh, lembrar, Duration.between(agora, fim)))
                .body(new Sessao(tokens.emitir(conta.id(), id, conta.nome(), conta.email(), agora, expiracao), "Bearer", expiracao, csrf));
    }
    private String cookie(String valor, boolean persistente, Duration prazo) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("athenaRefresh", valor).path("/api/v1/autenticacao").httpOnly(true).secure(cookieSeguro).sameSite("Lax");
        if (persistente || prazo.isZero()) builder.maxAge(prazo);
        return builder.build().toString();
    }
}
