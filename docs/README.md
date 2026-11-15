# Documentação do ATHENA

## Estrutura

```
docs/
├── arquitetura/
│   ├── livro-de-arquitetura-v1.0.md   decisões, justificativas e regras (fonte de verdade)
│   └── diagramas/                     componentes, comunicação e SAGA (sucesso e compensação)
├── api/                               contratos OpenAPI 3.1 dos 6 serviços e 2 BFFs
│   ├── *.yaml                         fonte editável de cada contrato
│   ├── _shared/components.yaml        segurança, paginação, erros e HATEOAS compartilhados
│   ├── dist/                          bundles autocontidos (gerados)
│   ├── apresentacao.html              os 8 contratos para leitura offline no navegador (gerado)
│   ├── matriz-autorizacao.md          papel e escopo de cada uma das 189 operações (gerado)
│   ├── mcp-ferramentas.md             catálogo das 81 tools MCP (gerado)
│   ├── PROPOSTAS.md                   decisões ainda em aberto
│   └── VALIDACAO.md                   evidências da validação automática
├── seguranca/                         mapeamento de endpoints e tools para papéis e escopos (gerado)
└── empreendedorismo/                  entregas da disciplina de Empreendedorismo
```

Os arquivos marcados como gerados são recriados por `npm run validar` e `npm run apresentacao`, na raiz do repositório. O ferramental de validação (`package.json`, `scripts/`, `redocly.yaml`, `.spectral.yaml`) também fica na raiz.

## Onde está cada item da Parte 2 — Arquitetura

As seções (§) referem-se ao [Livro de Arquitetura](arquitetura/livro-de-arquitetura-v1.0.md).

| Critério | Onde encontrar |
|---|---|
| **4.1 Decomposição em microsserviços** | Seis serviços e responsabilidades: §4.1. Justificativa das fronteiras: §3.1. Diagramas: [componentes e persistência](arquitetura/diagramas/athena-componentes-e-persistencia.png) e [comunicação entre serviços](arquitetura/diagramas/athena-comunicacao-entre-servicos.png). |
| **4.2 API REST** | Contratos completos em [api/](api/README.md), um por serviço e por BFF. Convenções de recursos, verbos e status: §6.2. Versionamento por URI (`/api/v1`) e sua justificativa: §6.2 e campo `info.description` de cada contrato. Concorrência e idempotência: §6.3. |
| **4.2 HATEOAS** | §6.4. Exemplos `editor-aguardando-revisao` e `aprovador-aprovada` no GET de requisito do [BFF Gestão](api/bff-gestao.yaml), com `_links` e `_actions` que mudam conforme estado e papel. |
| **4.3 API Gateway e routing** | Responsabilidades: §3.3. Rotas: §6.1 e tabela "Rotas do Gateway" em [api/README.md](api/README.md). |
| **4.4 Backend for Frontend** | Por que dois BFFs: §3.3. Composição do Livro: §4.3. Diferença entre as respostas para o mesmo requisito (versão 4 em revisão na Gestão, versão 3 publicada no Livro): §6.4 e seção "Mesmo requisito nos dois clientes" em [api/README.md](api/README.md). |
| **4.5 Database per service** | Uma instância PostgreSQL por serviço e justificativa da tecnologia: §3.2. Modelo e isolamento: §5.1. |
| **4.5 Consistência eventual** | Pontos e metas: §2.2 e §3.5. Projeção do mapa: §5.5. Índice MCP: §6.11. |
| **4.5 Dual write e outbox** | Análise do risco e projeto do outbox/inbox: §6.8. |
| **4.6 SAGA** | Escolha da orquestração frente à coreografia: §3.4. Transação entre Requisitos, Rastreabilidade e Revisões: §6.5. Compensações e mensagens atrasadas: §6.6. Prazos e retentativas: §6.7. Diagramas: [caminho feliz](arquitetura/diagramas/athena-saga-sucesso.png) e [falha com compensação](arquitetura/diagramas/athena-saga-compensacao.png). |
| **4.7 CQRS** | Serviço Rastreabilidade, modelo de escrita das relações e projeção do mapa: §3.5 e §5.5. Defasagem aceitável de até 5 s: §2.2. |

## Próximas etapas

O livro prevê ainda, para a implementação, o modelo físico e as migrações em `docs/dados`, os JSON Schemas das mensagens da SAGA em `docs/mensageria` e os runbooks de operação em `docs/operacao`. Essas pastas serão criadas junto com o código das Partes 3 e 4.
