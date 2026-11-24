"""Validação dos comandos de exclusão de projeto."""

import uuid
from datetime import datetime


def validar_comando(envelope):
    if not isinstance(envelope, dict) or type(envelope.get("schemaVersion")) is not int or envelope["schemaVersion"] != 1:
        raise ValueError("Versão de envelope inválida")
    if envelope.get("messageType") not in ("FecharProjeto", "LimparProjeto"):
        raise ValueError("Tipo de mensagem inválido")
    try:
        for campo in ("messageId", "projetoId", "correlationId"):
            uuid.UUID(str(envelope[campo]))
        payload = envelope["payload"]
        if not isinstance(payload, dict):
            raise ValueError("Payload inválido")
        for campo in ("exclusaoId", "projetoId"):
            uuid.UUID(str(payload[campo]))
        if uuid.UUID(str(payload["projetoId"])) != uuid.UUID(str(envelope["projetoId"])):
            raise ValueError("Projeto do payload divergente")
        ocorrido = datetime.fromisoformat(str(envelope["occurredAt"]).replace("Z", "+00:00"))
        if ocorrido.tzinfo is None:
            raise ValueError("Instante sem fuso horário")
    except (KeyError, TypeError, ValueError, AttributeError) as erro:
        raise ValueError(f"Envelope inválido: {erro}") from erro
    return envelope
