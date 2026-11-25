import json
import sys
from dataclasses import asdict, dataclass, field
from datetime import datetime, timezone
from time import perf_counter
from uuid import uuid4


def evento(nome, **campos):
    print(json.dumps({"evento": nome, **campos}, ensure_ascii=False), file=sys.stderr, flush=True)


@dataclass
class Medicao:
    operacao_id: str = field(default_factory=lambda: str(uuid4()))
    iniciado_em: str = field(default_factory=lambda: datetime.now(timezone.utc).isoformat())
    inicio: float = field(default_factory=perf_counter, repr=False)
    api_ms: float = 0
    mcp_ms: float = 0
    llm_ms: float = 0
    total_ms: float = 0
    tokens_entrada: int = 0
    tokens_saida: int = 0
    consumo_completo: bool = True
    tentativas: list = field(default_factory=list)

    def finalizar(self, config):
        self.total_ms = round((perf_counter() - self.inicio) * 1000, 2)
        custo = (self.tokens_entrada * config.preco_entrada + self.tokens_saida * config.preco_saida) / 1_000_000
        dados = asdict(self)
        dados.pop("inicio")
        for key in ("api_ms", "mcp_ms", "llm_ms"):
            dados[key] = round(dados[key], 2)
        dados.update(custo_referencia_usd=round(custo, 8) if self.consumo_completo else None,
                     custo_tokens_observados_usd=round(custo, 8),
                     referencia_custo={"modelo": config.modelo_referencia, "entrada_usd_milhao": config.preco_entrada,
                                       "saida_usd_milhao": config.preco_saida,
                                       "nota": "Simulação com tokens do Ollama; tokenizadores diferem. Não inclui energia/hardware."})
        return dados
