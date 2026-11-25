import json
import unittest

import httpx

from fluxo_requisito import Fluxo, PROJETO_DEMO

REQ = "5d59d1b9-5346-4c0d-9a18-8a01c998afb4"
VID = "62a088ba-8fa8-45b9-a3a6-a083d168727e"
LOTE = "11111111-1111-4111-8111-111111111111"
SUB = "22222222-2222-4222-8222-222222222223"


class TestFluxo(unittest.TestCase):
    def ambiente(self, estado="RASCUNHO", bloqueante=False, hash_lote="conteudo", etag=True):
        self.pedidos = []
        meta = {"requisitoId": REQ, "codigo": "REQ-12", "situacao": "ATIVO", "versaoId": VID,
                "estadoVersao": estado, "loteId": LOTE}
        versao = {"versaoId": VID, "estadoVersao": estado, "hashConteudo": "conteudo", "titulo": "Limite"}
        lote = {"loteId": LOTE, "requisitoId": REQ, "submissaoId": SUB, "versaoId": VID,
                "hashConteudo": hash_lote, "estado": "ATIVO", "pendencias": [{"bloqueante": bloqueante, "resolvida": False}],
                "decisao": {"decisao": "APROVAR"} if estado == "APROVADA" else None}

        def responder(r):
            self.pedidos.append(r)
            if r.method == "POST":
                return httpx.Response(201, json={"registrado": True})
            if "/versoes/" in r.url.path:
                return httpx.Response(200, json=versao, headers={"ETag": '"3"'} if etag else {})
            if "/revisoes/" in r.url.path:
                return httpx.Response(200, json=lote, headers={"ETag": '"7"'})
            return httpx.Response(200, json=meta)

        http = httpx.Client(base_url="http://gateway:8080", transport=httpx.MockTransport(responder))
        self.addCleanup(http.close)
        return Fluxo(http, PROJETO_DEMO, REQ)

    def escritas(self):
        return [r for r in self.pedidos if r.method == "POST"]

    def test_estado_nao_escreve(self):
        resultado = self.ambiente().executar("estado")
        self.assertEqual(resultado["estado"], "RASCUNHO")
        self.assertFalse(self.escritas())

    def test_submeter_nao_aprova_nem_publica(self):
        self.ambiente().executar("submeter")
        escritas = self.escritas()
        self.assertEqual(len(escritas), 1)
        self.assertTrue(escritas[0].url.path.endswith("/submissoes"))
        self.assertEqual(escritas[0].headers["If-Match"], '"3"')
        self.assertEqual(json.loads(escritas[0].content), {"versaoId": VID})

    def test_aprovar_so_registra_decisao_da_versao_lida(self):
        self.ambiente("AGUARDANDO_REVISAO").executar("aprovar", "Texto revisado por mim.")
        self.assertEqual(len(self.escritas()), 1)
        r = self.escritas()[0]
        self.assertTrue(r.url.path.endswith("/decisoes"))
        self.assertEqual(r.headers["If-Match"], '"7"')
        corpo = json.loads(r.content)
        self.assertEqual(corpo["hashConteudo"], "conteudo")
        self.assertEqual(corpo["submissaoId"], SUB)
        self.assertEqual(corpo["decisao"], "APROVAR")

    def test_publicar_exige_aprovacao(self):
        f = self.ambiente()
        with self.assertRaises(ValueError):
            f.executar("publicar")
        self.assertFalse(self.escritas())

    def test_publicar_preserva_vinculo_da_submissao(self):
        self.ambiente("APROVADA").executar("publicar")
        r = self.escritas()[0]
        self.assertTrue(r.url.path.endswith("/publicacoes"))
        self.assertEqual(json.loads(r.content), {"versaoId": VID, "submissaoId": SUB})

    def test_pendencia_bloqueante_impede_aprovacao(self):
        f = self.ambiente("AGUARDANDO_REVISAO", bloqueante=True)
        with self.assertRaisesRegex(ValueError, "bloqueante"):
            f.executar("aprovar", "Conferido")
        self.assertFalse(self.escritas())

    def test_lote_de_outro_conteudo_nao_e_aprovado(self):
        f = self.ambiente("AGUARDANDO_REVISAO", hash_lote="outro")
        with self.assertRaisesRegex(ValueError, "não corresponde"):
            f.executar("aprovar", "Conferido")
        self.assertFalse(self.escritas())

    def test_sem_justificativa_nao_aprova(self):
        f = self.ambiente("AGUARDANDO_REVISAO")
        with self.assertRaisesRegex(ValueError, "justificativa"):
            f.executar("aprovar")
        self.assertFalse(self.escritas())

    def test_sem_etag_nao_submete(self):
        f = self.ambiente(etag=False)
        with self.assertRaisesRegex(ValueError, "ETag"):
            f.executar("submeter")
        self.assertFalse(self.escritas())

    def test_mesma_tentativa_tem_chave_estavel(self):
        f = self.ambiente()
        f.executar("submeter")
        f.executar("submeter")
        self.assertEqual(self.escritas()[0].headers["Idempotency-Key"], self.escritas()[1].headers["Idempotency-Key"])


if __name__ == "__main__":
    unittest.main()
