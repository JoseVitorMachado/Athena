-- Índice de contexto do serviço Análise de Impacto/MCP (livro de arquitetura §6.11).

-- Extensão de vetores. O banco deste serviço usa a imagem com pgvector.
CREATE EXTENSION IF NOT EXISTS vector;

-- Cópia indexada do conteúdo autorizado, segmentada em trechos. A dimensão 384 é
-- a do modelo multilingual-e5-small previsto no livro.
CREATE TABLE trechos (
    id             uuid         PRIMARY KEY,
    projeto_id     uuid         NOT NULL,
    origem_id      uuid         NOT NULL,
    versao_id      uuid         NOT NULL,
    titulo         varchar(300) NOT NULL,
    texto          text         NOT NULL,
    modelo         varchar(100) NOT NULL,
    embedding      vector(384)  NOT NULL,
    indexado_em    timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT trechos_um_por_versao UNIQUE (origem_id, versao_id)
);

CREATE INDEX trechos_projeto_idx ON trechos (projeto_id);

-- Busca vetorial aproximada por similaridade de cosseno.
CREATE INDEX trechos_embedding_idx ON trechos USING hnsw (embedding vector_cosine_ops);

-- Busca textual em português, combinada com a vetorial.
CREATE INDEX trechos_texto_idx ON trechos
    USING gin (to_tsvector('portuguese', titulo || ' ' || texto));
