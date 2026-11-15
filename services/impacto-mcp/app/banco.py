"""Pool de conexões e migrações versionadas do serviço."""

import logging
import re
from pathlib import Path

from psycopg_pool import ConnectionPool

from . import config

LOG = logging.getLogger("impacto_mcp.banco")
MIGRACOES = Path(__file__).resolve().parent.parent / "migracoes"
_ARQUIVO = re.compile(r"^V(\d+)__.+\.sql$")

pool = ConnectionPool(config.DSN, min_size=1, max_size=5, open=False)


def migrar() -> None:
    """Aplica, em ordem e uma única vez, os arquivos V<n>__*.sql de migracoes/.

    Um advisory lock impede que duas réplicas migrem ao mesmo tempo.
    """
    arquivos = sorted(
        (int(m.group(1)), caminho)
        for caminho in MIGRACOES.iterdir()
        if (m := _ARQUIVO.match(caminho.name))
    )
    with pool.connection() as conexao:
        conexao.execute("SELECT pg_advisory_lock(727272)")
        try:
            conexao.execute(
                """CREATE TABLE IF NOT EXISTS historico_migracoes (
                       versao      integer     PRIMARY KEY,
                       arquivo     text        NOT NULL,
                       aplicada_em timestamptz NOT NULL DEFAULT now())"""
            )
            conexao.commit()
            aplicadas = {linha[0] for linha in conexao.execute("SELECT versao FROM historico_migracoes")}
            for versao, caminho in arquivos:
                if versao in aplicadas:
                    continue
                with conexao.transaction():
                    conexao.execute(caminho.read_text(encoding="utf-8"))
                    conexao.execute(
                        "INSERT INTO historico_migracoes (versao, arquivo) VALUES (%s, %s)",
                        (versao, caminho.name),
                    )
                LOG.info("Migração aplicada: %s", caminho.name)
        finally:
            conexao.execute("SELECT pg_advisory_unlock(727272)")
            conexao.commit()
