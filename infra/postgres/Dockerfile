# PostgreSQL do ATHENA.
#
# Cada serviço de domínio tem a sua própria instância deste banco (database per
# service). A imagem parte da oficial e acrescenta o que é comum às seis
# instâncias: script de inicialização, parâmetros de execução e healthcheck.
#
# O mesmo Dockerfile gera duas imagens, trocando apenas a base:
#   athena/postgres           -> postgres:17.9-alpine           (cinco serviços Java)
#   athena/postgres-pgvector  -> pgvector/pgvector:0.8.7-pg17   (impacto-mcp)
ARG IMAGEM_BASE=postgres:17.9-alpine
FROM ${IMAGEM_BASE}

LABEL org.opencontainers.image.title="athena-postgres" \
      org.opencontainers.image.description="PostgreSQL com inicialização e parâmetros do ATHENA"

# Executado uma única vez, quando o volume de dados ainda está vazio.
COPY init/ /docker-entrypoint-initdb.d/

# O volume fica em /var/lib/postgresql/data; os dados vão para um subdiretório
# porque alguns provisionadores de volume criam "lost+found" na raiz.
ENV PGDATA=/var/lib/postgresql/data/pgdata

HEALTHCHECK --interval=10s --timeout=5s --start-period=20s --retries=6 \
  CMD pg_isready --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" || exit 1

# Instâncias pequenas: são seis bancos na mesma máquina, cada um atendendo um
# único serviço com um pool de poucas conexões.
CMD ["postgres", \
     "-c", "max_connections=40", \
     "-c", "shared_buffers=64MB", \
     "-c", "timezone=UTC", \
     "-c", "log_min_duration_statement=500"]
