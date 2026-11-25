import os
from dataclasses import dataclass, field
from urllib.parse import urlparse
from uuid import UUID


@dataclass(frozen=True)
class Config:
    athena_url: str = "http://localhost:8080"
    projeto_id: str = "22222222-2222-4222-8222-222222222222"
    email: str = "leitor@athena.dev"
    senha: str = field(default="Athena@2026", repr=False)
    llm_url: str = "http://localhost:11434"
    modelo: str = "qwen2.5:3b-instruct-q4_K_M"
    modelo_digest: str = "357c53fb659c5076de1d65ccb0b397446227b71a42be9d1603d46168015c9e4b"
    llm_chave: str = field(default="", repr=False)
    timeout: float = 20.0
    tentativas: int = 2
    backoff: float = 2.0
    api_timeout: float = 15.0
    limite: int = 5
    max_tokens: int = 512
    contexto: int = 4096
    preco_entrada: float = 0.4
    preco_saida: float = 1.6
    modelo_referencia: str = "gpt-4.1-mini (referência histórica; não invocado)"

    def __post_init__(self):
        UUID(self.projeto_id)
        for value in (self.athena_url, self.llm_url):
            parsed = urlparse(value)
            if parsed.scheme not in {"http", "https"} or not parsed.hostname or parsed.username:
                raise ValueError("URLs devem ser HTTP(S), sem credenciais embutidas.")
        if not (0 < self.timeout <= 600 and 0 < self.api_timeout <= 120):
            raise ValueError("Timeout fora do intervalo permitido.")
        if not (1 <= self.tentativas <= 5 and 0 <= self.backoff <= 30):
            raise ValueError("Use 1 a 5 tentativas e backoff de 0 a 30 segundos.")
        if not (1 <= self.limite <= 20 and 64 <= self.max_tokens <= 2048 and self.contexto >= 2048):
            raise ValueError("Limites de recuperação ou geração inválidos.")
        if min(self.preco_entrada, self.preco_saida) < 0:
            raise ValueError("Preços devem ser não negativos.")

    @classmethod
    def from_env(cls):
        mapping = {
            "athena_url": ("ATHENA_URL", str), "projeto_id": ("ATHENA_PROJETO_ID", str),
            "email": ("ATHENA_EMAIL", str), "senha": ("ATHENA_SENHA", str),
            "llm_url": ("LLM_URL", str), "modelo": ("LLM_MODELO", str),
            "modelo_digest": ("LLM_MODELO_DIGEST", str), "llm_chave": ("LLM_CHAVE", str),
            "timeout": ("LLM_TIMEOUT", float), "tentativas": ("LLM_TENTATIVAS", int),
            "backoff": ("LLM_BACKOFF", float), "api_timeout": ("ATHENA_TIMEOUT", float),
            "limite": ("RAG_LIMITE", int), "max_tokens": ("LLM_MAX_TOKENS", int),
            "contexto": ("LLM_CONTEXTO", int),
            "preco_entrada": ("PRECO_ENTRADA_USD_MILHAO", float),
            "preco_saida": ("PRECO_SAIDA_USD_MILHAO", float),
            "modelo_referencia": ("MODELO_REFERENCIA_CUSTO", str),
        }
        return cls(**{key: convert(os.environ[env]) for key, (env, convert) in mapping.items() if env in os.environ})

    def headers_llm(self):
        return {"Authorization": f"Bearer {self.llm_chave}"} if self.llm_chave else {}
