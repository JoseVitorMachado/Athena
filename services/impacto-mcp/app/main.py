"""Serviço Análise de Impacto/MCP (Python + FastAPI).

REST de docs/api/impacto-mcp.yaml implementado nesta etapa:
  POST /api/v1/projetos/{projetoId}/contexto/buscas
  GET  /api/v1/projetos/{projetoId}/indice
e o endpoint MCP em /mcp, roteado pelo Gateway.
"""

import logging
import re
import uuid
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from pydantic import BaseModel, ConfigDict, Field
from starlette.concurrency import run_in_threadpool

from . import config, indice, mcp, processamento, projetos
from .banco import migrar, pool

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s %(message)s")
# O cliente do RabbitMQ registra cada passo da conexão em INFO; só avisos e erros interessam.
logging.getLogger("pika").setLevel(logging.WARNING)


@asynccontextmanager
async def ciclo_de_vida(_app: FastAPI):
    pool.open(wait=True, timeout=30)
    migrar()
    if config.CARREGAR_DEMO:
        indice.carregar_demo()
    parar, threads = processamento.iniciar()
    try:
        yield
    finally:
        parar.set()
        for thread in threads:
            thread.join()
        pool.close()


app = FastAPI(title="ATHENA - Análise de Impacto/MCP", version="0.3.0", lifespan=ciclo_de_vida)
app.include_router(mcp.router)


def problema(status: int, titulo: str, detalhe: str, instancia: str, codigo: str) -> JSONResponse:
    """Erro em Problem Details (RFC 9457)."""
    return JSONResponse(
        status_code=status,
        media_type="application/problem+json",
        content={"type": "about:blank", "title": titulo, "status": status, "detail": detalhe,
                 "instance": instancia, "codigo": codigo},
    )


def _estado_projeto(projeto_id):
    with pool.connection() as conexao:
        return projetos.estado(conexao, projeto_id)


@app.middleware("http")
async def verificar_projeto(request: Request, call_next):
    caminho = request.url.path
    correspondencia = re.match(r"^/api/v1/projetos/([^/]+)(?:/|$)", caminho)
    if correspondencia:
        try:
            projeto_id = uuid.UUID(correspondencia.group(1))
        except ValueError:
            return await call_next(request)
        bloqueado, excluido = await run_in_threadpool(_estado_projeto, projeto_id)
        if excluido and request.method == "GET":
            return problema(404, "Projeto excluído", "Projeto excluído.", caminho, "PROJETO_EXCLUIDO")
        if (bloqueado or excluido) and request.method != "GET":
            return problema(409, "Projeto em exclusão", "Projeto em exclusão.", caminho, "PROJETO_EM_EXCLUSAO")
    return await call_next(request)


@app.exception_handler(RequestValidationError)
async def entrada_invalida(request: Request, erro: RequestValidationError):
    campos = ", ".join(".".join(str(p) for p in e["loc"][1:]) or "corpo" for e in erro.errors())
    return problema(400, "Requisição inválida", f"Campos inválidos: {campos}", request.url.path,
                    "ENTRADA_INVALIDA")


class BuscaEntrada(BaseModel):
    """Schema BuscaEntrada de docs/api/impacto-mcp.yaml."""

    model_config = ConfigDict(extra="forbid")
    q: str = Field(min_length=1, max_length=2000)
    limite: int = Field(default=5, ge=1, le=20)


@app.post("/api/v1/projetos/{projeto_id}/contexto/buscas")
async def buscar_contexto(projeto_id: uuid.UUID, entrada: BuscaEntrada):
    trechos = await run_in_threadpool(indice.buscar, projeto_id, entrada.q, entrada.limite)
    avisos = [] if trechos else ["Nenhum trecho indexado corresponde à pergunta."]
    return {"trechos": trechos, "avisos": avisos}


@app.get("/api/v1/projetos/{projeto_id}/indice")
async def obter_indice(projeto_id: uuid.UUID):
    return await run_in_threadpool(indice.situacao, projeto_id)


def _consultar_banco() -> None:
    with pool.connection(timeout=2) as conexao:
        conexao.execute("SELECT 1")


# Probes: liveness não depende do banco; readiness sim (livro §8.2).
@app.get("/saude/vivo")
async def vivo():
    return {"status": "UP"}


@app.get("/saude/pronto")
async def pronto():
    try:
        await run_in_threadpool(_consultar_banco)
    except Exception:  # noqa: BLE001 - qualquer falha de banco significa "não pronto"
        return JSONResponse(status_code=503, content={"status": "DOWN"})
    return {"status": "UP"}
