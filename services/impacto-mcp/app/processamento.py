"""Recebimento confiável e processamento dos trabalhos de indexação."""

import json
import logging
import threading
import uuid

import pika
from psycopg.types.json import Jsonb

from . import config, embeddings
from .banco import pool
from .eventos import revisao_pode_aplicar, validar_envelope, versao_pode_ativar
from .segmentacao import segmentar

LOG = logging.getLogger("impacto_mcp.processamento")
FILA = "impacto-mcp.eventos"
ESPERAS = (2, 10, 30, 60)


def _publicar(canal, destino, corpo, propriedades, headers, expiracao=None):
    canal.basic_publish(exchange="", routing_key=destino, body=corpo, mandatory=True,
                        properties=pika.BasicProperties(delivery_mode=2, content_type=propriedades.content_type,
                                                        headers=headers, expiration=expiracao))


def _receber(canal, metodo, propriedades, corpo):
    headers = dict(propriedades.headers or {})
    try:
        envelope = validar_envelope(json.loads(corpo))
    except (ValueError, UnicodeDecodeError) as erro:
        headers["x-athena-motivo"] = str(erro)[:1000]
        _publicar(canal, FILA + ".dlq", corpo, propriedades, headers)
        canal.basic_ack(metodo.delivery_tag)
        return
    payload = envelope["payload"]
    try:
        with pool.connection() as conexao:
            inserido = conexao.execute(
                "INSERT INTO inbox (message_id, tipo) VALUES (%s, %s) ON CONFLICT DO NOTHING RETURNING message_id",
                (envelope["messageId"], envelope["messageType"])).fetchone()
            if inserido:
                if envelope["messageType"] == "RequisitoAlterado":
                    _alterar_requisito(conexao, envelope["projetoId"], payload, envelope["occurredAt"])
                    conexao.execute("UPDATE inbox SET processado_em = now() WHERE message_id = %s",
                                    (envelope["messageId"],))
                else:
                    conexao.execute(
                        """INSERT INTO trabalhos_indexacao (id, message_id, projeto_id, origem_id, versao_id, payload)
                           VALUES (%s, %s, %s, %s, %s, %s)""",
                        (uuid.uuid4(), envelope["messageId"], envelope["projetoId"], payload["requisitoId"],
                         payload["versaoId"], Jsonb(payload)))
    except Exception as erro:
        LOG.exception("Falha ao registrar mensagem")
        try:
            tentativas = max(0, int(headers.get("x-athena-tentativas", 0))) + 1
        except (TypeError, ValueError):
            tentativas = 5
        headers["x-athena-tentativas"] = tentativas
        headers["x-athena-motivo"] = str(erro)[:1000]
        destino = FILA + (".dlq" if tentativas >= 5 else ".retentativa")
        expiracao = None if tentativas >= 5 else str(ESPERAS[tentativas - 1] * 1000)
        _publicar(canal, destino, corpo, propriedades, headers, expiracao)
    canal.basic_ack(metodo.delivery_tag)


def _alterar_requisito(conexao, projeto, payload, ocorrido):
    # Usa o instante do evento (relógio de Requisitos), não o do consumo: se este serviço ficar
    # fora do ar e consumir o cancelamento tarde, uma publicação posterior não fica bloqueada.
    requisito = uuid.UUID(str(payload["requisitoId"]))
    conexao.execute("SELECT pg_advisory_xact_lock(hashtextextended(%s, 1))", (str(requisito),))
    linha = conexao.execute("""SELECT revisao FROM requisitos_cancelados
        WHERE projeto_id = %s AND requisito_id = %s""", (projeto, requisito)).fetchone()
    if not revisao_pode_aplicar(payload["revisao"], linha[0] if linha else None):
        return
    cancelado = payload["situacao"] == "CANCELADO"
    conexao.execute("""INSERT INTO requisitos_cancelados (projeto_id, requisito_id, revisao, cancelado_em, situacao)
        VALUES (%s, %s, %s, CASE WHEN %s THEN %s::timestamptz ELSE NULL END, %s)
        ON CONFLICT (projeto_id, requisito_id) DO UPDATE SET revisao = EXCLUDED.revisao,
        cancelado_em = CASE WHEN %s THEN EXCLUDED.cancelado_em ELSE requisitos_cancelados.cancelado_em END,
        situacao = EXCLUDED.situacao""",
        (projeto, requisito, payload["revisao"], cancelado, ocorrido, payload["situacao"], cancelado))
    if cancelado:
        conexao.execute("UPDATE trechos SET ativo = false WHERE projeto_id = %s AND origem_id = %s",
                        (projeto, requisito))


def consumir(parar):
    while not parar.is_set():
        conexao = None
        try:
            parametros = pika.ConnectionParameters(host=config.RABBITMQ_HOST, port=config.RABBITMQ_PORT,
                credentials=pika.PlainCredentials(config.RABBITMQ_USER, config.RABBITMQ_PASSWORD),
                heartbeat=60, blocked_connection_timeout=30, socket_timeout=5)
            conexao = pika.BlockingConnection(parametros)
            canal = conexao.channel()
            canal.confirm_delivery()
            canal.exchange_declare(exchange="athena.events", exchange_type="topic", durable=True)
            canal.queue_declare(queue=FILA + ".dlq", durable=True)
            canal.queue_declare(queue=FILA + ".retentativa", durable=True, arguments={
                "x-dead-letter-exchange": "", "x-dead-letter-routing-key": FILA})
            canal.queue_declare(queue=FILA, durable=True, arguments={
                "x-dead-letter-exchange": "", "x-dead-letter-routing-key": FILA + ".dlq"})
            canal.queue_bind(queue=FILA, exchange="athena.events", routing_key="requisitos.VersaoPublicada")
            canal.queue_bind(queue=FILA, exchange="athena.events", routing_key="requisitos.RequisitoAlterado")
            canal.basic_qos(prefetch_count=1)
            canal.basic_consume(queue=FILA, on_message_callback=_receber, auto_ack=False)
            while not parar.is_set():
                conexao.process_data_events(time_limit=1)
        except Exception:
            LOG.exception("Consumidor desconectado; nova tentativa em 5 segundos")
            parar.wait(5)
        finally:
            if conexao and conexao.is_open:
                conexao.close()


def _reindexar(conexao):
    linha = conexao.execute("SELECT id, texto FROM trechos WHERE modelo <> %s LIMIT 1", (embeddings.MODELO,)).fetchone()
    conexao.commit()
    if not linha:
        return False
    identificador, texto = linha
    vetor = embeddings.literal(embeddings.gerar_trechos([texto])[0])
    conexao.execute("UPDATE trechos SET embedding = %s::vector, modelo = %s, indexado_em = now() WHERE id = %s AND modelo <> %s",
                    (vetor, embeddings.MODELO, identificador, embeddings.MODELO))
    conexao.commit()
    return True


def _trabalho(conexao):
    # Lock de sessão mantém a reserva durante a inferência sem manter transação aberta.
    linha = conexao.execute("""SELECT id, message_id, projeto_id, origem_id, versao_id, payload, tentativas
        FROM trabalhos_indexacao WHERE estado = 'PENDENTE' AND proxima_tentativa <= now()
        ORDER BY criado_em FOR UPDATE SKIP LOCKED LIMIT 1""").fetchone()
    if not linha:
        conexao.commit()
        return False
    reservado = conexao.execute("SELECT pg_try_advisory_lock(hashtextextended(%s, 0))", (str(linha[0]),)).fetchone()[0]
    conexao.commit()
    if not reservado:
        return False
    identificador, mensagem, projeto, origem, versao, payload, tentativas = linha
    try:
        textos = segmentar(payload["codigo"], payload["titulo"], payload["conteudo"],
                           payload["criteriosAceite"], embeddings.contar_tokens)
        vetores = embeddings.gerar_trechos(textos)
        with conexao.transaction():
            conexao.execute("SELECT pg_advisory_xact_lock(hashtextextended(%s, 1))", (str(origem),))
            atual = conexao.execute("""SELECT max((t.payload->>'numero')::integer)
                FROM trabalhos_indexacao t JOIN trechos s ON s.origem_id = t.origem_id AND s.versao_id = t.versao_id
                WHERE s.ativo AND t.origem_id = %s AND t.estado = 'CONCLUIDO'""", (origem,)).fetchone()[0]
            cancelamento = conexao.execute("""SELECT cancelado_em, situacao FROM requisitos_cancelados
                WHERE projeto_id = %s AND requisito_id = %s""", (projeto, origem)).fetchone()
            bloqueado = (cancelamento and (cancelamento[1] == "CANCELADO" or
                         (cancelamento[0] and (not payload.get("data") or
                          conexao.execute("SELECT %s::timestamptz < %s",
                                          (payload["data"], cancelamento[0])).fetchone()[0]))))
            if not bloqueado and versao_pode_ativar(payload["numero"], atual):
                conexao.execute("UPDATE trechos SET ativo = false WHERE origem_id = %s", (origem,))
                for ordem, (texto, vetor) in enumerate(zip(textos, vetores)):
                    conexao.execute("""INSERT INTO trechos
                        (id, projeto_id, origem_id, versao_id, ordem, codigo, titulo, texto, modelo, embedding, ativo)
                        VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s::vector,true)
                        ON CONFLICT (origem_id, versao_id, ordem) DO UPDATE SET
                        codigo = EXCLUDED.codigo, titulo = EXCLUDED.titulo, texto = EXCLUDED.texto,
                        modelo = EXCLUDED.modelo, embedding = EXCLUDED.embedding, ativo = true, indexado_em = now()""",
                        (uuid.uuid4(), projeto, origem, versao, ordem, payload["codigo"], payload["titulo"],
                         texto, embeddings.MODELO, embeddings.literal(vetor)))
            conexao.execute("UPDATE trabalhos_indexacao SET estado = 'CONCLUIDO', concluido_em = now() WHERE id = %s", (identificador,))
            if mensagem:
                conexao.execute("UPDATE inbox SET processado_em = now() WHERE message_id = %s", (mensagem,))
    except Exception as erro:
        conexao.rollback()
        tentativas += 1
        conexao.execute("""UPDATE trabalhos_indexacao SET tentativas = %s, ultimo_erro = %s,
            estado = %s, proxima_tentativa = now() + (%s * interval '1 second') WHERE id = %s""",
            (tentativas, str(erro)[:1000], 'FALHOU' if tentativas >= 5 else 'PENDENTE',
             ESPERAS[min(tentativas - 1, 3)], identificador))
        conexao.commit()
        LOG.exception("Falha no trabalho de indexação %s", identificador)
    finally:
        conexao.execute("SELECT pg_advisory_unlock(hashtextextended(%s, 0))", (str(identificador),))
        conexao.commit()
    return True


def trabalhar(parar):
    while not parar.is_set():
        try:
            with pool.connection() as conexao:
                trabalhou = _trabalho(conexao) or _reindexar(conexao)
            if not trabalhou:
                parar.wait(1)
        except Exception:
            LOG.exception("Falha no trabalhador de indexação")
            parar.wait(5)


def iniciar():
    parar = threading.Event()
    threads = [threading.Thread(target=alvo, args=(parar,), daemon=True, name=nome)
               for alvo, nome in ((consumir, "consumidor"), (trabalhar, "indexador"))]
    for thread in threads:
        thread.start()
    return parar, threads
