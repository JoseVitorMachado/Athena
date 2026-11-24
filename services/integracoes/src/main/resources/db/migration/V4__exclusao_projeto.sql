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

-- Recupera a associação das mensagens antigas quando há resposta na outbox.
UPDATE inbox i SET projeto_id = (o.payload ->> 'projetoId')::uuid
FROM outbox o
WHERE o.payload ->> 'causationId' = i.message_id::text
  AND o.payload ->> 'projetoId' ~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';
