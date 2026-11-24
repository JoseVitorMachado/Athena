package br.ufla.athena.requisitos.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/**
 * Erro de negócio em Problem Details (RFC 9457), com o campo "codigo" previsto
 * no schema ProblemDetails de docs/api/_shared/components.yaml.
 */
public class Problema extends ErrorResponseException {

    public Problema(HttpStatus status, String codigo, String detalhe) {
        super(status, corpo(status, codigo, detalhe), null);
    }

    private static ProblemDetail corpo(HttpStatus status, String codigo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setProperty("codigo", codigo);
        return problema;
    }
}
