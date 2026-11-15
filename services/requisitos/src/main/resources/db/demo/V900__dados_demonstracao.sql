-- Dados de demonstração. Só são carregados quando FLYWAY_LOCATIONS inclui
-- classpath:db/demo. O projeto 22222222-... é o mesmo usado nos exemplos dos
-- contratos e nos dados de demonstração dos outros serviços.

INSERT INTO requisitos (id, projeto_id, codigo, documento_id, tipo, titulo) VALUES
    ('11111111-1111-4111-8111-111111111111', '22222222-2222-4222-8222-222222222222', 'REQ-1',
     '44444444-4444-4444-8444-444444444444', 'FUNCIONAL', 'Exportar documento de requisitos'),
    ('11111111-1111-4111-8111-111111111112', '22222222-2222-4222-8222-222222222222', 'REQ-2',
     '44444444-4444-4444-8444-444444444444', 'FUNCIONAL', 'Registrar relação entre requisitos'),
    ('11111111-1111-4111-8111-111111111113', '22222222-2222-4222-8222-222222222222', 'REQ-3',
     '44444444-4444-4444-8444-444444444444', 'NAO_FUNCIONAL', 'Consultas comuns respondem em até 1 segundo');

INSERT INTO versoes (id, requisito_id, numero, estado_versao, titulo, conteudo, criterios_aceite, hash_conteudo) VALUES
    ('33333333-3333-4333-8333-333333333331', '11111111-1111-4111-8111-111111111111', 1, 'RASCUNHO',
     'Exportar documento de requisitos',
     'O sistema deve permitir exportar o documento de requisitos.',
     '["Exportação preserva requisitos."]',
     encode(sha256(convert_to('O sistema deve permitir exportar o documento de requisitos.', 'UTF8')), 'hex')),
    ('33333333-3333-4333-8333-333333333332', '11111111-1111-4111-8111-111111111112', 1, 'RASCUNHO',
     'Registrar relação entre requisitos',
     'O editor deve conseguir relacionar dois requisitos do mesmo projeto.',
     '["Relação fica visível no mapa.", "Autorrelação é recusada."]',
     encode(sha256(convert_to('O editor deve conseguir relacionar dois requisitos do mesmo projeto.', 'UTF8')), 'hex')),
    ('33333333-3333-4333-8333-333333333333', '11111111-1111-4111-8111-111111111113', 1, 'RASCUNHO',
     'Consultas comuns respondem em até 1 segundo',
     'O p95 das consultas comuns deve ficar em até 1 segundo no cenário de referência.',
     '["Medição registrada com 20 usuários simultâneos."]',
     encode(sha256(convert_to('O p95 das consultas comuns deve ficar em até 1 segundo no cenário de referência.', 'UTF8')), 'hex'));

INSERT INTO sequencias_codigo (projeto_id, ultimo) VALUES ('22222222-2222-4222-8222-222222222222', 3);
