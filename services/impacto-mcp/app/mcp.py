"""Endpoint MCP mínimo por Streamable HTTP (JSON-RPC 2.0 em POST /mcp).

Implementa o suficiente para um cliente MCP descobrir e chamar a ferramenta de
busca de contexto: initialize, tools/list e tools/call. Autorização OAuth por
chamada (livro §7.5) e as demais ferramentas do catálogo entram na Parte 4.
"""

import json
import uuid

from fastapi import APIRouter, Request, Response
from fastapi.responses import JSONResponse

from . import indice

router = APIRouter()

VERSAO_PROTOCOLO = "2025-06-18"

FERRAMENTAS = [
    {
        "name": "buscar_contexto",
        "description": "Recupera trechos de requisitos do projeto relacionados a uma pergunta.",
        "inputSchema": {
            "type": "object",
            "properties": {
                "projetoId": {"type": "string", "format": "uuid"},
                "q": {"type": "string", "minLength": 1},
                "limite": {"type": "integer", "minimum": 1, "maximum": 20, "default": 5},
            },
            "required": ["projetoId", "q"],
            "additionalProperties": False,
        },
    }
]


def _resposta(id_, resultado=None, erro=None):
    corpo = {"jsonrpc": "2.0", "id": id_}
    if erro is not None:
        corpo["error"] = erro
    else:
        corpo["result"] = resultado
    return JSONResponse(corpo)


@router.post("/mcp")
async def mcp(request: Request):
    try:
        mensagem = await request.json()
    except json.JSONDecodeError:
        return _resposta(None, erro={"code": -32700, "message": "JSON inválido"})

    metodo = mensagem.get("method")
    id_ = mensagem.get("id")

    # Notificações não têm id e não recebem resposta.
    if id_ is None:
        return Response(status_code=202)

    if metodo == "initialize":
        return _resposta(id_, {
            "protocolVersion": VERSAO_PROTOCOLO,
            "capabilities": {"tools": {"listChanged": False}},
            "serverInfo": {"name": "athena-impacto-mcp", "version": "0.3.0"},
        })
    if metodo == "ping":
        return _resposta(id_, {})
    if metodo == "tools/list":
        return _resposta(id_, {"tools": FERRAMENTAS})
    if metodo == "tools/call":
        parametros = mensagem.get("params") or {}
        if parametros.get("name") != "buscar_contexto":
            return _resposta(id_, erro={"code": -32602, "message": "Ferramenta desconhecida"})
        argumentos = parametros.get("arguments") or {}
        try:
            projeto = uuid.UUID(str(argumentos["projetoId"]))
            pergunta = str(argumentos["q"]).strip()
            limite = max(1, min(int(argumentos.get("limite", 5)), 20))
            if not pergunta:
                raise ValueError("q vazio")
        except (KeyError, ValueError):
            return _resposta(id_, erro={"code": -32602, "message": "Argumentos inválidos"})
        trechos = indice.buscar(projeto, pergunta, limite)
        return _resposta(id_, {
            "content": [{"type": "text", "text": json.dumps({"trechos": trechos}, ensure_ascii=False)}],
            "structuredContent": {"trechos": trechos},
            "isError": False,
        })
    return _resposta(id_, erro={"code": -32601, "message": f"Método não suportado: {metodo}"})


@router.get("/mcp")
async def mcp_sem_stream():
    # Este servidor não abre stream iniciado pelo servidor (SSE).
    return Response(status_code=405, headers={"Allow": "POST"})
