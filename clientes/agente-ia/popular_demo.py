"""Carga explícita de fixtures acadêmicas; não é uma ferramenta disponível ao LLM."""
import argparse
import json
import os
import time
from pathlib import Path
from urllib.parse import urlparse
from uuid import NAMESPACE_URL, uuid5

import httpx

PROJETO = "22222222-2222-4222-8222-222222222222"
DOCUMENTO = "44444444-4444-4444-8444-444444444444"
CAMPOS = ("titulo", "conteudo", "criteriosAceite")


def salvar(path, dados):
    path.parent.mkdir(parents=True, exist_ok=True)
    temporario = path.with_suffix(".tmp")
    temporario.write_text(json.dumps(dados, ensure_ascii=False, indent=2) + "\n")
    temporario.replace(path)


def payload(item, candidata=False):
    dados = {k: item[k] for k in CAMPOS}
    if candidata:
        dados.update({k: v for k, v in item.get("candidata", {}).items() if k in CAMPOS})
    dados["titulo"] = f"[IA-V2-{item['chave']}] {dados['titulo']}"
    return dados


def conferir(versao, esperado):
    if any(versao.get(k) != esperado[k] for k in CAMPOS):
        raise ValueError("Fixture já existe com conteúdo diferente. Nenhum conteúdo existente será sobrescrito.")


class Demo:
    def __init__(self, url, destino):
        if urlparse(url).hostname not in {"localhost", "127.0.0.1", "gateway"}:
            raise ValueError("A carga é restrita ao ATHENA local e ao projeto de demonstração.")
        self.http = httpx.Client(base_url=url, timeout=20)
        self.base = f"/api/v1/gestao/projetos/{PROJETO}"
        self.destino = destino
        self.manifesto = {"dataset": "athena-ia-demo-v2", "projeto_id": PROJETO, "requisitos": {}}
        if (destino / "manifesto.json").exists():
            self.manifesto = json.loads((destino / "manifesto.json").read_text())
        if self.manifesto["projeto_id"] != PROJETO:
            raise ValueError("Manifesto de outro projeto.")
        self.login()

    def login(self):
        r = self.http.post("/api/v1/autenticacao/login", json={
            "email": os.getenv("DEMO_EMAIL", "aprovador@athena.dev"),
            "senha": os.getenv("DEMO_SENHA", "Athena@2026")})
        r.raise_for_status()
        self.http.headers["Authorization"] = "Bearer " + r.json()["accessToken"]

    def request(self, method, path, **kwargs):
        r = self.http.request(method, self.base + path, **kwargs)
        if r.status_code == 401:
            self.login()
            r = self.http.request(method, self.base + path, **kwargs)
        if r.is_error:
            # Exibir somente o problema de domínio, nunca headers de autenticação.
            raise ValueError(f"{method} {path}: HTTP {r.status_code}: {r.text[:500]}")
        return r

    def listar(self, path):
        itens, pagina = [], 0
        while True:
            dados = self.request("GET", path, params={"page": pagina, "size": 100}).json()
            itens.extend(dados["items"])
            pagina += 1
            if pagina >= dados["totalPages"]:
                return itens

    def guardar(self):
        salvar(self.destino / "manifesto.json", self.manifesto)

    def aguardar(self, funcao, descricao, segundos=120):
        fim = time.monotonic() + segundos
        while time.monotonic() < fim:
            resultado = funcao()
            if resultado:
                return resultado
            time.sleep(1)
        raise ValueError(f"Tempo esgotado: {descricao}. Execute novamente para retomar.")

    def criar(self, item):
        chave = item["chave"]
        marcador = f"[IA-V2-{chave}] "
        existentes = [r for r in self.listar("/requisitos") if r["titulo"].startswith(marcador)]
        registro = self.manifesto["requisitos"].get(chave)
        if len(existentes) > 1:
            raise ValueError(f"Mais de uma fixture com o marcador {marcador}.")
        if registro:
            meta = self.request("GET", f"/requisitos/{registro['id']}").json()
            if not meta["titulo"].startswith(marcador):
                raise ValueError("Identidade da fixture no manifesto mudou.")
        elif existentes:
            meta = existentes[0]
        else:
            # Não repetir automaticamente POST de criação após timeout ambíguo.
            meta = self.request("POST", "/requisitos", json={"documentoId": DOCUMENTO,
                                "tipo": "FUNCIONAL", **payload(item)}).json()
        registro = {"id": meta["requisitoId"], "codigo": meta["codigo"]}
        self.manifesto["requisitos"][chave] = registro
        self.guardar()
        path = f"/requisitos/{registro['id']}"
        meta = self.request("GET", path).json()
        if meta.get("situacao") == "CANCELADO":
            raise ValueError("Fixture foi cancelada; não será reativada pela carga.")
        versao = self.request("GET", path + (f"/versoes/{meta['versaoPublicadaId']}" if meta.get("versaoPublicadaId") else "/candidata")).json()
        conferir(versao, payload(item))
        print(f"{chave}: {registro['codigo']} presente", flush=True)

    def chave(self, acao, id_):
        return str(uuid5(NAMESPACE_URL, f"athena-ia-demo-v2:{acao}:{id_}"))

    def publicar(self, item):
        registro = self.manifesto["requisitos"][item["chave"]]
        path = f"/requisitos/{registro['id']}"
        meta = self.request("GET", path).json()
        if not meta.get("versaoPublicadaId"):
            versao = self.request("GET", path + "/candidata")
            vid = versao.json()["versaoId"]
            submissoes = [s for s in self.listar(path + "/submissoes") if s["versaoId"] == vid]
            if len(submissoes) > 1:
                raise ValueError("Fixture com múltiplas submissões: conferir manualmente.")
            submissao = submissoes[0] if submissoes else self.request("POST", path + "/submissoes",
                json={"versaoId": vid}, headers={"If-Match": versao.headers["etag"], "Idempotency-Key": self.chave("submeter", vid)}).json()
            spath = path + "/submissoes/" + submissao["submissaoId"]

            def concluida():
                s = self.request("GET", spath).json()
                if s["estadoSaga"] not in {"EM_ANDAMENTO", "CONCLUIDA"}:
                    raise ValueError("Saga da fixture exige inspeção: " + s["estadoSaga"])
                return s if s["estadoSaga"] == "CONCLUIDA" else None

            submissao = self.aguardar(concluida, "conclusão da submissão")
            lpath = "/revisoes/" + submissao["loteId"]
            def lote_ativo():
                r = self.request("GET", lpath)
                return r if r.json()["estado"] != "PREPARADO" else None

            lote = self.aguardar(lote_ativo, "ativação do lote de revisão")
            dados = lote.json()
            if any(p["bloqueante"] and not p["resolvida"] for p in dados["pendencias"]):
                raise ValueError("Lote contém pendências bloqueantes; a carga não as resolve automaticamente.")
            if dados.get("decisao") is None:
                self.request("POST", lpath + "/decisoes", json={
                    "decisao": "APROVAR", "justificativa": "Carga determinística de dados fictícios para a demonstração acadêmica de IA; não é aprovação proposta pelo agente.",
                    **{k: dados[k] for k in ("submissaoId", "versaoId", "hashConteudo")}},
                    headers={"If-Match": lote.headers["etag"], "Idempotency-Key": self.chave("aprovar", vid)})

            def aprovada():
                r = self.request("GET", path + "/candidata")
                return r if r.json()["estadoVersao"] == "APROVADA" else None

            versao = self.aguardar(aprovada, "propagação da aprovação")
            self.request("POST", path + "/publicacoes", json={"versaoId": vid, "submissaoId": submissao["submissaoId"]},
                         headers={"If-Match": versao.headers["etag"], "Idempotency-Key": self.chave("publicar", vid)})
            meta = self.request("GET", path).json()
        registro["versao_publicada_id"] = meta["versaoPublicadaId"]
        self.guardar()
        if "candidata" in item:
            meta = self.request("GET", path).json()
            esperado = payload(item, candidata=True)
            if not meta.get("versaoId"):
                publicada = self.request("GET", path + "/versoes/" + registro["versao_publicada_id"])
                candidata = self.request("PATCH", path + "/candidata", json=esperado, headers={"If-Match": publicada.headers["etag"]})
            else:
                candidata = self.request("GET", path + "/candidata")
            conferir(candidata.json(), esperado)
            registro["versao_candidata_id"] = candidata.json()["versaoId"]
            self.guardar()
        print(f"{registro['codigo']}: publicada", flush=True)

    def relacoes(self, itens):
        existentes = self.listar("/relacoes")
        for item in itens:
            dados = {"origemId": self.manifesto["requisitos"][item["origem"]]["id"],
                     "destinoId": self.manifesto["requisitos"][item["destino"]]["id"],
                     "tipo": item["tipo"], "justificativa": item["justificativa"]}
            if not any(mesma_relacao(r, dados) for r in existentes):
                existentes.append(self.request("POST", "/relacoes", json=dados).json())

    def indexadas(self, item):
        registro = self.manifesto["requisitos"][item["chave"]]
        r = self.http.post("/mcp", headers={"Accept": "application/json, text/event-stream"}, json={
            "jsonrpc": "2.0", "id": 1, "method": "tools/call", "params": {"name": "buscar_contexto",
            "arguments": {"projetoId": PROJETO, "q": payload(item)["titulo"], "limite": 10}}})
        r.raise_for_status()
        resposta = r.json()
        if "error" in resposta or resposta.get("result", {}).get("isError"):
            raise ValueError("MCP recusou a verificação da indexação.")
        trechos = resposta["result"]["structuredContent"]["trechos"]
        return any(t["referencia"]["origemId"] == registro["id"] and t["referencia"]["versaoId"] == registro["versao_publicada_id"] for t in trechos)

    def executar(self, dados):
        for n, id_ in ((1, "11111111-1111-4111-8111-111111111111"), (2, "11111111-1111-4111-8111-111111111112"), (3, "11111111-1111-4111-8111-111111111113")):
            meta = self.request("GET", "/requisitos/" + id_).json()
            self.manifesto["requisitos"][f"REQ-{n}"] = {"id": id_, "codigo": meta["codigo"], "versao_publicada_id": meta.get("versaoPublicadaId")}
        for item in dados["requisitos"]:
            self.criar(item)
        self.relacoes(dados["relacoes"])
        for item in dados["requisitos"]:
            self.publicar(item)
        for item in dados["requisitos"]:
            self.aguardar(lambda: self.indexadas(item), "indexação real de " + item["chave"])
        requisitos = self.manifesto["requisitos"]
        perguntas = [{**p, "fontes_esperadas": [requisitos[k]["codigo"] for k in p["fontes_esperadas"]]} for p in dados["perguntas"]]
        impactos = [{**i, "requisito_id": requisitos[i["requisito"]]["id"],
                     **{grupo: [requisitos[k]["codigo"] for k in i[grupo]] for grupo in ("positivos", "plausiveis", "negativos")}} for i in dados["impactos"]]
        salvar(self.destino / "perguntas.json", perguntas)
        salvar(self.destino / "impactos.json", impactos)
        self.manifesto["indexacao_verificada"] = True
        self.guardar()
        print("Carga concluída: oito fixtures publicadas e verificadas pelo MCP; originais preservados.")


def mesma_relacao(a, b):
    if a["tipo"] != b["tipo"]:
        return False
    if a["tipo"] == "RELACIONADO_A":
        return {a["origemId"], a["destinoId"]} == {b["origemId"], b["destinoId"]}
    return (a["origemId"], a["destinoId"]) == (b["origemId"], b["destinoId"])


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--aplicar", action="store_true", help="Criar, revisar e publicar as oito fixtures no projeto local de demonstração.")
    parser.add_argument("--saida", type=Path, default=Path("resultados/demo-v2"))
    args = parser.parse_args()
    dados = json.loads((Path(__file__).parent / "dados/demo-v2.json").read_text())
    if not args.aplicar:
        print(json.dumps(dados, ensure_ascii=False, indent=2))
        print("Prévia somente. Use --aplicar para carregar estas fixtures fictícias no ATHENA local.")
        return
    demo = Demo(os.getenv("ATHENA_URL", "http://localhost:8080"), args.saida)
    try:
        demo.executar(dados)
    finally:
        demo.http.close()


if __name__ == "__main__":
    main()
