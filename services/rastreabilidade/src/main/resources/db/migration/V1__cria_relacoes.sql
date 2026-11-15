-- Modelo de escrita das relações oficiais (livro de arquitetura §3.5 e §5.4).
-- A projeção de leitura do mapa (CQRS) fica no mesmo banco, em tabelas próprias,
-- e entra na Parte 4 junto com os consumidores de eventos.

CREATE TABLE relacoes (
    id            uuid        PRIMARY KEY,
    projeto_id    uuid        NOT NULL,
    origem_id     uuid        NOT NULL,
    destino_id    uuid        NOT NULL,
    tipo          varchar(20) NOT NULL,
    justificativa varchar(1000),
    revisao       bigint      NOT NULL DEFAULT 0,
    criado_em     timestamptz NOT NULL DEFAULT now(),
    atualizado_em timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT relacoes_tipo_valido CHECK (tipo IN ('DEPENDE_DE', 'BLOQUEIA', 'RELACIONADO_A')),
    -- Autorrelação é proibida.
    CONSTRAINT relacoes_sem_autorrelacao CHECK (origem_id <> destino_id),
    -- RELACIONADO_A não tem direção: os extremos ficam em ordem canônica, o que
    -- impede a duplicata com os extremos invertidos.
    CONSTRAINT relacoes_relacionado_a_canonico CHECK (tipo <> 'RELACIONADO_A' OR origem_id < destino_id),
    CONSTRAINT relacoes_sem_duplicata UNIQUE (projeto_id, origem_id, destino_id, tipo)
);

CREATE INDEX relacoes_listagem_idx ON relacoes (projeto_id, atualizado_em DESC, id);
CREATE INDEX relacoes_destino_idx ON relacoes (projeto_id, destino_id);
