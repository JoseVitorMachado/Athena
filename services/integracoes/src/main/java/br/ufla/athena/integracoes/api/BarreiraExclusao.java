package br.ufla.athena.integracoes.api;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class BarreiraExclusao extends OncePerRequestFilter {
    private static final Pattern CAMINHO = Pattern.compile("^/api/v1/projetos/([^/]+)(?:/.*)?$");
    private final JdbcTemplate banco;
    private final ObjectMapper json;

    public BarreiraExclusao(JdbcTemplate banco, ObjectMapper json) {
        this.banco = banco;
        this.json = json;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        Matcher caminho = CAMINHO.matcher(requisicao.getRequestURI().substring(requisicao.getContextPath().length()));
        if (!caminho.matches()) { cadeia.doFilter(requisicao, resposta); return; }
        UUID projetoId;
        try { projetoId = UUID.fromString(caminho.group(1)); }
        catch (IllegalArgumentException erro) { cadeia.doFilter(requisicao, resposta); return; }
        boolean leitura = requisicao.getMethod().equals("GET");
        String tabela = leitura ? "projetos_excluidos" : "projetos_bloqueados";
        if (Boolean.TRUE.equals(banco.queryForObject("SELECT EXISTS (SELECT 1 FROM " + tabela + " WHERE projeto_id = ?)", Boolean.class, projetoId))) {
            int status = leitura ? 404 : 409;
            resposta.setStatus(status);
            resposta.setContentType("application/problem+json");
            json.writeValue(resposta.getOutputStream(), Map.of("status", status,
                    "title", leitura ? "Not Found" : "Conflict", "detail", leitura ? "Projeto excluído." : "Projeto em exclusão.",
                    "codigo", leitura ? "PROJETO_EXCLUIDO" : "PROJETO_EM_EXCLUSAO"));
            return;
        }
        cadeia.doFilter(requisicao, resposta);
    }
}
