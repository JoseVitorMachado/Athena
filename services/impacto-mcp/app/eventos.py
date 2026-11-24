"""Validação do envelope de publicação e decisão de ordenação."""

import uuid
from datetime import datetime


def validar_envelope(envelope):
    if not isinstance(envelope, dict) or envelope.get("schemaVersion") != 1 or isinstance(envelope.get("schemaVersion"), bool):
        raise ValueError("Versão de envelope inválida")
    if envelope.get("messageType") not in ("VersaoPublicada", "RequisitoAlterado"):
        raise ValueError("Tipo de mensagem inválido")
    try:
        for campo in ("messageId", "projetoId"):
            uuid.UUID(str(envelope[campo]))
        payload = envelope["payload"]
        if not isinstance(payload, dict):
            raise ValueError("Payload inválido")
        if envelope["messageType"] == "RequisitoAlterado":
            for campo in ("requisitoId", "projetoId"):
                uuid.UUID(str(payload[campo]))
            if uuid.UUID(str(payload["projetoId"])) != uuid.UUID(str(envelope["projetoId"])):
                raise ValueError("Projeto do payload divergente")
            for campo, limite in (("codigo", 40), ("titulo", 300)):
                if not isinstance(payload[campo], str) or not payload[campo].strip() or len(payload[campo]) > limite:
                    raise ValueError(f"Campo inválido: {campo}")
            ocorrido = datetime.fromisoformat(str(envelope["occurredAt"]).replace("Z", "+00:00"))
            if ocorrido.tzinfo is None:
                raise ValueError("Instante sem fuso horário")
            if payload["situacao"] not in ("ATIVO", "CANCELADO"):
                raise ValueError("Situação inválida")
            revisao = payload["revisao"]
            if isinstance(revisao, bool) or not isinstance(revisao, int) or not 0 <= revisao <= 9223372036854775807:
                raise ValueError("Revisão inválida")
            return envelope
        for campo in ("publicacaoId", "requisitoId", "versaoId", "submissaoId", "retratoId"):
            uuid.UUID(str(payload[campo]))
        for campo in ("codigo", "titulo", "conteudo", "hashConteudo", "data"):
            if not isinstance(payload[campo], str) or not payload[campo].strip():
                raise ValueError(f"Campo inválido: {campo}")
        if len(payload["codigo"]) > 40 or len(payload["titulo"]) > 300:
            raise ValueError("Código ou título excede o limite")
        numero = payload["numero"]
        if isinstance(numero, bool) or not isinstance(numero, int) or numero < 1:
            raise ValueError("Número de versão inválido")
        criterios = payload["criteriosAceite"]
        if not isinstance(criterios, list) or any(not isinstance(c, str) or not c.strip() for c in criterios):
            raise ValueError("Critérios de aceite inválidos")
        data = datetime.fromisoformat(payload["data"].replace("Z", "+00:00"))
        if data.tzinfo is None:
            raise ValueError("Data sem fuso horário")
    except (KeyError, TypeError, ValueError, AttributeError) as erro:
        raise ValueError(f"Envelope inválido: {erro}") from erro
    return envelope


def versao_pode_ativar(numero, numero_ativo):
    return numero_ativo is None or numero >= numero_ativo


def revisao_pode_aplicar(revisao, revisao_atual):
    """Indica se a alteração é mais recente que a revisão registrada."""
    return revisao_atual is None or revisao > revisao_atual
