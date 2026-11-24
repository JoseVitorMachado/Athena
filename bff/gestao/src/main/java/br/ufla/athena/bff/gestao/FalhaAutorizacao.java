package br.ufla.athena.bff.gestao;

public class FalhaAutorizacao extends RuntimeException {
    private final int status;
    private final String codigo;

    public FalhaAutorizacao(int status, String codigo, String mensagem) {
        super(mensagem);
        this.status = status;
        this.codigo = codigo;
    }

    public int status() {
        return status;
    }

    public String codigo() {
        return codigo;
    }
}
