import asyncio
import csv
import json
from dataclasses import replace

import pytest

from athena_ia.avaliacao import consolidar_revisao, ficha_humana, verificar_fontes
from athena_ia.config import Config
from athena_ia.impacto import analisar, comparar_versoes
from athena_ia.llm import AnaliseAlvo, Gerador, esquema_saida, validar_saida
from athena_ia.metricas import Medicao
from popular_demo import conferir, mesma_relacao
from test_cliente import Modelo

FONTES = [{"id": "E1", "codigo": "REQ-1", "origem": "API_PUBLICADA", "texto": "exportar", "requisito_id": "1"},
          {"id": "E2", "codigo": "REQ-1", "origem": "API_CANDIDATA", "texto": "exportar PDF", "requisito_id": "1"},
          {"id": "E3", "codigo": "REQ-2", "origem": "API_RELACIONADO", "texto": "Preservar todas as relações oficiais.", "requisito_id": "2"}]
ANALISE = {"classificacao": "REVISAR", "trecho_alvo": "Preservar todas as relações oficiais.", "mudanca": "PDF adicionado", "efeito": "Pode perder relações.",
           "verificacao": "Comparar relações exportadas.", "fontes_mudanca_ids": ["E1", "E2"], "fontes_ids": ["E3"]}
TRIAGEM = {"trecho_alvo": ANALISE["trecho_alvo"], "vinculo": "DIRETO", "justificativa": "A exportação deve preservar relações.",
           "fontes_mudanca_ids": ["E1", "E2"], "fontes_ids": ["E3"]}


def test_diff_ignora_metadados_mas_preserva_antes_depois():
    antes = {"titulo": "Exportar", "conteudo": "Texto", "criteriosAceite": ["A"], "versaoId": "v1"}
    assert comparar_versoes(antes, {**antes, "versaoId": "v2"}) == []
    diff = comparar_versoes(antes, {**antes, "titulo": "Exportação", "criteriosAceite": ["A", "B"]})
    assert [d["campo"] for d in diff] == ["titulo", "criteriosAceite"]
    assert diff[1]["antes"] == ["A"] and diff[1]["depois"] == ["A", "B"]


def test_citacao_exige_as_duas_versoes_e_alvo_correto():
    formato = esquema_saida(AnaliseAlvo, FONTES, {"REQ-2"})
    assert formato["properties"]["fontes_ids"]["items"]["enum"] == ["E3"]
    assert formato["properties"]["trecho_alvo"]["enum"] == [FONTES[-1]["texto"]]
    assert validar_saida(json.dumps(ANALISE), AnaliseAlvo, FONTES, {"REQ-2"})
    for alteracao in ({"fontes_mudanca_ids": ["E1", "E1"]}, {"fontes_ids": ["E2"]}, {"trecho_alvo": "Uma frase inventada não é uma transcrição."}):
        with pytest.raises(ValueError):
            validar_saida(json.dumps({**ANALISE, **alteracao}), AnaliseAlvo, FONTES, {"REQ-2"})


def test_citacao_literal_decodifica_json_e_quebras_de_linha():
    texto = 'Mostrar "PDF" na lista.\nPreservar relações oficiais.'
    fontes = FONTES[:2] + [{**FONTES[2], "texto": json.dumps({"conteudo": texto, "criteriosAceite": []})}]
    a = {**ANALISE, "trecho_alvo": 'Mostrar "PDF" na lista.'}
    assert validar_saida(json.dumps(a), AnaliseAlvo, fontes, {"REQ-2"})
    assert a["trecho_alvo"] in esquema_saida(AnaliseAlvo, fontes, {"REQ-2"})["properties"]["trecho_alvo"]["enum"]


def test_titulo_mcp_nao_serve_como_evidencia_da_obrigacao():
    fontes = FONTES[:2] + [{**FONTES[2], "origem": "MCP_PUBLICADO",
        "texto": "REQ-2 Rastreabilidade\nPreservar todas as relações oficiais. Critérios de aceite: Não omitir relações."}]
    assert validar_saida(json.dumps(ANALISE), AnaliseAlvo, fontes, {"REQ-2"})
    with pytest.raises(ValueError, match="transcrição literal"):
        validar_saida(json.dumps({**ANALISE, "trecho_alvo": "REQ-2 Rastreabilidade"}), AnaliseAlvo, fontes, {"REQ-2"})
    assert "REQ-2 Rastreabilidade" not in esquema_saida(AnaliseAlvo, fontes, {"REQ-2"})["properties"]["trecho_alvo"]["enum"]


def test_sem_mudanca_nao_chama_llm():
    modelo = Modelo([])
    gerador = Gerador(Config(), lambda _: modelo)
    r, estado = asyncio.run(analisar(gerador, FONTES, {"mudancas": []}, {"REQ-2"}, Medicao()))
    assert estado == "ok" and r["sugestoes"] == [] and modelo.chamadas == 0


def test_so_titulo_exige_conferencia_editorial_sem_inventar_impacto_funcional():
    modelo = Modelo([])
    r, estado = asyncio.run(analisar(Gerador(Config(), lambda _: modelo), FONTES,
        {"mudancas": [{"campo": "titulo", "antes": "Prévia", "depois": "Pré-visualização"}]}, {"REQ-2"}, Medicao()))
    assert estado == "ok" and not r["sugestoes"] and modelo.chamadas == 0
    assert r["revisao_editorial"]["estado_validacao"] == "PENDENTE_HUMANO"
    assert r["revisao_editorial"]["antes"] == "Prévia"


def test_falha_em_um_alvo_preserva_outros_e_soma_tempo_e_tokens():
    fontes = FONTES + [{**FONTES[-1], "id": "E4", "codigo": "REQ-3", "requisito_id": "3"}]
    modelo = Modelo([TRIAGEM, ANALISE, TimeoutError()])
    conf = replace(Config(), tentativas=1)
    medicao = Medicao()
    r, estado = asyncio.run(analisar(Gerador(conf, lambda _: modelo), fontes,
        {"mudancas": [{"campo": "conteudo", "antes": "exportar", "depois": "exportar PDF"}], "relacoes": []}, {"REQ-2", "REQ-3"}, medicao))
    assert estado == "parcial" and [s["requisito"] for s in r["sugestoes"]] == ["REQ-2"]
    assert r["analises"][1]["classificacao"] == "INDETERMINADO"
    assert medicao.tokens_entrada == 200 and not medicao.consumo_completo
    assert medicao.llm_ms >= sum(t["duracao_ms"] for t in medicao.tentativas) - 0.1
    assert [t["etapa"] for t in medicao.tentativas] == ["REQ-2:escopo", "REQ-2:risco", "REQ-3:escopo"]


def test_controle_negativo_explicito_nao_vira_sugestao():
    modelo = Modelo([{**TRIAGEM, "vinculo": "INDEPENDENTE", "justificativa": "Formato não altera prévia textual."}])
    r, estado = asyncio.run(analisar(Gerador(Config(), lambda _: modelo), FONTES,
        {"mudancas": [{"campo": "conteudo", "antes": "exportar", "depois": "PDF"}], "relacoes": []}, {"REQ-2"}, Medicao()))
    assert estado == "ok" and not r["sugestoes"] and len(r["analises"]) == 1
    assert modelo.chamadas == 1  # A classificação independente não entra na etapa que imagina riscos.


def test_recuperacao_nao_e_nota_semantica():
    r = {"fontes": FONTES, "resultado": {"fontes_ids": ["E1"]}}
    v = verificar_fontes(r, ["REQ-1", "REQ-9"])
    assert v["cobertura_recuperacao"] == 0.5 and v["citacoes_resolvem"]
    assert verificar_fontes(r, [])["cobertura_recuperacao"] is None


def test_ficha_pendente_e_revisao_completa_exige_autoria(tmp_path):
    r = {"pergunta_id": "P1", "modo": "com_rag", "metricas": {"operacao_id": "op1"}}
    (tmp_path / "P1-com_rag.json").write_text(json.dumps(r))
    ficha_humana(tmp_path, [r])
    path = tmp_path / "revisao-humana.csv"
    assert consolidar_revisao(path)["pendentes"] == 1
    with path.open() as f:
        reader = csv.DictReader(f)
        campos, linhas = reader.fieldnames, list(reader)
    linhas[0].update(correcao="2", fundamentacao="2", ausencia="2")
    def gravar():
        with path.open("w", newline="") as f:
            w = csv.DictWriter(f, fieldnames=campos)
            w.writeheader()
            w.writerows(linhas)
    gravar()
    with pytest.raises(ValueError, match="revisor"):
        consolidar_revisao(path)
    linhas[0].update(revisor="Pessoa fictícia no teste", data="2026-11-25", observacoes="Dados de teste.")
    gravar()
    assert consolidar_revisao(path)["avaliadas"] == 1
    linhas[0]["operacao_id"] = "outro"
    gravar()
    with pytest.raises(ValueError, match="corresponde"):
        consolidar_revisao(path)


def test_carga_reconhece_simetria_e_nao_sobrescreve_divergencia():
    a = {"origemId": "a", "destinoId": "b", "tipo": "RELACIONADO_A"}
    b = {**a, "origemId": "b", "destinoId": "a"}
    assert mesma_relacao(a, b)
    assert not mesma_relacao({**a, "tipo": "DEPENDE_DE"}, {**b, "tipo": "DEPENDE_DE"})
    with pytest.raises(ValueError, match="sobrescrito"):
        conferir({"titulo": "alterado"}, {"titulo": "original", "conteudo": "texto", "criteriosAceite": []})
