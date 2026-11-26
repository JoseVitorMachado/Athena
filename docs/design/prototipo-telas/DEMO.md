# Roteiro de demonstração (~5 min) — Protótipo ATHENA

> Pré-requisito: abrir `index.html` no navegador (ou o link do GitHub Pages).
> Conta demo: **anagomes@athena.dev** · senha **Athena@2026**
> Dica: pressione `Cmd +` / `Cmd -` para ajustar o zoom da tela antes de começar.

## 1. Login (US02) · ~20s
- Tela de entrada com marca ATHENA; apontar a conta demo no rodapé do card.
- Demonstração rápida do erro genérico: digitar e-mail errado → "Credenciais inválidas" (sem revelar qual campo está errado).
- Logar com a conta demo.

## 2. Workspaces e Projetos (US04–US06) · ~60s
- Home com sidebar de workspaces: dois workspaces pré-carregados.
- Busca "Buscar workspace" filtra a lista em tempo real.
- Menu "Opções do workspace" → **Excluir workspace**: mostrar que o botão só habilita digitando o nome exato (cancelar sem excluir).
- Aba **Projetos arquivados**: contador "1", descrição "Projetos arquivados em [Workspace]".
- Card "Portal do Parceiro" (arquivado): menu … com "Restaurar projeto" e "Excluir permanentemente".
- Voltar à aba Projetos e clicar **Abrir** no "Plataforma ATHENA".

## 3. Visão Geral — épicos (US08) · ~40s
- "14 requisitos em 6 épicos · 10 relações ativas" no cabeçalho.
- Expandir "Organização de projetos" (clicar no cartão): lista as US do épico com ID, título e estado.
- Contador "4 US" de "Autenticação e Conta": a US-023 (cancelada) **não aparece** nos épicos ativos.
- "Novo Épico" → digitar "Pagamentos e Cash-out" → Criar: aparece imediato com "0 US".

## 4. Adicionar Requisito com template (US07) · ~30s
- Dentro do novo épico, clicar no **"+"** do cartão (pasta pré-preenchida).
- Título "Reembolso via Pix" → **"User Story completa"** → Adicionar.
- Editor abre com template INVEST: Especificação, Campos e comportamento, Regras de negócio.

## 5. Editor + ciclo de vida (US09–US13) · ~70s
- Digitar algo no texto: "Alterações pendentes…" → "Salvo" (auto-save).
- Gaveta **Detalhes** (US11): tipo, estado da candidata, versão publicada, dropdown **Pasta** para mover entre épicos.
- **Submeter revisão** → Em submissão (SAGA) → **Aguardando revisão** com toast "lote de revisão aberto".
- **Aprovar** → **Publicar v1** → toast "publicada no Livro".
- Menu **"…"** → **Cancelar US** (US13): modal explica que a US fica com tag "Cancelada" na aba Requisitos.

## 6. Aba Requisitos (US18) · ~30s
- Filtros **independentes**: desligar ATIVO, ligar CANCELADO → só a US cancelada aparece.
- Situação (ATIVO/CANCELADO) **não se mistura** com estado da candidata nem com publicação.
- Empty state: desligar os dois chips → "Nenhuma história de usuário encontrada para os filtros aplicados".

## 7. Mapa (US14–US17) · ~80s
- Arrastar com o mouse (pan) e dar zoom com a roda / botões + ajustar.
- Épicos como **pastas**; cartões com ID, título, estado e **nº de relações**; cancelada tracejada.
- Arestas com **setas** (DEPENDE_DE / BLOQUEIA) e **sem seta** (RELACIONADO_A), etiqueta centralizada.
- Hover num cartão: arestas conectadas destacam, o resto esmaece (US17).
- Hover numa aresta: tooltip "US-028 depende de US-009".

## 8. Gaveta lateral + Conectar relação (US15/US16) · ~50s
- Clicar em **US-016**: gaveta com acordeões **Relações / Critérios de aceite / Origem**.
- Leituras dos extremos: "Depende de US-009" e "É pré-requisito de US-028" — sem arestas duplicadas.
- **"+ Conectar relação"** → buscar "US-030" → DEPENDE_DE → preview "Esta US depende de US-030" → Conectar: aresta nova + contador atualiza.
- Tentar a mesma relação de novo → rejeitada ("leituras inversas representam a mesma aresta").
- **"Centralizar no mapa"** e **"Abrir requisito"**.

## 9. Trocar projeto (US24) + fecho (~20s)
- Clicar no chip do projeto no topo → modal de duas colunas; busca "InfinitePay" → selecionar workspace → abrir **App Mobile** → mapa com 1 cartão e 0 arestas (isolamento por projeto).
- Voltar para "Plataforma ATHENA" pelo mesmo modal.
- Menu do avatar → **Modo escuro/claro** (US23).
- Frase-chave: *"Cada clique demonstrado corresponde a um critério de aceitação das user stories US01–US08 e US13–US18 do documento."*

## Se o professor perguntar (arguição provável)
- **"Por que a recuperação mostra um código no toast?"** → Protótipo front-end sem backend de e-mail; a regra de negócio real (10 min de expiração, mensagem padronizada) está implementada no fluxo.
- **"Por que US cancelada não some da tabela?"** → Cancelamento preserva histórico para auditoria; o Livro corrente e as listagens ativas é que a excluem (US13/US27).
- **"Por que 'Depende de' e 'É pré-requisito de' aparecem separados?"** → Leituras da mesma aresta; o catálogo normaliza duplicações (seção 5.4).
- **"O que é a SAGA de 1,5s?"** → Protótipo simula a orquestração Requisitos → Rastreabilidade (retrato) → Revisões (lote) do Livro de Arquitetura.
- **"Três projetos compartilham tela?"** → Sim: os dados de cada projeto são carregados isoladamente no contexto (ATH), o mesmo front serve a todos.
- **"IA onde entra?"** → Fora do escopo destas telas: sugestões chegam via agente externo MCP e sempre exigem confirmação humana (US29).
