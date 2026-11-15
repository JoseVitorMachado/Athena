#!/bin/sh
# Inicialização comum aos bancos do ATHENA.
# Roda uma única vez, na criação do volume, com o usuário definido em POSTGRES_USER.
set -eu

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<SQL
-- Datas sempre em UTC, independentemente do host.
ALTER DATABASE "$POSTGRES_DB" SET timezone TO 'UTC';

-- O banco pertence a um único serviço: nenhum outro papel se conecta a ele.
REVOKE ALL ON DATABASE "$POSTGRES_DB" FROM PUBLIC;

COMMENT ON DATABASE "$POSTGRES_DB" IS 'ATHENA: banco exclusivo de um serviço de domínio';
SQL
