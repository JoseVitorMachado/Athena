FROM python:3.12.12-slim-bookworm@sha256:593bd06efe90efa80dc4eee3948be7c0fde4134606dd40d8dd8dbcade98e669c AS build
ENV PIP_DISABLE_PIP_VERSION_CHECK=1 PIP_NO_CACHE_DIR=1
WORKDIR /build
COPY requirements.txt .
RUN python -m venv /opt/venv && /opt/venv/bin/pip install -r requirements.txt

FROM python:3.12.12-slim-bookworm@sha256:593bd06efe90efa80dc4eee3948be7c0fde4134606dd40d8dd8dbcade98e669c
ENV PATH="/opt/venv/bin:$PATH" PYTHONDONTWRITEBYTECODE=1 PYTHONUNBUFFERED=1
RUN useradd --uid 1000 --create-home athena && mkdir /resultados && chown athena:athena /resultados
COPY --from=build /opt/venv /opt/venv
WORKDIR /app
COPY --chown=athena:athena athena_ia ./athena_ia
COPY --chown=athena:athena perguntas.json ./perguntas.json
COPY --chown=athena:athena popular_demo.py ./popular_demo.py
COPY --chown=athena:athena fluxo_requisito.py ./fluxo_requisito.py
COPY --chown=athena:athena dados ./dados
USER athena
CMD ["python", "-m", "athena_ia", "servir"]
