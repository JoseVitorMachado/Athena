CREATE TABLE projetos_bloqueados (
    projeto_id uuid PRIMARY KEY,
    exclusao_id uuid NOT NULL,
    fechado_em timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE projetos_excluidos (
    projeto_id uuid PRIMARY KEY,
    excluido_em timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE inbox ADD COLUMN projeto_id uuid;
ALTER TABLE resultados_comando ADD COLUMN projeto_id uuid;
ALTER TABLE cancelamentos_submissao ADD COLUMN projeto_id uuid;

UPDATE cancelamentos_submissao c SET projeto_id = l.projeto_id
FROM lotes l WHERE l.submissao_id = c.submissao_id;
UPDATE resultados_comando r SET projeto_id = l.projeto_id
FROM lotes l WHERE r.resposta_payload ->> 'submissaoId' = l.submissao_id::text;

-- Recupera a associação das mensagens antigas quando há resposta na outbox.
UPDATE inbox i SET projeto_id = (o.payload ->> 'projetoId')::uuid
FROM outbox o
WHERE o.payload ->> 'causationId' = i.message_id::text
  AND o.payload ->> 'projetoId' ~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';
UPDATE resultados_comando r SET projeto_id = (o.payload ->> 'projetoId')::uuid
FROM outbox o
WHERE r.projeto_id IS NULL AND o.payload ->> 'commandId' = r.command_id::text
  AND o.payload ->> 'projetoId' ~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';
