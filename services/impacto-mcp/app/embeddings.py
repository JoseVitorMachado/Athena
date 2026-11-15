"""Geração de embeddings.

O livro (§6.11) prevê o modelo multilingual-e5-small executado localmente. Nesta
etapa o serviço usa um embedding por hashing de termos, com a MESMA dimensão (384)
e vetores normalizados: a coluna, o índice HNSW e a busca por cosseno já são os
definitivos. A troca pelo modelo real é localizada nesta função e exige reindexar
os trechos (o nome do modelo fica gravado em cada um).
"""

import hashlib
import math
import re
import unicodedata

DIMENSAO = 384
MODELO = "hashing-pt-384-v1"

_PALAVRA = re.compile(r"[a-z0-9]+")
_STOPWORDS = {
    "a", "o", "as", "os", "de", "da", "do", "das", "dos", "e", "em", "um", "uma",
    "para", "por", "com", "que", "se", "no", "na", "nos", "nas", "ao", "aos", "ou",
    "deve", "devem", "quais", "qual",
}


def _termos(texto: str) -> list[str]:
    sem_acento = unicodedata.normalize("NFKD", texto.lower()).encode("ascii", "ignore").decode()
    palavras = [p for p in _PALAVRA.findall(sem_acento) if p not in _STOPWORDS and len(p) > 1]
    # Radical simples: aproxima "requisito", "requisitos" e "requisitar".
    radicais = [p[:6] for p in palavras]
    bigramas = [f"{a}_{b}" for a, b in zip(radicais, radicais[1:])]
    return radicais + bigramas


def gerar(texto: str) -> list[float]:
    vetor = [0.0] * DIMENSAO
    for termo in _termos(texto):
        digest = hashlib.blake2b(termo.encode(), digest_size=8).digest()
        posicao = int.from_bytes(digest[:4], "little") % DIMENSAO
        sinal = 1.0 if digest[4] & 1 else -1.0
        vetor[posicao] += sinal
    norma = math.sqrt(sum(v * v for v in vetor)) or 1.0
    return [v / norma for v in vetor]


def literal(vetor: list[float]) -> str:
    """Formato de texto aceito pelo tipo vector do pgvector."""
    return "[" + ",".join(f"{v:.6f}" for v in vetor) + "]"
