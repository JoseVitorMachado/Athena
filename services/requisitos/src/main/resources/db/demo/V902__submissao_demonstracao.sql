-- Submissão concluída do REQ-2. A V900 deixou a versão 1 em AGUARDANDO_REVISAO,
-- e Revisões já tem o lote dessa tentativa (submissão 9999...991); faltava o
-- registro da SAGA aqui, sem o qual a revisão não fica associada à candidata.
INSERT INTO submissoes (id, saga_id, projeto_id, requisito_id, versao_id, hash_conteudo, snapshot,
                        estado_saga, tentativas, prazo, retrato_id, lote_id, relacionados,
                        criado_em, atualizado_em)
VALUES ('99999999-9999-4999-8999-999999999991', '99999999-9999-4999-8999-9999999999a1',
        '22222222-2222-4222-8222-222222222222', '11111111-1111-4111-8111-111111111112',
        '33333333-3333-4333-8333-333333333332',
        encode(sha256(convert_to('O editor deve conseguir relacionar dois requisitos do mesmo projeto.', 'UTF8')), 'hex'),
        jsonb_build_object(
            'titulo', 'Registrar relação entre requisitos',
            'conteudo', 'O editor deve conseguir relacionar dois requisitos do mesmo projeto.',
            'criteriosAceite', jsonb_build_array('Relação fica visível no mapa.', 'Autorrelação é recusada.')),
        'CONCLUIDA', 1, now(), 'dddddddd-dddd-4ddd-8ddd-ddddddddddd2',
        'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1',
        jsonb_build_array(jsonb_build_object('requisitoId', '11111111-1111-4111-8111-111111111111',
                                             'tipo', 'DEPENDE_DE', 'sentido', 'ENTRADA')),
        now(), now());
