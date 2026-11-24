import asyncio
import os
import unittest
from unittest.mock import patch

for nome in ("DB_HOST", "DB_NAME", "DB_USER", "DB_PASSWORD", "RABBITMQ_HOST",
             "RABBITMQ_USER", "RABBITMQ_PASSWORD"):
    os.environ.setdefault(nome, "teste")

from prometheus_client import CONTENT_TYPE_LATEST, REGISTRY

from app.main import app


async def requisicao(caminho, metodo="GET"):
    mensagens = []
    recebido = False
    espera = asyncio.Event()

    async def receber():
        nonlocal recebido
        if not recebido:
            recebido = True
            return {"type": "http.request", "body": b"", "more_body": False}
        await espera.wait()
        return {"type": "http.disconnect"}

    async def enviar(mensagem):
        mensagens.append(mensagem)

    escopo = {"type": "http", "asgi": {"version": "3.0"}, "http_version": "1.1",
              "method": metodo, "scheme": "http", "path": caminho,
              "raw_path": caminho.encode(), "query_string": b"", "headers": [],
              "server": ("teste", 80), "client": ("teste", 1234), "root_path": ""}
    await app(escopo, receber, enviar)
    inicio = next(m for m in mensagens if m["type"] == "http.response.start")
    corpo = b"".join(m.get("body", b"") for m in mensagens if m["type"] == "http.response.body")
    return inicio, corpo


class TesteMetricas(unittest.TestCase):
    def test_rota_usa_template_inclusive_em_resposta_do_middleware(self):
        caminho = "/api/v1/projetos/11111111-1111-4111-8111-111111111111/indice"
        labels = {"application": "impacto-mcp", "method": "GET",
                  "uri": "/api/v1/projetos/{projeto_id}/indice", "status": "404"}
        antes = REGISTRY.get_sample_value("http_server_requests_seconds_count", labels) or 0
        with patch("app.main._estado_projeto", return_value=(False, True)):
            inicio, _ = asyncio.run(requisicao(caminho))
        self.assertEqual(inicio["status"], 404)
        self.assertEqual(REGISTRY.get_sample_value("http_server_requests_seconds_count", labels), antes + 1)
        self.assertIsNone(REGISTRY.get_sample_value("http_server_requests_seconds_count", {**labels, "uri": caminho}))

    def test_exposicao_nao_registra_proprio_endpoint(self):
        inicio, corpo = asyncio.run(requisicao("/metrics"))
        self.assertEqual(inicio["status"], 200)
        self.assertEqual(dict(inicio["headers"])[b"content-type"].decode(), CONTENT_TYPE_LATEST)
        self.assertIn(b'athena_indexacao_pendentes{application="impacto-mcp"}', corpo)
        self.assertIn(b'athena_embeddings_segundos_count{application="impacto-mcp"}', corpo)
        self.assertNotIn(b'uri="/metrics"', corpo)

    def test_caminhos_desconhecidos_nao_criam_labels_distintos(self):
        inicio, _ = asyncio.run(requisicao("/caminho-desconhecido/123"))
        self.assertEqual(inicio["status"], 404)
        labels = {"application": "impacto-mcp", "method": "GET", "uri": "NAO_ENCONTRADA", "status": "404"}
        self.assertGreaterEqual(REGISTRY.get_sample_value("http_server_requests_seconds_count", labels), 1)
