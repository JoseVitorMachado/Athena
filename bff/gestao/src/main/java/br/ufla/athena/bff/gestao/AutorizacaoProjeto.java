package br.ufla.athena.bff.gestao;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AutorizacaoProjeto implements HandlerInterceptor, WebMvcConfigurer {
    public static final String VINCULO = AutorizacaoProjeto.class.getName() + ".vinculo";
    private final PapelAtual papelAtual;

    public AutorizacaoProjeto(PapelAtual papelAtual) {
        this.papelAtual = papelAtual;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(this).addPathPatterns("/api/v1/gestao/projetos/**");
    }

    @Override
    public boolean preHandle(HttpServletRequest pedido, HttpServletResponse resposta, Object handler) {
        @SuppressWarnings("unchecked")
        Map<String, String> variaveis = (Map<String, String>) pedido.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (variaveis == null || !variaveis.containsKey("projetoId")) {
            throw new FalhaAutorizacao(403, "SEM_ACESSO_AO_PROJETO", "Acesso ao projeto negado.");
        }
        String projetoId;
        try {
            projetoId = UUID.fromString(variaveis.get("projetoId")).toString();
        } catch (IllegalArgumentException e) {
            throw new FalhaAutorizacao(403, "SEM_ACESSO_AO_PROJETO", "Acesso ao projeto negado.");
        }
        JwtAuthenticationToken autenticacao = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        PapelAtual.Vinculo vinculo = papelAtual.consultar(projetoId, autenticacao.getToken().getSubject());
        pedido.setAttribute(VINCULO, vinculo);
        String caminho = pedido.getRequestURI().substring(pedido.getContextPath().length());
        PapelAtual.Papel minimo = PoliticaProjeto.papelMinimo(pedido.getMethod(), caminho);
        if (vinculo.arquivado() && !"GET".equals(pedido.getMethod())) {
            throw new FalhaAutorizacao(409, "PROJETO_ARQUIVADO", "Projeto arquivado permite apenas leitura.");
        }
        if (minimo == null || !vinculo.papel().permite(minimo)) {
            throw new FalhaAutorizacao(403, "PAPEL_INSUFICIENTE", "Papel insuficiente para esta operação.");
        }
        return true;
    }
}
