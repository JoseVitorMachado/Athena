# Propostas e decisões pendentes
Fonte: Arquitetura v1.0. `x-proposta` significa escolha de forma/rota, não regra aprovada. `x-decidir` é questão de negócio/segurança ainda aberta; nenhuma validação sintática resolve isso. Não implementar permissões indefinidas como autorização automática.
## Dez pontos do pedido
1. Recuperação contraditória (§7.1): credencial opaca, sem formato nem validade fixados no contrato.
2. Rotas públicas adicionais do Gateway: propostas no README.
3. Resolver/atribuir pendências: política indefinida; negar até decisão, piso Leitor não concede escrita.
4. Estado de lote: string aberta com exemplo PREPARADO; não inventar enum completo.
5. Cobertura: indicadores indefinidos, mapa vazio enquanto não definidos.
6. CSRF: proposto X-CSRF-Token; confirmar entrega na renovação/início de sessão.
7. Registro dinâmico OAuth/MCP: sem endpoint ou anúncio de suporte até decisão.
8. Autorização de conectores: fluxo indefinido.
9. Idempotência na sincronização: proposta obrigatória; sem If-Match adicional.
10. Ciclo/duplicação: proposta 409 RELACAO_CICLICA/RELACAO_DUPLICADA; autorrelação/entre projetos 400.
## Escolhas de representação
Campos de payload, nomes de UUIDs, coleções de acompanhamento, títulos, ordenação e formatos de conteúdo são propostas de contrato onde o livro não fornece esquema físico. Rotas com `x-proposta` identificam criações/propostas específicas; o catálogo de ordenação limita-se a atualizadoEm e id por padrão. Epicos/documentos seguem papel Editor do inventário, mas §7.4 reserva organização a Mantenedor: confirmar a fronteira. Cookie proposto `athenaRefresh`, Path=/api/v1/autenticacao. Hash de conteúdo/ação proposto SHA-256 hexadecimal; canonicalização exige decisão. Nomes OAuth padronizados são preservados em inglês/snake_case, exceção necessária aos campos de negócio camelCase pt-BR. Origem `.example.invalid` é ilustrativa.
## Pendências por operação
| Contrato | Operação | Questão |
|---|---|---|
| requisitos | `GET /api/v1/projetos/{projetoId}/requisitos` | Qual catálogo de tipos de requisito? |
| requisitos | `POST /api/v1/projetos/{projetoId}/requisitos` | Qual catálogo de tipos de requisito? |
| requisitos | `POST /api/v1/projetos/{projetoId}/requisitos/{requisitoId}/confirmacoes-publicacao` | Definir prova de interação em tela autenticada, canonicalização do hash e transporte para agente. Token delegado sozinho NÃO pode emitir comprovante. |
| rastreabilidade | `POST /api/v1/projetos/{projetoId}/relacoes` | Confirmar 409 para ciclo/duplicação e 400 para autorrelação/relação entre projetos. |
| rastreabilidade | `PATCH /api/v1/projetos/{projetoId}/relacoes/{relacaoId}` | Confirmar status dos conflitos de relações. |
| rastreabilidade | `GET /api/v1/projetos/{projetoId}/cobertura` | Quais indicadores e fórmulas compõem a cobertura? |
| rastreabilidade | `POST /api/v1/projetos/{projetoId}/mapa/reconstrucoes` | Qual identidade de serviço pode iniciar reconstrução? |
| revisoes | `GET /api/v1/projetos/{projetoId}/lotes` | Quais são os estados do lote além de PREPARADO e a nomenclatura da ativação? |
| revisoes | `GET /api/v1/projetos/{projetoId}/lotes/{loteId}` | Quais são os valores finais do enum de estado do lote? |
| revisoes | `PATCH /api/v1/projetos/{projetoId}/lotes/{loteId}/pendencias/{pendenciaId}` | Quem pode resolver ou atribuir pendências? x-papel-minimo Leitor é apenas piso autenticado, NÃO autorização de escrita; operação bloqueada até definir política. |
| revisoes | `POST /api/v1/projetos/{projetoId}/lotes/{loteId}/confirmacoes-decisao` | Definir prova de interação em tela autenticada, canonicalização do hash e transporte para agente. Token delegado sozinho NÃO pode emitir comprovante. |
| projetos-acesso | `POST /api/v1/autenticacao/cadastro` | Qual política de senha e validação de e-mail? |
| projetos-acesso | `POST /api/v1/autenticacao/recuperacao` | Recuperação: código 6 dígitos/10 min ou token de alta entropia/15 min? Não fixar formato de credencial. |
| projetos-acesso | `POST /api/v1/autenticacao/recuperacao/conclusao` | Recuperação: código 6 dígitos/10 min ou token de alta entropia/15 min? Não fixar formato de credencial. |
| projetos-acesso | `POST /api/v1/autenticacao/renovacao` | Confirmar nome X-CSRF-Token e mecanismo de entrega/renovação do token CSRF. |
| projetos-acesso | `POST /api/v1/autenticacao/logout` | Confirmar nome X-CSRF-Token e mecanismo de entrega/renovação do token CSRF. |
| projetos-acesso | `POST /api/v1/workspaces` | Quem pode criar workspace/projeto no workspace? Exige política própria; papel Leitor é piso, não concessão. |
| projetos-acesso | `PATCH /api/v1/workspaces/{workspaceId}` | Qual permissão de organização para workspace, cuja propriedade é independente dos projetos? |
| projetos-acesso | `POST /api/v1/projetos` | Quem pode criar workspace/projeto no workspace? Exige política própria; papel Leitor é piso, não concessão. |
| projetos-acesso | `GET /api/v1/workspaces/{workspaceId}/membros` | Definir leitura de membros no workspace independentemente dos projetos. |
| projetos-acesso | `POST /api/v1/workspaces/{workspaceId}/membros` | Definir papéis/permissões dos membros do workspace: livro detalha papéis por projeto. |
| projetos-acesso | `PATCH /api/v1/workspaces/{workspaceId}/membros/{membroId}` | Qual política para membros do workspace? |
| projetos-acesso | `DELETE /api/v1/workspaces/{workspaceId}/membros/{membroId}` | Qual política para membros do workspace? |
| projetos-acesso | `POST /api/v1/projetos/{projetoId}/arquivamentos` | Arquivamento assíncrono exige recurso de acompanhamento? 201 só após quiescência confirmada. |
| projetos-acesso | `POST /api/v1/projetos/{projetoId}/restauracoes` | Arquivamento assíncrono exige recurso de acompanhamento? 201 só após quiescência confirmada. |
| projetos-acesso | `GET /api/v1/projetos/{projetoId}/exclusoes/{exclusaoId}` | Como autorizar acompanhamento do Owner quando o acesso ao projeto estiver bloqueado por exclusão? |
| projetos-acesso | `GET /oauth2/jwks` | Registro dinâmico OAuth/MCP será suportado? Não anunciar registration_endpoint sem decisão. |
| projetos-acesso | `GET /.well-known/oauth-authorization-server` | Registro dinâmico OAuth/MCP será suportado? Não anunciar registration_endpoint sem decisão. |
| projetos-acesso | `GET /.well-known/oauth-protected-resource` | Registro dinâmico OAuth/MCP será suportado? Não anunciar registration_endpoint sem decisão. |
| projetos-acesso | `GET /oauth2/authorize` | Como a sessão autenticada chega ao fluxo authorize mantendo JWT só em memória? Registro dinâmico de clientes indefinido. |
| projetos-acesso | `POST /oauth2/token` | Confirmar autenticação dos clientes e interoperabilidade da troca nas bibliotecas; registro dinâmico não definido. |
| projetos-acesso | `POST /oauth2/revoke` | Confirmar método de autenticação de clientes públicos e confidenciais na revogação. |
| integracoes | `POST /api/v1/projetos/{projetoId}/conexoes` | Como autorizar conectores Google/OpenProject e quais operações são suportadas? |
| integracoes | `PATCH /api/v1/projetos/{projetoId}/conexoes/{conexaoId}` | Qual fluxo de autorização dos conectores? |
| integracoes | `POST /api/v1/projetos/{projetoId}/vinculos` | Como autorizar conectores Google/OpenProject e quais operações são suportadas? |
| integracoes | `POST /api/v1/projetos/{projetoId}/sincronizacoes` | Confirmar Idempotency-Key obrigatória na sincronização e catálogo completo de estados. |
| integracoes | `GET /api/v1/projetos/{projetoId}/sincronizacoes` | Qual catálogo completo de estados da sincronização? |
| integracoes | `POST /api/v1/projetos/{projetoId}/sincronizacoes/{sincronizacaoId}/resolucoes` | Quais comandos e efeitos são permitidos na resolução humana de conflito/resultado desconhecido? |
| integracoes | `POST /api/v1/projetos/{projetoId}/tarefas-externas` | Quais campos OpenProject são obrigatórios e qual recurso acompanha criação de tarefa externa? |
| impacto-mcp | `POST /api/v1/projetos/{projetoId}/analises` | Qual protocolo para agente registrar resultado da análise, estados e timeout sem agente conectado? |
| impacto-mcp | `GET /api/v1/projetos/{projetoId}/analises/{analiseId}` | Definir catálogo de status da análise e estadoValidacao/tipo de sugestão. |
| impacto-mcp | `POST /api/v1/projetos/{projetoId}/analises/{analiseId}/sugestoes/{sugestaoId}/avaliacoes` | Quais valores/efeitos da avaliação? Aceite cria relação na mesma chamada ou exige criação explícita no domínio? |
| impacto-mcp | `GET /api/v1/projetos/{projetoId}/indice` | Quais situações do índice e unidade final do atraso? |
| impacto-mcp | `POST /api/v1/projetos/{projetoId}/analises/{analiseId}/sugestoes` | Confirmar fluxo/identificador para registrar sugestões do agente e papel de Repórter versus Editor. |
| bff-gestao | `GET /api/v1/gestao/projetos/{projetoId}/requisitos` | Qual catálogo de tipos de requisito? |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/requisitos` | Qual catálogo de tipos de requisito? |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/requisitos/{requisitoId}/confirmacoes-publicacao` | Definir prova de interação em tela autenticada, canonicalização do hash e transporte para agente. Token delegado sozinho NÃO pode emitir comprovante. |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/relacoes` | Confirmar 409 para ciclo/duplicação e 400 para autorrelação/relação entre projetos. |
| bff-gestao | `PATCH /api/v1/gestao/projetos/{projetoId}/relacoes/{relacaoId}` | Confirmar status dos conflitos de relações. |
| bff-gestao | `GET /api/v1/gestao/projetos/{projetoId}/revisoes` | Quais são os estados do lote além de PREPARADO e a nomenclatura da ativação? |
| bff-gestao | `GET /api/v1/gestao/projetos/{projetoId}/revisoes/{loteId}` | Quais são os valores finais do enum de estado do lote? |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/revisoes/{loteId}/confirmacoes-decisao` | Definir prova de interação em tela autenticada, canonicalização do hash e transporte para agente. Token delegado sozinho NÃO pode emitir comprovante. |
| bff-gestao | `POST /api/v1/gestao/workspaces` | Quem pode criar workspace/projeto no workspace? Exige política própria; papel Leitor é piso, não concessão. |
| bff-gestao | `PATCH /api/v1/gestao/workspaces/{workspaceId}` | Qual permissão de organização para workspace, cuja propriedade é independente dos projetos? |
| bff-gestao | `POST /api/v1/gestao/projetos` | Quem pode criar workspace/projeto no workspace? Exige política própria; papel Leitor é piso, não concessão. |
| bff-gestao | `GET /api/v1/gestao/workspaces/{workspaceId}/membros` | Definir leitura de membros no workspace independentemente dos projetos. |
| bff-gestao | `POST /api/v1/gestao/workspaces/{workspaceId}/membros` | Definir papéis/permissões dos membros do workspace: livro detalha papéis por projeto. |
| bff-gestao | `PATCH /api/v1/gestao/workspaces/{workspaceId}/membros/{membroId}` | Qual política para membros do workspace? |
| bff-gestao | `DELETE /api/v1/gestao/workspaces/{workspaceId}/membros/{membroId}` | Qual política para membros do workspace? |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/arquivamentos` | Arquivamento assíncrono exige recurso de acompanhamento? 201 só após quiescência confirmada. |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/restauracoes` | Arquivamento assíncrono exige recurso de acompanhamento? 201 só após quiescência confirmada. |
| bff-gestao | `GET /api/v1/gestao/projetos/{projetoId}/exclusoes/{exclusaoId}` | Como autorizar acompanhamento do Owner quando o acesso ao projeto estiver bloqueado por exclusão? |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/conexoes` | Como autorizar conectores Google/OpenProject e quais operações são suportadas? |
| bff-gestao | `PATCH /api/v1/gestao/projetos/{projetoId}/conexoes/{conexaoId}` | Qual fluxo de autorização dos conectores? |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/vinculos` | Como autorizar conectores Google/OpenProject e quais operações são suportadas? |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/sincronizacoes` | Confirmar Idempotency-Key obrigatória na sincronização e catálogo completo de estados. |
| bff-gestao | `GET /api/v1/gestao/projetos/{projetoId}/sincronizacoes` | Qual catálogo completo de estados da sincronização? |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/sincronizacoes/{sincronizacaoId}/resolucoes` | Quais comandos e efeitos são permitidos na resolução humana de conflito/resultado desconhecido? |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/tarefas-externas` | Quais campos OpenProject são obrigatórios e qual recurso acompanha criação de tarefa externa? |

## Rotas e formas propostas por operação
| Contrato | Operação |
|---|---|
| requisitos | `POST /api/v1/projetos/{projetoId}/documentos` |
| requisitos | `POST /api/v1/projetos/{projetoId}/epicos` |
| requisitos | `POST /api/v1/projetos/{projetoId}/requisitos` |
| requisitos | `POST /api/v1/projetos/{projetoId}/baselines` |
| requisitos | `POST /api/v1/projetos/{projetoId}/requisitos/{requisitoId}/confirmacoes-publicacao` |
| rastreabilidade | `POST /api/v1/projetos/{projetoId}/relacoes` |
| rastreabilidade | `PATCH /api/v1/projetos/{projetoId}/relacoes/{relacaoId}` |
| revisoes | `POST /api/v1/projetos/{projetoId}/lotes/{loteId}/confirmacoes-decisao` |
| projetos-acesso | `POST /api/v1/autenticacao/cadastro` |
| projetos-acesso | `POST /api/v1/autenticacao/login` |
| projetos-acesso | `POST /api/v1/autenticacao/recuperacao` |
| projetos-acesso | `POST /api/v1/autenticacao/recuperacao/conclusao` |
| projetos-acesso | `POST /api/v1/autenticacao/renovacao` |
| projetos-acesso | `POST /api/v1/autenticacao/logout` |
| projetos-acesso | `POST /api/v1/workspaces` |
| projetos-acesso | `POST /api/v1/projetos` |
| projetos-acesso | `POST /api/v1/workspaces/{workspaceId}/membros` |
| projetos-acesso | `PATCH /api/v1/workspaces/{workspaceId}/membros/{membroId}` |
| projetos-acesso | `DELETE /api/v1/workspaces/{workspaceId}/membros/{membroId}` |
| projetos-acesso | `POST /api/v1/convites/{conviteId}/aceite` |
| projetos-acesso | `POST /api/v1/transferencias-propriedade/{transferenciaId}/aceite` |
| projetos-acesso | `GET /oauth2/jwks` |
| projetos-acesso | `GET /.well-known/oauth-authorization-server` |
| projetos-acesso | `GET /.well-known/oauth-protected-resource` |
| projetos-acesso | `GET /oauth2/authorize` |
| projetos-acesso | `POST /oauth2/token` |
| projetos-acesso | `POST /oauth2/revoke` |
| integracoes | `POST /api/v1/projetos/{projetoId}/conexoes` |
| integracoes | `POST /api/v1/projetos/{projetoId}/vinculos` |
| integracoes | `POST /api/v1/projetos/{projetoId}/sincronizacoes` |
| integracoes | `POST /api/v1/projetos/{projetoId}/sincronizacoes/{sincronizacaoId}/resolucoes` |
| integracoes | `POST /api/v1/projetos/{projetoId}/tarefas-externas` |
| impacto-mcp | `POST /api/v1/projetos/{projetoId}/analises` |
| impacto-mcp | `POST /api/v1/projetos/{projetoId}/analises/{analiseId}/sugestoes/{sugestaoId}/avaliacoes` |
| impacto-mcp | `POST /api/v1/projetos/{projetoId}/contexto/buscas` |
| impacto-mcp | `GET /api/v1/projetos/{projetoId}/indice` |
| impacto-mcp | `POST /api/v1/projetos/{projetoId}/reindexacoes` |
| impacto-mcp | `POST /api/v1/projetos/{projetoId}/analises/{analiseId}/sugestoes` |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/requisitos` |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/requisitos/{requisitoId}/confirmacoes-publicacao` |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/relacoes` |
| bff-gestao | `PATCH /api/v1/gestao/projetos/{projetoId}/relacoes/{relacaoId}` |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/revisoes/{loteId}/confirmacoes-decisao` |
| bff-gestao | `POST /api/v1/gestao/workspaces` |
| bff-gestao | `POST /api/v1/gestao/projetos` |
| bff-gestao | `POST /api/v1/gestao/workspaces/{workspaceId}/membros` |
| bff-gestao | `PATCH /api/v1/gestao/workspaces/{workspaceId}/membros/{membroId}` |
| bff-gestao | `DELETE /api/v1/gestao/workspaces/{workspaceId}/membros/{membroId}` |
| bff-gestao | `POST /api/v1/gestao/convites/{conviteId}/aceite` |
| bff-gestao | `POST /api/v1/gestao/transferencias-propriedade/{transferenciaId}/aceite` |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/conexoes` |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/vinculos` |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/sincronizacoes` |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/sincronizacoes/{sincronizacaoId}/resolucoes` |
| bff-gestao | `POST /api/v1/gestao/projetos/{projetoId}/tarefas-externas` |

## Representações propostas em todos os endpoints

Todas as operações carregam `x-proposta: true` e `x-motivo-proposta` para os nomes/formatos de payload e o conjunto de ordenação que o livro não especifica completamente. As regras do livro não ficam opcionais por essa marca. O inventário completo está na matriz; a tabela anterior destaca as propostas adicionais específicas.

O GET Gestão de requisito usa o ETag do requisito. Ações de candidata obtêm ETag em GET candidata. Histórico do Livro restringe estadoVersao a PUBLICADA também no schema, e não somente na descrição.

Códigos de conflito adicionais propostos nos exemplos: SUBMISSAO_ATIVA, CANDIDATA_FORA_DE_RASCUNHO, LOTE_JA_DECIDIDO, LOTE_PREPARADO, PENDENCIAS_BLOQUEANTES, CANDIDATA_NAO_APROVADA, VERSAO_DIVERGENTE. A semântica vem do livro; os nomes são escolhas de contrato.
