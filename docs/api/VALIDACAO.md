# Evidências de validação

Execução local em 21/10/2026 (America/Sao_Paulo). Node.js 24.19.0; versões exatas e integridade das dependências em package-lock.json.

| Verificação | Resultado |
|---|---|
| Redocly lint, 8 contratos + compartilhado | Zero erros e zero avisos |
| Redocly bundle | 8 bundles autocontidos, nenhum $ref externo restante |
| Spectral, regras OAS + regras ATHENA | Zero erros e zero avisos |
| Matriz de autorização | 189 operações presentes; operationIds únicos globalmente |
| Catálogo MCP | 81 JSON Schemas de entrada compilados; projeto obrigatório |
| Aprovação/publicação MCP | Escopos separados; comprovante humano obrigatório na entrada da tool |
| Testes das regras | 13 mutações inválidas detectadas; baseline válido |
| Exemplos HATEOAS | 3 exemplos coerentes; mesmo requisito, candidata 4 e publicada 3 |
| Referência restrita do Livro | Somente disponivel:false; sem identidade nem conteúdo |

## Operações por contrato

| Contrato | Operações |
|---|---|
| bff-gestao.yaml | 69 |
| bff-livro.yaml | 4 |
| impacto-mcp.yaml | 10 |
| integracoes.yaml | 13 |
| projetos-acesso.yaml | 46 |
| rastreabilidade.yaml | 9 |
| requisitos.yaml | 31 |
| revisoes.yaml | 7 |

## Reprodução

```sh
npm ci
npm run validar
```

O comando reconstrói catálogo, matriz e bundles e executa todos os validadores. Saída desta execução: [validacao.log](validacao.log). `scripts/testar-regras.cjs` testa remoção de papel, escopo indevido, acesso anônimo, ausência de 429/Problem Details/Location/If-Match/Idempotency-Key, GET com corpo, perda de rota, operationId repetido, enum inválido e rejeição sem justificativa. Também verifica os três exemplos HATEOAS e o sigilo de referências.

As regras ATHENA verificam cada endpoint contra inventario.json e também validam os exemplos por JSON Schema 2020-12 com formatos. O arquivo compartilhado é uma biblioteca sem endpoints: apenas nele, a regra de componente não utilizado é desativada, pois os consumidores estão nos demais arquivos. Campos de licença não são inventados. A regra padrão que exige 2xx por operação é substituída pelo inventário para permitir o 302 normativo de OAuth authorize. Nenhum controle de segurança, cabeçalho obrigatório ou validação de exemplo foi desativado.

A revisão automática bloqueou uma tentativa inicial de telemetria do Redocly, por risco de envio de metadados não públicos. A execução final desativa telemetria e consulta de atualização, tanto no comando quanto na configuração, e concluiu as verificações locais. Não foi necessário conceder acesso ao destino da telemetria.

## Limites das evidências

Lint, schemas e exemplos não comprovam transações, consumo único de comprovante, idempotência, ausência de vazamentos na implementação, desempenho, recuperação SAGA ou compatibilidade de clientes MCP/OAuth. Os contratos registram o comportamento exigido; sua execução e ensaios ainda pertencem à implementação. Questões x-decidir estão em PROPOSTAS.md e permanecem pendentes mesmo com lint válido. A camada de segurança OAuth pública/confidencial condicionada ao grant usa extensão explícita, porque OpenAPI não expressa essa condição nativamente.
