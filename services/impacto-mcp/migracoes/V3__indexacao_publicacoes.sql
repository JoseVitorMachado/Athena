ALTER TABLE trechos ADD COLUMN ordem integer NOT NULL DEFAULT 0;
ALTER TABLE trechos ADD COLUMN ativo boolean NOT NULL DEFAULT true;
ALTER TABLE trechos ADD COLUMN codigo varchar(40);
ALTER TABLE trechos DROP CONSTRAINT trechos_um_por_versao;
ALTER TABLE trechos ADD CONSTRAINT trechos_por_ordem UNIQUE (origem_id, versao_id, ordem);
CREATE INDEX trechos_ativos_projeto_idx ON trechos (projeto_id) WHERE ativo;
CREATE INDEX trechos_ativos_embedding_idx ON trechos USING hnsw (embedding vector_cosine_ops) WHERE ativo;

CREATE TABLE trabalhos_indexacao (
    id uuid PRIMARY KEY,
    message_id uuid UNIQUE REFERENCES inbox(message_id),
    projeto_id uuid NOT NULL,
    origem_id uuid NOT NULL,
    versao_id uuid NOT NULL,
    payload jsonb NOT NULL,
    estado varchar(20) NOT NULL DEFAULT 'PENDENTE' CHECK (estado IN ('PENDENTE', 'CONCLUIDO', 'FALHOU')),
    tentativas integer NOT NULL DEFAULT 0,
    ultimo_erro varchar(1000),
    proxima_tentativa timestamptz NOT NULL DEFAULT now(),
    criado_em timestamptz NOT NULL DEFAULT now(),
    concluido_em timestamptz
);
CREATE INDEX trabalhos_pendentes_idx ON trabalhos_indexacao (proxima_tentativa) WHERE estado = 'PENDENTE';
