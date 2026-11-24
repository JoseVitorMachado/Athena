package br.ufla.athena.revisoes.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = LoteController.class)
public class ErrosConfirmacao {
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public org.springframework.http.ProblemDetail dadosInvalidos(Exception erro) {
        return new Problema(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Dados da requisição inválidos.").getBody();
    }
}
