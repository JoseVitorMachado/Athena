package br.ufla.athena.projetosacesso.mensageria;

public class MensagemInvalidaException extends RuntimeException {
    public MensagemInvalidaException(String motivo) { super(motivo); }
}
