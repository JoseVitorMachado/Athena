import asyncio
import copy
import json
import re
from time import perf_counter
from typing import Literal

import httpx
from langchain_ollama import ChatOllama
from langchain_core.messages import HumanMessage
from ollama import ResponseError
from pydantic import BaseModel, ConfigDict, Field

from .metricas import evento


class Resposta(BaseModel):
    model_config = ConfigDict(extra="forbid")
    resposta: str = Field(min_length=1)
    justificativa: str = Field(min_length=1)
    fontes_ids: list[str]


class Sugestao(BaseModel):
    model_config = ConfigDict(extra="forbid")
    requisito: str
    justificativa: str = Field(min_length=1)
    fontes_ids: list[str] = Field(min_length=1)


class Impacto(Resposta):
    sugestoes: list[Sugestao]


class AnaliseAlvo(BaseModel):
    model_config = ConfigDict(extra="forbid")
    trecho_alvo: str = Field(min_length=1, max_length=300)
    mudanca: str = Field(min_length=1, max_length=280)
    efeito: str = Field(min_length=1, max_length=280)
    verificacao: str = Field(min_length=1, max_length=280)
    classificacao: Literal["REVISAR", "SEM_IMPACTO_IDENTIFICADO", "INDETERMINADO"]
    fontes_mudanca_ids: list[str] = Field(min_length=2, max_length=2)
    fontes_ids: list[str] = Field(min_length=1)


class EscopoAlvo(BaseModel):
    model_config = ConfigDict(extra="forbid")
    trecho_alvo: str = Field(min_length=1, max_length=300)
    justificativa: str = Field(min_length=1, max_length=280)
    vinculo: Literal["DIRETO", "INDEPENDENTE", "INCERTO"]
    fontes_mudanca_ids: list[str] = Field(min_length=2, max_length=2)
    fontes_ids: list[str] = Field(min_length=1)


def textos_citaveis(fonte):
    try:
        dados = json.loads(fonte["texto"])
    except (ValueError, TypeError):
        dados = None
    if isinstance(dados, dict):
        return [t for t in [dados.get("conteudo"), *(dados.get("criteriosAceite") or [])] if isinstance(t, str) and t]
    # O cabeçalho do trecho MCP identifica a fonte; não descreve sua obrigação.
    # Não permitir que apenas o título seja usado como prova de independência.
    texto = fonte["texto"]
    if fonte.get("origem") == "MCP_PUBLICADO":
        texto = re.sub(r"^REQ-\d+[^\n]*\n", "", texto, count=1)
    return [texto] if texto.strip() else []


def esquema_saida(schema, fontes, alvos=None):
    """Restringe a gramática a identidades reais, sem decidir quais alvos sugerir."""
    formato = schema.model_json_schema()
    ids = [f["id"] for f in fontes]
    referencias = formato["properties"]["fontes_ids"]
    if ids:
        referencias["items"]["enum"] = ids
        referencias["minItems"] = 1
    else:
        referencias["maxItems"] = 0
    if schema in (AnaliseAlvo, EscopoAlvo):
        referencias["items"]["enum"] = [f["id"] for f in fontes if f["codigo"] in (alvos or set())]
        # O modelo escolhe uma frase real. A gramática impede citações inventadas;
        # não decide a classificação nem se a conclusão é semanticamente correta.
        trechos = [frase.strip()[:300] for f in fontes if f["codigo"] in (alvos or set())
                   for texto in textos_citaveis(f) for frase in re.split(r"(?<=[.!?])\s+|\n", texto) if frase.strip()]
        formato["properties"]["trecho_alvo"]["enum"] = list(dict.fromkeys(trechos))
        mudanca = formato["properties"]["fontes_mudanca_ids"]
        mudanca["items"]["enum"] = [f["id"] for f in fontes if f["origem"] in {"API_PUBLICADA", "API_CANDIDATA"}]
        mudanca["uniqueItems"] = True
    if schema is Impacto:
        alternativas = []
        for alvo in sorted(alvos or []):
            candidato = copy.deepcopy(formato["$defs"]["Sugestao"])
            candidato["properties"]["requisito"]["const"] = alvo
            candidato["properties"]["fontes_ids"]["items"]["enum"] = [f["id"] for f in fontes if f["codigo"] == alvo]
            alternativas.append(candidato)
        if alternativas:
            formato["$defs"]["Sugestao"] = {"oneOf": alternativas}
        else:
            formato["properties"]["sugestoes"]["maxItems"] = 0
    return formato


def validar_saida(texto, schema, fontes, alvos=None, validar_codigos=True):
    dados = schema.model_validate_json(texto).model_dump()
    if fontes and not dados["fontes_ids"]:
        raise ValueError("Resposta com contexto precisa citar pelo menos uma fonte recuperada.")
    ids = {f["id"] for f in fontes}
    codigos = {f["codigo"] for f in fontes}
    refs = list(dados["fontes_ids"])
    if schema in (AnaliseAlvo, EscopoAlvo):
        alvo_ids = {f["id"] for f in fontes if f["codigo"] in (alvos or set())}
        mudanca_ids = {f["id"] for f in fontes if f["origem"] in {"API_PUBLICADA", "API_CANDIDATA"}}
        if not set(refs) <= alvo_ids or set(dados["fontes_mudanca_ids"]) != mudanca_ids:
            raise ValueError("A análise precisa citar o alvo e as duas versões da mudança separadamente.")
        def normalizar(texto):
            return " ".join(texto.split())
        if not any(normalizar(dados["trecho_alvo"]) in normalizar(t) for f in fontes if f["id"] in refs for t in textos_citaveis(f)):
            raise ValueError("O trecho do alvo precisa ser uma transcrição literal de sua evidência.")
        refs.extend(dados["fontes_mudanca_ids"])
    for sugestao in dados.get("sugestoes", []):
        if sugestao["requisito"] not in (alvos or set()):
            raise ValueError("Sugestão cita alvo inexistente ou o próprio requisito analisado.")
        refs.extend(sugestao["fontes_ids"])
        citadas = [f for f in fontes if f["id"] in sugestao["fontes_ids"]]
        if not any(f["codigo"] == sugestao["requisito"] for f in citadas):
            raise ValueError("Sugestão sem evidência do requisito afetado.")
    if not set(refs) <= ids:
        raise ValueError("Referência não recuperada.")
    if schema is Impacto:
        mencionados = set(re.findall(r"\bREQ-\d+\b", dados["resposta"])) & (alvos or set())
        sugeridos = {s["requisito"] for s in dados["sugestoes"]}
        if not mencionados <= sugeridos:
            raise ValueError("Resumo de impacto sem sugestões estruturadas correspondentes.")
    if validar_codigos:
        citados = set(re.findall(r"\bREQ-\d+\b", json.dumps(dados, ensure_ascii=False)))
        if not citados <= codigos:
            raise ValueError("Código de requisito não recuperado.")
    return dados


class Gerador:
    def __init__(self, config, factory=None, sleep=asyncio.sleep):
        self.config = config
        self.factory = factory
        self.sleep = sleep

    def modelo(self, schema, fontes, alvos):
        if self.factory:
            return self.factory(schema)
        return ChatOllama(model=self.config.modelo, base_url=self.config.llm_url,
                          temperature=0, seed=42, num_ctx=self.config.contexto,
                          num_predict=self.config.max_tokens, keep_alive="10m",
                          format=esquema_saida(schema, fontes, alvos),
                          client_kwargs={"timeout": self.config.timeout, "headers": self.config.headers_llm()})

    async def gerar(self, prompt, schema, fontes, medicao, alvos=None, validar_codigos=True, etapa=None):
        inicio = perf_counter()
        llm_anterior = medicao.llm_ms
        modelo = self.modelo(schema, fontes, alvos)
        mensagens = prompt.to_messages() if hasattr(prompt, "to_messages") else prompt
        for numero in range(1, self.config.tentativas + 1):
            tentativa_inicio = perf_counter()
            mensagem = None
            registro = {"numero": numero}
            if etapa:
                registro["etapa"] = etapa
            try:
                # Limita a duração total, incluindo todos os chunks, não só o tempo entre bytes.
                mensagem = await asyncio.wait_for(modelo.ainvoke(mensagens), timeout=self.config.timeout)
                uso = mensagem.usage_metadata or {}
                meta = mensagem.response_metadata or {}
                entrada = uso.get("input_tokens", meta.get("prompt_eval_count"))
                saida = uso.get("output_tokens", meta.get("eval_count"))
                if entrada is None or saida is None:
                    medicao.consumo_completo = False
                medicao.tokens_entrada += entrada or 0
                medicao.tokens_saida += saida or 0
                registro.update(tokens_entrada=entrada, tokens_saida=saida,
                                 carga_modelo_ms=round(meta.get("load_duration", 0) / 1_000_000, 2))
                dados = validar_saida(mensagem.content, schema, fontes, alvos, validar_codigos)
                registro["estado"] = "ok"
                return dados, "ok"
            except (TimeoutError, httpx.HTTPError, ConnectionError, ResponseError, ValueError) as exc:
                if mensagem is None:
                    # Uma tentativa interrompida pode ter consumido tokens; não inventar consumo zero.
                    medicao.consumo_completo = False
                registro.update(estado="falha", erro=type(exc).__name__)
                if mensagem is not None and isinstance(exc, ValueError):
                    registro["saida_rejeitada"] = mensagem.content
                    registro["motivo_validacao"] = str(exc) if type(exc) is ValueError else "JSON fora do esquema esperado."
                    mensagens = [*mensagens, HumanMessage(content=(
                        "A resposta anterior falhou na validação. Retorne JSON no esquema pedido. "
                        + ("Transcreva trecho_alvo literalmente do alvo; fontes_mudanca_ids deve citar as duas versões distintas. " if schema in (AnaliseAlvo, EscopoAlvo) else "") +
                        "fontes_ids deve conter apenas IDs E1, E2 etc. existentes nas evidências. "
                        "Cada sugestão precisa citar uma evidência do próprio requisito afetado. "
                        "Cada requisito citado como afetado no resumo precisa de um item em sugestoes."))]
                evento("llm_falhou", operacao_id=medicao.operacao_id, tentativa=numero, erro=type(exc).__name__)
                status = getattr(exc, "status_code", None)
                if isinstance(exc, httpx.HTTPStatusError):
                    status = exc.response.status_code
                permanente = status is not None and 400 <= status < 500 and status not in (408, 429)
                if numero < self.config.tentativas and not permanente:
                    atraso = self.config.backoff * 2 ** (numero - 1)
                    registro["backoff_s"] = atraso
                    evento("llm_retry", operacao_id=medicao.operacao_id, proxima_tentativa=numero + 1, espera_s=atraso)
                    await self.sleep(atraso)
                else:
                    break
            finally:
                registro["duracao_ms"] = round((perf_counter() - tentativa_inicio) * 1000, 2)
                medicao.tentativas.append(registro)
                medicao.llm_ms = llm_anterior + (perf_counter() - inicio) * 1000
        evento("llm_fallback", operacao_id=medicao.operacao_id, quantidade_fontes=len(fontes))
        dados = {"resposta": "Geração indisponível ou resposta inválida. Consulte as evidências recuperadas para continuar manualmente.",
                 "justificativa": "Nenhuma conclusão automática foi produzida; referências abaixo são dados recuperados, não uma decisão.",
                 "fontes_ids": [f["id"] for f in fontes]}
        if schema is Impacto:
            dados["sugestoes"] = []
        if schema is AnaliseAlvo:
            dados = {"classificacao": "INDETERMINADO", "mudanca": "Consultar o diff das versões.",
                     "trecho_alvo": "",
                     "efeito": "Não foi possível gerar uma análise válida para este alvo.",
                     "verificacao": "Revisar manualmente as evidências; não interpretar falha como ausência de impacto.",
                     "fontes_ids": [f["id"] for f in fontes if f["codigo"] in (alvos or set())],
                     "fontes_mudanca_ids": [f["id"] for f in fontes if f["origem"] in {"API_PUBLICADA", "API_CANDIDATA"}]}
        if schema is EscopoAlvo:
            dados = {"trecho_alvo": "", "vinculo": "INCERTO", "justificativa": "Triagem indisponível; conferir manualmente o vínculo com a mudança.",
                     "fontes_ids": [f["id"] for f in fontes if f["codigo"] in (alvos or set())],
                     "fontes_mudanca_ids": [f["id"] for f in fontes if f["origem"] in {"API_PUBLICADA", "API_CANDIDATA"}]}
        return dados, "degradado"
