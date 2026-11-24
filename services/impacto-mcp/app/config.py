"""Configuração por variáveis de ambiente (compose ou ConfigMap/Secret)."""

import os


def _obrigatoria(nome: str) -> str:
    valor = os.environ.get(nome)
    if not valor:
        raise RuntimeError(f"Variável de ambiente obrigatória ausente: {nome}")
    return valor


DB_HOST = _obrigatoria("DB_HOST")
DB_PORT = int(os.environ.get("DB_PORT", "5432"))
DB_NAME = _obrigatoria("DB_NAME")
DB_USER = _obrigatoria("DB_USER")
DB_PASSWORD = _obrigatoria("DB_PASSWORD")

# Carrega trechos de exemplo do projeto de demonstração quando o índice está vazio.
CARREGAR_DEMO = os.environ.get("CARREGAR_DEMO", "false").lower() == "true"

DSN = (
    f"host={DB_HOST} port={DB_PORT} dbname={DB_NAME} "
    f"user={DB_USER} password={DB_PASSWORD} connect_timeout=5"
)

RABBITMQ_HOST = _obrigatoria("RABBITMQ_HOST")
RABBITMQ_PORT = int(os.environ.get("RABBITMQ_PORT", "5672"))
RABBITMQ_USER = _obrigatoria("RABBITMQ_USER")
RABBITMQ_PASSWORD = _obrigatoria("RABBITMQ_PASSWORD")

PROJETOS_ACESSO_URL = os.environ.get("PROJETOS_ACESSO_URL", "http://projetos-acesso:8080").rstrip("/")
REQUISITOS_URL = os.environ.get("REQUISITOS_URL", "http://requisitos:8080").rstrip("/")
REVISOES_URL = os.environ.get("REVISOES_URL", "http://revisoes:8080").rstrip("/")
