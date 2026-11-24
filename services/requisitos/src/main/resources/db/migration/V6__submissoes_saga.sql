CREATE TABLE submissoes (
    id uuid PRIMARY KEY, saga_id uuid NOT NULL UNIQUE, projeto_id uuid NOT NULL,
    requisito_id uuid NOT NULL REFERENCES requisitos(id), versao_id uuid NOT NULL REFERENCES versoes(id),
    hash_conteudo varchar(64) NOT NULL, snapshot jsonb NOT NULL,
    estado_saga varchar(30) NOT NULL CHECK (estado_saga IN ('EM_ANDAMENTO','CONCLUIDA','COMPENSANDO','COMPENSACAO_PENDENTE','COMPENSADA')),
    etapa_saga varchar(30) CHECK (etapa_saga IN ('AGUARDANDO_RETRATO','AGUARDANDO_LOTE','CANCELANDO_LOTE','CANCELANDO_RETRATO')),
    comando_esperado_id uuid, comando_retrato_id uuid, comando_lote_id uuid,
    comando_cancelar_lote_id uuid, comando_cancelar_retrato_id uuid,
    tentativas integer NOT NULL, proxima_acao_em timestamptz, prazo timestamptz NOT NULL,
    retrato_id uuid, lote_id uuid, relacionados jsonb, motivo_falha varchar(1000),
    revisao bigint NOT NULL DEFAULT 0, criado_em timestamptz NOT NULL, atualizado_em timestamptz NOT NULL
);
CREATE UNIQUE INDEX submissoes_uma_ativa ON submissoes (requisito_id)
    WHERE estado_saga IN ('EM_ANDAMENTO','COMPENSANDO','COMPENSACAO_PENDENTE');
CREATE INDEX submissoes_agendamento ON submissoes (proxima_acao_em) WHERE proxima_acao_em IS NOT NULL;
CREATE TABLE chaves_idempotencia (
    chave varchar(200) NOT NULL, operacao varchar(60) NOT NULL, projeto_id uuid NOT NULL,
    impressao varchar(64) NOT NULL, recurso_id uuid NOT NULL, criado_em timestamptz NOT NULL,
    PRIMARY KEY (projeto_id, operacao, chave)
);
