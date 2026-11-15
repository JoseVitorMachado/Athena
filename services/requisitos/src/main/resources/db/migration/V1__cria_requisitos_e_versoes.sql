-- Requisitos e suas versões (livro de arquitetura §5.1 a §5.3).

CREATE TABLE requisitos (
    id             uuid         PRIMARY KEY,
    projeto_id     uuid         NOT NULL,
    codigo         varchar(20)  NOT NULL,
    documento_id   uuid         NOT NULL,
    epico_id       uuid,
    responsavel_id uuid,
    tipo           varchar(40),
    titulo         varchar(300) NOT NULL,
    situacao       varchar(20)  NOT NULL DEFAULT 'ATIVO',
    revisao        bigint       NOT NULL DEFAULT 0,
    criado_em      timestamptz  NOT NULL DEFAULT now(),
    atualizado_em  timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT requisitos_situacao_valida CHECK (situacao IN ('ATIVO', 'CANCELADO')),
    -- O código legível (REQ-42) é único dentro do projeto.
    CONSTRAINT requisitos_codigo_unico_por_projeto UNIQUE (projeto_id, codigo)
);

CREATE INDEX requisitos_listagem_idx ON requisitos (projeto_id, situacao, atualizado_em DESC, id);

CREATE TABLE versoes (
    id               uuid         PRIMARY KEY,
    requisito_id     uuid         NOT NULL REFERENCES requisitos (id),
    numero           integer      NOT NULL,
    estado_versao    varchar(30)  NOT NULL,
    titulo           varchar(300) NOT NULL,
    conteudo         text         NOT NULL,
    criterios_aceite jsonb        NOT NULL DEFAULT '[]'::jsonb,
    hash_conteudo    varchar(64)  NOT NULL,
    atualizado_em    timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT versoes_estado_valido CHECK (estado_versao IN
        ('RASCUNHO', 'EM_SUBMISSAO', 'AGUARDANDO_REVISAO', 'APROVADA', 'PUBLICADA')),
    CONSTRAINT versoes_numero_unico_por_requisito UNIQUE (requisito_id, numero)
);

-- Cada requisito tem no máximo uma candidata aberta (versão ainda não publicada).
CREATE UNIQUE INDEX versoes_uma_candidata_aberta_idx
    ON versoes (requisito_id) WHERE estado_versao <> 'PUBLICADA';

-- Último número usado no código REQ-n de cada projeto.
CREATE TABLE sequencias_codigo (
    projeto_id uuid    PRIMARY KEY,
    ultimo     integer NOT NULL
);
