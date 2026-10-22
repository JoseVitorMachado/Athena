# ATHENA — contratos de API 1.0.0

Comece por [docs/api/README.md](docs/api/README.md).

Contém seis serviços de domínio, dois BFFs, componentes compartilhados, bundles autocontidos, catálogo MCP e matriz de autorização. Pendências do livro estão em docs/api/PROPOSTAS.md.

```sh
npm ci
npm run validar
```

Node.js 24.19.0 usado na validação; dependências fixadas no package-lock.json. A validação desativa telemetria e consulta de atualização do Redocly. Não há implementação de backend neste pacote.

## Abrir a documentação no Swagger UI

Com Docker instalado e em execução, rode na raiz deste pacote (`athena-api`):

```sh
docker run --rm -p 127.0.0.1:8080:8080 \
  -e SWAGGER_JSON=/contratos/bff-gestao.bundled.yaml \
  -v "$PWD/docs/api/dist:/contratos:ro" \
  docker.swagger.io/swaggerapi/swagger-ui
```

Abra **http://localhost:8080** no navegador. Para encerrar, pressione `Ctrl+C` no terminal.

O comando abre o contrato de Gestão. Para consultar outra API, substitua `bff-gestao.bundled.yaml` por outro arquivo de `docs/api/dist/`, como `bff-livro.bundled.yaml` ou `requisitos.bundled.yaml`. Os bundles já incluem as referências necessárias; não é preciso rodar `npm ci` para visualizá-los.

O Swagger permite consultar rotas, schemas e exemplos. O botão **Try it out** só executará chamadas reais quando houver um backend disponível e o endereço em `servers` apontar para ele. O host `athena.example.invalid` dos contratos é apenas ilustrativo.
