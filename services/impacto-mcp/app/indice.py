"""Índice de contexto e busca combinada (textual + vetorial)."""

import uuid

from psycopg.types.json import Jsonb

from . import embeddings, projetos
from .banco import pool

PROJETO_DEMO = uuid.UUID("22222222-2222-4222-8222-222222222222")
PUBLICACOES_DEMO = [
    ("11111111-1111-4111-8111-111111111111", "33333333-3333-4333-8333-333333333331", "REQ-1",
     "Exportar documento de requisitos", "O sistema deve permitir exportar o documento de requisitos.",
     ["Exportação preserva requisitos."]),
    ("11111111-1111-4111-8111-111111111113", "33333333-3333-4333-8333-333333333333", "REQ-3",
     "Consultas comuns respondem em até 1 segundo",
     "O p95 das consultas comuns deve ficar em até 1 segundo no cenário de referência.",
     ["Medição registrada com 20 usuários simultâneos."]),
]


def carregar_demo():
    with pool.connection() as conexao:
        conexao.execute("SELECT pg_advisory_xact_lock(727273)")
        if conexao.execute("SELECT EXISTS(SELECT 1 FROM trechos) OR EXISTS(SELECT 1 FROM trabalhos_indexacao)").fetchone()[0]:
            return
        projetos.bloquear_acesso(conexao, PROJETO_DEMO)
        if any(projetos.estado(conexao, PROJETO_DEMO)):
            return
        for origem, versao, codigo, titulo, conteudo, criterios in PUBLICACOES_DEMO:
            payload = {"numero": 1, "codigo": codigo, "titulo": titulo, "conteudo": conteudo,
                       "criteriosAceite": criterios}
            conexao.execute("""INSERT INTO trabalhos_indexacao (id, projeto_id, origem_id, versao_id, payload)
                              VALUES (%s, %s, %s, %s, %s)""",
                            (uuid.uuid4(), PROJETO_DEMO, origem, versao, Jsonb(payload)))


def buscar(projeto_id, pergunta: str, limite: int) -> list[dict]:
    with pool.connection() as conexao:
        if any(projetos.estado(conexao, projeto_id)):
            return []
    vetor = embeddings.literal(embeddings.gerar_consulta(pergunta))
    with pool.connection() as conexao:
        linhas = conexao.execute(
            """WITH candidatos AS (
                   SELECT origem_id, versao_id, codigo, titulo, texto,
                          1 - (embedding <=> %(vetor)s::vector) AS similaridade,
                          ts_rank(to_tsvector('portuguese', titulo || ' ' || texto),
                                  plainto_tsquery('portuguese', %(q)s)) AS relevancia_texto
                     FROM trechos
                    WHERE projeto_id = %(projeto)s AND ativo AND modelo = %(modelo)s
                      AND NOT EXISTS (SELECT 1 FROM projetos_bloqueados WHERE projeto_id = %(projeto)s)
                      AND NOT EXISTS (SELECT 1 FROM projetos_excluidos WHERE projeto_id = %(projeto)s)
                    ORDER BY embedding <=> %(vetor)s::vector LIMIT 50)
               SELECT origem_id, versao_id, codigo, titulo, texto, similaridade
                 FROM candidatos
                ORDER BY 0.7 * similaridade + 0.3 * least(relevancia_texto * 10, 1) DESC
                LIMIT %(limite)s""",
            {"vetor": vetor, "q": pergunta, "projeto": projeto_id, "limite": limite, "modelo": embeddings.MODELO},
        ).fetchall()
    return [{"texto": texto, "similaridade": round(max(similaridade, 0.0), 4),
             # O schema Evidencia não aceita campos extras: o código vai junto do título.
             "referencia": {"origemId": str(origem), "versaoId": str(versao),
                            "trecho": f"{codigo} · {titulo}" if codigo else titulo,
                            "referencia": f"/api/v1/gestao/projetos/{projeto_id}/requisitos/{origem}"}}
            for origem, versao, codigo, titulo, texto, similaridade in linhas]


def situacao(projeto_id) -> dict:
    with pool.connection() as conexao:
        quantidade, ultima = conexao.execute(
            "SELECT count(*), max(indexado_em) FROM trechos WHERE projeto_id = %s", (projeto_id,)).fetchone()
        total, pendentes, atraso = conexao.execute("""SELECT count(*), count(*) FILTER (WHERE estado = 'PENDENTE'),
            coalesce(extract(epoch from now() - min(criado_em) FILTER (WHERE estado = 'PENDENTE')), 0)::integer
            FROM trabalhos_indexacao WHERE projeto_id = %s""", (projeto_id,)).fetchone()
    return {"ultimaAtualizacao": ultima.isoformat().replace("+00:00", "Z") if ultima else None,
            "situacao": "ATUALIZACAO_PENDENTE" if pendentes else ("ATUALIZADO" if quantidade or total else "VAZIO"),
            "atraso": max(0, atraso), "modeloEmbeddings": embeddings.MODELO}
