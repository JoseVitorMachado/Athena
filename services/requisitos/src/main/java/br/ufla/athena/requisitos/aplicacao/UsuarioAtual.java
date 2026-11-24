package br.ufla.athena.requisitos.aplicacao;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Identifica o autor informado pela rede interna, com alternativa de demonstração. */
@Component
public class UsuarioAtual {
    private final UUID demonstracao;

    public UsuarioAtual(@Value("${athena.usuario-demo}") UUID demonstracao) {
        this.demonstracao = demonstracao;
    }

    /** Retorna o usuário do pedido corrente, quando válido. */
    public UUID obter() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes atributos) {
            String valor = atributos.getRequest().getHeader("X-Athena-Usuario");
            try {
                if (valor != null) {
                    UUID id = UUID.fromString(valor);
                    if (id.toString().equalsIgnoreCase(valor)) return id;
                }
            } catch (IllegalArgumentException ignorada) {
                // Cabeçalhos inválidos usam o usuário de demonstração.
            }
        }
        return demonstracao;
    }
}
