-- Inbox (livro §6.8): registra cada mensagem recebida para que reentregas do
-- RabbitMQ não dupliquem trechos. O consumidor de eventos entra na Parte 4.

CREATE TABLE inbox (
    message_id    uuid         PRIMARY KEY,
    tipo          varchar(120) NOT NULL,
    recebido_em   timestamptz  NOT NULL DEFAULT now(),
    processado_em timestamptz
);
