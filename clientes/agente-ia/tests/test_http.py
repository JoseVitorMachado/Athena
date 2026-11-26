import json
import threading
from http.server import ThreadingHTTPServer
from urllib.request import Request, urlopen
from urllib.error import HTTPError

import pytest
from athena_ia import __main__ as servidor
from athena_ia.athena import ErroAthena


@pytest.fixture
def endpoint():
    http = ThreadingHTTPServer(('127.0.0.1', 0), servidor.Handler)
    thread = threading.Thread(target=http.serve_forever, daemon=True)
    thread.start()
    yield f'http://127.0.0.1:{http.server_port}'
    http.shutdown()
    http.server_close()
    thread.join()


def post(url, body, token='Bearer usuario'):
    req = Request(url + '/perguntar', data=json.dumps(body).encode(), headers={'Authorization': token}, method='POST')
    try:
        with urlopen(req) as response:
            return response.status, json.load(response)
    except HTTPError as e:
        return e.code, json.load(e)


def test_autenticacao_e_validacao(endpoint):
    assert post(endpoint, {}, '')[0] == 401
    assert post(endpoint, {'projetoId': 'invalido', 'pergunta': 'oi'})[0] == 400
    assert post(endpoint, {'projetoId': '22222222-2222-4222-8222-222222222222', 'pergunta': ' '})[0] == 400


def test_resposta_e_token_por_requisicao(endpoint, monkeypatch):
    async def fake(config, token, pergunta):
        assert token == 'usuario'
        assert config.projeto_id == '22222222-2222-4222-8222-222222222222'
        return {'estado': 'degradado', 'resultado': {'resposta': 'evidências', 'fontes_ids': ['E1']},
                'fontes': [{'id': 'E1', 'codigo': 'REQ-1', 'texto': 'trecho'}]}
    monkeypatch.setattr(servidor, 'perguntar_http', fake)
    status, body = post(endpoint, {'projetoId': '22222222-2222-4222-8222-222222222222', 'pergunta': 'oi'})
    assert status == 200 and body['estado'] == 'degradado' and body['fontes'][0]['codigo'] == 'REQ-1'
    assert body['metricas']['tempo_total_ms'] >= 0


def test_erro_athena_e_limite(endpoint, monkeypatch):
    async def fake(*args):
        e = ErroAthena('erro')
        e.status = 403
        raise e
    monkeypatch.setattr(servidor, 'perguntar_http', fake)
    body = {'projetoId': '22222222-2222-4222-8222-222222222222', 'pergunta': 'oi'}
    assert post(endpoint, body)[0] == 403
    servidor.limite_geracoes.acquire()
    servidor.limite_geracoes.acquire()
    try:
        assert post(endpoint, body)[0] == 429
        with urlopen(endpoint + '/health') as response:
            assert response.status == 200
    finally:
        servidor.limite_geracoes.release()
        servidor.limite_geracoes.release()


def test_token_usuario_nao_faz_login_nem_renova():
    import asyncio
    import httpx
    from athena_ia.athena import ClienteAthena
    from athena_ia.config import Config

    caminhos = []
    def responder(request):
        caminhos.append(request.url.path)
        assert request.headers['Authorization'] == 'Bearer token-do-usuario'
        return httpx.Response(401, json={})

    async def executar():
        async with ClienteAthena(Config(), transport=httpx.MockTransport(responder), token_usuario='token-do-usuario') as cliente:
            with pytest.raises(ErroAthena) as erro:
                await cliente.buscar_contexto('teste')
            assert erro.value.status == 401
    asyncio.run(executar())
    assert caminhos == ['/mcp']
