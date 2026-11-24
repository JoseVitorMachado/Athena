import unittest

from app.comandos import validar_comando
from app.projetos import estado, ignorar_evento


class TesteComandos(unittest.TestCase):
    def envelope(self):
        identificador = "11111111-1111-4111-8111-111111111111"
        return {"schemaVersion": 1, "messageId": identificador, "messageType": "FecharProjeto",
                "occurredAt": "2026-01-01T00:00:00Z", "correlationId": identificador,
                "projetoId": identificador, "payload": {"exclusaoId": identificador, "projetoId": identificador}}

    def test_validos(self):
        for tipo in ("FecharProjeto", "LimparProjeto"):
            envelope = self.envelope()
            envelope["messageType"] = tipo
            self.assertIs(validar_comando(envelope), envelope)

    def test_invalidos(self):
        for campo, valor in (("schemaVersion", True), ("messageId", "inválido"),
                             ("messageType", "Outro"), ("correlationId", None),
                             ("occurredAt", "2026-01-01T00:00:00")):
            envelope = self.envelope()
            envelope[campo] = valor
            with self.subTest(campo=campo), self.assertRaises(ValueError):
                validar_comando(envelope)
        for campo, valor in (("exclusaoId", "inválido"), ("projetoId", "22222222-2222-4222-8222-222222222222")):
            envelope = self.envelope()
            envelope["payload"][campo] = valor
            with self.subTest(campo=campo), self.assertRaises(ValueError):
                validar_comando(envelope)

    def test_estado_consulta_as_duas_marcas(self):
        class Conexao:
            def execute(self, sql, parametros):
                self.sql, self.parametros = sql, parametros
                return self

            def fetchone(self):
                return True, False

        conexao = Conexao()
        self.assertEqual(estado(conexao, "projeto"), (True, False))
        self.assertIn("projetos_bloqueados", conexao.sql)
        self.assertIn("projetos_excluidos", conexao.sql)
        self.assertEqual(conexao.parametros, ("projeto", "projeto"))

    def test_guarda_de_eventos(self):
        for tipo in ("VersaoPublicada", "RequisitoAlterado"):
            self.assertTrue(ignorar_evento(tipo, False, True))
            self.assertFalse(ignorar_evento(tipo, False, False))
        self.assertTrue(ignorar_evento("VersaoPublicada", True, False))
        self.assertFalse(ignorar_evento("RequisitoAlterado", True, False))
