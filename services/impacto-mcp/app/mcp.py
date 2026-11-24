"""Endpoint MCP por Streamable HTTP (JSON-RPC 2.0 em POST /mcp)."""

import hashlib
import json
import uuid
from typing import Literal

import httpx
from fastapi import APIRouter, Request, Response
from fastapi.responses import JSONResponse
from pydantic import BaseModel, ConfigDict, Field, ValidationError, model_validator
from starlette.concurrency import run_in_threadpool

from . import config, indice, metricas

router = APIRouter()
VERSAO_PROTOCOLO = "2025-06-18"
PAPEIS = ("LEITOR", "REPORTER", "EDITOR", "APROVADOR", "MANTENEDOR", "OWNER")
Decisao = Literal["APROVAR", "REJEITAR", "SOLICITAR_CORRECOES"]


class Argumentos(BaseModel):
    model_config = ConfigDict(extra="forbid")
    projetoId: uuid.UUID


class Busca(Argumentos):
    q: str = Field(min_length=1)
    limite: int = Field(default=5, ge=1, le=20, strict=True)

    @model_validator(mode="after")
    def pergunta_valida(self):
        if not self.q.strip():
            raise ValueError("Pergunta vazia")
        return self


class Preparacao(Argumentos):
    acao: Literal["DECIDIR", "PUBLICAR"]
    loteId: uuid.UUID | None = None
    requisitoId: uuid.UUID | None = None
    versaoId: uuid.UUID
    decisao: Decisao | None = None

    @model_validator(mode="after")
    def alvo_valido(self):
        if self.acao == "DECIDIR":
            valido = self.loteId is not None and self.decisao is not None and self.requisitoId is None
        else:
            valido = self.requisitoId is not None and self.loteId is None and self.decisao is None
        if not valido:
            raise ValueError("Alvo ou decisão incompatível com a ação")
        return self


class Decidir(Argumentos):
    loteId: uuid.UUID
    decisao: Decisao
    justificativa: str | None = Field(default=None, max_length=2000)
    comprovanteHumano: str = Field(min_length=1)

    @model_validator(mode="after")
    def justificativa_valida(self):
        if self.decisao != "APROVAR" and not (self.justificativa or "").strip():
            raise ValueError("Justificativa obrigatória")
        return self


class Publicar(Argumentos):
    requisitoId: uuid.UUID
    comprovanteHumano: str = Field(min_length=1)


MODELOS = {"buscar_contexto": Busca, "preparar_confirmacao": Preparacao,
           "decidir_revisao": Decidir, "publicar_versao": Publicar}
DESCRICOES = {
    "buscar_contexto": "Recupera trechos de requisitos relacionados a uma pergunta.",
    "preparar_confirmacao": "Prepara a confirmação humana na interface do ATHENA.",
    "decidir_revisao": "Registra uma decisão com comprovante humano.",
    "publicar_versao": "Publica a versão candidata com comprovante humano.",
}
FERRAMENTAS = [{"name": nome, "description": DESCRICOES[nome], "inputSchema": modelo.model_json_schema()}
               for nome, modelo in MODELOS.items()]


def hash_acao(acao, alvo, versao, decisao=None):
    texto = f"decidir:{alvo}:{versao}:{decisao}" if acao == "DECIDIR" else f"publicar:{alvo}:{versao}"
    return hashlib.sha256(texto.encode("utf-8")).hexdigest()


def _resposta(id_, resultado=None, erro=None):
    corpo = {"jsonrpc": "2.0", "id": id_}
    corpo["error" if erro is not None else "result"] = erro if erro is not None else resultado
    return JSONResponse(corpo)


def _resultado(dados, erro=False):
    return {"content": [{"type": "text", "text": json.dumps(dados, ensure_ascii=False)}],
            "structuredContent": dados, "isError": erro}


class ErroRpc(Exception):
    def __init__(self, codigo, mensagem):
        self.codigo, self.mensagem = codigo, mensagem


async def autorizar(cliente, projeto, usuario):
    try:
        resposta = await cliente.get(f"{config.PROJETOS_ACESSO_URL}/api/v1/internal/projetos/{projeto}/membros/{usuario}")
        if resposta.status_code == 404:
            raise ErroRpc(-32003, "Acesso ao projeto negado")
        if resposta.status_code != 200:
            raise ErroRpc(-32004, "Serviço de identidade indisponível")
        papel = resposta.json()["papel"]
        if papel not in PAPEIS:
            raise ValueError("Papel inválido")
        return papel
    except (httpx.RequestError, ValueError, KeyError, TypeError):
        raise ErroRpc(-32004, "Serviço de identidade indisponível") from None


def _problema(codigo, detalhe, status=502):
    return {"type": "about:blank", "title": "Falha na operação", "status": status,
            "detail": detalhe, "codigo": codigo}


class ErroDominio(Exception):
    def __init__(self, problema):
        self.problema = problema


def _verificar(resposta):
    if resposta.is_error:
        try:
            problema = resposta.json()
            if not isinstance(problema, dict):
                raise ValueError()
        except ValueError:
            problema = _problema("SERVICO_INDISPONIVEL", "Falha no serviço de domínio.", resposta.status_code)
        raise ErroDominio(problema)
    try:
        return resposta.json()
    except ValueError:
        raise ErroDominio(_problema("RESPOSTA_INVALIDA", "Resposta inválida do serviço de domínio.")) from None


async def executar(cliente, nome, args, usuario, papel):
    projeto = args.projetoId
    if nome == "buscar_contexto":
        trechos = await run_in_threadpool(indice.buscar, projeto, args.q.strip(), args.limite)
        return {"trechos": trechos}
    if nome == "preparar_confirmacao":
        alvo = args.loteId if args.acao == "DECIDIR" else args.requisitoId
        caminho = (f"/api/v1/gestao/projetos/{projeto}/revisoes/{alvo}/confirmacoes-decisao"
                   if args.acao == "DECIDIR" else
                   f"/api/v1/gestao/projetos/{projeto}/requisitos/{alvo}/confirmacoes-publicacao")
        return {"hashAcao": hash_acao(args.acao, alvo, args.versaoId, args.decisao),
                "instrucoes": f"Na interface do ATHENA, confirme a ação e informe sua senha pela rota POST {caminho}, com versaoId, hashAcao, confirmacao: true e senha. Use o comprovante retornado na ferramenta correspondente."}
    headers = {"X-Athena-Usuario": str(usuario), "X-Athena-Papel": papel, "X-Athena-Canal": "MCP"}
    chave = hashlib.sha256(args.comprovanteHumano.encode("utf-8")).hexdigest()
    if nome == "decidir_revisao":
        url = f"{config.REVISOES_URL}/api/v1/projetos/{projeto}/lotes/{args.loteId}"
        resposta = await cliente.get(url, headers=headers)
        lote = _verificar(resposta)
        corpo = {campo: lote[campo] for campo in ("versaoId", "submissaoId", "hashConteudo")}
        corpo.update(decisao=args.decisao, comprovanteHumano=args.comprovanteHumano)
        if args.justificativa is not None:
            corpo["justificativa"] = args.justificativa
        destino = url + "/decisoes"
    else:
        url = f"{config.REQUISITOS_URL}/api/v1/projetos/{projeto}/requisitos/{args.requisitoId}"
        resposta = await cliente.get(url + "/candidata", headers=headers)
        candidata = _verificar(resposta)
        corpo = {"versaoId": candidata["versaoId"], "comprovanteHumano": args.comprovanteHumano}
        pagina = 0
        while True:
            dados = _verificar(await cliente.get(url + "/submissoes", params={"page": pagina, "size": 100}, headers=headers))
            submissao = next((item for item in dados["items"] if item["estadoSaga"] == "CONCLUIDA"), None)
            if submissao is not None:
                corpo["submissaoId"] = submissao["submissaoId"]
                break
            pagina += 1
            if pagina >= dados["totalPages"]:
                raise ErroDominio(_problema("SUBMISSAO_NAO_ENCONTRADA", "Nenhuma submissão concluída encontrada.", 409))
        destino = url + "/publicacoes"
    etag = resposta.headers.get("etag")
    if not etag:
        raise ErroDominio(_problema("RESPOSTA_INVALIDA", "ETag ausente na resposta do serviço de domínio."))
    headers.update({"If-Match": etag, "Idempotency-Key": chave})
    return _verificar(await cliente.post(destino, headers=headers, json=corpo))


@router.post("/mcp")
async def mcp(request: Request):
    try:
        mensagem = await request.json()
    except (json.JSONDecodeError, UnicodeDecodeError):
        return _resposta(None, erro={"code": -32700, "message": "JSON inválido"})
    if not isinstance(mensagem, dict):
        return _resposta(None, erro={"code": -32600, "message": "Requisição inválida"})
    metodo, id_ = mensagem.get("method"), mensagem.get("id")
    if id_ is None:
        return Response(status_code=202)
    if metodo == "initialize":
        return _resposta(id_, {"protocolVersion": VERSAO_PROTOCOLO, "capabilities": {"tools": {"listChanged": False}},
                               "serverInfo": {"name": "athena-impacto-mcp", "version": "0.3.0"}})
    if metodo == "ping":
        return _resposta(id_, {})
    if metodo == "tools/list":
        return _resposta(id_, {"tools": FERRAMENTAS})
    if metodo != "tools/call":
        return _resposta(id_, erro={"code": -32601, "message": f"Método não suportado: {metodo}"})
    try:
        usuario = uuid.UUID(request.headers.get("X-Athena-Usuario", ""))
    except ValueError:
        return _resposta(id_, erro={"code": -32001, "message": "Não autenticado"})
    parametros = mensagem.get("params")
    if not isinstance(parametros, dict) or parametros.get("name") not in MODELOS:
        return _resposta(id_, erro={"code": -32602, "message": "Ferramenta desconhecida"})
    nome = parametros["name"]
    with metricas.FERRAMENTA_SEGUNDOS.labels(application=metricas.APLICACAO, ferramenta=nome).time():
        try:
            args = MODELOS[nome].model_validate(parametros.get("arguments", {}))
        except ValidationError:
            return _resposta(id_, erro={"code": -32602, "message": "Argumentos inválidos"})
        async with httpx.AsyncClient(timeout=3.0) as cliente:
            try:
                papel = await autorizar(cliente, args.projetoId, usuario)
                if nome in ("decidir_revisao", "publicar_versao") and PAPEIS.index(papel) < PAPEIS.index("APROVADOR"):
                    raise ErroRpc(-32003, "Acesso ao projeto negado")
                return _resposta(id_, _resultado(await executar(cliente, nome, args, usuario, papel)))
            except ErroRpc as erro:
                return _resposta(id_, erro={"code": erro.codigo, "message": erro.mensagem})
            except ErroDominio as erro:
                return _resposta(id_, _resultado(erro.problema, True))
            except (httpx.RequestError, KeyError, TypeError, ValueError):
                return _resposta(id_, _resultado(_problema("SERVICO_INDISPONIVEL", "Falha na comunicação com o serviço de domínio."), True))


@router.get("/mcp")
async def mcp_sem_stream():
    return Response(status_code=405, headers={"Allow": "POST"})
