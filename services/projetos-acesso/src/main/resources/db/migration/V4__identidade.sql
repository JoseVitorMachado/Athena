CREATE TABLE usuarios (
    id uuid PRIMARY KEY,
    nome varchar(120) NOT NULL,
    email varchar(254) NOT NULL UNIQUE CHECK (email = lower(email)),
    senha_hash varchar(255) NOT NULL,
    criado_em timestamptz NOT NULL DEFAULT now(),
    atualizado_em timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE membros (
    projeto_id uuid NOT NULL REFERENCES projetos(id),
    usuario_id uuid NOT NULL REFERENCES usuarios(id),
    papel varchar(20) NOT NULL CHECK (papel IN ('LEITOR', 'REPORTER', 'EDITOR', 'APROVADOR', 'MANTENEDOR', 'OWNER')),
    criado_em timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (projeto_id, usuario_id)
);
CREATE TABLE sessoes (
    id uuid PRIMARY KEY,
    usuario_id uuid NOT NULL REFERENCES usuarios(id),
    criada_em timestamptz NOT NULL,
    expira_em timestamptz NOT NULL,
    lembrar boolean NOT NULL,
    revogada_em timestamptz,
    csrf_hash varchar(64) NOT NULL
);
CREATE TABLE tokens_renovacao (
    hash varchar(64) PRIMARY KEY,
    sessao_id uuid NOT NULL REFERENCES sessoes(id),
    familia uuid NOT NULL,
    csrf_hash varchar(64) NOT NULL,
    criado_em timestamptz NOT NULL,
    consumido_em timestamptz,
    expira_em timestamptz NOT NULL
);
