CREATE TABLE retratos (
    id uuid PRIMARY KEY,
    projeto_id uuid NOT NULL,
    requisito_id uuid NOT NULL,
    submissao_id uuid NOT NULL UNIQUE,
    versao_id uuid NOT NULL,
    hash_conteudo varchar(64) NOT NULL,
    estado varchar(20) NOT NULL CHECK (estado IN ('ATIVO', 'CANCELADO')),
    relacoes jsonb NOT NULL,
    criado_em timestamptz NOT NULL DEFAULT now(),
    atualizado_em timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE resultados_comando (
    command_id uuid PRIMARY KEY,
    message_type varchar(100) NOT NULL,
    resposta_tipo varchar(100) NOT NULL,
    resposta_payload jsonb NOT NULL,
    criado_em timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE cancelamentos_submissao (
    submissao_id uuid PRIMARY KEY,
    cancelado_em timestamptz NOT NULL DEFAULT now()
);
