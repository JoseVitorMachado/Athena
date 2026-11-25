#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."

# Sempre religar apenas o LLM desta demonstração, inclusive se uma consulta falhar.
trap 'docker compose start ollama >/dev/null' EXIT
docker compose stop ollama
docker compose exec -T agente-ia python -m athena_ia perguntar \
  "Qual é a meta do REQ-3?" --saida /resultados/falha-ollama.json
docker compose start ollama
docker compose exec -T agente-ia python -m athena_ia aquecer
docker compose exec -T agente-ia python -m athena_ia perguntar \
  "Qual é a meta do REQ-3?" --saida /resultados/recuperacao.json
trap - EXIT
