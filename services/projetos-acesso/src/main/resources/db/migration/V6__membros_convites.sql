ALTER TABLE membros ADD COLUMN atualizado_em timestamptz NOT NULL DEFAULT now();

CREATE TABLE historico_membros (
    id uuid PRIMARY KEY, projeto_id uuid NOT NULL, membro_id uuid NOT NULL, autor_id uuid NOT NULL,
    papel_anterior varchar(20), papel_novo varchar(20), data timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX historico_membros_projeto_data_idx ON historico_membros (projeto_id, data DESC);

CREATE TABLE convites (
    id uuid PRIMARY KEY, projeto_id uuid NOT NULL REFERENCES projetos(id),
    email varchar(254) NOT NULL CHECK (email = lower(email)),
    papel varchar(20) NOT NULL CHECK (papel IN ('LEITOR','REPORTER','EDITOR','APROVADOR','MANTENEDOR')),
    criado_por uuid NOT NULL, criado_em timestamptz NOT NULL DEFAULT now(),
    atualizado_em timestamptz NOT NULL DEFAULT now(), expira_em timestamptz NOT NULL,
    aceito_em timestamptz, aceito_por uuid, cancelado_em timestamptz
);
CREATE UNIQUE INDEX convites_pendente_idx ON convites (projeto_id, email) WHERE aceito_em IS NULL AND cancelado_em IS NULL;
CREATE INDEX convites_email_idx ON convites (email);

CREATE TABLE transferencias_propriedade (
    id uuid PRIMARY KEY, projeto_id uuid NOT NULL REFERENCES projetos(id), owner_atual_id uuid NOT NULL,
    destinatario_id uuid NOT NULL, criado_em timestamptz NOT NULL DEFAULT now(),
    atualizado_em timestamptz NOT NULL DEFAULT now(), aceita_em timestamptz, cancelada_em timestamptz
);
CREATE UNIQUE INDEX transferencias_pendente_idx ON transferencias_propriedade (projeto_id) WHERE aceita_em IS NULL AND cancelada_em IS NULL;
