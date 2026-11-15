package br.ufla.athena.bff.livro;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Informa qual instância respondeu. Com várias réplicas do BFF, o cabeçalho
 * mostra o balanceamento entre elas (no Kubernetes, o nome do pod).
 */
@Component
public class InstanciaFilter extends OncePerRequestFilter {

    static final String CABECALHO = "X-Athena-Instancia";

    private final String instancia = nomeDoHost();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setHeader(CABECALHO, instancia);
        chain.doFilter(request, response);
    }

    private static String nomeDoHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "desconhecida";
        }
    }
}
