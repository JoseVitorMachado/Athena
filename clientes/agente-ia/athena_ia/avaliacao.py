import csv
import json
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo

from .metricas import evento


def verificar_fontes(resultado, esperadas):
    fontes = {f["id"]: f for f in resultado["fontes"]}
    citadas = resultado["resultado"]["fontes_ids"]
    recuperadas = {f["codigo"] for f in fontes.values()}
    return {"citacoes_resolvem": all(id_ in fontes for id_ in citadas),
            "fontes_esperadas": esperadas,
            "fontes_esperadas_recuperadas": sorted(set(esperadas) & recuperadas),
            "cobertura_recuperacao": len(set(esperadas) & recuperadas) / len(set(esperadas)) if esperadas else None,
            "nota": "Verificação de identidade e recuperação; não avalia correção semântica ou apoio de cada afirmação."}


def ficha_humana(pasta, resultados):
    campos = ["operacao_id", "caso", "modo", "arquivo", "correcao", "fundamentacao", "ausencia", "observacoes", "revisor", "data"]
    with (pasta / "revisao-humana.csv").open("w", newline="") as arquivo:
        writer = csv.DictWriter(arquivo, fieldnames=campos)
        writer.writeheader()
        for r in resultados:
            caso = r.get("pergunta_id", r.get("cenario_id"))
            writer.writerow({"operacao_id": r["metricas"]["operacao_id"], "caso": caso, "modo": r["modo"],
                             "arquivo": f"{caso}-{r['modo']}.json"})
    (pasta / "RUBRICA.md").write_text("""# Revisão humana pendente

Leia cada JSON (pergunta, resposta, gabarito, versões, fontes, estado e métricas) antes de preencher o CSV.
Não atribua notas apenas por palavras-chave ou por uma citação existir. A coluna `esperado` é um roteiro
autorado para estes dados fictícios, não uma verdade universal. Divergências justificadas são permitidas.

| Dimensão | 0 | 1 | 2 |
|---|---|---|---|
| Correção | Errada/contraditória ou indisponível | Parcial, omite parte relevante | Responde corretamente ao pedido |
| Fundamentação | Fatos inventados/sem apoio ou indisponível | Apoio parcial, referências pouco precisas | Afirmações sustentadas nas fontes; sem contexto, admite essa limitação |
| Ausência | Inventa o que falta ou indisponível | Reconhece só parte das lacunas | Distingue o documentado do ausente e publicada de candidata |

Para impacto, correção considera o delta, efeito plausível e verificação concreta; fundamentação exige
evidência do alvo e das duas versões. Relação no mapa não basta. Ausência considera alvos sem efeito
demonstrável e informação insuficiente. Uma falha de geração deve ser identificada como tal nas observações,
sem tratá-la como uma resposta semântica correta. Rejeitar controles negativos não prova cobertura global.

Preencha as três notas (0, 1 ou 2), observações, nome do revisor e data ISO (YYYY-MM-DD).
Deixe a linha inteiramente sem notas enquanto estiver pendente. Não preencha nome de outra pessoa.
Use `python -m athena_ia consolidar-revisao --arquivo <pasta>/revisao-humana.csv`
para validar o preenchimento e obter médias separadas por modo. Esse comando não altera os JSONs originais.
Análise assistida pelo Codex, se houver, é um documento separado e não substitui esta revisão humana.
""")


def consolidar_revisao(arquivo):
    arquivo = Path(arquivo)
    linhas = list(csv.DictReader(arquivo.open()))
    avaliadas, pendentes, vistos = [], 0, set()
    for linha in linhas:
        if linha["operacao_id"] in vistos:
            raise ValueError("Operação duplicada na revisão.")
        vistos.add(linha["operacao_id"])
        caminho = (arquivo.parent / linha["arquivo"]).resolve()
        if caminho.parent != arquivo.parent.resolve():
            raise ValueError("A evidência precisa estar na mesma pasta da ficha.")
        evidencia = json.loads(caminho.read_text())
        if evidencia["metricas"]["operacao_id"] != linha["operacao_id"] or evidencia["modo"] != linha["modo"]:
            raise ValueError("Ficha não corresponde à evidência da operação.")
        notas = [linha[k].strip() for k in ("correcao", "fundamentacao", "ausencia")]
        if not any(notas):
            pendentes += 1
            continue
        if any(n not in {"0", "1", "2"} for n in notas) or not all(linha[k].strip() for k in ("revisor", "data", "observacoes")):
            raise ValueError("Linha avaliada exige três notas 0–2, revisor, data e observações.")
        datetime.strptime(linha["data"], "%Y-%m-%d")
        avaliadas.append(linha)
    grupos = {}
    for modo in sorted({l["modo"] for l in avaliadas}):
        grupo = [l for l in avaliadas if l["modo"] == modo]
        grupos[modo] = {"quantidade": len(grupo), **{k: round(sum(int(l[k]) for l in grupo) / len(grupo), 3) for k in ("correcao", "fundamentacao", "ausencia")}}
    return {"origem": "Ficha preenchida externamente; o programa valida o formato, não a identidade do revisor.",
            "avaliadas": len(avaliadas), "pendentes": pendentes, "medias_por_modo": grupos}


def celula(valor):
    return str(valor).replace("|", "\\|").replace("\n", "<br>")


async def avaliar(assistente, perguntas, destino):
    itens = json.loads(Path(perguntas).read_text())
    if len(itens) < 5 or len({p["id"] for p in itens}) != len(itens):
        raise ValueError("A avaliação exige pelo menos cinco perguntas com IDs distintos.")
    pasta = Path(destino) / datetime.now(ZoneInfo("America/Sao_Paulo")).strftime("%Y%m%d-%H%M%S-%f")
    pasta.mkdir(parents=True)
    resultados = []
    for indice, item in enumerate(itens):
        # Alternar a ordem reduz o viés de sempre executar o mesmo modo primeiro.
        for rag in ((False, True) if indice % 2 == 0 else (True, False)):
            evento("avaliacao_iniciada", pergunta_id=item["id"], rag=rag)
            r = await assistente.executar(item["pergunta"], usar_rag=rag)
            r.update(pergunta_id=item["id"], esperado=item["esperado"], avaliacao_humana="PENDENTE")
            r["verificacoes_automaticas"] = verificar_fontes(r, item.get("fontes_esperadas", []))
            resultados.append(r)
            # Salvar cada operação para preservar progresso em caso de interrupção.
            (pasta / f"{item['id']}-{r['modo']}.json").write_text(json.dumps(r, ensure_ascii=False, indent=2) + "\n")
    campos = ["pergunta_id", "modo", "pergunta", "estado", "resposta", "justificativa", "referencias", "trechos_usados",
              "mcp_ms", "llm_ms", "total_ms", "tokens_entrada", "tokens_saida", "custo_referencia_usd", "modelo", "esperado", "avaliacao_humana"]
    linhas = []
    for r in resultados:
        linhas.append({**{k: r[k] for k in ("pergunta_id", "modo", "pergunta", "estado", "modelo", "esperado", "avaliacao_humana")},
                       "resposta": r["resultado"]["resposta"], "justificativa": r["resultado"]["justificativa"],
                       "referencias": json.dumps(r["resultado"]["fontes_ids"], ensure_ascii=False),
                       "trechos_usados": json.dumps(r["fontes"], ensure_ascii=False),
                       **{k: r["metricas"][k] for k in ("mcp_ms", "llm_ms", "total_ms", "tokens_entrada", "tokens_saida", "custo_referencia_usd")}})
    with (pasta / "comparacao.csv").open("w", newline="") as arquivo:
        writer = csv.DictWriter(arquivo, fieldnames=campos)
        writer.writeheader()
        writer.writerows(linhas)
    markdown = ["# Comparação com e sem RAG", "", "Execuções reais; avaliação humana ainda pendente. Falhas/degradações não equivalem a respostas do LLM.", "",
                "| Pergunta | Modo | Estado | Resposta | Fontes | MCP ms | LLM ms | Total ms | USD referência |",
                "|---|---|---|---|---|---:|---:|---:|---:|"]
    for r in linhas:
        markdown.append("| " + " | ".join(celula(r[k]) for k in ("pergunta_id", "modo", "estado", "resposta", "referencias", "mcp_ms", "llm_ms", "total_ms", "custo_referencia_usd")) + " |")
    markdown.extend(["", "O CSV e os JSONs preservam os trechos, perguntas, justificativas, versões e métricas completas.",
                     "Rubrica humana: correção (0–2), apoio nas fontes (0–2), reconhecimento de informação ausente (0–2).",
                     "Compare também citações inventadas e cobertura. Não use presença de palavras-chave como prova de qualidade.",
                     "Custo é uma simulação de referência, não gasto efetivo. null indica consumo incompleto em alguma tentativa."])
    (pasta / "comparacao.md").write_text("\n".join(markdown) + "\n")
    ficha_humana(pasta, resultados)
    return {"pasta": str(pasta), "operacoes": len(resultados), "degradadas": sum(r["estado"] != "ok" for r in resultados)}


async def avaliar_impactos(assistente, cenarios, destino):
    itens = json.loads(Path(cenarios).read_text())
    pasta = Path(destino) / ("impactos-" + datetime.now(ZoneInfo("America/Sao_Paulo")).strftime("%Y%m%d-%H%M%S-%f"))
    pasta.mkdir(parents=True)
    resultados = []
    for item in itens:
        evento("cenario_impacto_iniciado", cenario=item["id"])
        r = await assistente.executar("Quais requisitos podem precisar de revisão por esta alteração?", requisito_id=item["requisito_id"])
        sugeridos = {s["requisito"] for s in r["resultado"]["sugestoes"]}
        analisados = {a["requisito"] for a in r["resultado"].get("analises", []) if a["estado_geracao"] == "ok"}
        negativos = set(item["negativos"])
        r.update(cenario_id=item["id"], esperado=item, avaliacao_humana="PENDENTE", verificacoes_automaticas={
            "positivos_sugeridos": sorted(sugeridos & set(item["positivos"])),
            "positivos_nao_sugeridos": sorted(set(item["positivos"]) - sugeridos),
            "negativos_sugeridos": sorted(sugeridos & negativos),
            "negativos_analisados": sorted(analisados & negativos),
            "negativos_nao_avaliados": sorted(negativos - analisados),
            "nota": "Confronto com cenários autorados; conclusões semânticas exigem revisão humana. Não contar alvos ausentes como rejeições corretas."})
        resultados.append(r)
        (pasta / f"{item['id']}-{r['modo']}.json").write_text(json.dumps(r, ensure_ascii=False, indent=2) + "\n")
    ficha_humana(pasta, resultados)
    return {"pasta": str(pasta), "operacoes": len(resultados), "degradadas": sum(r["estado"] != "ok" for r in resultados)}
