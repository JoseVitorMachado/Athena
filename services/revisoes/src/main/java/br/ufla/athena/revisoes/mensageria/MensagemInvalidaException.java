package br.ufla.athena.revisoes.mensageria;

public class MensagemInvalidaException extends RuntimeException {
    public MensagemInvalidaException(String motivo) { super(motivo); }
}
