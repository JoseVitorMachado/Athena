package br.ufla.athena.rastreabilidade.mensageria;

public class MensagemInvalidaException extends RuntimeException {
    public MensagemInvalidaException(String motivo) { super(motivo); }
}
