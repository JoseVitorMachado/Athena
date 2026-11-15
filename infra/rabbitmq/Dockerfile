# RabbitMQ do ATHENA.
#
# Parte da imagem oficial e acrescenta a topologia de mensageria definida no
# livro de arquitetura (§6.8): as exchanges duráveis athena.commands e
# athena.events, do tipo topic. Assim o broker já sobe pronto e nenhum serviço
# depende de outro para criar as exchanges.
FROM rabbitmq:4.3.7-management-alpine

LABEL org.opencontainers.image.title="athena-rabbitmq" \
      org.opencontainers.image.description="RabbitMQ com as exchanges do ATHENA"

COPY --chown=rabbitmq:rabbitmq 20-athena.conf /etc/rabbitmq/conf.d/20-athena.conf
COPY --chown=rabbitmq:rabbitmq definitions.json.modelo /etc/rabbitmq/definitions.json.modelo
COPY athena-entrypoint.sh /usr/local/bin/athena-entrypoint.sh

HEALTHCHECK --interval=10s --timeout=10s --start-period=30s --retries=6 \
  CMD rabbitmq-diagnostics -q check_port_connectivity || exit 1

ENTRYPOINT ["athena-entrypoint.sh"]
CMD ["rabbitmq-server"]
