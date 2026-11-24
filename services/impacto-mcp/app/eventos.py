"""Validação do envelope de publicação e decisão de ordenação."""

import uuid
from datetime import datetime


def validar_envelope(envelope):
    if not isinstance(envelope, dict) or envelope.get("schemaVersion") != 1 or isinstance(envelope.get("schemaVersion"), bool):
        raise ValueError("Versão de envelope inválida")
    if envelope.get("messageType") != "VersaoPublicada":
        raise ValueError("Tipo de mensagem inválido")
    try:
        for campo in ("messageId", "projetoId"):
            uuid.UUID(str(envelope[campo]))
        payload = envelope["payload"]
        if not isinstance(payload, dict):
            raise ValueError("Payload inválido")
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
