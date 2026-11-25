import argparse
import asyncio
import json
import sys
from http.server import BaseHTTPRequestHandler, HTTPServer
from pathlib import Path

import httpx

from .athena import ClienteAthena, ErroAthena
from .avaliacao import avaliar, avaliar_impactos, consolidar_revisao
from .chain import Assistente
from .config import Config
from .metricas import evento


def preparar_modelo(config):
    with httpx.Client(base_url=config.llm_url, headers=config.headers_llm(), timeout=900) as cliente:
        evento("modelo_download", modelo=config.modelo)
        with cliente.stream("POST", "/api/pull", json={"model": config.modelo, "stream": True}) as r:
            r.raise_for_status()
            for linha in r.iter_lines():
                dados = json.loads(linha)
                if "error" in dados:
                    raise ValueError("Ollama não conseguiu baixar o modelo.")
        r = cliente.get("/api/tags")
        r.raise_for_status()
        item = next((m for m in r.json()["models"] if m["name"] == config.modelo), None)
        if not item or (config.modelo_digest and item["digest"] != config.modelo_digest):
            raise ValueError("Digest do modelo difere do configurado. Revise a versão antes da demonstração.")
        evento("modelo_verificado", modelo=config.modelo, digest=item["digest"])
        return aquecer(config)


def aquecer(config):
    with httpx.Client(base_url=config.llm_url, headers=config.headers_llm(), timeout=180) as cliente:
        r = cliente.post("/api/generate", json={"model": config.modelo, "prompt": "Olá", "stream": False,
                                                "keep_alive": "10m", "options": {"num_predict": 1, "num_ctx": config.contexto}})
        r.raise_for_status()
        dados = r.json()
        return {"estado": "aquecido", "modelo": config.modelo, "total_ms": dados.get("total_duration", 0) / 1e6,
                "carga_ms": dados.get("load_duration", 0) / 1e6}


def servir():
    class Health(BaseHTTPRequestHandler):
        def do_GET(self):
            if self.path != "/health":
                self.send_error(404)
                return
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(b'{"status":"ok","cliente":"athena-agente-ia"}')

        def log_message(self, *_):
            pass

    evento("cliente_pronto", comandos="verificar, perguntar, impacto, avaliar")
    HTTPServer(("0.0.0.0", 8090), Health).serve_forever()


async def executar(args, config):
    async with ClienteAthena(config) as cliente:
        assistente = Assistente(config, cliente)
        if args.comando == "verificar":
            await cliente.inicializar_mcp()
            tools = await cliente.rpc("tools/list")
            trechos = await cliente.buscar_contexto("exportar documento")
            await cliente.autenticar(forcar_renovacao=True)
            await cliente.get("/mapa")
            return {"login": "ok", "renovacao": "ok", "mcp": "ok",
                    "ferramentas": [t["name"] for t in tools["tools"]], "trechos": trechos}
        if args.comando == "perguntar":
            return await assistente.executar(args.pergunta, usar_rag=not args.sem_rag)
        if args.comando == "impacto":
            return await assistente.executar("Quais outros requisitos podem precisar de revisão por causa desta alteração? Justifique com evidências.", requisito_id=args.requisito)
        if args.comando == "avaliar-impactos":
            return await avaliar_impactos(assistente, args.cenarios, args.destino)
        return await avaliar(assistente, args.perguntas, args.destino)


def main():
    parser = argparse.ArgumentParser(description="Cliente de IA do ATHENA: sugestões com evidências e decisão humana.")
    sub = parser.add_subparsers(dest="comando", required=True)
    for nome in ("servir", "preparar-modelo", "aquecer", "verificar"):
        sub.add_parser(nome)
    perguntar = sub.add_parser("perguntar")
    perguntar.add_argument("pergunta")
    perguntar.add_argument("--sem-rag", action="store_true")
    perguntar.add_argument("--saida", type=Path)
    impacto = sub.add_parser("impacto")
    impacto.add_argument("--requisito", default="11111111-1111-4111-8111-111111111111")
    impacto.add_argument("--saida", type=Path)
    avaliacao = sub.add_parser("avaliar")
    avaliacao.add_argument("--perguntas", type=Path, default=Path(__file__).resolve().parent.parent / "perguntas.json")
    avaliacao.add_argument("--destino", type=Path, default=Path("resultados"))
    cenarios = sub.add_parser("avaliar-impactos")
    cenarios.add_argument("--cenarios", type=Path, required=True)
    cenarios.add_argument("--destino", type=Path, default=Path("resultados"))
    revisao = sub.add_parser("consolidar-revisao")
    revisao.add_argument("--arquivo", type=Path, required=True)
    args = parser.parse_args()
    try:
        config = Config.from_env()
        if args.comando == "servir":
            servir()
            return
        if args.comando == "consolidar-revisao":
            resultado = consolidar_revisao(args.arquivo)
        elif args.comando == "preparar-modelo":
            resultado = preparar_modelo(config)
        elif args.comando == "aquecer":
            resultado = aquecer(config)
        else:
            resultado = asyncio.run(executar(args, config))
        texto = json.dumps(resultado, ensure_ascii=False, indent=2) + "\n"
        if getattr(args, "saida", None):
            args.saida.parent.mkdir(parents=True, exist_ok=True)
            args.saida.write_text(texto)
        print(texto, end="")
    except (ErroAthena, httpx.HTTPError, ValueError, KeyError) as exc:
        # Não imprimir exceções HTTP que possam carregar headers/credenciais.
        evento("operacao_falhou", tipo=type(exc).__name__, detalhe=str(exc) if isinstance(exc, ErroAthena) else "Verifique configuração, contratos e disponibilidade dos serviços.")
        sys.exit(1)


if __name__ == "__main__":
    main()
