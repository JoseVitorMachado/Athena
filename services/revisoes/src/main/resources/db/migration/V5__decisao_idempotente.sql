ALTER TABLE decisoes DROP CONSTRAINT decisoes_aplicacao_valida;
ALTER TABLE decisoes ADD CONSTRAINT decisoes_aplicacao_valida
    CHECK (aplicacao_em_requisitos IN ('PENDENTE', 'APLICADA', 'DESCARTADA'));

CREATE TABLE chaves_idempotencia (
    projeto_id uuid NOT NULL,
    operacao varchar(100) NOT NULL,
    chave text NOT NULL,
    impressao varchar(64) NOT NULL,
    recurso_id uuid NOT NULL REFERENCES decisoes (id),
    criado_em timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (projeto_id, operacao, chave)
);
