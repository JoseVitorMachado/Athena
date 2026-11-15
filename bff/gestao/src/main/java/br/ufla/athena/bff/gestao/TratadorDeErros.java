package br.ufla.athena.bff.gestao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class TratadorDeErros extends ResponseEntityExceptionHandler {

    /** Sem o serviço dono do dado o BFF não inventa resposta: 503 em Problem Details. */
    @ExceptionHandler(DominioIndisponivelException.class)
    public ProblemDetail dominioIndisponivel(DominioIndisponivelException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "O serviço " + e.getServico() + " não respondeu. Tente novamente em instantes.");
        problema.setTitle("Serviço indisponível");
        return problema;
    }
}
