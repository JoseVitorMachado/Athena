package br.ufla.athena.rastreabilidade.dominio;

/** Catálogo de relações do MVP (livro §5.4). */
public enum TipoRelacao {
    DEPENDE_DE,
    BLOQUEIA,
    /** Sem direção: os extremos são gravados em ordem canônica. */
    RELACIONADO_A
}
