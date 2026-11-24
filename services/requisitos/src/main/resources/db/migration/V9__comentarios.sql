CREATE TABLE comentarios (
    id uuid PRIMARY KEY, projeto_id uuid NOT NULL,
    requisito_id uuid NOT NULL REFERENCES requisitos(id), versao_id uuid NOT NULL REFERENCES versoes(id),
    autor_id uuid NOT NULL, texto varchar(5000) NOT NULL, ancora_selecao varchar(1000),
    ancora_situacao varchar(20) CHECK (ancora_situacao IN ('LOCALIZADA','NAO_LOCALIZADA')),
    mencoes jsonb NOT NULL DEFAULT '[]', resolvido boolean NOT NULL DEFAULT false,
    resolvido_por uuid, resolvido_em timestamptz, revisao bigint NOT NULL DEFAULT 0,
    criado_em timestamptz NOT NULL DEFAULT now(), atualizado_em timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX comentarios_requisito_criado_idx ON comentarios (requisito_id, criado_em);
CREATE TABLE comentarios_historico (
    id uuid PRIMARY KEY, comentario_id uuid NOT NULL REFERENCES comentarios(id), autor_id uuid NOT NULL,
    texto_anterior varchar(5000), resolvido_anterior boolean, data timestamptz NOT NULL DEFAULT now()
);
ALTER TABLE submissoes ADD COLUMN autor_id uuid;
