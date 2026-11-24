CREATE TABLE projetos_bloqueados (
    projeto_id uuid PRIMARY KEY,
    exclusao_id uuid NOT NULL,
    fechado_em timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE projetos_excluidos (
    projeto_id uuid PRIMARY KEY,
    excluido_em timestamptz NOT NULL DEFAULT now()
);
