package br.ufla.athena.requisitos.mensageria;

public class MensagemInvalidaException extends RuntimeException {
    public MensagemInvalidaException(String motivo) { super(motivo); }
}
