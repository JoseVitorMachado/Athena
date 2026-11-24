package br.ufla.athena.integracoes.mensageria;

public class MensagemInvalidaException extends RuntimeException {
    public MensagemInvalidaException(String motivo) { super(motivo); }
}
