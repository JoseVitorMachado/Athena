CREATE TABLE decisoes_aplicadas (
    decisao_id uuid PRIMARY KEY,
    versao_id uuid NOT NULL,
    resultado varchar(10) NOT NULL CHECK (resultado IN ('APLICADA', 'DESCARTADA')),
    motivo text,
    aplicada_em timestamptz NOT NULL DEFAULT now()
);
