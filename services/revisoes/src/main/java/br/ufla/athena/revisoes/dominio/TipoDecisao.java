package br.ufla.athena.revisoes.dominio;

public enum TipoDecisao {
    APROVAR,
    REJEITAR,
    SOLICITAR_CORRECOES;

    /** Rejeitar ou pedir correções exige justificativa (schema DecisaoEntrada). */
    public boolean exigeJustificativa() {
        return this != APROVAR;
    }
}
