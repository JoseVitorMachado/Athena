package br.ufla.athena.rastreabilidade.aplicacao;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class BarreiraExclusao extends OncePerRequestFilter {
    private static final Pattern CAMINHO = Pattern.compile("^/api/v1/projetos/([^/]+)(?:/.*)?$");
    private final JdbcTemplate banco;
    private final ObjectMapper json;
    private final TransactionTemplate transacao;

    public BarreiraExclusao(JdbcTemplate banco, ObjectMapper json, TransactionTemplate transacao) {
        this.banco = banco; this.json = json; this.transacao = transacao;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        Matcher caminho = CAMINHO.matcher(requisicao.getRequestURI().substring(requisicao.getContextPath().length()));
        if (caminho.matches()) {
            UUID projeto;
            try { projeto = UUID.fromString(caminho.group(1)); }
            catch (IllegalArgumentException erro) { cadeia.doFilter(requisicao, resposta); return; }
            if (!"GET".equals(requisicao.getMethod())) {
                try {
                    transacao.executeWithoutResult(estado -> {
                        banco.query("SELECT pg_advisory_xact_lock(hashtext(?))", rs -> { }, "exclusao:" + projeto);
                        try { verificar(requisicao, resposta, cadeia, projeto); }
                        catch (IOException | ServletException erro) { throw new FalhaFiltro(erro); }
                    });
                } catch (FalhaFiltro erro) {
                    if (erro.getCause() instanceof IOException causa) throw causa;
                    throw (ServletException) erro.getCause();
                }
                return;
            }
            verificar(requisicao, resposta, cadeia, projeto);
            return;
        }
        cadeia.doFilter(requisicao, resposta);
    }

    private void verificar(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia, UUID projeto)
            throws IOException, ServletException {
            boolean excluido = Boolean.TRUE.equals(banco.queryForObject(
                    "SELECT EXISTS (SELECT 1 FROM projetos_excluidos WHERE projeto_id = ?)", Boolean.class, projeto));
            boolean bloqueado = !"GET".equals(requisicao.getMethod()) && Boolean.TRUE.equals(banco.queryForObject(
                    "SELECT EXISTS (SELECT 1 FROM projetos_bloqueados WHERE projeto_id = ?)", Boolean.class, projeto));
            if (excluido || bloqueado) {
                int status = excluido && "GET".equals(requisicao.getMethod()) ? 404 : 409;
                resposta.setStatus(status);
                resposta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                json.writeValue(resposta.getOutputStream(), Map.of("status", status,
                        "title", status == 404 ? "Not Found" : "Conflict",
                        "detail", status == 404 ? "Projeto excluído." : "Projeto em exclusão.",
                        "codigo", status == 404 ? "PROJETO_EXCLUIDO" : "PROJETO_EM_EXCLUSAO"));
                return;
            }
        cadeia.doFilter(requisicao, resposta);
    }

    private static class FalhaFiltro extends RuntimeException {
        FalhaFiltro(Exception causa) { super(causa); }
    }
}
