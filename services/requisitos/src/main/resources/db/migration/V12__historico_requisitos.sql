CREATE TABLE historico_requisitos (
    id uuid PRIMARY KEY, projeto_id uuid NOT NULL,
    requisito_id uuid NOT NULL REFERENCES requisitos(id), versao_id uuid,
    acao varchar(40) NOT NULL, autor_id uuid, detalhe varchar(500),
    data timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX historico_requisitos_requisito_data_idx ON historico_requisitos (requisito_id, data DESC);

INSERT INTO historico_requisitos (id, projeto_id, requisito_id, acao, data)
SELECT gen_random_uuid(), projeto_id, id, 'REQUISITO_CRIADO', criado_em FROM requisitos;
INSERT INTO historico_requisitos (id, projeto_id, requisito_id, versao_id, acao, autor_id, data)
SELECT gen_random_uuid(), projeto_id, requisito_id, versao_id, 'SUBMETIDA', autor_id, criado_em FROM submissoes;
INSERT INTO historico_requisitos (id, projeto_id, requisito_id, versao_id, acao, data)
SELECT gen_random_uuid(), projeto_id, requisito_id, versao_id,
       CASE estado_saga WHEN 'CONCLUIDA' THEN 'SAGA_CONCLUIDA' ELSE 'SAGA_COMPENSADA' END, atualizado_em
FROM submissoes WHERE estado_saga IN ('CONCLUIDA', 'COMPENSADA');
INSERT INTO historico_requisitos (id, projeto_id, requisito_id, versao_id, acao, autor_id, data)
SELECT gen_random_uuid(), r.projeto_id, p.requisito_id, p.versao_id, 'PUBLICADA', p.autor, p.data
FROM publicacoes p JOIN requisitos r ON r.id=p.requisito_id;
INSERT INTO historico_requisitos (id, projeto_id, requisito_id, versao_id, acao, autor_id, data)
SELECT gen_random_uuid(), projeto_id, requisito_id, candidata_id,
       CASE situacao WHEN 'CANCELADO' THEN 'CANCELADO' ELSE 'REATIVADO' END, autor_id, criado_em
FROM alteracoes_situacao;
INSERT INTO historico_requisitos (id, projeto_id, requisito_id, versao_id, acao, autor_id, data)
SELECT gen_random_uuid(), projeto_id, requisito_id, versao_id, 'COMENTARIO_CRIADO', autor_id, criado_em FROM comentarios;
INSERT INTO historico_requisitos (id, projeto_id, requisito_id, versao_id, acao, detalhe, data)
SELECT gen_random_uuid(), r.projeto_id, r.id, v.id, 'DECISAO_APLICADA', d.resultado, d.aplicada_em
FROM decisoes_aplicadas d JOIN versoes v ON v.id=d.versao_id JOIN requisitos r ON r.id=v.requisito_id;
