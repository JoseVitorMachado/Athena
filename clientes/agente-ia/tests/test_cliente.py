import asyncio
import json
from dataclasses import replace
from datetime import datetime, timedelta, timezone

import httpx
import pytest
from langchain_core.messages import AIMessage

from athena_ia.athena import ClienteAthena, ErroAthena
from athena_ia.chain import Assistente
from athena_ia.config import Config
from athena_ia.llm import Gerador, Impacto, Resposta, esquema_saida, validar_saida
from athena_ia.metricas import Medicao


def sessao(token):
    return {"accessToken": token, "csrfToken": "csrf-" + token,
            "expiraEm": (datetime.now(timezone.utc) + timedelta(minutes=15)).isoformat()}


def test_refresh_cookie_csrf_rotacao_e_401():
    chamadas = []

    def responder(req):
        chamadas.append(req.url.path)
        if req.url.path.endswith("/login"):
            return httpx.Response(200, json=sessao("primeiro"), headers={"Set-Cookie": "athenaRefresh=refresh1; Path=/api/v1/autenticacao; HttpOnly"})
        if req.url.path.endswith("/renovacao"):
            assert req.headers["X-CSRF-Token"] == "csrf-primeiro"
            assert "athenaRefresh=refresh1" in req.headers["cookie"]
            return httpx.Response(200, json=sessao("segundo"), headers={"Set-Cookie": "athenaRefresh=refresh2; Path=/api/v1/autenticacao"})
        if req.headers["Authorization"] == "Bearer primeiro":
            return httpx.Response(401)
        return httpx.Response(200, json={"ok": True})

    async def caso():
        async with ClienteAthena(Config(), httpx.MockTransport(responder)) as c:
            assert await c.get("/mapa") == {"ok": True}
            assert c.csrf == "csrf-segundo"
            assert c.http.cookies.get("athenaRefresh") == "refresh2"
    asyncio.run(caso())
    assert len(chamadas) == 4


def test_sessao_expirada_renova_antes_da_consulta():
    caminhos = []

    def responder(req):
        caminhos.append(req.url.path)
        return httpx.Response(200, json=sessao("novo") if req.url.path.endswith("renovacao") else {})

    async def caso():
        async with ClienteAthena(Config(), httpx.MockTransport(responder)) as c:
            c.token, c.csrf = "antigo", "csrf"
            c.http.cookies.set("athenaRefresh", "r", domain="localhost.local", path="/api/v1/autenticacao")
            await c.get("/mapa")
    asyncio.run(caso())
    assert caminhos[0].endswith("renovacao")


def test_refresh_invalido_refaz_login_uma_vez():
    caminhos = []

    def responder(req):
        caminhos.append(req.url.path)
        return httpx.Response(401) if req.url.path.endswith("renovacao") else httpx.Response(200, json=sessao("novo"))

    async def caso():
        async with ClienteAthena(Config(), httpx.MockTransport(responder)) as c:
            c.token, c.csrf = "antigo", "csrf"
            c.http.cookies.set("athenaRefresh", "r")
            await c.autenticar()
            assert c.token == "novo"
    asyncio.run(caso())
    assert [p.rsplit("/", 1)[-1] for p in caminhos] == ["renovacao", "login"]


@pytest.mark.parametrize("resultado", [{"error": {"code": -32003}}, {"result": {"isError": True}}])
def test_erros_mcp_nao_sao_contexto_vazio(resultado):
    def responder(req):
        if req.url.path.endswith("login"):
            return httpx.Response(200, json=sessao("token"))
        return httpx.Response(200, json={"jsonrpc": "2.0", "id": json.loads(req.content)["id"], **resultado})

    async def caso():
        async with ClienteAthena(Config(), httpx.MockTransport(responder)) as c:
            with pytest.raises(ErroAthena):
                await c.buscar_contexto("pergunta")
    asyncio.run(caso())


class Modelo:
    def __init__(self, respostas):
        self.respostas, self.chamadas = iter(respostas), 0

    async def ainvoke(self, _):
        self.chamadas += 1
        r = next(self.respostas)
        if isinstance(r, Exception):
            raise r
        if r == "demorar":
            await asyncio.sleep(10)
        return AIMessage(content=json.dumps(r), usage_metadata={"input_tokens": 100, "output_tokens": 20, "total_tokens": 120})


RESPOSTA = {"resposta": "Não há dados suficientes.", "justificativa": "Contexto ausente.", "fontes_ids": []}


def test_retry_backoff_e_fallback_preserva_fontes():
    esperas = []

    async def dormir(s):
        esperas.append(s)

    modelo = Modelo([httpx.ConnectError("falha")] * 3)
    conf = replace(Config(), tentativas=3, backoff=2)
    m = Medicao()
    fontes = [{"id": "E1", "codigo": "REQ-1", "texto": "conteúdo publicado"}]
    dados, estado = asyncio.run(Gerador(conf, lambda _: modelo, dormir).gerar([], Resposta, fontes, m))
    assert estado == "degradado" and dados["fontes_ids"] == ["E1"]
    assert esperas == [2, 4] and modelo.chamadas == 3
    assert m.finalizar(conf)["custo_referencia_usd"] is None


def test_timeout_total_nao_trava_operacao():
    modelo = Modelo(["demorar", "demorar"])
    conf = replace(Config(), timeout=0.01, backoff=0)
    m = Medicao()
    _, estado = asyncio.run(Gerador(conf, lambda _: modelo).gerar([], Resposta, [], m))
    assert estado == "degradado" and len(m.tentativas) == 2
    assert 10 <= m.llm_ms < 1000


def test_retry_recupera_e_soma_tokens_da_saida_rejeitada():
    modelo = Modelo([{**RESPOSTA, "fontes_ids": ["E999"]}, RESPOSTA])
    conf = replace(Config(), backoff=0)
    m = Medicao()
    _, estado = asyncio.run(Gerador(conf, lambda _: modelo).gerar([], Resposta, [], m))
    assert estado == "ok" and m.tokens_entrada == 200 and m.tokens_saida == 40
    assert m.finalizar(conf)["custo_referencia_usd"] == pytest.approx(0.000144)


def test_rejeita_sugestao_sem_evidencia_do_alvo():
    fontes = [{"id": "E1", "codigo": "REQ-1"}, {"id": "E2", "codigo": "REQ-2"}]
    dados = {**RESPOSTA, "fontes_ids": ["E1"], "sugestoes": [{"requisito": "REQ-2", "justificativa": "revisar", "fontes_ids": ["E1"]}]}
    with pytest.raises(ValueError):
        validar_saida(json.dumps(dados), Impacto, fontes, {"REQ-2"})
    dados["sugestoes"][0]["fontes_ids"] = ["E2"]
    assert validar_saida(json.dumps(dados), Impacto, fontes, {"REQ-2"})["sugestoes"]


def test_gramatica_restringe_cada_alvo_as_suas_fontes():
    fontes = [{"id": "E1", "codigo": "REQ-1"}, {"id": "E2", "codigo": "REQ-2"}]
    schema = esquema_saida(Impacto, fontes, {"REQ-2"})
    item = schema["$defs"]["Sugestao"]["oneOf"][0]["properties"]
    assert item["requisito"]["const"] == "REQ-2"
    assert item["fontes_ids"]["items"]["enum"] == ["E2"]
    assert esquema_saida(Resposta, [], set())["properties"]["fontes_ids"]["maxItems"] == 0
    assert schema["properties"]["fontes_ids"]["minItems"] == 1


def test_resposta_com_contexto_nao_pode_omitir_citacoes():
    with pytest.raises(ValueError, match="precisa citar"):
        validar_saida(json.dumps(RESPOSTA), Resposta, [{"id": "E1", "codigo": "REQ-1"}])


def test_chain_chama_tool_real_e_baseline_nao_recupera():
    class Cliente:
        consultas = []
        async def inicializar_mcp(self):
            pass
        async def buscar_contexto(self, q):
            self.consultas.append(q)
            return [{"texto": "p95 até 1 segundo", "similaridade": 0.8,
                     "referencia": {"trecho": "REQ-3 · Latência", "origemId": "id3", "versaoId": "v3", "referencia": "/rota"}}]
    c = Cliente()
    modelo = Modelo([RESPOSTA, {"resposta": "p95 até 1 segundo [REQ-3].", "justificativa": "Meta publicada.", "fontes_ids": ["E1"]}])
    a = Assistente(Config(), c, Gerador(Config(), lambda _: modelo))
    sem = asyncio.run(a.executar("Qual latência?", usar_rag=False))
    assert c.consultas == [] and sem["fontes"] == []
    com = asyncio.run(a.executar("Qual latência?"))
    assert c.consultas == ["Qual latência?"] and com["fontes"][0]["codigo"] == "REQ-3"
    assert com["metricas"]["tokens_entrada"] == 100


def test_erro_de_permissao_nao_chama_llm():
    class Cliente:
        async def inicializar_mcp(self):
            raise ErroAthena("Erro MCP -32003")
    modelo = Modelo([RESPOSTA])
    a = Assistente(Config(), Cliente(), Gerador(Config(), lambda _: modelo))
    with pytest.raises(ErroAthena):
        asyncio.run(a.executar("Pergunta"))
    assert modelo.chamadas == 0


def test_config_nao_expoe_senha_e_rejeita_url_com_credencial():
    assert "Athena@2026" not in repr(Config())
    with pytest.raises(ValueError):
        Config(llm_url="http://segredo:senha@exemplo.com")


def test_impacto_compara_versoes_e_preserva_direcao_das_relacoes():
    raiz = "11111111-1111-4111-8111-111111111111"
    alvo = "11111111-1111-4111-8111-111111111112"
    class Cliente:
        async def requisito(self, id_):
            return {"requisitoId": id_, "codigo": "REQ-1" if id_ == raiz else "REQ-2", "titulo": "Exportação",
                    "versaoId": "candidata", "versaoPublicadaId": "publicada", "situacao": "ATIVO"}
        async def versao(self, id_, versao):
            if id_ == alvo:
                return {"versaoId": versao, "conteudo": "Registrar relações entre requisitos."}
            return {"versaoId": versao, "conteudo": "PDF e Markdown" if versao == "candidata" else "Exportar documento"}
        async def get(self, path):
            assert path == "/mapa"
            return {"arestas": [{"origemId": raiz, "destinoId": alvo, "tipo": "DEPENDE_DE"}], "situacaoProjecao": "ATUALIZADO"}
        async def inicializar_mcp(self):
            pass
        async def buscar_contexto(self, _):
            return []
    resposta = {"classificacao": "REVISAR", "trecho_alvo": "Registrar relações entre requisitos.", "mudanca": "Inclusão de PDF e Markdown.",
                "efeito": "Os novos formatos podem perder relações registradas [REQ-2].",
                "verificacao": "Comparar relações antes e depois de exportar em cada formato.",
                "fontes_mudanca_ids": ["E1", "E2"], "fontes_ids": ["E3"]}
    triagem = {"trecho_alvo": resposta["trecho_alvo"], "justificativa": "A exportação usa as relações.", "vinculo": "DIRETO",
               "fontes_mudanca_ids": ["E1", "E2"], "fontes_ids": ["E3"]}
    modelo = Modelo([triagem, resposta])
    a = Assistente(Config(), Cliente(), Gerador(Config(), lambda _: modelo))
    r = asyncio.run(a.executar("Impacto?", requisito_id=raiz))
    assert r["estado"] == "ok"
    assert [f["versao_id"] for f in r["fontes"][:2]] == ["publicada", "candidata"]
    assert r["contexto_impacto"]["relacoes"][0]["origemId"] == raiz
    assert r["resultado"]["sugestoes"][0]["estado_validacao"] == "PENDENTE_HUMANO"


def test_avaliacao_grava_dez_operacoes_sem_enviar_gabarito_ao_modelo(tmp_path):
    from athena_ia.avaliacao import avaliar
    class AssistenteFake:
        chamadas = []
        async def executar(self, pergunta, usar_rag):
            self.chamadas.append((pergunta, usar_rag))
            return {"modo": "com_rag" if usar_rag else "sem_rag", "pergunta": pergunta,
                    "estado": "ok", "modelo": "fake", "resultado": RESPOSTA, "fontes": [],
                    "metricas": Medicao().finalizar(Config())}
    p = tmp_path / "perguntas.json"
    p.write_text(json.dumps([{"id": f"P{i}", "pergunta": f"Pergunta {i}", "esperado": "GABARITO_PRIVADO"} for i in range(5)]))
    a = AssistenteFake()
    r = asyncio.run(avaliar(a, p, tmp_path / "saidas"))
    assert r["operacoes"] == 10 and len(a.chamadas) == 10
    assert "GABARITO_PRIVADO" not in str(a.chamadas)
    from pathlib import Path
    assert len(list(Path(r["pasta"]).glob("*.json"))) == 10
    assert (Path(r["pasta"]) / "comparacao.csv").exists()
