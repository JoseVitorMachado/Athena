-- Lotes de revisão, pendências e decisões (livro de arquitetura §6.9).

CREATE TABLE lotes (
    id            uuid        PRIMARY KEY,
    projeto_id    uuid        NOT NULL,
    requisito_id  uuid        NOT NULL,
    submissao_id  uuid        NOT NULL,
    versao_id     uuid        NOT NULL,
    hash_conteudo varchar(64) NOT NULL,
    -- O livro define PREPARADO (criado pela SAGA, ainda sem aceitar decisões) e a
    -- ativação posterior. ATIVO, ENCERRADO e CANCELADO são a proposta desta
    -- implementação para o restante do ciclo, que o contrato deixa em aberto.
    estado        varchar(20) NOT NULL DEFAULT 'PREPARADO',
    revisao       bigint      NOT NULL DEFAULT 0,
    criado_em     timestamptz NOT NULL DEFAULT now(),
    atualizado_em timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT lotes_estado_valido CHECK (estado IN ('PREPARADO', 'ATIVO', 'ENCERRADO', 'CANCELADO')),
    -- Cada tentativa de submissão tem o seu próprio lote.
    CONSTRAINT lotes_um_por_submissao UNIQUE (submissao_id)
);

CREATE INDEX lotes_listagem_idx ON lotes (projeto_id, atualizado_em DESC, id);
CREATE INDEX lotes_requisito_idx ON lotes (projeto_id, requisito_id);

CREATE TABLE pendencias (
    id             uuid         PRIMARY KEY,
    lote_id        uuid         NOT NULL REFERENCES lotes (id),
    descricao      varchar(500) NOT NULL,
    bloqueante     boolean      NOT NULL DEFAULT false,
    resolvida      boolean      NOT NULL DEFAULT false,
    responsavel_id uuid,
    atualizado_em  timestamptz  NOT NULL DEFAULT now()
);

CREATE INDEX pendencias_lote_idx ON pendencias (lote_id);

CREATE TABLE decisoes (
    id                      uuid        PRIMARY KEY,
    -- A primeira decisão final válida encerra o lote; uma segunda viola esta
    -- unicidade mesmo sob concorrência.
    lote_id                 uuid        NOT NULL UNIQUE REFERENCES lotes (id),
    decisao                 varchar(30) NOT NULL,
    justificativa           varchar(2000),
    submissao_id            uuid        NOT NULL,
    versao_id               uuid        NOT NULL,
    hash_conteudo           varchar(64) NOT NULL,
    autor                   uuid        NOT NULL,
    data                    timestamptz NOT NULL DEFAULT now(),
    aplicacao_em_requisitos varchar(20) NOT NULL DEFAULT 'PENDENTE',
    CONSTRAINT decisoes_decisao_valida CHECK (decisao IN ('APROVAR', 'REJEITAR', 'SOLICITAR_CORRECOES')),
    CONSTRAINT decisoes_aplicacao_valida CHECK (aplicacao_em_requisitos IN ('PENDENTE', 'APLICADA'))
);
