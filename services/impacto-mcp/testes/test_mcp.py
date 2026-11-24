import asyncio
import os
import unittest

for nome in ("DB_HOST", "DB_NAME", "DB_USER", "DB_PASSWORD", "RABBITMQ_HOST",
             "RABBITMQ_USER", "RABBITMQ_PASSWORD"):
    os.environ.setdefault(nome, "teste")

import httpx
from pydantic import ValidationError
from app.mcp import Decidir, ErroRpc, Preparacao, Publicar, autorizar, hash_acao

A = "11111111-1111-4111-8111-111111111111"
B = "22222222-2222-4222-8222-222222222222"


class TesteFerramentas(unittest.TestCase):
    def test_hashes(self):
        self.assertEqual(hash_acao("DECIDIR", A, B, "APROVAR"),
                         "3f1838af14728f300220ecd8abd49a362cdc6f58ac6f6bf464a9e2a11ac0d10c")
        self.assertEqual(hash_acao("PUBLICAR", A, B),
                         "195f688d88043a747e4c2873a96b1176b51d6084b79aaef14ab8d5ca5f294d7a")

    def test_argumentos(self):
        Preparacao.model_validate({"projetoId": A, "acao": "DECIDIR", "loteId": A, "versaoId": B, "decisao": "APROVAR"})
        Publicar.model_validate({"projetoId": A, "requisitoId": B, "comprovanteHumano": "token"})
        Decidir.model_validate({"projetoId": A, "loteId": B, "decisao": "REJEITAR", "justificativa": "Motivo", "comprovanteHumano": "token"})
        for modelo, argumentos in (
            (Preparacao, {"projetoId": A, "acao": "DECIDIR", "versaoId": B}),
            (Preparacao, {"projetoId": A, "acao": "PUBLICAR", "requisitoId": B, "versaoId": B, "decisao": "APROVAR"}),
            (Decidir, {"projetoId": A, "loteId": B, "decisao": "REJEITAR", "comprovanteHumano": "token"}),
            (Publicar, {"projetoId": A, "requisitoId": B, "comprovanteHumano": "", "extra": 1}),
        ):
            with self.subTest(modelo=modelo), self.assertRaises(ValidationError):
                modelo.model_validate(argumentos)

    def test_autorizacao(self):
        async def verificar(status, corpo):
            async def responder(request):
                return httpx.Response(status, json=corpo)
            async with httpx.AsyncClient(transport=httpx.MockTransport(responder)) as cliente:
                return await autorizar(cliente, A, B)
        self.assertEqual(asyncio.run(verificar(200, {"papel": "EDITOR"})), "EDITOR")
        for status, corpo, codigo in ((404, {}, -32003), (503, {}, -32004), (200, {"papel": "OUTRO"}, -32004)):
            with self.subTest(status=status), self.assertRaises(ErroRpc) as erro:
                asyncio.run(verificar(status, corpo))
            self.assertEqual(erro.exception.codigo, codigo)

    def test_indisponibilidade(self):
        async def verificar():
            async def responder(request):
                raise httpx.ConnectError("indisponível", request=request)
            async with httpx.AsyncClient(transport=httpx.MockTransport(responder)) as cliente:
                await autorizar(cliente, A, B)
        with self.assertRaises(ErroRpc) as erro:
            asyncio.run(verificar())
        self.assertEqual(erro.exception.codigo, -32004)
