package br.ufla.athena.revisoes.dominio;

/**
 * Estados do lote. PREPARADO e a ativação vêm do livro (§6.9); ENCERRADO e
 * CANCELADO completam o ciclo nesta implementação.
 */
public enum EstadoLote {
    /** Criado pela SAGA; ainda não aceita decisões. */
    PREPARADO,
    /** Aberto para decisão. */
    ATIVO,
    /** Recebeu a decisão final. */
    ENCERRADO,
    /** Cancelado por compensação da SAGA. */
    CANCELADO
}
