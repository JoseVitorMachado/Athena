"""Sessão em memória. A superfície de domínio do cliente é somente leitura."""
import asyncio
from datetime import datetime, timedelta, timezone
from itertools import count
from uuid import UUID

import httpx

from .config import Config
from .texto import texto_plano


class ErroAthena(RuntimeError):
    pass


class ClienteAthena:
    def __init__(self, config: Config, transport=None):
        self.config = config
        self.http = httpx.AsyncClient(base_url=config.athena_url.rstrip("/"), timeout=config.api_timeout,
                                      transport=transport, follow_redirects=False)
        self.token = ""
        self.csrf = ""
        self.expira = datetime.min.replace(tzinfo=timezone.utc)
        self.lock = asyncio.Lock()
        self.ids = count(1)

    async def __aenter__(self):
        return self

    async def __aexit__(self, *_):
        await self.http.aclose()

    @staticmethod
    def verificar(resposta):
        if resposta.status_code >= 400:
            # Nunca incluir headers, corpo da requisição ou credenciais no erro/log.
            raise ErroAthena(f"ATHENA retornou HTTP {resposta.status_code} em {resposta.request.url.path}.")
        try:
            return resposta.json()
        except ValueError as exc:
            raise ErroAthena("ATHENA retornou JSON inválido.") from exc

    async def autenticar(self, token_rejeitado=None, forcar_renovacao=False):
        async with self.lock:
            if token_rejeitado is not None and self.token and self.token != token_rejeitado:
                return
            if not forcar_renovacao and token_rejeitado is None and self.token and self.expira > datetime.now(timezone.utc) + timedelta(seconds=30):
                return
            resposta = None
            if self.csrf and self.http.cookies.get("athenaRefresh"):
                resposta = await self.http.post("/api/v1/autenticacao/renovacao", headers={"X-CSRF-Token": self.csrf})
                if resposta.status_code not in (200, 401, 403):
                    self.verificar(resposta)
            if resposta is None or resposta.status_code in (401, 403):
                self.http.cookies.clear()
                resposta = await self.http.post("/api/v1/autenticacao/login", json={"email": self.config.email, "senha": self.config.senha})
            dados = self.verificar(resposta)
            try:
                self.token = dados["accessToken"]
                self.csrf = dados["csrfToken"]
                self.expira = datetime.fromisoformat(dados["expiraEm"].replace("Z", "+00:00"))
            except (KeyError, ValueError, TypeError) as exc:
                raise ErroAthena("Resposta de autenticação incompleta.") from exc

    async def request(self, method, path, **kwargs):
        await self.autenticar()
        token_usado = self.token
        headers = {**kwargs.pop("headers", {}), "Authorization": f"Bearer {token_usado}"}
        resposta = await self.http.request(method, path, headers=headers, **kwargs)
        if resposta.status_code == 401:
            await self.autenticar(token_rejeitado=token_usado)
            headers["Authorization"] = f"Bearer {self.token}"
            resposta = await self.http.request(method, path, headers=headers, **kwargs)
        return self.verificar(resposta)

    async def rpc(self, method, params=None):
        id_ = next(self.ids)
        corpo = {"jsonrpc": "2.0", "id": id_, "method": method}
        if params is not None:
            corpo["params"] = params
        dados = await self.request("POST", "/mcp", json=corpo,
                                   headers={"Accept": "application/json, text/event-stream", "MCP-Protocol-Version": "2025-06-18"})
        if dados.get("id") != id_ or dados.get("jsonrpc") != "2.0":
            raise ErroAthena("Envelope MCP inválido.")
        if "error" in dados:
            raise ErroAthena(f"Erro MCP {dados['error'].get('code')}; consulta não concluída.")
        result = dados.get("result", {})
        if result.get("isError"):
            raise ErroAthena("A ferramenta MCP retornou uma falha de domínio.")
        return result

    async def inicializar_mcp(self):
        await self.rpc("initialize", {"protocolVersion": "2025-06-18", "capabilities": {},
                                      "clientInfo": {"name": "athena-agente-ia", "version": "0.1.0"}})
        # Notification has no response body (HTTP 202 in this server).
        await self.autenticar()
        r = await self.http.post("/mcp", json={"jsonrpc": "2.0", "method": "notifications/initialized"},
                                 headers={"Authorization": f"Bearer {self.token}", "Accept": "application/json, text/event-stream"})
        if r.status_code != 202:
            raise ErroAthena("Falha ao inicializar a sessão MCP.")

    async def buscar_contexto(self, q):
        resultado = await self.rpc("tools/call", {"name": "buscar_contexto", "arguments": {
            "projetoId": self.config.projeto_id, "q": q, "limite": self.config.limite}})
        try:
            trechos = resultado["structuredContent"]["trechos"]
            if not isinstance(trechos, list):
                raise TypeError()
            return trechos
        except (KeyError, TypeError) as exc:
            raise ErroAthena("Resposta MCP sem lista de trechos.") from exc

    async def get(self, caminho):
        prefixo = f"/api/v1/gestao/projetos/{self.config.projeto_id}"
        return await self.request("GET", prefixo + caminho)

    async def requisito(self, id_):
        return await self.get(f"/requisitos/{UUID(id_)}")

    async def versao(self, id_, versao_id):
        versao = await self.get(f"/requisitos/{UUID(id_)}/versoes/{UUID(versao_id)}")
        # O editor da Gestão salva HTML; o LLM e as citações trabalham com texto plano.
        if isinstance(versao, dict) and isinstance(versao.get("conteudo"), str):
            versao["conteudo"] = texto_plano(versao["conteudo"])
        return versao
