-- Retratos correspondentes às publicações de demonstração de Requisitos.
INSERT INTO retratos (id, projeto_id, requisito_id, submissao_id, versao_id, hash_conteudo, estado, relacoes)
SELECT dados.id::uuid, '22222222-2222-4222-8222-222222222222'::uuid,
       dados.requisito_id::uuid, dados.submissao_id::uuid, dados.versao_id::uuid,
       encode(sha256(convert_to(dados.conteudo, 'UTF8')), 'hex'), 'ATIVO',
       (SELECT COALESCE(jsonb_agg(jsonb_build_object(
            'relacaoId', r.id, 'projetoId', r.projeto_id, 'origemId', r.origem_id,
            'destinoId', r.destino_id, 'tipo', r.tipo, 'justificativa', r.justificativa,
            'atualizadoEm', to_char(r.atualizado_em AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS.US"Z"')
        ) ORDER BY r.id), '[]'::jsonb)
        FROM relacoes r WHERE r.projeto_id = '22222222-2222-4222-8222-222222222222'
          AND (r.origem_id = dados.requisito_id::uuid OR r.destino_id = dados.requisito_id::uuid)
          AND r.id IN ('77777777-7777-4777-8777-777777777771', '77777777-7777-4777-8777-777777777772'))
FROM (VALUES
    ('dddddddd-dddd-4ddd-8ddd-ddddddddddd1', '11111111-1111-4111-8111-111111111111',
     '99999999-9999-4999-8999-999999999981', '33333333-3333-4333-8333-333333333331',
     'O sistema deve permitir exportar o documento de requisitos.'),
    ('dddddddd-dddd-4ddd-8ddd-ddddddddddd3', '11111111-1111-4111-8111-111111111113',
     '99999999-9999-4999-8999-999999999983', '33333333-3333-4333-8333-333333333333',
     'O p95 das consultas comuns deve ficar em até 1 segundo no cenário de referência.')
) AS dados(id, requisito_id, submissao_id, versao_id, conteudo);
