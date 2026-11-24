# syntax=docker/dockerfile:1.7
# Serviço Análise de Impacto/MCP (Python 3.12 + FastAPI).
#
# Build em três estágios:
#   1. "dependencias" instala os pacotes Python num ambiente virtual isolado;
#   2. "modelo" obtém os arquivos verificados;
#   3. a imagem final copia o ambiente, o modelo e o código da aplicação.
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

# ---- estágio 2: modelo -------------------------------------------------------
# Modelo incluído na imagem: versão fixa, verificada por sha256 e sem download
# na subida do container.
FROM scratch AS modelo
ADD --checksum=sha256:ca456c06b3a9505ddfd9131408916dd79290368331e7d76bb621f1cba6bc8665 https://huggingface.co/intfloat/multilingual-e5-small/resolve/614241f622f53c4eeff9890bdc4f31cfecc418b3/onnx/model.onnx /opt/modelo/model.onnx
ADD --checksum=sha256:0b44a9d7b51c3c62626640cda0e2c2f70fdacdc25bbbd68038369d14ebdf4c39 https://huggingface.co/intfloat/multilingual-e5-small/resolve/614241f622f53c4eeff9890bdc4f31cfecc418b3/onnx/tokenizer.json /opt/modelo/tokenizer.json

# ---- estágio 3: execução ------------------------------------------------------
FROM python:3.12.16-slim

RUN groupadd --system --gid 10001 athena \
 && useradd --system --uid 10001 --gid athena --no-create-home athena

ENV PATH="/opt/venv/bin:$PATH" \
    PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    MODELO_DIR=/opt/modelo \
    # Telemetria do onnxruntime desligada desde o import: nenhum dado de uso sai do serviço.
    ORT_DISABLE_TELEMETRY=1

COPY --from=dependencias /opt/venv /opt/venv
COPY --from=modelo --chown=10001:10001 /opt/modelo /opt/modelo
WORKDIR /app
COPY app ./app
COPY migracoes ./migracoes

USER 10001
EXPOSE 8000

HEALTHCHECK --interval=10s --timeout=5s --start-period=20s --retries=6 \
  CMD python -c "import urllib.request,sys; sys.exit(0 if urllib.request.urlopen('http://127.0.0.1:8000/saude/pronto', timeout=3).status == 200 else 1)"

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--no-access-log"]
