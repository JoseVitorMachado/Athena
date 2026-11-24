-- Mensageria confiável (livro de arquitetura §6.8).
-- A outbox passa a guardar destino (exchange e routing key) e o último erro de
-- publicação; o publicador só marca publicado_em após a confirmação do broker.

ALTER TABLE outbox ADD COLUMN exchange varchar(100) NOT NULL DEFAULT 'athena.events';
ALTER TABLE outbox ADD COLUMN routing_key varchar(200) NOT NULL DEFAULT '';
ALTER TABLE outbox ADD COLUMN ultimo_erro varchar(1000);

-- A inbox impede que uma reentrega repita o efeito (livro §6.8).
CREATE TABLE inbox (
    message_id uuid PRIMARY KEY,
    tipo varchar(120) NOT NULL,
    command_id uuid,
    recebido_em timestamptz NOT NULL DEFAULT now(),
    processado_em timestamptz
);
