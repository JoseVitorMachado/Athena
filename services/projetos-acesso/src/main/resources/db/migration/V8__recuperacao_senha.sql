CREATE TABLE codigos_recuperacao (
    id uuid PRIMARY KEY,
    usuario_id uuid NOT NULL REFERENCES usuarios(id),
    hash varchar(64) NOT NULL,
    criado_em timestamptz NOT NULL,
    expira_em timestamptz NOT NULL,
    usado_em timestamptz,
    tentativas int NOT NULL DEFAULT 0,
    invalidado_em timestamptz
);
CREATE INDEX idx_codigos_recuperacao_usuario ON codigos_recuperacao(usuario_id);
