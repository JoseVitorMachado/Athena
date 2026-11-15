-- Conexões de um projeto com provedores externos (livro de arquitetura §6.10).
-- As credenciais dos conectores ficam fora desta tabela: entram com o armazenamento
-- cifrado, junto com a implementação dos conectores.

CREATE TABLE conexoes (
    id                 uuid         PRIMARY KEY,
    projeto_id         uuid         NOT NULL,
    provedor           varchar(20)  NOT NULL,
    nome               varchar(120) NOT NULL,
    url_base           varchar(500),
    projeto_externo_id varchar(200),
    revisao            bigint       NOT NULL DEFAULT 0,
    criado_em          timestamptz  NOT NULL DEFAULT now(),
    atualizado_em      timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT conexoes_provedor_valido CHECK (provedor IN ('GOOGLE', 'OPENPROJECT', 'GITHUB'))
);

CREATE INDEX conexoes_listagem_idx ON conexoes (projeto_id, atualizado_em DESC, id);
