CREATE TABLE baselines (
    id uuid PRIMARY KEY, projeto_id uuid NOT NULL, nome varchar(120) NOT NULL,
    autor_id uuid NOT NULL, criada_em timestamptz NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX baselines_nome_projeto_idx ON baselines (projeto_id, lower(nome));
CREATE INDEX baselines_listagem_idx ON baselines (projeto_id, criada_em DESC, id);

CREATE TABLE baseline_itens (
    baseline_id uuid NOT NULL REFERENCES baselines(id),
    requisito_id uuid NOT NULL REFERENCES requisitos(id),
    versao_id uuid NOT NULL REFERENCES versoes(id), retrato_id uuid NOT NULL,
    codigo varchar(20) NOT NULL, titulo varchar(300) NOT NULL, numero integer NOT NULL,
    PRIMARY KEY (baseline_id, requisito_id)
);
