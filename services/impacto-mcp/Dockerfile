# Serviço Análise de Impacto/MCP (Python 3.12 + FastAPI).
#
# Build em dois estágios:
#   1. "dependencias" instala os pacotes Python num ambiente virtual isolado;
#   2. a imagem final copia só esse ambiente e o código da aplicação.
# Ganho: pip, cache de download e qualquer ferramenta usada na instalação ficam
# no primeiro estágio. A imagem que roda leva apenas o interpretador, os pacotes
# já instalados e o código, e roda com usuário sem privilégios.

# ---- estágio 1: dependências --------------------------------------------------
FROM python:3.12.16-slim AS dependencias
ENV PIP_DISABLE_PIP_VERSION_CHECK=1
RUN python -m venv /opt/venv
COPY requirements.txt .
RUN --mount=type=cache,target=/root/.cache/pip \
    /opt/venv/bin/pip install -r requirements.txt

# ---- estágio 2: execução ------------------------------------------------------
FROM python:3.12.16-slim

RUN groupadd --system --gid 10001 athena \
 && useradd --system --uid 10001 --gid athena --no-create-home athena

ENV PATH="/opt/venv/bin:$PATH" \
    PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

COPY --from=dependencias /opt/venv /opt/venv
WORKDIR /app
COPY app ./app
COPY migracoes ./migracoes

USER 10001
EXPOSE 8000

HEALTHCHECK --interval=10s --timeout=5s --start-period=20s --retries=6 \
  CMD python -c "import urllib.request,sys; sys.exit(0 if urllib.request.urlopen('http://127.0.0.1:8000/saude/pronto', timeout=3).status == 200 else 1)"

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--no-access-log"]
