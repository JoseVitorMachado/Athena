# Ferramentas MCP — ATHENA 1.0.0

Catálogo de contrato proposto para /mcp (Streamable HTTP), separado de OpenAPI (§§6.1, 6.11 e 7.5). Nomes correspondem aos operationIds de domínio. As ferramentas não são uma implementação MCP nem comprovam compatibilidade dos clientes.

Cada chamada revalida usuário, autorização independente da interface, projeto delegado, escopo e papel atual. Troca o token para aud do serviço proprietário, preservando sujeito/projeto/delegação e reduzindo escopos. Não repassa token MCP bruto. Revogação bloqueia chamadas e entregas posteriores inclusive em stream já aberto. Não existe tool de autoemitir confirmação humana. Cada emissão só ocorre na tela ATHENA autenticada e no serviço responsável.

Escopos são independentes: read consulta; write sugestões/conteúdo/relações/submissões; approve decide; publish publica; admin administra; delete-project exclui fisicamente e exige Owner. Um escopo não implica outro. Decidir e publicar exigem corpo.comprovanteHumano emitido em tela, uso único, cinco minutos e hash da ação, consumido na transação do efeito. Revalidar versão, papel e acesso. Não aceitar declaração do agente como prova.

O cliente externo gera texto. criarAnalise prepara contexto e registra operação; criarSugestaoAnalise propõe o canal de retorno de saída não confiável e depende da decisão indicada. Similaridade não é probabilidade. Sugestão não cria relação oficial sem ação autorizada. Políticas x-decidir permanecem pendentes e não concedem permissão. Em especial editarPendencia é bloqueada até definir responsável.

Parâmetros de caminho e consulta aparecem no nível superior; payload em corpo; If-Match em ifMatch; Idempotency-Key em chaveIdempotencia. Campos padrão OAuth não são tools. O esquema expandido é autocontido, draft 2020-12. O projeto das tools é sempre projetoId explícito; conta/workspace/autenticação e endpoints exclusivos internos ficam fora da delegação de projeto. Roteamento MCP é feito pelo servidor; x-rota é destino interno, nunca URL para repassar token MCP.

Erros: devolver falha MCP estruturada com codigo, traceId e status de domínio, sem marcar efeito confirmado após timeout. Idempotência recupera a operação já existente. Comunicação SAGA/eventos pertence a /docs/mensageria, fora desta entrega REST. Campos propostos e pendências estão em PROPOSTAS.md.

## Índice

| Tool | Domínio | Papel mínimo | Escopo | Operação de domínio |
|---|---|---|---|---|
| listarDocumentos | requisitos | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/documentos |
| criarDocumento | requisitos | Editor | athena:write | POST /api/v1/projetos/{projetoId}/documentos |
| obterDocumento | requisitos | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/documentos/{documentoId} |
| editarDocumento | requisitos | Editor | athena:write | PATCH /api/v1/projetos/{projetoId}/documentos/{documentoId} |
| listarEpicos | requisitos | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/epicos |
| criarEpico | requisitos | Editor | athena:write | POST /api/v1/projetos/{projetoId}/epicos |
| editarEpico | requisitos | Editor | athena:write | PATCH /api/v1/projetos/{projetoId}/epicos/{epicoId} |
| listarRequisitos | requisitos | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/requisitos |
| criarRequisito | requisitos | Editor | athena:write | POST /api/v1/projetos/{projetoId}/requisitos |
| obterRequisito | requisitos | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/requisitos/{requisitoId} |
| editarRequisito | requisitos | Editor | athena:write | PATCH /api/v1/projetos/{projetoId}/requisitos/{requisitoId} |
| obterCandidata | requisitos | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/requisitos/{requisitoId}/candidata |
| editarCandidata | requisitos | Editor | athena:write | PATCH /api/v1/projetos/{projetoId}/requisitos/{requisitoId}/candidata |
| listarVersoes | requisitos | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/requisitos/{requisitoId}/versoes |
| obterVersao | requisitos | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/requisitos/{requisitoId}/versoes/{versaoId} |
| listarSubmissoes | requisitos | Editor | athena:read | GET /api/v1/projetos/{projetoId}/requisitos/{requisitoId}/submissoes |
| criarSubmissao | requisitos | Editor | athena:write | POST /api/v1/projetos/{projetoId}/requisitos/{requisitoId}/submissoes |
| obterSubmissao | requisitos | Editor | athena:read | GET /api/v1/projetos/{projetoId}/requisitos/{requisitoId}/submissoes/{submissaoId} |
| listarPublicacoes | requisitos | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/requisitos/{requisitoId}/publicacoes |
| criarPublicacao | requisitos | Aprovador | athena:publish | POST /api/v1/projetos/{projetoId}/requisitos/{requisitoId}/publicacoes |
| obterPublicacao | requisitos | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/requisitos/{requisitoId}/publicacoes/{publicacaoId} |
| listarHistoricoRequisito | requisitos | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/requisitos/{requisitoId}/historico |
| criarCancelamento | requisitos | Editor | athena:write | POST /api/v1/projetos/{projetoId}/requisitos/{requisitoId}/cancelamentos |
| criarReativacao | requisitos | Editor | athena:write | POST /api/v1/projetos/{projetoId}/requisitos/{requisitoId}/reativacoes |
| listarComentarios | requisitos | Repórter | athena:read | GET /api/v1/projetos/{projetoId}/requisitos/{requisitoId}/comentarios |
| criarComentario | requisitos | Repórter | athena:write | POST /api/v1/projetos/{projetoId}/requisitos/{requisitoId}/comentarios |
| editarComentario | requisitos | Repórter | athena:write | PATCH /api/v1/projetos/{projetoId}/requisitos/{requisitoId}/comentarios/{comentarioId} |
| listarBaselines | requisitos | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/baselines |
| criarBaseline | requisitos | Editor | athena:write | POST /api/v1/projetos/{projetoId}/baselines |
| obterBaseline | requisitos | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/baselines/{baselineId} |
| listarRelacoes | rastreabilidade | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/relacoes |
| criarRelacao | rastreabilidade | Editor | athena:write | POST /api/v1/projetos/{projetoId}/relacoes |
| obterRelacao | rastreabilidade | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/relacoes/{relacaoId} |
| editarRelacao | rastreabilidade | Editor | athena:write | PATCH /api/v1/projetos/{projetoId}/relacoes/{relacaoId} |
| removerRelacao | rastreabilidade | Editor | athena:write | DELETE /api/v1/projetos/{projetoId}/relacoes/{relacaoId} |
| obterMapa | rastreabilidade | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/mapa |
| obterCobertura | rastreabilidade | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/cobertura |
| obterRetrato | rastreabilidade | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/retratos/{retratoId} |
| listarLotes | revisoes | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/lotes |
| obterLote | revisoes | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/lotes/{loteId} |
| listarPendencias | revisoes | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/lotes/{loteId}/pendencias |
| editarPendencia | revisoes | Leitor | athena:write | PATCH /api/v1/projetos/{projetoId}/lotes/{loteId}/pendencias/{pendenciaId} |
| criarDecisao | revisoes | Aprovador | athena:approve | POST /api/v1/projetos/{projetoId}/lotes/{loteId}/decisoes |
| listarDecisoes | revisoes | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/lotes/{loteId}/decisoes |
| obterProjeto | projetos-acesso | Leitor | athena:read | GET /api/v1/projetos/{projetoId} |
| editarProjeto | projetos-acesso | Mantenedor | athena:admin | PATCH /api/v1/projetos/{projetoId} |
| criarArquivamento | projetos-acesso | Mantenedor | athena:admin | POST /api/v1/projetos/{projetoId}/arquivamentos |
| criarRestauracao | projetos-acesso | Mantenedor | athena:admin | POST /api/v1/projetos/{projetoId}/restauracoes |
| criarExclusaoProjeto | projetos-acesso | Owner | athena:delete-project | POST /api/v1/projetos/{projetoId}/exclusoes |
| obterExclusaoProjeto | projetos-acesso | Owner | athena:read | GET /api/v1/projetos/{projetoId}/exclusoes/{exclusaoId} |
| listarMembrosProjeto | projetos-acesso | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/membros |
| editarMembroProjeto | projetos-acesso | Mantenedor | athena:admin | PATCH /api/v1/projetos/{projetoId}/membros/{membroId} |
| removerMembroProjeto | projetos-acesso | Mantenedor | athena:admin | DELETE /api/v1/projetos/{projetoId}/membros/{membroId} |
| listarHistoricoMembros | projetos-acesso | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/historico-membros |
| listarConvites | projetos-acesso | Mantenedor | athena:read | GET /api/v1/projetos/{projetoId}/convites |
| criarConvite | projetos-acesso | Mantenedor | athena:admin | POST /api/v1/projetos/{projetoId}/convites |
| removerConvite | projetos-acesso | Mantenedor | athena:admin | DELETE /api/v1/projetos/{projetoId}/convites/{conviteId} |
| criarTransferenciaPropriedade | projetos-acesso | Owner | athena:admin | POST /api/v1/projetos/{projetoId}/transferencias-propriedade |
| listarConexoes | integracoes | Mantenedor | athena:read | GET /api/v1/projetos/{projetoId}/conexoes |
| criarConexao | integracoes | Mantenedor | athena:admin | POST /api/v1/projetos/{projetoId}/conexoes |
| obterConexao | integracoes | Mantenedor | athena:read | GET /api/v1/projetos/{projetoId}/conexoes/{conexaoId} |
| editarConexao | integracoes | Mantenedor | athena:admin | PATCH /api/v1/projetos/{projetoId}/conexoes/{conexaoId} |
| removerConexao | integracoes | Mantenedor | athena:admin | DELETE /api/v1/projetos/{projetoId}/conexoes/{conexaoId} |
| listarVinculos | integracoes | Editor | athena:read | GET /api/v1/projetos/{projetoId}/vinculos |
| criarVinculo | integracoes | Editor | athena:write | POST /api/v1/projetos/{projetoId}/vinculos |
| removerVinculo | integracoes | Editor | athena:write | DELETE /api/v1/projetos/{projetoId}/vinculos/{vinculoId} |
| criarSincronizacao | integracoes | Editor | athena:write | POST /api/v1/projetos/{projetoId}/sincronizacoes |
| listarSincronizacoes | integracoes | Editor | athena:read | GET /api/v1/projetos/{projetoId}/sincronizacoes |
| obterSincronizacao | integracoes | Editor | athena:read | GET /api/v1/projetos/{projetoId}/sincronizacoes/{sincronizacaoId} |
| criarResolucaoSincronizacao | integracoes | Editor | athena:write | POST /api/v1/projetos/{projetoId}/sincronizacoes/{sincronizacaoId}/resolucoes |
| criarTarefaExterna | integracoes | Editor | athena:write | POST /api/v1/projetos/{projetoId}/tarefas-externas |
| criarAnalise | impacto-mcp | Leitor | athena:read | POST /api/v1/projetos/{projetoId}/analises |
| listarAnalises | impacto-mcp | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/analises |
| obterAnalise | impacto-mcp | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/analises/{analiseId} |
| criarAvaliacaoSugestao | impacto-mcp | Editor | athena:write | POST /api/v1/projetos/{projetoId}/analises/{analiseId}/sugestoes/{sugestaoId}/avaliacoes |
| buscarContexto | impacto-mcp | Leitor | athena:read | POST /api/v1/projetos/{projetoId}/contexto/buscas |
| obterIndice | impacto-mcp | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/indice |
| listarOperacoesImpacto | impacto-mcp | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/operacoes |
| obterOperacaoImpacto | impacto-mcp | Leitor | athena:read | GET /api/v1/projetos/{projetoId}/operacoes/{operacaoId} |
| criarReindexacao | impacto-mcp | Mantenedor | athena:admin | POST /api/v1/projetos/{projetoId}/reindexacoes |
| criarSugestaoAnalise | impacto-mcp | Editor | athena:write | POST /api/v1/projetos/{projetoId}/analises/{analiseId}/sugestoes |

## listarDocumentos

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    }
  },
  "required": [
    "projetoId"
  ],
  "additionalProperties": false
}
```

## criarDocumento

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "titulo": {
          "type": "string",
          "example": "Especificação funcional"
        },
        "descricao": {
          "type": "string",
          "example": "Escopo do produto"
        }
      },
      "additionalProperties": false,
      "required": [
        "titulo"
      ],
      "example": {
        "titulo": "Especificação funcional"
      }
    }
  },
  "required": [
    "projetoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## obterDocumento

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "documentoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como documentoId."
    }
  },
  "required": [
    "projetoId",
    "documentoId"
  ],
  "additionalProperties": false
}
```

## editarDocumento

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

§§5.2, 6.3, 7.4. PATCH de requisito altera somente responsavelId, epicoId e tipo; metadados não contornam bloqueios. Organização de documentos/épicos conforme inventário; confirmar fronteira com Mantenedor.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "documentoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como documentoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "titulo": {
          "type": "string",
          "example": "Especificação revisada"
        },
        "descricao": {
          "type": "string",
          "example": "Escopo atualizado"
        }
      },
      "additionalProperties": false,
      "minProperties": 1,
      "example": {
        "titulo": "Documentação"
      }
    }
  },
  "required": [
    "projetoId",
    "documentoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## listarEpicos

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    }
  },
  "required": [
    "projetoId"
  ],
  "additionalProperties": false
}
```

## criarEpico

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "titulo": {
          "type": "string",
          "example": "Documentação"
        },
        "descricao": {
          "type": "string",
          "example": "Organizar requisitos"
        }
      },
      "additionalProperties": false,
      "required": [
        "titulo"
      ],
      "example": {
        "titulo": "Documentação"
      }
    }
  },
  "required": [
    "projetoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## editarEpico

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

§§5.2, 6.3, 7.4. PATCH de requisito altera somente responsavelId, epicoId e tipo; metadados não contornam bloqueios. Organização de documentos/épicos conforme inventário; confirmar fronteira com Mantenedor.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "epicoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como epicoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "titulo": {
          "type": "string",
          "example": "Documentação"
        },
        "descricao": {
          "type": "string",
          "example": "Organizar requisitos"
        }
      },
      "additionalProperties": false,
      "minProperties": 1,
      "example": {
        "titulo": "Documentação"
      }
    }
  },
  "required": [
    "projetoId",
    "epicoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## listarRequisitos

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

Decisão pendente: Qual catálogo de tipos de requisito?

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    },
    "q": {
      "type": "string",
      "example": "exportar",
      "description": "Filtro autorizado (§6.2); valor inválido retorna 400. Encaminhar em query como q."
    },
    "situacao": {
      "type": "string",
      "example": "ATIVO",
      "enum": [
        "ATIVO",
        "CANCELADO"
      ],
      "default": "ATIVO",
      "description": "Filtro autorizado (§6.2); valor inválido retorna 400. Encaminhar em query como situacao."
    },
    "estadoVersao": {
      "type": "string",
      "example": "RASCUNHO",
      "enum": [
        "RASCUNHO",
        "EM_SUBMISSAO",
        "AGUARDANDO_REVISAO",
        "APROVADA",
        "PUBLICADA"
      ],
      "description": "Filtro autorizado (§6.2); valor inválido retorna 400. Encaminhar em query como estadoVersao."
    },
    "tipo": {
      "type": "string",
      "example": "FUNCIONAL",
      "description": "Filtro autorizado (§6.2); valor inválido retorna 400. Encaminhar em query como tipo."
    },
    "epicoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "Filtro autorizado (§6.2); valor inválido retorna 400. Encaminhar em query como epicoId."
    },
    "responsavelId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "Filtro autorizado (§6.2); valor inválido retorna 400. Encaminhar em query como responsavelId."
    }
  },
  "required": [
    "projetoId"
  ],
  "additionalProperties": false
}
```

## criarRequisito

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

Decisão pendente: Qual catálogo de tipos de requisito?

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "documentoId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        },
        "epicoId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        },
        "responsavelId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        },
        "tipo": {
          "type": "string",
          "example": "FUNCIONAL",
          "description": "Catálogo não definido: x-decidir da operação."
        },
        "titulo": {
          "type": "string",
          "example": "Exportar documento de requisitos."
        },
        "conteudo": {
          "type": "string",
          "example": "O sistema deve permitir exportar o documento."
        },
        "criteriosAceite": {
          "type": "array",
          "items": {
            "type": "string",
            "example": "Exportação preserva requisitos."
          },
          "example": [
            "Exportação preserva requisitos."
          ]
        }
      },
      "additionalProperties": false,
      "required": [
        "documentoId",
        "titulo",
        "conteudo",
        "criteriosAceite"
      ],
      "example": {
        "documentoId": "11111111-1111-4111-8111-111111111111",
        "titulo": "Exportar documento de requisitos.",
        "conteudo": "O sistema deve permitir exportar o documento.",
        "criteriosAceite": [
          "Exportação preserva requisitos."
        ]
      }
    }
  },
  "required": [
    "projetoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## obterRequisito

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como requisitoId."
    }
  },
  "required": [
    "projetoId",
    "requisitoId"
  ],
  "additionalProperties": false
}
```

## editarRequisito

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

§§5.2, 6.3, 7.4. Apenas metadados responsavelId, epicoId e tipo; não altera conteúdo nem contorna bloqueios.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como requisitoId."
    },
    "ifMatch": {
      "type": "string",
      "example": "\"rev-7\"",
      "description": "§6.3. ETag forte do recurso alvo da ação. Comparação e gravação atômicas; ausente 428, divergente 412. A revisão técnica não é a versão do conteúdo. Encaminhar em header como If-Match."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "responsavelId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        },
        "epicoId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        },
        "tipo": {
          "type": "string",
          "example": "FUNCIONAL"
        }
      },
      "additionalProperties": false,
      "minProperties": 1,
      "example": {
        "responsavelId": "11111111-1111-4111-8111-111111111111"
      }
    }
  },
  "required": [
    "projetoId",
    "requisitoId",
    "ifMatch",
    "corpo"
  ],
  "additionalProperties": false
}
```

## obterCandidata

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como requisitoId."
    }
  },
  "required": [
    "projetoId",
    "requisitoId"
  ],
  "additionalProperties": false
}
```

## editarCandidata

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

§§5.2, 6.3. If-Match da candidata. EM_SUBMISSAO ou AGUARDANDO_REVISAO: 409. Editar APROVADA retorna RASCUNHO e invalida elegibilidade; PUBLICADA é imutável. Após publicação inicia outra candidata; cancelado/arquivado bloqueia escrita.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como requisitoId."
    },
    "ifMatch": {
      "type": "string",
      "example": "\"rev-7\"",
      "description": "§6.3. ETag forte do recurso alvo da ação. Comparação e gravação atômicas; ausente 428, divergente 412. A revisão técnica não é a versão do conteúdo. Encaminhar em header como If-Match."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "titulo": {
          "type": "string",
          "example": "Exportar documento de requisitos."
        },
        "conteudo": {
          "type": "string",
          "example": "O sistema deve permitir exportar o documento."
        },
        "criteriosAceite": {
          "type": "array",
          "items": {
            "type": "string",
            "example": "Exportação preserva requisitos."
          },
          "example": [
            "Exportação preserva requisitos."
          ]
        }
      },
      "additionalProperties": false,
      "minProperties": 1,
      "example": {
        "conteudo": "Conteúdo atualizado."
      }
    }
  },
  "required": [
    "projetoId",
    "requisitoId",
    "ifMatch",
    "corpo"
  ],
  "additionalProperties": false
}
```

## listarVersoes

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como requisitoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    }
  },
  "required": [
    "projetoId",
    "requisitoId"
  ],
  "additionalProperties": false
}
```

## obterVersao

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

§§5.2, 6.5–6.9. Registro histórico preservado; versão PUBLICADA e registro de publicação imutáveis.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como requisitoId."
    },
    "versaoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como versaoId."
    }
  },
  "required": [
    "projetoId",
    "requisitoId",
    "versaoId"
  ],
  "additionalProperties": false
}
```

## listarSubmissoes

Papel mínimo: **Editor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como requisitoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    }
  },
  "required": [
    "projetoId",
    "requisitoId"
  ],
  "additionalProperties": false
}
```

## criarSubmissao

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

§§5.2, 6.3, 6.5–6.9. If-Match da candidata. Apenas RASCUNHO; submissão ativa ou estado divergente retorna 409. 202 após transação local; SAGA independe da IA, conclui ao preparar revisão. Ativação do lote é posterior. Prazo inicial 90 s; compensação confirmada antes de liberar candidata.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como requisitoId."
    },
    "ifMatch": {
      "type": "string",
      "example": "\"rev-7\"",
      "description": "§6.3. ETag forte do recurso alvo da ação. Comparação e gravação atômicas; ausente 428, divergente 412. A revisão técnica não é a versão do conteúdo. Encaminhar em header como If-Match."
    },
    "chaveIdempotencia": {
      "type": "string",
      "example": "tentativa-20261021-001",
      "minLength": 1,
      "description": "§6.3. Ordem: autenticar; verificar acesso atual; consultar chave no escopo usuário/projeto/operação e comparar pedido; repetição idêntica recupera resultado/acompanhamento por 24 h sem reavaliar If-Match; pedido divergente retorna 409. Só operação nova valida presença/valor de If-Match, quando exigido. Reserva, efeito e resultado são transacionais. Encaminhar em header como Idempotency-Key."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "versaoId": {
          "type": "string",
          "example": "33333333-3333-4333-8333-333333333333",
          "format": "uuid"
        }
      },
      "additionalProperties": false,
      "required": [
        "versaoId"
      ],
      "example": {
        "versaoId": "33333333-3333-4333-8333-333333333333"
      }
    }
  },
  "required": [
    "projetoId",
    "requisitoId",
    "ifMatch",
    "chaveIdempotencia",
    "corpo"
  ],
  "additionalProperties": false
}
```

## obterSubmissao

Papel mínimo: **Editor**; escopo **athena:read**; projeto **projetoId**.

§§5.2, 6.5–6.9. Acompanhamento persistido; etapa só durante fase correspondente. Compensação pendente não libera candidata.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como requisitoId."
    },
    "submissaoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como submissaoId."
    }
  },
  "required": [
    "projetoId",
    "requisitoId",
    "submissaoId"
  ],
  "additionalProperties": false
}
```

## listarPublicacoes

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como requisitoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    }
  },
  "required": [
    "projetoId",
    "requisitoId"
  ],
  "additionalProperties": false
}
```

## criarPublicacao

Papel mínimo: **Aprovador**; escopo **athena:publish**; projeto **projetoId**.

§§4.3, 6.3, 6.9, 7.5. If-Match da candidata. Exige APROVADA já aplicada em Requisitos e versão/tentativa correspondentes; divergência 409. Registro imutável vincula o retrato aprovado. MCP exige comprovante humano consumido atomicamente; publicar não é aprovar.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como requisitoId."
    },
    "ifMatch": {
      "type": "string",
      "example": "\"rev-7\"",
      "description": "§6.3. ETag forte do recurso alvo da ação. Comparação e gravação atômicas; ausente 428, divergente 412. A revisão técnica não é a versão do conteúdo. Encaminhar em header como If-Match."
    },
    "chaveIdempotencia": {
      "type": "string",
      "example": "tentativa-20261021-001",
      "minLength": 1,
      "description": "§6.3. Ordem: autenticar; verificar acesso atual; consultar chave no escopo usuário/projeto/operação e comparar pedido; repetição idêntica recupera resultado/acompanhamento por 24 h sem reavaliar If-Match; pedido divergente retorna 409. Só operação nova valida presença/valor de If-Match, quando exigido. Reserva, efeito e resultado são transacionais. Encaminhar em header como Idempotency-Key."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "versaoId": {
          "type": "string",
          "example": "33333333-3333-4333-8333-333333333333",
          "format": "uuid"
        },
        "submissaoId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        },
        "comprovanteHumano": {
          "type": "string",
          "example": "comprovante-opaco",
          "description": "§7.5. Somente emitido pelo serviço responsável após confirmação em tela autenticada ATHENA; uso único, 5 minutos, vinculado a usuário/projeto/ação/versão e hash da ação. Validar e consumir na transação do efeito. Obrigatório apenas para decisão/publicação delegada; declaração do agente não vale como confirmação."
        }
      },
      "additionalProperties": false,
      "required": [
        "versaoId",
        "submissaoId",
        "comprovanteHumano"
      ],
      "example": {
        "versaoId": "33333333-3333-4333-8333-333333333333",
        "submissaoId": "11111111-1111-4111-8111-111111111111"
      }
    }
  },
  "required": [
    "projetoId",
    "requisitoId",
    "ifMatch",
    "chaveIdempotencia",
    "corpo"
  ],
  "additionalProperties": false
}
```

## obterPublicacao

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

§§5.2, 6.5–6.9. Registro histórico preservado; versão PUBLICADA e registro de publicação imutáveis.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como requisitoId."
    },
    "publicacaoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como publicacaoId."
    }
  },
  "required": [
    "projetoId",
    "requisitoId",
    "publicacaoId"
  ],
  "additionalProperties": false
}
```

## listarHistoricoRequisito

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como requisitoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    }
  },
  "required": [
    "projetoId",
    "requisitoId"
  ],
  "additionalProperties": false
}
```

## criarCancelamento

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

§§5.3, 6.3. If-Match do requisito. Confirmação true obrigatória; SAGA ou revisão ativa retorna 409. Preserva histórico e remove do Livro corrente.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como requisitoId."
    },
    "ifMatch": {
      "type": "string",
      "example": "\"rev-7\"",
      "description": "§6.3. ETag forte do recurso alvo da ação. Comparação e gravação atômicas; ausente 428, divergente 412. A revisão técnica não é a versão do conteúdo. Encaminhar em header como If-Match."
    },
    "chaveIdempotencia": {
      "type": "string",
      "example": "tentativa-20261021-001",
      "minLength": 1,
      "description": "§6.3. Ordem: autenticar; verificar acesso atual; consultar chave no escopo usuário/projeto/operação e comparar pedido; repetição idêntica recupera resultado/acompanhamento por 24 h sem reavaliar If-Match; pedido divergente retorna 409. Só operação nova valida presença/valor de If-Match, quando exigido. Reserva, efeito e resultado são transacionais. Encaminhar em header como Idempotency-Key."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "confirmacao": {
          "type": "boolean",
          "const": true,
          "example": true
        }
      },
      "additionalProperties": false,
      "required": [
        "confirmacao"
      ],
      "example": {
        "confirmacao": true
      }
    }
  },
  "required": [
    "projetoId",
    "requisitoId",
    "ifMatch",
    "chaveIdempotencia",
    "corpo"
  ],
  "additionalProperties": false
}
```

## criarReativacao

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

§§5.3, 6.3. If-Match do requisito. Retorna ATIVO com nova candidata RASCUNHO baseada no último conteúdo; exige nova aprovação/publicação para voltar ao Livro.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como requisitoId."
    },
    "ifMatch": {
      "type": "string",
      "example": "\"rev-7\"",
      "description": "§6.3. ETag forte do recurso alvo da ação. Comparação e gravação atômicas; ausente 428, divergente 412. A revisão técnica não é a versão do conteúdo. Encaminhar em header como If-Match."
    },
    "chaveIdempotencia": {
      "type": "string",
      "example": "tentativa-20261021-001",
      "minLength": 1,
      "description": "§6.3. Ordem: autenticar; verificar acesso atual; consultar chave no escopo usuário/projeto/operação e comparar pedido; repetição idêntica recupera resultado/acompanhamento por 24 h sem reavaliar If-Match; pedido divergente retorna 409. Só operação nova valida presença/valor de If-Match, quando exigido. Reserva, efeito e resultado são transacionais. Encaminhar em header como Idempotency-Key."
    },
    "corpo": {
      "type": "object",
      "properties": {},
      "additionalProperties": false,
      "example": {}
    }
  },
  "required": [
    "projetoId",
    "requisitoId",
    "ifMatch",
    "chaveIdempotencia",
    "corpo"
  ],
  "additionalProperties": false
}
```

## listarComentarios

Papel mínimo: **Repórter**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como requisitoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    }
  },
  "required": [
    "projetoId",
    "requisitoId"
  ],
  "additionalProperties": false
}
```

## criarComentario

Papel mínimo: **Repórter**; escopo **athena:write**; projeto **projetoId**.

§7.4. Vincula versão e menções; âncora perdida fica NAO_LOCALIZADA, nunca reassociada silenciosamente.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como requisitoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "versaoId": {
          "type": "string",
          "example": "33333333-3333-4333-8333-333333333333",
          "format": "uuid"
        },
        "texto": {
          "type": "string",
          "example": "Rever este critério."
        },
        "ancora": {
          "type": "object",
          "properties": {
            "selecao": {
              "type": "string",
              "example": "Texto selecionado"
            },
            "situacao": {
              "type": "string",
              "example": "LOCALIZADA",
              "enum": [
                "LOCALIZADA",
                "NAO_LOCALIZADA"
              ]
            }
          },
          "additionalProperties": false,
          "required": [
            "selecao",
            "situacao"
          ],
          "example": {
            "selecao": "Texto selecionado",
            "situacao": "LOCALIZADA"
          }
        },
        "mencoes": {
          "type": "array",
          "items": {
            "type": "string",
            "example": "11111111-1111-4111-8111-111111111111",
            "format": "uuid"
          },
          "example": [
            "11111111-1111-4111-8111-111111111111"
          ]
        }
      },
      "additionalProperties": false,
      "required": [
        "versaoId",
        "texto"
      ],
      "example": {
        "versaoId": "33333333-3333-4333-8333-333333333333",
        "texto": "Rever este critério."
      }
    }
  },
  "required": [
    "projetoId",
    "requisitoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## editarComentario

Papel mínimo: **Repórter**; escopo **athena:write**; projeto **projetoId**.

§7.4. Repórter edita/resolve apenas comentário próprio; Editor resolve no conteúdo que administra. Preserva histórico. Âncora NAO_LOCALIZADA não é reassociada silenciosamente.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como requisitoId."
    },
    "comentarioId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como comentarioId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "texto": {
          "type": "string",
          "example": "Critério esclarecido."
        },
        "resolvido": {
          "type": "boolean",
          "example": true
        }
      },
      "additionalProperties": false,
      "minProperties": 1,
      "example": {
        "texto": "Critério esclarecido."
      }
    }
  },
  "required": [
    "projetoId",
    "requisitoId",
    "comentarioId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## listarBaselines

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    }
  },
  "required": [
    "projetoId"
  ],
  "additionalProperties": false
}
```

## criarBaseline

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

§§5.1, 7.4. Conjunto imutável de versões e retratos autorizados.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "nome": {
          "type": "string",
          "example": "Entrega 1"
        },
        "versoes": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "requisitoId": {
                "type": "string",
                "example": "11111111-1111-4111-8111-111111111111",
                "format": "uuid"
              },
              "versaoId": {
                "type": "string",
                "example": "33333333-3333-4333-8333-333333333333",
                "format": "uuid"
              },
              "retratoId": {
                "type": "string",
                "example": "11111111-1111-4111-8111-111111111111",
                "format": "uuid"
              }
            },
            "additionalProperties": false,
            "required": [
              "requisitoId",
              "versaoId",
              "retratoId"
            ],
            "example": {
              "requisitoId": "11111111-1111-4111-8111-111111111111",
              "versaoId": "33333333-3333-4333-8333-333333333333",
              "retratoId": "11111111-1111-4111-8111-111111111111"
            }
          },
          "example": [
            {
              "requisitoId": "11111111-1111-4111-8111-111111111111",
              "versaoId": "33333333-3333-4333-8333-333333333333",
              "retratoId": "11111111-1111-4111-8111-111111111111"
            }
          ],
          "minItems": 1
        }
      },
      "additionalProperties": false,
      "required": [
        "nome",
        "versoes"
      ],
      "example": {
        "nome": "Entrega 1",
        "versoes": [
          {
            "requisitoId": "11111111-1111-4111-8111-111111111111",
            "versaoId": "33333333-3333-4333-8333-333333333333",
            "retratoId": "11111111-1111-4111-8111-111111111111"
          }
        ]
      }
    }
  },
  "required": [
    "projetoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## obterBaseline

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "baselineId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como baselineId."
    }
  },
  "required": [
    "projetoId",
    "baselineId"
  ],
  "additionalProperties": false
}
```

## listarRelacoes

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "Filtro autorizado (§6.2); valor inválido retorna 400. Encaminhar em query como requisitoId."
    },
    "tipo": {
      "type": "string",
      "example": "DEPENDE_DE",
      "enum": [
        "DEPENDE_DE",
        "BLOQUEIA",
        "RELACIONADO_A"
      ],
      "description": "Filtro autorizado (§6.2); valor inválido retorna 400. Encaminhar em query como tipo."
    }
  },
  "required": [
    "projetoId"
  ],
  "additionalProperties": false
}
```

## criarRelacao

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

§5.4. Trava transacional por projeto em toda mutação; normalizar RELACIONADO_A. Proibir autorrelação, entre projetos, duplicação e ciclos por tipo e grafo combinado: DEPENDE_DE A→B implica precedência B→A; BLOQUEIA A→B implica A→B. RELACIONADO_A não participa de ciclos. Proposta: duplicação 409 RELACAO_DUPLICADA; ciclo 409 RELACAO_CICLICA; autorrelação/entre projetos 400.

Decisão pendente: Confirmar 409 para ciclo/duplicação e 400 para autorrelação/relação entre projetos.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "origemId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        },
        "destinoId": {
          "type": "string",
          "example": "33333333-3333-4333-8333-333333333333",
          "format": "uuid"
        },
        "tipo": {
          "type": "string",
          "example": "DEPENDE_DE",
          "enum": [
            "DEPENDE_DE",
            "BLOQUEIA",
            "RELACIONADO_A"
          ]
        },
        "justificativa": {
          "type": "string",
          "example": "Dependência funcional"
        }
      },
      "additionalProperties": false,
      "required": [
        "origemId",
        "destinoId",
        "tipo"
      ],
      "example": {
        "origemId": "11111111-1111-4111-8111-111111111111",
        "destinoId": "33333333-3333-4333-8333-333333333333",
        "tipo": "DEPENDE_DE"
      }
    }
  },
  "required": [
    "projetoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## obterRelacao

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "relacaoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como relacaoId."
    }
  },
  "required": [
    "projetoId",
    "relacaoId"
  ],
  "additionalProperties": false
}
```

## editarRelacao

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

§5.4. Trava transacional por projeto em toda mutação; normalizar RELACIONADO_A. Proibir autorrelação, entre projetos, duplicação e ciclos por tipo e grafo combinado: DEPENDE_DE A→B implica precedência B→A; BLOQUEIA A→B implica A→B. RELACIONADO_A não participa de ciclos. Proposta: duplicação 409 RELACAO_DUPLICADA; ciclo 409 RELACAO_CICLICA; autorrelação/entre projetos 400.

Decisão pendente: Confirmar status dos conflitos de relações.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "relacaoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como relacaoId."
    },
    "ifMatch": {
      "type": "string",
      "example": "\"rev-7\"",
      "description": "§6.3. ETag forte do recurso alvo da ação. Comparação e gravação atômicas; ausente 428, divergente 412. A revisão técnica não é a versão do conteúdo. Encaminhar em header como If-Match."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "origemId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        },
        "destinoId": {
          "type": "string",
          "example": "33333333-3333-4333-8333-333333333333",
          "format": "uuid"
        },
        "tipo": {
          "type": "string",
          "example": "DEPENDE_DE",
          "enum": [
            "DEPENDE_DE",
            "BLOQUEIA",
            "RELACIONADO_A"
          ]
        },
        "justificativa": {
          "type": "string",
          "example": "Dependência funcional"
        }
      },
      "additionalProperties": false,
      "minProperties": 1,
      "example": {
        "justificativa": "Dependência esclarecida."
      }
    }
  },
  "required": [
    "projetoId",
    "relacaoId",
    "ifMatch",
    "corpo"
  ],
  "additionalProperties": false
}
```

## removerRelacao

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

§5.4. Trava transacional por projeto em toda mutação; normalizar RELACIONADO_A. Proibir autorrelação, entre projetos, duplicação e ciclos por tipo e grafo combinado: DEPENDE_DE A→B implica precedência B→A; BLOQUEIA A→B implica A→B. RELACIONADO_A não participa de ciclos. Proposta: duplicação 409 RELACAO_DUPLICADA; ciclo 409 RELACAO_CICLICA; autorrelação/entre projetos 400.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "relacaoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como relacaoId."
    },
    "ifMatch": {
      "type": "string",
      "example": "\"rev-7\"",
      "description": "§6.3. ETag forte do recurso alvo da ação. Comparação e gravação atômicas; ausente 428, divergente 412. A revisão técnica não é a versão do conteúdo. Encaminhar em header como If-Match."
    }
  },
  "required": [
    "projetoId",
    "relacaoId",
    "ifMatch"
  ],
  "additionalProperties": false
}
```

## obterMapa

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

§5.5. Máximo 500 nós/1.000 arestas; truncamento explícito. Reconstrução mantém geração anterior sinalizada; sem anterior retorna 503 com Retry-After. Último consumo não comprova atualização.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    }
  },
  "required": [
    "projetoId"
  ],
  "additionalProperties": false
}
```

## obterCobertura

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

Decisão pendente: Quais indicadores e fórmulas compõem a cobertura?

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    }
  },
  "required": [
    "projetoId"
  ],
  "additionalProperties": false
}
```

## obterRetrato

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

§5.4. Criado exclusivamente por mensageria; não existe POST REST. Publicado é imutável; compensação cancela retrato sem apagar relações oficiais.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "retratoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como retratoId."
    }
  },
  "required": [
    "projetoId",
    "retratoId"
  ],
  "additionalProperties": false
}
```

## listarLotes

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

Decisão pendente: Quais são os estados do lote além de PREPARADO e a nomenclatura da ativação?

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    },
    "estado": {
      "type": "string",
      "example": "PREPARADO",
      "description": "Filtro autorizado (§6.2); valor inválido retorna 400. Encaminhar em query como estado."
    },
    "requisitoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "Filtro autorizado (§6.2); valor inválido retorna 400. Encaminhar em query como requisitoId."
    },
    "responsavelId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "Filtro autorizado (§6.2); valor inválido retorna 400. Encaminhar em query como responsavelId."
    }
  },
  "required": [
    "projetoId"
  ],
  "additionalProperties": false
}
```

## obterLote

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

Decisão pendente: Quais são os valores finais do enum de estado do lote?

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "loteId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como loteId."
    }
  },
  "required": [
    "projetoId",
    "loteId"
  ],
  "additionalProperties": false
}
```

## listarPendencias

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "loteId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como loteId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    }
  },
  "required": [
    "projetoId",
    "loteId"
  ],
  "additionalProperties": false
}
```

## editarPendencia

Papel mínimo: **Leitor**; escopo **athena:write**; projeto **projetoId**.

§6.9. If-Match da pendência. Política de resolver/atribuir não definida; contrato não concede essa permissão. Retornar 403 até decisão documentada.

Decisão pendente: Quem pode resolver ou atribuir pendências? x-papel-minimo Leitor é apenas piso autenticado, NÃO autorização de escrita; operação bloqueada até definir política.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "loteId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como loteId."
    },
    "pendenciaId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como pendenciaId."
    },
    "ifMatch": {
      "type": "string",
      "example": "\"rev-7\"",
      "description": "§6.3. ETag forte do recurso alvo da ação. Comparação e gravação atômicas; ausente 428, divergente 412. A revisão técnica não é a versão do conteúdo. Encaminhar em header como If-Match."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "resolvida": {
          "type": "boolean",
          "example": true
        },
        "responsavelId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        }
      },
      "additionalProperties": false,
      "minProperties": 1,
      "example": {
        "resolvida": true
      }
    }
  },
  "required": [
    "projetoId",
    "loteId",
    "pendenciaId",
    "ifMatch",
    "corpo"
  ],
  "additionalProperties": false
}
```

## criarDecisao

Papel mínimo: **Aprovador**; escopo **athena:approve**; projeto **projetoId**.

§§6.3, 6.9, 7.5. If-Match do lote. Primeira decisão final válida vence; 409 se já decidido, PREPARADO ou pendências bloqueantes abertas. Rejeição/correções exigem justificativa. Evento por outbox; aplicação em Requisitos pode ficar PENDENTE, inclusive DLQ. MCP exige comprovante humano consumido na transação; autoaprovação permitida com decisão explícita.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "loteId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como loteId."
    },
    "ifMatch": {
      "type": "string",
      "example": "\"rev-7\"",
      "description": "§6.3. ETag forte do recurso alvo da ação. Comparação e gravação atômicas; ausente 428, divergente 412. A revisão técnica não é a versão do conteúdo. Encaminhar em header como If-Match."
    },
    "chaveIdempotencia": {
      "type": "string",
      "example": "tentativa-20261021-001",
      "minLength": 1,
      "description": "§6.3. Ordem: autenticar; verificar acesso atual; consultar chave no escopo usuário/projeto/operação e comparar pedido; repetição idêntica recupera resultado/acompanhamento por 24 h sem reavaliar If-Match; pedido divergente retorna 409. Só operação nova valida presença/valor de If-Match, quando exigido. Reserva, efeito e resultado são transacionais. Encaminhar em header como Idempotency-Key."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "decisao": {
          "type": "string",
          "example": "APROVAR",
          "enum": [
            "APROVAR",
            "REJEITAR",
            "SOLICITAR_CORRECOES"
          ]
        },
        "justificativa": {
          "type": "string",
          "example": "Critérios atendidos."
        },
        "submissaoId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        },
        "versaoId": {
          "type": "string",
          "example": "33333333-3333-4333-8333-333333333333",
          "format": "uuid"
        },
        "hashConteudo": {
          "type": "string",
          "example": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
          "pattern": "^[a-f0-9]{64}$"
        },
        "comprovanteHumano": {
          "type": "string",
          "example": "comprovante-opaco",
          "description": "§7.5. Somente emitido pelo serviço responsável após confirmação em tela autenticada ATHENA; uso único, 5 minutos, vinculado a usuário/projeto/ação/versão e hash da ação. Validar e consumir na transação do efeito. Obrigatório apenas para decisão/publicação delegada; declaração do agente não vale como confirmação."
        }
      },
      "additionalProperties": false,
      "required": [
        "decisao",
        "submissaoId",
        "versaoId",
        "hashConteudo",
        "comprovanteHumano"
      ],
      "example": {
        "decisao": "APROVAR",
        "submissaoId": "11111111-1111-4111-8111-111111111111",
        "versaoId": "33333333-3333-4333-8333-333333333333",
        "hashConteudo": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
      },
      "allOf": [
        {
          "if": {
            "properties": {
              "decisao": {
                "enum": [
                  "REJEITAR",
                  "SOLICITAR_CORRECOES"
                ]
              }
            },
            "required": [
              "decisao"
            ]
          },
          "then": {
            "required": [
              "justificativa"
            ],
            "properties": {
              "justificativa": {
                "type": "string",
                "minLength": 1
              }
            }
          }
        }
      ]
    }
  },
  "required": [
    "projetoId",
    "loteId",
    "ifMatch",
    "chaveIdempotencia",
    "corpo"
  ],
  "additionalProperties": false
}
```

## listarDecisoes

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "loteId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como loteId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    }
  },
  "required": [
    "projetoId",
    "loteId"
  ],
  "additionalProperties": false
}
```

## obterProjeto

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    }
  },
  "required": [
    "projetoId"
  ],
  "additionalProperties": false
}
```

## editarProjeto

Papel mínimo: **Mantenedor**; escopo **athena:admin**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "nome": {
          "type": "string",
          "example": "ATHENA"
        },
        "descricao": {
          "type": "string",
          "example": "Descrição atualizada"
        }
      },
      "additionalProperties": false,
      "minProperties": 1,
      "example": {
        "descricao": "Descrição atualizada"
      }
    }
  },
  "required": [
    "projetoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## criarArquivamento

Papel mínimo: **Mantenedor**; escopo **athena:admin**; projeto **projetoId**.

§§5.6, 9.2. Arquivar permite consulta e bloqueia escritas; restaurar reabre projeto. Barreira persistida bloqueia novas admissões e aguarda quiescência; não anunciar conclusão antes das confirmações.

Decisão pendente: Arquivamento assíncrono exige recurso de acompanhamento? 201 só após quiescência confirmada.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "corpo": {
      "type": "object",
      "properties": {},
      "additionalProperties": false,
      "example": {}
    }
  },
  "required": [
    "projetoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## criarRestauracao

Papel mínimo: **Mantenedor**; escopo **athena:admin**; projeto **projetoId**.

§§5.6, 9.2. Arquivar permite consulta e bloqueia escritas; restaurar reabre projeto. Barreira persistida bloqueia novas admissões e aguarda quiescência; não anunciar conclusão antes das confirmações.

Decisão pendente: Arquivamento assíncrono exige recurso de acompanhamento? 201 só após quiescência confirmada.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "corpo": {
      "type": "object",
      "properties": {},
      "additionalProperties": false,
      "example": {}
    }
  },
  "required": [
    "projetoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## criarExclusaoProjeto

Papel mínimo: **Owner**; escopo **athena:delete-project**; projeto **projetoId**.

§§5.6, 9.2. If-Match do projeto arquivado. Owner confirma nome; 409 com SAGA/revisão/sincronização ativa. FECHANDO bloqueia admissões, exige barreira local e quiescência dos domínios; depois EXCLUINDO e limpeza idempotente. Só CONCLUIDA após todas as confirmações. Preserva marcas técnicas, não apaga arquivos externos.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "ifMatch": {
      "type": "string",
      "example": "\"rev-7\"",
      "description": "§6.3. ETag forte do recurso alvo da ação. Comparação e gravação atômicas; ausente 428, divergente 412. A revisão técnica não é a versão do conteúdo. Encaminhar em header como If-Match."
    },
    "chaveIdempotencia": {
      "type": "string",
      "example": "tentativa-20261021-001",
      "minLength": 1,
      "description": "§6.3. Ordem: autenticar; verificar acesso atual; consultar chave no escopo usuário/projeto/operação e comparar pedido; repetição idêntica recupera resultado/acompanhamento por 24 h sem reavaliar If-Match; pedido divergente retorna 409. Só operação nova valida presença/valor de If-Match, quando exigido. Reserva, efeito e resultado são transacionais. Encaminhar em header como Idempotency-Key."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "confirmacaoNome": {
          "type": "string",
          "example": "ATHENA"
        }
      },
      "additionalProperties": false,
      "required": [
        "confirmacaoNome"
      ],
      "example": {
        "confirmacaoNome": "ATHENA"
      }
    }
  },
  "required": [
    "projetoId",
    "ifMatch",
    "chaveIdempotencia",
    "corpo"
  ],
  "additionalProperties": false
}
```

## obterExclusaoProjeto

Papel mínimo: **Owner**; escopo **athena:read**; projeto **projetoId**.

§5.6. Durante exclusão, somente Owner admitido para acompanhamento; não devolver conteúdo excluído.

Decisão pendente: Como autorizar acompanhamento do Owner quando o acesso ao projeto estiver bloqueado por exclusão?

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "exclusaoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como exclusaoId."
    }
  },
  "required": [
    "projetoId",
    "exclusaoId"
  ],
  "additionalProperties": false
}
```

## listarMembrosProjeto

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    }
  },
  "required": [
    "projetoId"
  ],
  "additionalProperties": false
}
```

## editarMembroProjeto

Papel mínimo: **Mantenedor**; escopo **athena:admin**; projeto **projetoId**.

§7.4. Mantenedor gerencia até Aprovador, nunca Mantenedor/Owner nem promove a Mantenedor. Owner gerencia até Mantenedor. Owner não sai, é removido ou se rebaixa antes da transferência confirmada; escopo não amplia papel. Workspace não concede acesso implícito a projeto.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "membroId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como membroId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "papel": {
          "type": "string",
          "example": "Leitor",
          "enum": [
            "Leitor",
            "Repórter",
            "Editor",
            "Aprovador",
            "Mantenedor",
            "Owner"
          ]
        }
      },
      "additionalProperties": false,
      "required": [
        "papel"
      ],
      "example": {
        "papel": "Editor"
      }
    }
  },
  "required": [
    "projetoId",
    "membroId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## removerMembroProjeto

Papel mínimo: **Mantenedor**; escopo **athena:admin**; projeto **projetoId**.

§7.4. Mantenedor gerencia até Aprovador, nunca Mantenedor/Owner nem promove a Mantenedor. Owner gerencia até Mantenedor. Owner não sai, é removido ou se rebaixa antes da transferência confirmada; escopo não amplia papel. Workspace não concede acesso implícito a projeto.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "membroId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como membroId."
    }
  },
  "required": [
    "projetoId",
    "membroId"
  ],
  "additionalProperties": false
}
```

## listarHistoricoMembros

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    }
  },
  "required": [
    "projetoId"
  ],
  "additionalProperties": false
}
```

## listarConvites

Papel mínimo: **Mantenedor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    }
  },
  "required": [
    "projetoId"
  ],
  "additionalProperties": false
}
```

## criarConvite

Papel mínimo: **Mantenedor**; escopo **athena:admin**; projeto **projetoId**.

§7.4. Mantenedor gerencia até Aprovador, nunca Mantenedor/Owner nem promove a Mantenedor. Owner gerencia até Mantenedor. Owner não sai, é removido ou se rebaixa antes da transferência confirmada; escopo não amplia papel. Workspace não concede acesso implícito a projeto. Owner nunca por convite; aceite pela conta destinatária e revalidação do papel na entrada.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "email": {
          "type": "string",
          "example": "destinatario@example.invalid",
          "format": "email"
        },
        "papel": {
          "type": "string",
          "example": "Leitor",
          "enum": [
            "Leitor",
            "Repórter",
            "Editor",
            "Aprovador",
            "Mantenedor"
          ]
        }
      },
      "additionalProperties": false,
      "required": [
        "email",
        "papel"
      ],
      "example": {
        "email": "destinatario@example.invalid",
        "papel": "Leitor"
      }
    }
  },
  "required": [
    "projetoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## removerConvite

Papel mínimo: **Mantenedor**; escopo **athena:admin**; projeto **projetoId**.

§7.4. Mantenedor gerencia até Aprovador, nunca Mantenedor/Owner nem promove a Mantenedor. Owner gerencia até Mantenedor. Owner não sai, é removido ou se rebaixa antes da transferência confirmada; escopo não amplia papel. Workspace não concede acesso implícito a projeto.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "conviteId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como conviteId."
    }
  },
  "required": [
    "projetoId",
    "conviteId"
  ],
  "additionalProperties": false
}
```

## criarTransferenciaPropriedade

Papel mínimo: **Owner**; escopo **athena:admin**; projeto **projetoId**.

§7.4. Owner atual confirma destinatário membro ativo; conclusão depende do aceite.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "destinatarioId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        },
        "confirmacao": {
          "type": "boolean",
          "const": true,
          "example": true
        }
      },
      "additionalProperties": false,
      "required": [
        "destinatarioId",
        "confirmacao"
      ],
      "example": {
        "destinatarioId": "11111111-1111-4111-8111-111111111111",
        "confirmacao": true
      }
    }
  },
  "required": [
    "projetoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## listarConexoes

Papel mínimo: **Mantenedor**; escopo **athena:read**; projeto **projetoId**.

§6.10. Credenciais só writeOnly no backend, nunca retornadas. Google prioriza drive.file com seleção explícita; OpenProject com mínimo acesso ao projeto. GitHub MVP somente vínculos. Importação explícita entra como rascunho/proposta, jamais sobrescreve publicada. Fluxo de autorização de conectores não definido.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    }
  },
  "required": [
    "projetoId"
  ],
  "additionalProperties": false
}
```

## criarConexao

Papel mínimo: **Mantenedor**; escopo **athena:admin**; projeto **projetoId**.

§6.10. Credenciais só writeOnly no backend, nunca retornadas. Google prioriza drive.file com seleção explícita; OpenProject com mínimo acesso ao projeto. GitHub MVP somente vínculos. Importação explícita entra como rascunho/proposta, jamais sobrescreve publicada. Fluxo de autorização de conectores não definido.

Decisão pendente: Como autorizar conectores Google/OpenProject e quais operações são suportadas?

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "provedor": {
          "type": "string",
          "example": "GOOGLE",
          "enum": [
            "GOOGLE",
            "OPENPROJECT",
            "GITHUB"
          ]
        },
        "nome": {
          "type": "string",
          "example": "Documentos do projeto"
        },
        "configuracao": {
          "type": "object",
          "properties": {
            "urlBase": {
              "type": "string",
              "example": "https://openproject.example.invalid",
              "format": "uri"
            },
            "projetoExternoId": {
              "type": "string",
              "example": "42"
            }
          },
          "additionalProperties": false,
          "example": {}
        },
        "credenciais": {
          "type": "object",
          "properties": {
            "segredo": {
              "type": "string",
              "example": "segredo-ilustrativo",
              "writeOnly": true
            }
          },
          "additionalProperties": false,
          "writeOnly": true,
          "required": [
            "segredo"
          ],
          "example": {
            "segredo": "segredo-ilustrativo"
          }
        }
      },
      "additionalProperties": false,
      "required": [
        "provedor",
        "nome"
      ],
      "example": {
        "provedor": "GOOGLE",
        "nome": "Documentos do projeto"
      }
    }
  },
  "required": [
    "projetoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## obterConexao

Papel mínimo: **Mantenedor**; escopo **athena:read**; projeto **projetoId**.

§6.10. Credenciais só writeOnly no backend, nunca retornadas. Google prioriza drive.file com seleção explícita; OpenProject com mínimo acesso ao projeto. GitHub MVP somente vínculos. Importação explícita entra como rascunho/proposta, jamais sobrescreve publicada. Fluxo de autorização de conectores não definido.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "conexaoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como conexaoId."
    }
  },
  "required": [
    "projetoId",
    "conexaoId"
  ],
  "additionalProperties": false
}
```

## editarConexao

Papel mínimo: **Mantenedor**; escopo **athena:admin**; projeto **projetoId**.

§6.10. Credenciais só writeOnly no backend, nunca retornadas. Google prioriza drive.file com seleção explícita; OpenProject com mínimo acesso ao projeto. GitHub MVP somente vínculos. Importação explícita entra como rascunho/proposta, jamais sobrescreve publicada. Fluxo de autorização de conectores não definido.

Decisão pendente: Qual fluxo de autorização dos conectores?

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "conexaoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como conexaoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "provedor": {
          "type": "string",
          "example": "GOOGLE",
          "enum": [
            "GOOGLE",
            "OPENPROJECT",
            "GITHUB"
          ]
        },
        "nome": {
          "type": "string",
          "example": "Documentos do projeto"
        },
        "configuracao": {
          "type": "object",
          "properties": {
            "urlBase": {
              "type": "string",
              "example": "https://openproject.example.invalid",
              "format": "uri"
            },
            "projetoExternoId": {
              "type": "string",
              "example": "42"
            }
          },
          "additionalProperties": false,
          "example": {}
        },
        "credenciais": {
          "type": "object",
          "properties": {
            "segredo": {
              "type": "string",
              "example": "segredo-ilustrativo",
              "writeOnly": true
            }
          },
          "additionalProperties": false,
          "writeOnly": true,
          "required": [
            "segredo"
          ],
          "example": {
            "segredo": "segredo-ilustrativo"
          }
        }
      },
      "additionalProperties": false,
      "minProperties": 1,
      "example": {
        "nome": "Documentação do projeto"
      }
    }
  },
  "required": [
    "projetoId",
    "conexaoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## removerConexao

Papel mínimo: **Mantenedor**; escopo **athena:admin**; projeto **projetoId**.

§6.10. Credenciais só writeOnly no backend, nunca retornadas. Google prioriza drive.file com seleção explícita; OpenProject com mínimo acesso ao projeto. GitHub MVP somente vínculos. Importação explícita entra como rascunho/proposta, jamais sobrescreve publicada. Fluxo de autorização de conectores não definido.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "conexaoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como conexaoId."
    }
  },
  "required": [
    "projetoId",
    "conexaoId"
  ],
  "additionalProperties": false
}
```

## listarVinculos

Papel mínimo: **Editor**; escopo **athena:read**; projeto **projetoId**.

§6.10. Credenciais só writeOnly no backend, nunca retornadas. Google prioriza drive.file com seleção explícita; OpenProject com mínimo acesso ao projeto. GitHub MVP somente vínculos. Importação explícita entra como rascunho/proposta, jamais sobrescreve publicada. Fluxo de autorização de conectores não definido.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    }
  },
  "required": [
    "projetoId"
  ],
  "additionalProperties": false
}
```

## criarVinculo

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

§6.10. Credenciais só writeOnly no backend, nunca retornadas. Google prioriza drive.file com seleção explícita; OpenProject com mínimo acesso ao projeto. GitHub MVP somente vínculos. Importação explícita entra como rascunho/proposta, jamais sobrescreve publicada. Fluxo de autorização de conectores não definido.

Decisão pendente: Como autorizar conectores Google/OpenProject e quais operações são suportadas?

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "conexaoId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        },
        "requisitoId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        },
        "identificadorExterno": {
          "type": "string",
          "example": "artefato-42"
        },
        "urlExterna": {
          "type": "string",
          "example": "https://example.invalid/artefatos/42",
          "format": "uri"
        },
        "versaoExternaConhecida": {
          "type": "string",
          "example": "etag-7"
        }
      },
      "additionalProperties": false,
      "required": [
        "conexaoId",
        "requisitoId",
        "identificadorExterno"
      ],
      "example": {
        "conexaoId": "11111111-1111-4111-8111-111111111111",
        "requisitoId": "11111111-1111-4111-8111-111111111111",
        "identificadorExterno": "artefato-42"
      }
    }
  },
  "required": [
    "projetoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## removerVinculo

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

§6.10. Credenciais só writeOnly no backend, nunca retornadas. Google prioriza drive.file com seleção explícita; OpenProject com mínimo acesso ao projeto. GitHub MVP somente vínculos. Importação explícita entra como rascunho/proposta, jamais sobrescreve publicada. Fluxo de autorização de conectores não definido.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "vinculoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como vinculoId."
    }
  },
  "required": [
    "projetoId",
    "vinculoId"
  ],
  "additionalProperties": false
}
```

## criarSincronizacao

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

§6.10. Operação e intenção persistidas atomicamente; compara versão externa e revisão local. Timeout consulta/reconcilia antes de repetir; sem prova do efeito fica RESULTADO_DESCONHECIDO com resolução humana e sem nova criação automática. 429 externo respeita Retry-After. §6.10. Credenciais só writeOnly no backend, nunca retornadas. Google prioriza drive.file com seleção explícita; OpenProject com mínimo acesso ao projeto. GitHub MVP somente vínculos. Importação explícita entra como rascunho/proposta, jamais sobrescreve publicada. Fluxo de autorização de conectores não definido.

Decisão pendente: Confirmar Idempotency-Key obrigatória na sincronização e catálogo completo de estados.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "chaveIdempotencia": {
      "type": "string",
      "example": "tentativa-20261021-001",
      "minLength": 1,
      "description": "§6.3. Ordem: autenticar; verificar acesso atual; consultar chave no escopo usuário/projeto/operação e comparar pedido; repetição idêntica recupera resultado/acompanhamento por 24 h sem reavaliar If-Match; pedido divergente retorna 409. Só operação nova valida presença/valor de If-Match, quando exigido. Reserva, efeito e resultado são transacionais. Encaminhar em header como Idempotency-Key."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "vinculoId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        }
      },
      "additionalProperties": false,
      "required": [
        "vinculoId"
      ],
      "example": {
        "vinculoId": "11111111-1111-4111-8111-111111111111"
      }
    }
  },
  "required": [
    "projetoId",
    "chaveIdempotencia",
    "corpo"
  ],
  "additionalProperties": false
}
```

## listarSincronizacoes

Papel mínimo: **Editor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

Decisão pendente: Qual catálogo completo de estados da sincronização?

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    }
  },
  "required": [
    "projetoId"
  ],
  "additionalProperties": false
}
```

## obterSincronizacao

Papel mínimo: **Editor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "sincronizacaoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como sincronizacaoId."
    }
  },
  "required": [
    "projetoId",
    "sincronizacaoId"
  ],
  "additionalProperties": false
}
```

## criarResolucaoSincronizacao

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

§6.10. Registra evidência humana; não aceita comando genérico de sobrescrever versão publicada ou recriar automaticamente efeito desconhecido.

Decisão pendente: Quais comandos e efeitos são permitidos na resolução humana de conflito/resultado desconhecido?

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "sincronizacaoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como sincronizacaoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "justificativa": {
          "type": "string",
          "example": "Efeito externo conferido manualmente."
        },
        "resultadoVerificado": {
          "type": "string",
          "example": "A tarefa já existe no provedor."
        },
        "identificadorExterno": {
          "type": "string",
          "example": "artefato-42"
        }
      },
      "additionalProperties": false,
      "required": [
        "justificativa",
        "resultadoVerificado"
      ],
      "example": {
        "justificativa": "Efeito externo conferido manualmente.",
        "resultadoVerificado": "A tarefa já existe no provedor."
      }
    }
  },
  "required": [
    "projetoId",
    "sincronizacaoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## criarTarefaExterna

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

§6.10. Criação OpenProject com operação persistida; resultado desconhecido exige reconciliação sem duplicar tarefa.

Decisão pendente: Quais campos OpenProject são obrigatórios e qual recurso acompanha criação de tarefa externa?

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "conexaoId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        },
        "requisitoId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        },
        "titulo": {
          "type": "string",
          "example": "Implementar exportação"
        },
        "descricao": {
          "type": "string",
          "example": "Criar exportação do documento."
        }
      },
      "additionalProperties": false,
      "required": [
        "conexaoId",
        "requisitoId",
        "titulo"
      ],
      "example": {
        "conexaoId": "11111111-1111-4111-8111-111111111111",
        "requisitoId": "11111111-1111-4111-8111-111111111111",
        "titulo": "Implementar exportação"
      }
    }
  },
  "required": [
    "projetoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## criarAnalise

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

§6.11. Persiste preparação de contexto; agente externo executa LLM e devolve sugestões não confiáveis. Backend gera apenas embeddings locais; falha LLM permite referências e fluxo manual.

Decisão pendente: Qual protocolo para agente registrar resultado da análise, estados e timeout sem agente conectado?

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "requisitoId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        },
        "versaoId": {
          "type": "string",
          "example": "33333333-3333-4333-8333-333333333333",
          "format": "uuid"
        },
        "objetivo": {
          "type": "string",
          "example": "Identificar impactos da alteração"
        }
      },
      "additionalProperties": false,
      "required": [
        "requisitoId",
        "versaoId"
      ],
      "example": {
        "requisitoId": "11111111-1111-4111-8111-111111111111",
        "versaoId": "33333333-3333-4333-8333-333333333333"
      }
    }
  },
  "required": [
    "projetoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## listarAnalises

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    }
  },
  "required": [
    "projetoId"
  ],
  "additionalProperties": false
}
```

## obterAnalise

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

Decisão pendente: Definir catálogo de status da análise e estadoValidacao/tipo de sugestão.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "analiseId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como analiseId."
    }
  },
  "required": [
    "projetoId",
    "analiseId"
  ],
  "additionalProperties": false
}
```

## criarAvaliacaoSugestao

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

§§6.11, 7.4–7.5. Revalida versão/hash e acesso oficial, inclusive rascunhos; 409 se desatualizados. Sugestão jamais se torna relação oficial sozinha; aplicação exige ação autorizada no domínio.

Decisão pendente: Quais valores/efeitos da avaliação? Aceite cria relação na mesma chamada ou exige criação explícita no domínio?

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "analiseId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como analiseId."
    },
    "sugestaoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como sugestaoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "avaliacao": {
          "type": "string",
          "example": "ACEITAR"
        },
        "versoesUtilizadas": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "requisitoId": {
                "type": "string",
                "example": "11111111-1111-4111-8111-111111111111",
                "format": "uuid"
              },
              "versaoId": {
                "type": "string",
                "example": "33333333-3333-4333-8333-333333333333",
                "format": "uuid"
              },
              "hashConteudo": {
                "type": "string",
                "example": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
              }
            },
            "additionalProperties": false,
            "required": [
              "requisitoId",
              "versaoId",
              "hashConteudo"
            ],
            "example": {
              "requisitoId": "11111111-1111-4111-8111-111111111111",
              "versaoId": "33333333-3333-4333-8333-333333333333",
              "hashConteudo": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            }
          },
          "example": []
        },
        "justificativa": {
          "type": "string",
          "example": "Evidências conferidas"
        }
      },
      "additionalProperties": false,
      "required": [
        "avaliacao",
        "versoesUtilizadas",
        "justificativa"
      ],
      "example": {
        "avaliacao": "ACEITAR",
        "versoesUtilizadas": [],
        "justificativa": "Evidências conferidas"
      }
    }
  },
  "required": [
    "projetoId",
    "analiseId",
    "sugestaoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## buscarContexto

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

§6.11. Busca híbrida textual/vetorial, cinco trechos padrão, remove duplicatas com referências. Filtra acesso antes de entregar e revalida nas fontes oficiais inclusive rascunhos/revogação. Similaridade não é probabilidade. Chunks 300–400 tokens, ~50 sobreposição, e5-small até 512, query:/passage:, vetores normalizados.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "q": {
          "type": "string",
          "example": "Quais requisitos dependem da exportação?"
        },
        "limite": {
          "type": "integer",
          "example": 5,
          "default": 5,
          "minimum": 1
        }
      },
      "additionalProperties": false,
      "required": [
        "q"
      ],
      "example": {
        "q": "Quais requisitos dependem da exportação?"
      }
    }
  },
  "required": [
    "projetoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## obterIndice

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

Decisão pendente: Quais situações do índice e unidade final do atraso?

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    }
  },
  "required": [
    "projetoId"
  ],
  "additionalProperties": false
}
```

## listarOperacoesImpacto

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "page": {
      "type": "integer",
      "example": 0,
      "default": 0,
      "minimum": 0,
      "description": "Página inicial zero (§6.2). Encaminhar em query como page."
    },
    "size": {
      "type": "integer",
      "example": 20,
      "default": 20,
      "minimum": 1,
      "maximum": 100,
      "description": "Quantidade por página (§6.2). Encaminhar em query como size."
    },
    "sort": {
      "type": "string",
      "example": "atualizadoEm,desc",
      "default": "atualizadoEm,desc",
      "enum": [
        "atualizadoEm,desc",
        "atualizadoEm,asc",
        "id,asc",
        "id,desc"
      ],
      "description": "§6.2: id é desempate estável; valor fora do enum retorna 400. Operações especializam o enum. Encaminhar em query como sort."
    }
  },
  "required": [
    "projetoId"
  ],
  "additionalProperties": false
}
```

## obterOperacaoImpacto

Papel mínimo: **Leitor**; escopo **athena:read**; projeto **projetoId**.

Operação conforme §§5, 6.2–6.3 e 7.4. Revalidar acesso e estado no domínio proprietário.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "operacaoId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como operacaoId."
    }
  },
  "required": [
    "projetoId",
    "operacaoId"
  ],
  "additionalProperties": false
}
```

## criarReindexacao

Papel mínimo: **Mantenedor**; escopo **athena:admin**; projeto **projetoId**.

§§6.8, 6.11. Trabalho persistido, revalida versão/exclusão antes de ativar chunks; troca atômica preserva anterior; trabalho atrasado não reativa dados excluídos.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "corpo": {
      "type": "object",
      "properties": {},
      "additionalProperties": false,
      "example": {}
    }
  },
  "required": [
    "projetoId",
    "corpo"
  ],
  "additionalProperties": false
}
```

## criarSugestaoAnalise

Papel mínimo: **Editor**; escopo **athena:write**; projeto **projetoId**.

§§6.11, 7.4–7.5. Proposta de rota de registro da saída externa não confiável; valida esquema, versões, evidências e acesso, sem criar relação oficial. Escopo athena:write e papel não substituem avaliação.

Decisão pendente: Confirmar fluxo/identificador para registrar sugestões do agente e papel de Repórter versus Editor.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "properties": {
    "projetoId": {
      "type": "string",
      "example": "22222222-2222-4222-8222-222222222222",
      "format": "uuid",
      "description": "UUID estável (§5.1). Encaminhar em path como projetoId."
    },
    "analiseId": {
      "type": "string",
      "example": "11111111-1111-4111-8111-111111111111",
      "format": "uuid",
      "description": "UUID do recurso (§5.1). Encaminhar em path como analiseId."
    },
    "corpo": {
      "type": "object",
      "properties": {
        "sugestaoId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        },
        "tipo": {
          "type": "string",
          "example": "RELACAO"
        },
        "requisitoAnalisadoId": {
          "type": "string",
          "example": "11111111-1111-4111-8111-111111111111",
          "format": "uuid"
        },
        "alvos": {
          "type": "array",
          "items": {
            "type": "string",
            "example": "11111111-1111-4111-8111-111111111111",
            "format": "uuid"
          },
          "example": [
            "33333333-3333-4333-8333-333333333333"
          ]
        },
        "versoesUtilizadas": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "requisitoId": {
                "type": "string",
                "example": "11111111-1111-4111-8111-111111111111",
                "format": "uuid"
              },
              "versaoId": {
                "type": "string",
                "example": "33333333-3333-4333-8333-333333333333",
                "format": "uuid"
              },
              "hashConteudo": {
                "type": "string",
                "example": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
              }
            },
            "additionalProperties": false,
            "required": [
              "requisitoId",
              "versaoId",
              "hashConteudo"
            ],
            "example": {
              "requisitoId": "11111111-1111-4111-8111-111111111111",
              "versaoId": "33333333-3333-4333-8333-333333333333",
              "hashConteudo": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
            }
          },
          "example": []
        },
        "evidencias": {
          "type": "array",
          "items": {
            "type": "object",
            "properties": {
              "origemId": {
                "type": "string",
                "example": "11111111-1111-4111-8111-111111111111",
                "format": "uuid"
              },
              "versaoId": {
                "type": "string",
                "example": "33333333-3333-4333-8333-333333333333",
                "format": "uuid"
              },
              "trecho": {
                "type": "string",
                "example": "O documento deve ser exportável."
              },
              "referencia": {
                "type": "string",
                "example": "/api/v1/gestao/projetos/22222222-2222-4222-8222-222222222222/requisitos/11111111-1111-4111-8111-111111111111",
                "format": "uri-reference"
              }
            },
            "additionalProperties": false,
            "required": [
              "origemId",
              "versaoId",
              "trecho",
              "referencia"
            ],
            "example": {
              "origemId": "11111111-1111-4111-8111-111111111111",
              "versaoId": "33333333-3333-4333-8333-333333333333",
              "trecho": "O documento deve ser exportável.",
              "referencia": "/api/v1/gestao/projetos/22222222-2222-4222-8222-222222222222/requisitos/11111111-1111-4111-8111-111111111111"
            }
          },
          "example": []
        },
        "justificativa": {
          "type": "string",
          "example": "A alteração afeta requisito relacionado."
        },
        "estadoValidacao": {
          "type": "string",
          "example": "PENDENTE"
        }
      },
      "additionalProperties": false,
      "required": [
        "sugestaoId",
        "tipo",
        "requisitoAnalisadoId",
        "alvos",
        "versoesUtilizadas",
        "evidencias",
        "justificativa",
        "estadoValidacao"
      ],
      "example": {
        "sugestaoId": "11111111-1111-4111-8111-111111111111",
        "tipo": "RELACAO",
        "requisitoAnalisadoId": "11111111-1111-4111-8111-111111111111",
        "alvos": [
          "33333333-3333-4333-8333-333333333333"
        ],
        "versoesUtilizadas": [],
        "evidencias": [],
        "justificativa": "A alteração afeta requisito relacionado.",
        "estadoValidacao": "PENDENTE"
      }
    }
  },
  "required": [
    "projetoId",
    "analiseId",
    "corpo"
  ],
  "additionalProperties": false
}
```
