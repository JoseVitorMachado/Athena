# ATHENA — contratos HTTP e MCP 1.0.0

Contratos OpenAPI 3.1 elaborados contract-first a partir do **Livro de Arquitetura v1.0**, sem extrair controllers ou gerar especificação a partir de uma aplicação. Os YAMLs são a fonte editável; `dist/` contém somente os bundles produzidos pelo Redocly. Esta entrega especifica interfaces; não implementa os serviços nem comprova execução, autorização ou compatibilidade MCP.

## Índice e prioridade

| Prioridade | Arquivo | Conteúdo |
|---|---|---|
| P0 | [_shared/components.yaml](_shared/components.yaml) | Segurança, paginação, cabeçalhos, erros, enums e HATEOAS |
| P0 | [bff-gestao.yaml](bff-gestao.yaml) | Gestão, composição e repasses com referências aos schemas de domínio |
| P0 | [bff-livro.yaml](bff-livro.yaml) | Somente GET; conteúdo publicado autorizado |
| P0 | [requisitos.yaml](requisitos.yaml) | Documentos, épicos, candidatas, submissões, versões, publicações e baselines |
| P1 | [rastreabilidade.yaml](rastreabilidade.yaml) | Relações oficiais, mapa, cobertura e retratos |
| P1 | [revisoes.yaml](revisoes.yaml) | Lotes, pendências e decisões |
| P1 | [projetos-acesso.yaml](projetos-acesso.yaml) | Identidade, OAuth, sessões, workspace, projetos e membros |
| P2 | [integracoes.yaml](integracoes.yaml) | Conexões, vínculos, sincronizações e tarefas externas |
| P2 | [impacto-mcp.yaml](impacto-mcp.yaml) | Análises, sugestões, busca de contexto e indexação |
| P2 | [mcp-ferramentas.md](mcp-ferramentas.md) | Catálogo de 81 tools com JSON Schema de entrada expandido |
| P2 | [matriz-autorizacao.md](matriz-autorizacao.md) | 189 operações; papel e escopo extraídos dos contratos |
| Revisão | [PROPOSTAS.md](PROPOSTAS.md) | Escolhas de representação e perguntas não resolvidas pelo livro |
| Validação | [VALIDACAO.md](VALIDACAO.md) | Comandos, verificações e limites das evidências |

`x-proposta` identifica escolhas de nomes/representação; não é aprovação de negócio. `x-decidir` é pergunta pendente. Exemplos não constituem catálogo normativo quando o schema não tem enum. Não implementar políticas pendentes concedendo a permissão do exemplo. Nenhuma atualização deste pacote altera o livro original.

## Rotas do Gateway

As rotas consagradas estão na §6.1; as demais são **propostas**, pois a tabela do livro não as define. O Gateway não repete escritas nem coordena SAGA. Uma única origem HTTPS atende Gestão, Livro e API; host `.example.invalid` é ilustrativo, nunca configuração de produção.

| Entrada externa | Destino / transformação | Acesso | Situação |
|---|---|---|---|
| `/api/v1/gestao/**` | BFF Gestão, mesmo caminho | JWT + confirmação da sessão/autorização atual | Livro §6.1 |
| `/api/v1/livro/**` | BFF Livro, mesmo caminho | JWT + acesso atual; somente GET | Livro §6.1 |
| `/mcp` | Impacto/MCP, Streamable HTTP | OAuth e autorização por chamada, inclusive streams | Livro §6.1 |
| `/api/v1/autenticacao/cadastro` e `/login` | Fachada de identidade → Projetos e acesso | Públicas, com limites | Proposta §7.1 |
| `/api/v1/autenticacao/recuperacao` e `/recuperacao/conclusao` | Fachada de identidade → Projetos e acesso | Públicas, resposta sem enumerar conta | Proposta §7.1 |
| `/api/v1/autenticacao/renovacao` e `/logout` | Fachada de identidade → Projetos e acesso | Cookie refresh **e** X-CSRF-Token **e** Origin válido | Proposta §§7.2–7.3 |
| `/api/v1/usuarios/me` | Fachada de identidade → Projetos e acesso | JWT, conta atual | Proposta |
| `/api/v1/autorizacoes-agente/**` | Fachada de identidade → Projetos e acesso | JWT, somente titular | Proposta |
| `/oauth2/authorize` | Servidor de autorização em Projetos e acesso | Usuário autenticado; seleção explícita | Proposta §7.5 |
| `/oauth2/token` | Servidor de autorização em Projetos e acesso | Código+PKCE / refresh válido / cliente serviço autenticado, conforme grant | Proposta §§7.1 e 7.5 |
| `/oauth2/revoke` | Servidor de autorização em Projetos e acesso | Autenticação/vinculação do cliente ao token | Proposta |
| `/oauth2/jwks` | Chaves públicas de Projetos e acesso | Pública | Proposta de rota, exceção permitida §6.1 |
| `/.well-known/oauth-authorization-server` | Metadados do emissor | Pública | Proposta |
| `/.well-known/oauth-protected-resource` | Metadados do recurso `https://<origem>/mcp` | Pública | Proposta |
| `/api/v1/impacto/projetos/{projetoId}/**` | Fachada REST → Impacto; remover segmento `/impacto`, trocar token para aud impacto-mcp | JWT, escopo delegado se aplicável, papel/acesso atuais | Proposta para REST mencionado na §6.1 |

Essas fachadas são fronteiras do Gateway, não exposição direta de instâncias de domínio. O Gateway e as fachadas obtêm identidade interna apropriada; chamadas em nome do usuário usam troca de token e não simples repasse do bearer externo. Rotas sem correspondência explícita são negadas. `/api/v1/internal/**`, métricas e endpoints técnicos nunca são roteados externamente. As APIs dos seis domínios ficam em rede interna; URLs dos servidores nos YAMLs apenas estabelecem uma origem ilustrativa para a documentação. A política de criação/listagem de recursos de conta/workspace não concede papel em projeto.

Protocolos OAuth mantêm nomes padronizados (`grant_type`, `access_token`, `code_verifier` etc.), endpoints de descoberta e URLs não versionadas para interoperabilidade. Campos de negócio permanecem em português camelCase. Erros normativos OAuth oferecem `application/json` além do Problem Details dos erros de borda; não traduzir `invalid_grant` para outro formato no protocolo. `security` no token/revoke representa o ramo de cliente confidencial; `x-seguranca-condicional` documenta o ramo de cliente público com PKCE/credencial de concessão. A aplicação precisa implementar os ramos e comprovar interoperabilidade; não exigir HTTP Basic a cliente público nem bearer prévio para obter seu primeiro token. O formato OpenAPI não expressa de modo nativo segurança condicionada a campo de formulário; a extensão é parte normativa deste contrato.

Registro dinâmico de clientes não está anunciado nem presumido. A credencial de recuperação é opaca porque a §7.1 contradiz formato e validade. Login e recuperação têm limite por conta/IP e resposta que não enumera usuários. O Location da recuperação aceita é constante para instruções de conclusão, não URI de conta ou prova de existência.

## Mesmo requisito nos dois clientes

Exemplos completos nomeados estão no 200 dos GETs dos BFFs. O UUID é `11111111-1111-4111-8111-111111111111`, código `REQ-42`, projeto `22222222-2222-4222-8222-222222222222`.

| Campo | Gestão: `editor-aguardando-revisao` | Livro: `leitor-publicado` |
|---|---|---|
| Identidade | Mesmo UUID e REQ-42 | Mesmo UUID e REQ-42 |
| Título | Exportar documento de requisitos. | Exportar documento de requisitos. |
| Versão exibida | `versaoEmEdicao: 4` | `versaoPublicada: 3` |
| Estado | `AGUARDANDO_REVISAO` | Conteúdo publicado da versão 3 |
| Revisão | `loteId` e `pendenciasAbertas: 2` | Sem detalhes privados da candidata |
| Conteúdo | Representação para gestão de candidata e revisão | `conteudo` e `criteriosAceite` publicados |
| Referências | Mapa e relações atuais no recurso de rastreabilidade | Relações do retrato da publicação |
| Ações | Apenas `consultar-revisao`, GET com href | Sem `_actions` |
| Links | self, colecao, versoes, submissao, revisao, conforme autorização | self, colecao e versoes publicadas; não expõe submissão/revisão que Leitor não pode consultar |

O exemplo `aprovador-aprovada` apresenta `publicar`, com POST para `/api/v1/gestao/projetos/{projetoId}/requisitos/{requisitoId}/publicacoes` e precondições If-Match e Idempotency-Key. Publicar via MCP exige também comprovante humano específico; `athena:approve` e `athena:publish` são independentes. `_actions` é formato próprio inspirado em HAL-FORMS. Toda execução revalida permissão, estado, versão e acesso, inclusive com link obtido antes.

O Livro mantém a versão 3 enquanto a candidata 4 está em revisão. Cancelamento retira do Livro corrente. Reativação exige nova aprovação/publicação. Referência sem autorização é **apenas** `{"disponivel":false}`, ou é omitida se até a existência for informação restrita. Referência autorizada a cancelado vem marcada. Rastreabilidade indisponível permite conteúdo com avisos; conteúdo/acesso/situação indisponível resulta em 503. A Gestão não presume aprovação quando Revisões está indisponível e omite ações dependentes dessa informação.

## Concorrência e idempotência

GET de recurso único devolve ETag forte da revisão técnica. If-Match é comparado atomicamente no serviço proprietário: ausente 428, antigo 412. Os BFFs preservam o significado do ETag, não calculam outro a partir da composição de campos. Em visão composta, o ETag corresponde ao recurso proprietário indicado abaixo; informação auxiliar pode mudar independentemente.

| Operação | ETag usado |
|---|---|
| PATCH de metadados do requisito, cancelamento e reativação | Requisito |
| PATCH candidata, submissão e publicação | Candidata |
| Decisão | Lote |
| PATCH/DELETE relação | Relação |
| PATCH pendência | Pendência |
| Exclusão de projeto / workspace | Projeto / workspace |

O GET de requisito da Gestão devolve o ETag do requisito. Para editar/submeter/publicar candidata, o cliente obtém o ETag específico em GET `/api/v1/gestao/projetos/{projetoId}/requisitos/{requisitoId}/candidata`. Não reutiliza o ETag dos metadados nessas ações. O número da versão de conteúdo nunca substitui a revisão técnica.

Submissão, decisão, publicação, cancelamento, reativação e exclusões usam Idempotency-Key conforme inventário. Sincronização inclui a exigência como proposta expressa. Autenticar → verificar acesso atual → procurar chave e comparar pedido → recuperar resultado idêntico (24 h) sem reavaliar If-Match → somente nova operação valida If-Match aplicável. Reuso divergente é 409. Falha ou timeout não prova ausência de efeito. Nenhum 202 é promessa de conclusão da operação.

O filtro projetoId é o parâmetro obrigatório de caminho nas coleções de projeto; não se repete em query para evitar dois identificadores conflitantes. Listagens usam page=0, size=20, máximo 100 e sort=atualizadoEm,desc, com id como desempate; enum admite somente atualizadoEm asc/desc e id asc/desc nesta proposta. Recursos imutáveis ordenam atualizadoEm pelo instante de registro (data/criadoEm) sem alterar seu conteúdo. Valor fora do enum retorna 400. Cancelados ficam fora das listagens de requisitos por padrão.

## Validação e reprodução

Na raiz do pacote, com Node.js e npm:

```sh
npm ci
npm run validar
```

A validação executa catálogo MCP, matriz, Redocly lint, bundles e Spectral com regras ATHENA. `scripts/inventario.json` é o inventário congelado independente da extração para detectar perda de rota, verbo, status ou cabeçalho obrigatório. Mudanças intencionais precisam revisar o inventário junto com o contrato. O script de matriz verifica operationIds globais e registra mapeamento em `docs/seguranca`. As fontes não dependem do diretório de implementação de uma API.

## Fontes normativas

Fonte de comportamento: Arquitetura v1.0, especialmente §§4, 5, 6.1–6.4, 6.8–6.11 e 7.1–7.5; figuras fornecidas confirmam fronteiras, ordem de compensação e ativação posterior do lote. Fontes de formato/protocolo: [OpenAPI 3.1](https://spec.openapis.org/oas/v3.1.0.html), [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457), [RFC 6585](https://www.rfc-editor.org/rfc/rfc6585), [RFC 6749](https://www.rfc-editor.org/rfc/rfc6749), [PKCE RFC 7636](https://www.rfc-editor.org/rfc/rfc7636), [Token Exchange RFC 8693](https://www.rfc-editor.org/rfc/rfc8693), [Authorization Server Metadata RFC 8414](https://www.rfc-editor.org/rfc/rfc8414), [Protected Resource Metadata RFC 9728](https://www.rfc-editor.org/rfc/rfc9728) e [Revogação RFC 7009](https://www.rfc-editor.org/rfc/rfc7009). Não se presume suporte da biblioteca sem ensaio de compatibilidade.
