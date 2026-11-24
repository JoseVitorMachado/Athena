import unittest

from app.eventos import revisao_pode_aplicar, validar_envelope, versao_pode_ativar


class TesteEventos(unittest.TestCase):
    def envelope(self):
        identificador = "11111111-1111-4111-8111-111111111111"
        return {"schemaVersion": 1, "messageId": identificador, "messageType": "VersaoPublicada",
                "projetoId": identificador, "payload": {"publicacaoId": identificador,
                "requisitoId": identificador, "versaoId": identificador, "submissaoId": identificador,
                "retratoId": identificador, "numero": 1, "codigo": "REQ-1", "titulo": "Título",
                "conteudo": "Conteúdo", "criteriosAceite": ["Critério"], "hashConteudo": "hash",
                "data": "2026-01-01T00:00:00Z"}}

    def test_valido(self):
        envelope = self.envelope()
        self.assertIs(validar_envelope(envelope), envelope)

    def test_invalidos(self):
        for campo, valor in (("numero", 0), ("criteriosAceite", "texto"), ("versaoId", "inválido"),
                             ("data", "ontem")):
            envelope = self.envelope()
            envelope["payload"][campo] = valor
            with self.subTest(campo=campo), self.assertRaises(ValueError):
                validar_envelope(envelope)
        envelope = self.envelope()
        envelope["messageType"] = "Outro"
        with self.assertRaises(ValueError):
            validar_envelope(envelope)

    def envelope_alteracao(self):
        identificador = "11111111-1111-4111-8111-111111111111"
        return {"schemaVersion": 1, "messageId": identificador, "messageType": "RequisitoAlterado",
                "projetoId": identificador, "occurredAt": "2026-11-24T12:00:00Z", "payload": {"requisitoId": identificador,
                "projetoId": identificador, "codigo": "REQ-1", "titulo": "Título",
                "situacao": "CANCELADO", "revisao": 2}}

    def test_alteracao_valida(self):
        for situacao in ("ATIVO", "CANCELADO"):
            envelope = self.envelope_alteracao()
            envelope["payload"]["situacao"] = situacao
            self.assertIs(validar_envelope(envelope), envelope)

    def test_alteracao_invalida(self):
        for campo, valor in (("requisitoId", "inválido"), ("projetoId", "22222222-2222-4222-8222-222222222222"),
                             ("codigo", " "), ("titulo", "x" * 301), ("situacao", "OUTRO"),
                             ("revisao", True), ("revisao", -1), ("revisao", "2")):
            envelope = self.envelope_alteracao()
            envelope["payload"][campo] = valor
            with self.subTest(campo=campo, valor=valor), self.assertRaises(ValueError):
                validar_envelope(envelope)
        for campo, valor in (("schemaVersion", True), ("messageId", "inválido"), ("occurredAt", "2026-11-24T12:00:00")):
            envelope = self.envelope_alteracao()
            envelope[campo] = valor
            with self.subTest(campo=campo), self.assertRaises(ValueError):
                validar_envelope(envelope)

    def test_ordem_revisoes(self):
        self.assertTrue(revisao_pode_aplicar(1, None))
        self.assertFalse(revisao_pode_aplicar(1, 2))
        self.assertFalse(revisao_pode_aplicar(2, 2))
        self.assertTrue(revisao_pode_aplicar(3, 2))

    def test_ordem_versoes(self):
        self.assertFalse(versao_pode_ativar(1, 2))
        self.assertTrue(versao_pode_ativar(2, 2))
        self.assertTrue(versao_pode_ativar(3, 2))
        self.assertTrue(versao_pode_ativar(1, None))


if __name__ == "__main__":
    unittest.main()
