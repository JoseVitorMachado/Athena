CREATE TABLE epicos (
    id uuid PRIMARY KEY, projeto_id uuid NOT NULL, titulo varchar(200) NOT NULL,
    descricao varchar(2000), revisao bigint NOT NULL DEFAULT 0,
    criado_em timestamptz NOT NULL DEFAULT now(), atualizado_em timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX epicos_projeto_idx ON epicos (projeto_id);
ALTER TABLE requisitos ADD COLUMN fora_do_livro boolean NOT NULL DEFAULT false;
CREATE TABLE alteracoes_situacao (
    id uuid PRIMARY KEY, projeto_id uuid NOT NULL, requisito_id uuid NOT NULL REFERENCES requisitos(id),
    situacao varchar(20) NOT NULL, candidata_id uuid, autor_id uuid NOT NULL,
    chave_idempotencia varchar(200), criado_em timestamptz NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX alteracoes_situacao_idempotencia_idx
    ON alteracoes_situacao (requisito_id, situacao, chave_idempotencia) WHERE chave_idempotencia IS NOT NULL;
