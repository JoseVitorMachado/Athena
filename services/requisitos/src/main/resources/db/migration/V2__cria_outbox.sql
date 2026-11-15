-- Outbox (livro de arquitetura §6.8): a intenção de publicar uma mensagem é
-- gravada na mesma transação local do efeito de negócio. O publicador que lê
-- esta tabela e envia ao RabbitMQ entra na Parte 4; a estrutura já fica pronta.

CREATE TABLE outbox (
    id               uuid         PRIMARY KEY,
    tipo             varchar(120) NOT NULL,
    saga_id          uuid,
    payload          jsonb        NOT NULL,
    criado_em        timestamptz  NOT NULL DEFAULT now(),
    tentativas       integer      NOT NULL DEFAULT 0,
    proxima_tentativa timestamptz NOT NULL DEFAULT now(),
    publicado_em     timestamptz
);

CREATE INDEX outbox_pendentes_idx ON outbox (proxima_tentativa) WHERE publicado_em IS NULL;
