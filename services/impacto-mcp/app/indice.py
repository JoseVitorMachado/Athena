"""Índice de contexto: ingestão de trechos e busca combinada (textual + vetorial)."""

import uuid

from . import embeddings
from .banco import pool

# Conteúdo publicado nos dados de demonstração do serviço Requisitos. Na Parte 4
# a ingestão passa a ser feita pelos eventos de Requisitos via RabbitMQ.
PROJETO_DEMO = uuid.UUID("22222222-2222-4222-8222-222222222222")
TRECHOS_DEMO = [
    ("11111111-1111-4111-8111-111111111111", "33333333-3333-4333-8333-333333333331",
     "Exportar documento de requisitos",
     "O sistema deve permitir exportar o documento de requisitos. "
     "Critério: a exportação preserva requisitos."),
    ("11111111-1111-4111-8111-111111111112", "33333333-3333-4333-8333-333333333332",
     "Registrar relação entre requisitos",
     "O editor deve conseguir relacionar dois requisitos do mesmo projeto. "
     "Critérios: a relação fica visível no mapa; autorrelação é recusada."),
    ("11111111-1111-4111-8111-111111111113", "33333333-3333-4333-8333-333333333333",
     "Consultas comuns respondem em até 1 segundo",
     "O p95 das consultas comuns deve ficar em até 1 segundo no cenário de referência. "
     "Critério: medição registrada com 20 usuários simultâneos."),
]


def indexar(projeto_id, origem_id, versao_id, titulo: str, texto: str) -> None:
    """Grava (ou substitui) o trecho de uma versão; idempotente por origem e versão."""
    vetor = embeddings.literal(embeddings.gerar(f"{titulo}. {texto}"))
    with pool.connection() as conexao:
        conexao.execute(
            """INSERT INTO trechos (id, projeto_id, origem_id, versao_id, titulo, texto, modelo, embedding)
               VALUES (%s, %s, %s, %s, %s, %s, %s, %s::vector)
               ON CONFLICT (origem_id, versao_id) DO UPDATE
                  SET titulo = EXCLUDED.titulo, texto = EXCLUDED.texto, modelo = EXCLUDED.modelo,
                      embedding = EXCLUDED.embedding, indexado_em = now()""",
            (uuid.uuid4(), projeto_id, origem_id, versao_id, titulo, texto, embeddings.MODELO, vetor),
        )


def carregar_demo() -> None:
    with pool.connection() as conexao:
        existentes = conexao.execute("SELECT count(*) FROM trechos").fetchone()[0]
    if existentes == 0:
        for origem, versao, titulo, texto in TRECHOS_DEMO:
            indexar(PROJETO_DEMO, origem, versao, titulo, texto)


def buscar(projeto_id, pergunta: str, limite: int) -> list[dict]:
    """Combina similaridade de cosseno (pgvector) com relevância textual (tsvector).

    Retorna no máximo `limite` trechos do projeto, sem duplicatas, do mais ao menos
    relevante. A similaridade não é apresentada como probabilidade de acerto.
    """
    vetor = embeddings.literal(embeddings.gerar(pergunta))
    with pool.connection() as conexao:
        linhas = conexao.execute(
            """WITH candidatos AS (
                   SELECT origem_id, versao_id, titulo, texto,
                          1 - (embedding <=> %(vetor)s::vector) AS similaridade,
                          ts_rank(to_tsvector('portuguese', titulo || ' ' || texto),
                                  plainto_tsquery('portuguese', %(q)s)) AS relevancia_texto
                     FROM trechos
                    WHERE projeto_id = %(projeto)s
                    ORDER BY embedding <=> %(vetor)s::vector
                    LIMIT 50)
               SELECT origem_id, versao_id, titulo, texto, similaridade
                 FROM candidatos
                ORDER BY 0.7 * similaridade + 0.3 * least(relevancia_texto * 10, 1) DESC
                LIMIT %(limite)s""",
            {"vetor": vetor, "q": pergunta, "projeto": projeto_id, "limite": limite},
        ).fetchall()
    return [
        {
            "texto": texto,
            "similaridade": round(max(similaridade, 0.0), 4),
            "referencia": {
                "origemId": str(origem),
                "versaoId": str(versao),
                "trecho": titulo,
                "referencia": f"/api/v1/gestao/projetos/{projeto_id}/requisitos/{origem}",
            },
        }
        for origem, versao, titulo, texto, similaridade in linhas
    ]


def situacao(projeto_id) -> dict:
    with pool.connection() as conexao:
        quantidade, ultima = conexao.execute(
            "SELECT count(*), max(indexado_em) FROM trechos WHERE projeto_id = %s", (projeto_id,)
        ).fetchone()
    return {
        "ultimaAtualizacao": ultima.isoformat().replace("+00:00", "Z") if ultima else None,
        # Sem consumidor de eventos ainda, não há fila pendente a medir.
        "situacao": "ATUALIZADO" if quantidade else "VAZIO",
        "atraso": 0,
        "modeloEmbeddings": embeddings.MODELO,
    }
