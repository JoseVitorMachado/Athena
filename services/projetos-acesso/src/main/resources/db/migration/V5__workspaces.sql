CREATE TABLE workspaces (
    id uuid PRIMARY KEY,
    nome varchar(120) NOT NULL,
    owner_id uuid NOT NULL,
    excluido_em timestamptz,
    exclusao_id uuid,
    revisao bigint NOT NULL DEFAULT 0,
    criado_em timestamptz NOT NULL DEFAULT now(),
    atualizado_em timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE projetos ADD COLUMN excluido_em timestamptz;

INSERT INTO workspaces(id, nome, owner_id)
SELECT DISTINCT ON (workspace_id) workspace_id, 'Workspace', owner_id
FROM projetos ORDER BY workspace_id, criado_em
ON CONFLICT DO NOTHING;

CREATE INDEX workspaces_owner_idx ON workspaces (owner_id);
