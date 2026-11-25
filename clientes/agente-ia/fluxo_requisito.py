"""Operações manuais do projeto local de demonstração; não são tools do LLM."""
import argparse
import json
import os
import time
from urllib.parse import urlparse
from uuid import NAMESPACE_URL, UUID, uuid5

import httpx

PROJETO_DEMO = "22222222-2222-4222-8222-222222222222"


class Fluxo:
    def __init__(self, http, projeto, requisito):
        self.http = http
        self.base = f"/api/v1/gestao/projetos/{UUID(projeto)}"
        self.path = self.base + f"/requisitos/{UUID(requisito)}"

    def request(self, metodo, path, **kwargs):
        resposta = self.http.request(metodo, path, **kwargs)
        if resposta.is_error:
            raise ValueError(f"HTTP {resposta.status_code} em {path}. Consulte o estado antes de repetir a etapa.")
        return resposta

    def consultar(self):
        meta = self.request("GET", self.path).json()
        vid = meta.get("versaoId") or meta.get("versaoPublicadaId")
        versao = self.request("GET", self.path + f"/versoes/{UUID(vid)}") if vid else None
        return meta, versao

    def resumo(self, meta, versao):
        return {"codigo": meta["codigo"], "requisitoId": meta["requisitoId"],
                "estado": meta.get("estadoVersao") or ("PUBLICADA" if meta.get("versaoPublicadaId") else "SEM_VERSAO"),
                "versaoPublicadaId": meta.get("versaoPublicadaId"), "loteId": meta.get("loteId"),
                "versao": versao.json() if versao is not None else None}

    def gravar(self, path, corpo, recurso):
        etag = recurso.headers.get("etag")
        if not etag:
            raise ValueError("Resposta sem ETag; a operação não foi enviada.")
        identidade = json.dumps([path, etag, corpo], sort_keys=True, ensure_ascii=False)
        # A mesma tentativa pode ser retomada após uma resposta perdida.
        chave = str(uuid5(NAMESPACE_URL, "athena-fluxo-manual:" + identidade))
        return self.request("POST", path, json=corpo,
                            headers={"If-Match": etag, "Idempotency-Key": chave}).json()

    def executar(self, acao, justificativa=None):
        meta, versao = self.consultar()
        if acao == "estado":
            return self.resumo(meta, versao)
        if meta["situacao"] != "ATIVO" or not meta.get("versaoId") or versao is None:
            raise ValueError("A etapa exige um requisito ativo com versão candidata. Consulte o estado.")
        candidata = versao.json()
        esperado = {"submeter": "RASCUNHO", "aprovar": "AGUARDANDO_REVISAO", "publicar": "APROVADA"}[acao]
        if candidata["estadoVersao"] != esperado:
            raise ValueError(f"A etapa {acao} exige {esperado}; estado atual: {candidata['estadoVersao']}.")
        if acao == "submeter":
            resultado = self.gravar(self.path + "/submissoes", {"versaoId": candidata["versaoId"]}, versao)
        else:
            if not meta.get("loteId"):
                raise ValueError("O lote de revisão ainda não está disponível. Consulte o estado novamente.")
            lpath = self.base + f"/revisoes/{UUID(meta['loteId'])}"
            lote = self.request("GET", lpath)
            dados = lote.json()
            if (dados["requisitoId"] != meta["requisitoId"] or dados["versaoId"] != candidata["versaoId"]
                    or dados["hashConteudo"] != candidata["hashConteudo"]):
                raise ValueError("O lote não corresponde à versão atual. Nenhuma decisão foi enviada.")
            if acao == "aprovar":
                if not justificativa or not justificativa.strip():
                    raise ValueError("Informe --justificativa com o motivo da sua aprovação.")
                if dados["estado"] != "ATIVO" or dados.get("decisao"):
                    raise ValueError("O lote não está disponível para uma nova decisão.")
                if any(p["bloqueante"] and not p["resolvida"] for p in dados["pendencias"]):
                    raise ValueError("Há pendência bloqueante aberta. Resolva-a antes de aprovar.")
                resultado = self.gravar(lpath + "/decisoes", {
                    "decisao": "APROVAR", "justificativa": justificativa.strip(),
                    **{k: dados[k] for k in ("submissaoId", "versaoId", "hashConteudo")}}, lote)
            else:
                if (dados.get("decisao") or {}).get("decisao") != "APROVAR":
                    raise ValueError("O lote ainda não registra uma aprovação.")
                resultado = self.gravar(self.path + "/publicacoes", {
                    "versaoId": candidata["versaoId"], "submissaoId": dados["submissaoId"]}, versao)
        return {"etapa": acao, "resultado": resultado}

    def aguardar(self, acao, segundos=20):
        esperado = {"submeter": "AGUARDANDO_REVISAO", "aprovar": "APROVADA", "publicar": "PUBLICADA"}[acao]
        fim = time.monotonic() + segundos
        while True:
            meta, versao = self.consultar()
            estado = self.resumo(meta, versao)
            if estado["estado"] == esperado or time.monotonic() >= fim:
                estado["transicao_confirmada"] = estado["estado"] == esperado
                return estado
            time.sleep(1)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("acao", choices=["estado", "submeter", "aprovar", "publicar"])
    parser.add_argument("--requisito", required=True, type=UUID)
    parser.add_argument("--justificativa")
    args = parser.parse_args()
    url = os.getenv("ATHENA_URL", "http://gateway:8080")
    projeto = os.getenv("ATHENA_PROJETO_ID", PROJETO_DEMO)
    if urlparse(url).hostname not in {"localhost", "127.0.0.1", "gateway"} or projeto != PROJETO_DEMO:
        parser.error("Este utilitário é restrito ao projeto local de demonstração.")
    if args.acao == "aprovar" and not (args.justificativa or "").strip():
        parser.error("aprovar exige --justificativa com o motivo da sua decisão.")
    email = os.getenv("FLUXO_EMAIL", "aprovador@athena.dev" if args.acao in {"aprovar", "publicar"} else "editor@athena.dev")
    try:
        with httpx.Client(base_url=url, timeout=15) as http:
            login = http.post("/api/v1/autenticacao/login", json={
                "email": email, "senha": os.getenv("FLUXO_SENHA", "Athena@2026")})
            if login.status_code != 200:
                raise ValueError(f"Login recusado: HTTP {login.status_code}.")
            http.headers["Authorization"] = "Bearer " + login.json()["accessToken"]
            fluxo = Fluxo(http, projeto, str(args.requisito))
            resultado = fluxo.executar(args.acao, args.justificativa)
            print(json.dumps({"usuario": email, **resultado}, ensure_ascii=False, indent=2), flush=True)
            if args.acao != "estado":
                print(json.dumps(fluxo.aguardar(args.acao), ensure_ascii=False, indent=2), flush=True)
    except (ValueError, KeyError, httpx.HTTPError) as exc:
        detalhe = str(exc) if isinstance(exc, ValueError) else "Falha de comunicação. Consulte o estado antes de repetir."
        print(json.dumps({"erro": detalhe}, ensure_ascii=False))
        raise SystemExit(1)


if __name__ == "__main__":
    main()
