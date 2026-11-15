-- Dados de demonstração (carregados só com classpath:db/demo em FLYWAY_LOCATIONS).
-- O lote avalia a versão 1 do REQ-2 dos dados de demonstração do serviço Requisitos;
-- o hash é calculado sobre o mesmo conteúdo.

INSERT INTO lotes (id, projeto_id, requisito_id, submissao_id, versao_id, hash_conteudo, estado) VALUES
    ('aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1', '22222222-2222-4222-8222-222222222222',
     '11111111-1111-4111-8111-111111111112', '99999999-9999-4999-8999-999999999991',
     '33333333-3333-4333-8333-333333333332',
     encode(sha256(convert_to('O editor deve conseguir relacionar dois requisitos do mesmo projeto.', 'UTF8')), 'hex'),
     'ATIVO');

INSERT INTO pendencias (id, lote_id, descricao, bloqueante, resolvida) VALUES
    ('bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbb1', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1',
     'Confirmar critério de aceite sobre autorrelação', true, true),
    ('bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbb2', 'aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaa1',
     'Revisar a redação do título', false, false);
