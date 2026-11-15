-- Registro imutável de cada publicação (livro de arquitetura §4.3): é a partir
-- dele que o BFF Livro compõe o conteúdo publicado.

CREATE TABLE publicacoes (
    id           uuid        PRIMARY KEY,
    requisito_id uuid        NOT NULL REFERENCES requisitos (id),
    versao_id    uuid        NOT NULL UNIQUE REFERENCES versoes (id),
    submissao_id uuid        NOT NULL,
    retrato_id   uuid        NOT NULL,
    autor        uuid        NOT NULL,
    data         timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX publicacoes_requisito_idx ON publicacoes (requisito_id, data DESC);
