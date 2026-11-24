-- Retrato da submissão de demonstração do REQ-2 (submissão 9999...991), em
-- revisão no lote de demonstração de Revisões.
INSERT INTO retratos (id, projeto_id, requisito_id, submissao_id, versao_id, hash_conteudo, estado, relacoes)
SELECT 'dddddddd-dddd-4ddd-8ddd-ddddddddddd2'::uuid, '22222222-2222-4222-8222-222222222222'::uuid,
       '11111111-1111-4111-8111-111111111112'::uuid, '99999999-9999-4999-8999-999999999991'::uuid,
       '33333333-3333-4333-8333-333333333332'::uuid,
       encode(sha256(convert_to('O editor deve conseguir relacionar dois requisitos do mesmo projeto.', 'UTF8')), 'hex'),
       'ATIVO',
       (SELECT COALESCE(jsonb_agg(jsonb_build_object(
            'relacaoId', r.id, 'projetoId', r.projeto_id, 'origemId', r.origem_id,
            'destinoId', r.destino_id, 'tipo', r.tipo, 'justificativa', r.justificativa,
            'atualizadoEm', to_char(r.atualizado_em AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS.US"Z"')
        ) ORDER BY r.id), '[]'::jsonb)
        FROM relacoes r WHERE r.id = '77777777-7777-4777-8777-777777777771');
