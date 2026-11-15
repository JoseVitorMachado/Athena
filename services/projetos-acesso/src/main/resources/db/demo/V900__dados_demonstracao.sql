-- Dados de demonstração (carregados só com classpath:db/demo em FLYWAY_LOCATIONS).
-- O projeto 22222222-... é o mesmo referenciado pelos dados dos outros serviços.

INSERT INTO projetos (id, workspace_id, nome, descricao, owner_id) VALUES
    ('22222222-2222-4222-8222-222222222222', '66666666-6666-4666-8666-666666666666', 'ATHENA',
     'Gestão, rastreabilidade e revisão de requisitos', '55555555-5555-4555-8555-555555555555');
