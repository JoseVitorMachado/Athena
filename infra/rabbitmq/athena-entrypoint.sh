#!/bin/sh
# Gera o arquivo de definições do broker a partir do modelo, preenchendo usuário
# e senha com o que vier do ambiente, e entrega o controle ao entrypoint oficial.
#
# Quando o RabbitMQ importa definições na subida ele deixa de criar o usuário
# padrão, então o usuário precisa estar no próprio arquivo. Para a senha não
# ficar gravada na imagem, o arquivo é montado aqui, em tempo de execução.
set -eu

: "${RABBITMQ_DEFAULT_USER:?defina RABBITMQ_DEFAULT_USER}"
: "${RABBITMQ_DEFAULT_PASS:?defina RABBITMQ_DEFAULT_PASS}"

for valor in "$RABBITMQ_DEFAULT_USER" "$RABBITMQ_DEFAULT_PASS"; do
  case "$valor" in
    *\"* | *\\*)
      echo "athena-entrypoint: usuário e senha do RabbitMQ não podem conter aspas duplas nem barra invertida" >&2
      exit 1
      ;;
  esac
done

escapar() {
  printf '%s' "$1" | sed -e 's/[&|]/\\&/g'
}

destino=/tmp/athena-definitions.json
# Num restart do container o arquivo da subida anterior continua em /tmp, com dono
# rabbitmq; com fs.protected_regular o kernel impede até o root de reabri-lo para
# escrita. Remover antes de gerar de novo.
rm -f "$destino"
umask 077
sed -e "s|__USUARIO__|$(escapar "$RABBITMQ_DEFAULT_USER")|g" \
    -e "s|__SENHA__|$(escapar "$RABBITMQ_DEFAULT_PASS")|g" \
    /etc/rabbitmq/definitions.json.modelo > "$destino"

if [ "$(id -u)" = "0" ]; then
  chown rabbitmq:rabbitmq "$destino"
fi

exec docker-entrypoint.sh "$@"
