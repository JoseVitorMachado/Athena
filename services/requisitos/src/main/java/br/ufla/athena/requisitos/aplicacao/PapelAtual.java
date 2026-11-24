package br.ufla.athena.requisitos.aplicacao;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Lê o papel informado pela rede interna. */
@Component
public class PapelAtual {
    /** Retorna o papel do pedido ou null quando ausente ou inválido. */
    public RegrasComentario.Papel obter() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes atributos) {
            String valor = atributos.getRequest().getHeader("X-Athena-Papel");
            try {
                return valor == null ? null : RegrasComentario.Papel.valueOf(valor);
            } catch (IllegalArgumentException ignorada) {
                return null;
            }
        }
        return null;
    }
}
