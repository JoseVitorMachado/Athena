import asyncio

import httpx

from athena_ia.athena import ClienteAthena
from athena_ia.config import Config
from athena_ia.texto import texto_plano


def test_texto_plano_inalterado():
    assert texto_plano("O p95 < 1 s.\nSegunda linha") == "O p95 < 1 s.\nSegunda linha"


def test_html_do_editor_vira_linhas():
    html = "<h2>Exportação</h2><div>Gera <strong>PDF</strong> &amp; DOCX</div><div><br></div><ul><li>Um</li><li>Dois</li></ul>"
    assert texto_plano(html) == "Exportação\nGera PDF & DOCX\n- Um\n- Dois"


def test_versao_da_api_chega_em_texto_plano():
    versao = {"versaoId": "x", "conteudo": "<div>Deve&nbsp;exportar</div><script>alert(1)</script>"}

    def responder(request):
        if request.url.path.endswith("/login"):
            return httpx.Response(200, json={"accessToken": "t", "expiraEm": "2999-01-01T00:00:00Z", "csrfToken": "c"})
        return httpx.Response(200, json=versao)

    async def executar():
        async with ClienteAthena(Config(), transport=httpx.MockTransport(responder)) as cliente:
            return await cliente.versao("11111111-1111-4111-8111-111111111111", "22222222-2222-4222-8222-222222222222")

    assert asyncio.run(executar())["conteudo"] == "Deve exportar"
