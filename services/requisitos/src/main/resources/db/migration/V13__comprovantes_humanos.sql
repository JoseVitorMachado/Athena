CREATE TABLE comprovantes_humanos (
    id uuid PRIMARY KEY,
    hash_token varchar(64) UNIQUE NOT NULL,
    usuario_id uuid NOT NULL,
    projeto_id uuid NOT NULL,
    acao varchar(20) NOT NULL,
    alvo_id uuid NOT NULL,
    versao_id uuid NOT NULL,
    hash_acao varchar(64) NOT NULL,
    criado_em timestamptz NOT NULL DEFAULT now(),
    expira_em timestamptz NOT NULL,
    consumido_em timestamptz
);
