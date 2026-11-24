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
