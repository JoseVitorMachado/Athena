-- Preserva a última revisão e o instante do último cancelamento após a reativação.
CREATE TABLE requisitos_cancelados (
    projeto_id uuid NOT NULL,
    requisito_id uuid NOT NULL,
    revisao bigint NOT NULL CHECK (revisao >= 0),
    cancelado_em timestamptz,
    situacao varchar(9) NOT NULL CHECK (situacao IN ('ATIVO', 'CANCELADO')),
    PRIMARY KEY (projeto_id, requisito_id)
);
