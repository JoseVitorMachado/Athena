import unittest

from app.segmentacao import segmentar


def contar(texto):
    return len(texto.split()) + 2


class TesteSegmentacao(unittest.TestCase):
    def test_curto(self):
        trechos = segmentar("REQ-1", "Título", "Conteúdo", ["Critério"], contar)
        self.assertEqual(len(trechos), 1)
        self.assertTrue(trechos[0].startswith("REQ-1 Título"))
        self.assertIn("Critério", trechos[0])

    def test_longo_com_sobreposicao(self):
        palavras = [f"palavra{i}" for i in range(900)]
        trechos = segmentar("REQ-1", "Título", " ".join(palavras), ["Critério"], contar)
        self.assertGreater(len(trechos), 2)
        primeiro = set(trechos[0].split())
        segundo = set(trechos[1].split())
        self.assertGreaterEqual(len(primeiro & segundo), 45)
        self.assertTrue(all(contar("passage: " + t) <= 400 for t in trechos))
        self.assertIn("Critério", trechos[-1])

    def test_limite_com_tokenizacao_densa(self):
        def contar_caracteres(texto):
            return len(texto) + 2
        trechos = segmentar("R", "T", "palavra " * 250, [], contar_caracteres)
        self.assertTrue(all(contar_caracteres("passage: " + t) <= 512 for t in trechos))


if __name__ == "__main__":
    unittest.main()
