CREATE TABLE exclusoes_projeto (
    id uuid PRIMARY KEY, projeto_id uuid NOT NULL REFERENCES projetos(id),
    estado varchar(20) NOT NULL CHECK (estado IN ('FECHANDO','EXCLUINDO','CONCLUIDA')),
    solicitante_id uuid NOT NULL, chave_idempotencia varchar(200),
    criada_em timestamptz NOT NULL DEFAULT now(), atualizada_em timestamptz NOT NULL DEFAULT now(),
    concluida_em timestamptz
);
CREATE UNIQUE INDEX exclusao_projeto_ativa_idx ON exclusoes_projeto(projeto_id) WHERE estado <> 'CONCLUIDA';
CREATE TABLE exclusao_confirmacoes (
    exclusao_id uuid NOT NULL REFERENCES exclusoes_projeto(id), servico varchar(40) NOT NULL,
    quiescencia_em timestamptz, limpeza_em timestamptz, ultimo_envio_em timestamptz,
    PRIMARY KEY (exclusao_id, servico)
);
ALTER TABLE projetos ADD COLUMN em_exclusao boolean NOT NULL DEFAULT false;
