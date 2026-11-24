package br.ufla.athena.bff.gestao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class TratadorDeErros extends ResponseEntityExceptionHandler {

    @ExceptionHandler(FalhaAutorizacao.class)
    public ProblemDetail falhaAutorizacao(FalhaAutorizacao e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(e.status()), e.getMessage());
        problema.setProperty("codigo", e.codigo());
        return problema;
    }

    @Override
    protected org.springframework.http.ResponseEntity<Object> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException e,
            org.springframework.http.HttpHeaders cabecalhos, org.springframework.http.HttpStatusCode status,
            org.springframework.web.context.request.WebRequest pedido) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido.");
        problema.setProperty("codigo", "ENTRADA_INVALIDA");
        return org.springframework.http.ResponseEntity.badRequest()
                .contentType(org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON).body(problema);
    }

    /** Sem o serviço dono do dado o BFF não inventa resposta: 503 em Problem Details. */
    @ExceptionHandler(DominioIndisponivelException.class)
    public ProblemDetail dominioIndisponivel(DominioIndisponivelException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "O serviço " + e.getServico() + " não respondeu. Tente novamente em instantes.");
        problema.setTitle("Serviço indisponível");
        problema.setProperty("codigo", "DOMINIO_INDISPONIVEL");
        return problema;
    }
}
