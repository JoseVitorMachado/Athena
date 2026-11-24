"""Métricas de execução e de requisições do serviço."""

from time import perf_counter

from prometheus_client import Gauge, Histogram
from starlette.routing import Match

APLICACAO = "impacto-mcp"
BUCKETS = (0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1, 2.5, 5, 10)

FERRAMENTA_SEGUNDOS = Histogram(
    "athena_mcp_ferramenta_segundos", "Tempo de execução das ferramentas MCP.",
    ("application", "ferramenta"), buckets=BUCKETS,
)
EMBEDDINGS_SEGUNDOS = Histogram(
    "athena_embeddings_segundos", "Tempo de geração de embeddings.",
    ("application",), buckets=BUCKETS,
).labels(application=APLICACAO)
INDEXACAO_PENDENTES = Gauge(
    "athena_indexacao_pendentes", "Trabalhos de indexação pendentes.", ("application",),
).labels(application=APLICACAO)
HTTP_SEGUNDOS = Histogram(
    "http_server_requests_seconds", "Tempo de requisições HTTP.",
    ("application", "method", "uri", "status"), buckets=BUCKETS,
)


def _uri(request):
    rota = request.scope.get("route")
    if rota is not None:
        return rota.path
    # O middleware de projeto pode responder antes do roteamento.
    parcial = None
    for rota in request.app.routes:
        correspondencia, _ = rota.matches(request.scope)
        if correspondencia == Match.FULL:
            return rota.path
        if correspondencia == Match.PARTIAL:
            parcial = rota.path
    return parcial or "NAO_ENCONTRADA"


async def medir_http(request, call_next):
    if request.url.path == "/metrics":
        return await call_next(request)
    inicio = perf_counter()
    status = 500
    try:
        resposta = await call_next(request)
        status = resposta.status_code
        return resposta
    finally:
        HTTP_SEGUNDOS.labels(application=APLICACAO, method=request.method,
                             uri=_uri(request), status=str(status)).observe(perf_counter() - inicio)
