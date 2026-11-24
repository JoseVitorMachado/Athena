package br.ufla.athena.requisitos.aplicacao;

import br.ufla.athena.requisitos.api.Problema;
import org.springframework.http.HttpStatus;

/** Validação da revisão exigida nas novas operações. */
public final class Precondicao {
    private Precondicao() { }

    public static void verificar(String ifMatch, long revisao) {
        if (ifMatch == null || ifMatch.isBlank())
            throw new Problema(HttpStatus.PRECONDITION_REQUIRED, "PRECONDICAO_OBRIGATORIA", "Informe If-Match.");
        if (!ifMatch.trim().equals("\"" + revisao + "\""))
            throw new Problema(HttpStatus.PRECONDITION_FAILED, "REVISAO_DIVERGENTE", "A revisão foi alterada.");
    }
}
