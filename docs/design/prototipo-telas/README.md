# ATHENA — Protótipo navegável de alta fidelidade

Protótipo de front-end das telas do ATHENA (gestão de requisitos de software — UFLA/Sistemas Distribuídos), cobrindo a **jornada completa**: autenticação → workspaces e projetos → organização por épicos, mapa de rastreabilidade e editor.

**Demo online:** https://clararochag.github.io/athena-prototype/

**Como rodar offline:** abra `index.html` no navegador (Chrome/Safari/Edge). Sem build, sem dependências — HTML/CSS/JS puro, dados mockados em memória (`data.js`).

> As fontes Inter/Newsreader vêm do Google Fonts; sem internet, o sistema usa fontes do SO.

## Telas implementadas (user stories do PDF)

| Tela | US | O que testar |
|---|---|---|
| **Criar conta** | US01 | Nome, e-mail, senha com checklist visual em tempo real (8 caracteres / Letras / Números / Caractere especial), confirmação de senha, aceite dos Termos; e-mail já registrado é recusado; sessão automática após o registro |
| **Login** | US02 | E-mail + senha com ícone de visibilidade, "Lembrar este dispositivo", erro genérico "Credenciais inválidas", atalhos para recuperação e criação de conta |
| **Recuperar acesso** | US03 | E-mail → "Enviar código" com loading; mensagem padronizada sem revelar existência da conta ("Se houver uma conta com esse e-mail…") |
| **Criar nova senha** | US03 | Código de 6 dígitos em caixas individuais (salto automático, colar), nova senha + confirmação com checklist, "Reenviar código", "Usar outro e-mail", "Voltar para entrar"; código errado é recusado |
| **Workspaces** | US04 | Sidebar com lista e busca em tempo real, "+" para criar, renomear e excluir com confirmação digitando o nome exato (botão habilita só com o nome correto), aviso "Arquivos nos serviços conectados não serão removidos" |
| **Projetos** | US05 | Cards com busca, "Novo projeto" (nome único por workspace), "Abrir" entra no contexto do projeto, menu … com editar/arquivar |
| **Projetos arquivados** | US06 | Aba com contador e descrição contextual, empty state "Nenhum projeto arquivado neste workspace.", restaurar e excluir permanentemente (confirmação por nome) |
| **Trocar projeto** | US24 | Modal em duas colunas (workspaces ← projetos), busca filtra as duas listas, flag verde "Atual", checkmark no workspace corrente, atalho "Gerenciar workspaces" |
| **Visão geral** | US08 | Cartões expansíveis de épicos (contador "X US"), US cancelada não aparece nos épicos ativos |
| **Modal Novo Épico** | US08 | Validação de nome obrigatório, aparece imediato com "0 US" |
| **Modal Adicionar Requisito** | US07 | Título + Pasta/Épico (pré-preenchido de dentro de um épico) + "Em branco" / "User Story completa" (editor abre com template) |
| **Configurações → Pastas** | US08 | Renomear épico inline, excluir bloqueado se contém requisitos |
| **Editor de requisitos** | US09–US13 | Toolbar WYSIWYG, auto-save ("Alterações pendentes…" → "Salvo"), título editável, critérios Dado/Quando/Então, gaveta **Detalhes** com dropdown de pasta (US11), menu … com **Cancelar US** |
| **Ciclo de vida da candidata** | US11/US13/US28 | RASCUNHO → EM_SUBMISSÃO (SAGA) → AGUARDANDO_REVISÃO → APROVADA → PUBLICADA; rejeição exige justificativa; editar APROVADA/PUBLICADA invalida; cancelar/reativar US |
| **Aba Requisitos** | US18 | Filtros independentes: situação (ATIVO/CANCELADO), estado da candidata, publicação, épico + busca global; empty state; canceladas com tag |
| **Mapa** | US14 | Quadro infinito: pan (arrastar), zoom (roda/botões), épicos como pastas, cartões com ID/título/status/relações, cancelada tracejada, status de projeção |
| **Painel lateral do Mapa** | US15 | Detalhes, acordeões Relações / Critérios de aceite / Origem, "Centralizar no mapa" e "Abrir requisito" (duplo clique também) |
| **Grafo de relações** | US17 | Setas em DEPENDE_DE/BLOQUEIA, sem seta em RELACIONADO_A, etiqueta centralizada, hover destaca conexões + tooltip "US-001 depende de US-002" |
| **Modal + Conectar relação** | US16 | Busca por código/nome, semântica com preview de leitura, duplicatas rejeitadas, leituras dos extremos (Depende de / É pré-requisito de / Bloqueia / Bloqueada por) |

## Conta demo

| E-mail | Senha |
|---|---|
| `anagomes@athena.dev` | `Athena@2026` |

A recuperação de senha gera um código de demonstração exibido no toast (em produção chegaria por e-mail).

## Arquitetura do protótipo

- `index.html` — shell e ordem de carregamento
- `styles.css` — design system (dark/light, tokens CSS)
- `data.js` — dados mockados: `DB` (usuários, sessão, 2 workspaces, 3 projetos) + `ATH` (contexto do projeto ativo, carregado por `loadProject` → isolamento de dados por projeto)
- `auth.js` — US01–US03 (criar conta, login, recuperação, nova senha)
- `home.js` — US04–US06 (workspaces, projetos, arquivados) + US24 (trocar projeto)
- `map.js` — canvas infinito, grafo de relações, gaveta lateral, modal de conexão
- `app.js` — roteador (auth → home → projeto), shell, Visão Geral, Requisitos, Editor, modais, ciclo de vida
- `tests/test.js` — smoke test funcional (**115 checks**, roda com jsdom: `npm i jsdom && node tests/test.js`)

## Notas de fidelidade ao documento

- Leituras inversas **não** duplicam arestas (seção 5.4 do Livro de Arquitetura).
- Situação ATIVO/CANCELADO é independente do estado da candidata (US08/US13).
- Cancelar US exige confirmação e remove das listagens ativas, preservando histórico (US13).
- Exclusão de workspace/projeto exige confirmação pelo nome exato; arquivos em serviços conectados não são removidos (US04/Livro 5.6).
- Mensagens de recuperação são padronizadas, sem revelar existência da conta (US03).
- O mapa informa estado da projeção ("atualizada · há instantes") — não anuncia tempo real (US14).
- Papéis cumulativos exibidos em Configurações → Membros (US26 — estático).
