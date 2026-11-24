CREATE TABLE projetos_bloqueados (
    projeto_id uuid PRIMARY KEY, exclusao_id uuid NOT NULL, fechado_em timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE projetos_excluidos (
    projeto_id uuid PRIMARY KEY, excluido_em timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE resultados_comando ADD COLUMN projeto_id uuid;
ALTER TABLE cancelamentos_submissao ADD COLUMN projeto_id uuid;
UPDATE cancelamentos_submissao c SET projeto_id = r.projeto_id FROM retratos r WHERE r.submissao_id = c.submissao_id;
UPDATE resultados_comando c SET projeto_id = r.projeto_id FROM retratos r WHERE c.resposta_payload->>'submissaoId' = r.submissao_id::text;
