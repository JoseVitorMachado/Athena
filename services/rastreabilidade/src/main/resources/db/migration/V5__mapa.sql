CREATE TABLE sequencias_escrita (projeto_id uuid PRIMARY KEY, posicao bigint NOT NULL);
CREATE TABLE mapa_nos (projeto_id uuid NOT NULL, requisito_id uuid NOT NULL, codigo varchar(100) NOT NULL, titulo text NOT NULL, situacao varchar(30) NOT NULL, revisao_origem bigint NOT NULL, atualizado_em timestamptz NOT NULL, PRIMARY KEY (projeto_id, requisito_id));
CREATE TABLE mapa_arestas (relacao_id uuid PRIMARY KEY, projeto_id uuid NOT NULL, origem_id uuid, destino_id uuid, tipo varchar(20), justificativa varchar(1000), revisao_origem bigint NOT NULL, atualizado_em timestamptz NOT NULL, removida boolean NOT NULL DEFAULT false);
CREATE INDEX mapa_arestas_projeto_idx ON mapa_arestas (projeto_id);
CREATE TABLE mapa_estado (projeto_id uuid PRIMARY KEY, geracao uuid NOT NULL, situacao varchar(20) NOT NULL CHECK (situacao IN ('ATUALIZADO', 'RECONSTRUINDO')), posicao_aplicada bigint NOT NULL, disponivel boolean NOT NULL DEFAULT false, ultima_atualizacao timestamptz NOT NULL);
