-- Dados de demonstração (carregados só com classpath:db/demo em FLYWAY_LOCATIONS).
-- Os requisitos referenciados são os dos dados de demonstração do serviço Requisitos.

INSERT INTO relacoes (id, projeto_id, origem_id, destino_id, tipo, justificativa) VALUES
    ('77777777-7777-4777-8777-777777777771', '22222222-2222-4222-8222-222222222222',
     '11111111-1111-4111-8111-111111111111', '11111111-1111-4111-8111-111111111112',
     'DEPENDE_DE', 'A exportação precisa das relações registradas.'),
    ('77777777-7777-4777-8777-777777777772', '22222222-2222-4222-8222-222222222222',
     '11111111-1111-4111-8111-111111111111', '11111111-1111-4111-8111-111111111113',
     'RELACIONADO_A', 'A exportação é uma das consultas com meta de tempo.');
