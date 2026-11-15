package br.ufla.athena.bff.gestao;

/** O serviço de domínio não respondeu (conexão recusada ou timeout). */
public class DominioIndisponivelException extends RuntimeException {

    private final String servico;

    public DominioIndisponivelException(String servico, Throwable causa) {
        super("Serviço " + servico + " indisponível", causa);
        this.servico = servico;
    }

    public String getServico() {
        return servico;
    }
}
