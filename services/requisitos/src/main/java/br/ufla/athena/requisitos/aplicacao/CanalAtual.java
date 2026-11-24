package br.ufla.athena.requisitos.aplicacao;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class CanalAtual {
    private CanalAtual() { }

    public static String cabecalho(String nome) {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes atributos
                ? atributos.getRequest().getHeader(nome) : null;
    }
}
