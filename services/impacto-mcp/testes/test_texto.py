import unittest

from app.segmentacao import segmentar
from app.texto import texto_plano


class TesteTextoPlano(unittest.TestCase):
    def test_texto_plano_inalterado(self):
        self.assertEqual(texto_plano("O p95 < 1 s.\nOutra linha"), "O p95 < 1 s.\nOutra linha")

    def test_blocos_e_quebras_viram_linhas(self):
        html = "<div>Primeira</div><p>Segunda<br>Terceira</p><h2>Título</h2>"
        self.assertEqual(texto_plano(html), "Primeira\nSegunda\nTerceira\nTítulo")

    def test_lista_e_entidades(self):
        html = "<ul><li>PDF &amp; DOCX</li><li>Até&nbsp;10 MB</li></ul>"
        self.assertEqual(texto_plano(html), "- PDF & DOCX\n- Até 10 MB")

    def test_script_e_style_descartados(self):
        html = "<div>Visível</div><script>alert(1)</script><style>p{}</style>"
        self.assertEqual(texto_plano(html), "Visível")

    def test_segmentar_nao_indexa_tags(self):
        html = "<div>O sistema deve <strong>exportar</strong> o documento.</div><div><br></div>"
        trechos = segmentar("REQ-1", "Exportar", html, ["Preserva os requisitos"], lambda t: len(t.split()) + 2)
        self.assertTrue(all("<" not in trecho for trecho in trechos))
        self.assertIn("O sistema deve exportar o documento.", trechos[0])


if __name__ == "__main__":
    unittest.main()
