CREATE TABLE notificacoes (
    id uuid PRIMARY KEY, destinatario_id uuid NOT NULL, projeto_id uuid,
    tipo varchar(40) NOT NULL, mensagem varchar(500) NOT NULL, link varchar(500),
    lida boolean NOT NULL DEFAULT false, origem varchar(200),
    criada_em timestamptz NOT NULL DEFAULT now(), atualizado_em timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX notificacoes_destinatario_lida_criada_idx ON notificacoes (destinatario_id, lida, criada_em DESC);
CREATE UNIQUE INDEX notificacoes_destinatario_origem_idx ON notificacoes (destinatario_id, origem) WHERE origem IS NOT NULL;
