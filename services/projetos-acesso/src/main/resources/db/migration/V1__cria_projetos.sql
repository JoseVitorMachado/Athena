-- Projetos (livro de arquitetura §4.1 e §5.1). Workspaces, membros, papéis,
-- contas e sessões entram em migrações próprias, junto com a autenticação.

CREATE TABLE projetos (
    id            uuid         PRIMARY KEY,
    workspace_id  uuid         NOT NULL,
    nome          varchar(120) NOT NULL,
    descricao     varchar(1000),
    arquivado     boolean      NOT NULL DEFAULT false,
    owner_id      uuid         NOT NULL,
    revisao       bigint       NOT NULL DEFAULT 0,
    criado_em     timestamptz  NOT NULL DEFAULT now(),
    atualizado_em timestamptz  NOT NULL DEFAULT now()
);

CREATE INDEX projetos_listagem_idx ON projetos (arquivado, atualizado_em DESC, id);
CREATE INDEX projetos_workspace_idx ON projetos (workspace_id);
