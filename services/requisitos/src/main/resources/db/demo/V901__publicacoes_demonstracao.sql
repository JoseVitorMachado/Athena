-- Publicações de demonstração. Reproduz o caso do livro (§6.4): o REQ-1 tem a
-- versão 1 publicada no Livro enquanto a versão 2 é elaborada na Gestão.

UPDATE versoes SET estado_versao = 'PUBLICADA'
 WHERE id IN ('33333333-3333-4333-8333-333333333331', '33333333-3333-4333-8333-333333333333');

INSERT INTO versoes (id, requisito_id, numero, estado_versao, titulo, conteudo, criterios_aceite, hash_conteudo) VALUES
    ('33333333-3333-4333-8333-333333333334', '11111111-1111-4111-8111-111111111111', 2, 'RASCUNHO',
     'Exportar documento de requisitos',
     'O sistema deve permitir exportar o documento de requisitos em PDF e Markdown.',
     '["Exportação preserva requisitos.", "Formatos PDF e Markdown disponíveis."]',
     encode(sha256(convert_to('O sistema deve permitir exportar o documento de requisitos em PDF e Markdown.', 'UTF8')), 'hex'));

INSERT INTO publicacoes (id, requisito_id, versao_id, submissao_id, retrato_id, autor) VALUES
    ('cccccccc-cccc-4ccc-8ccc-ccccccccccc1', '11111111-1111-4111-8111-111111111111',
     '33333333-3333-4333-8333-333333333331', '99999999-9999-4999-8999-999999999981',
     'dddddddd-dddd-4ddd-8ddd-ddddddddddd1', '55555555-5555-4555-8555-555555555555'),
    ('cccccccc-cccc-4ccc-8ccc-ccccccccccc3', '11111111-1111-4111-8111-111111111113',
     '33333333-3333-4333-8333-333333333333', '99999999-9999-4999-8999-999999999983',
     'dddddddd-dddd-4ddd-8ddd-ddddddddddd3', '55555555-5555-4555-8555-555555555555');
