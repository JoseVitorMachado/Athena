// ============================================================
// ATHENA — dados do protótipo (mock em memória)
// Estrutura: DB (usuários, sessão, workspaces e projetos)
// ATH = contexto do projeto ativo (carregado por loadProject)
// ============================================================
window.DB = {
  users: [
    { name: "Ana Clara", email: "anagomes@athena.dev", password: "Athena@2026" },
  ],
  session: { user: null },
  ui: {
    view: "home",        // home | visao | requisitos | mapa | editor
    authView: "login",   // login | signup | recover | reset
    homeTab: "projetos", // projetos | arquivados
    homeWs: "W1",
    wsSearch: "", projSearch: "",
    tpWs: null,          // workspace selecionado no modal Trocar projeto
    focusId: null,       // restaura foco após re-render (buscas)
  },
  pendingHash: null,     // deep-link guardado até o login
  devCode: null,         // código de recuperação "enviado" (protótipo)

  workspaces: [
    {
      id: "W1", name: "UFLA — Sistemas Distribuídos",
      projects: [
        {
          id: "P1", name: "Plataforma ATHENA", desc: "Gestão, rastreabilidade e revisão de requisitos",
          archived: false,
          data: {
            project: { name: "Plataforma ATHENA", code: "ATH", created: "05/10/2026" },
            seq: { req: 30, epico: 6, rel: 10 },
            epicos: [
              { id: "E1", name: "Autenticação e Conta",   open: true,  x: 140, y: 120 },
              { id: "E2", name: "Organização de projetos", open: false, x: 500, y: 120 },
              { id: "E3", name: "Editor de Requisitos",   open: false, x: 500, y: 470 },
              { id: "E4", name: "Rastreabilidade",        open: true,  x: 140, y: 560 },
              { id: "E5", name: "Revisão e Publicação",   open: false, x: 900, y: 120 },
              { id: "E6", name: "Integrações",            open: false, x: 900, y: 500 },
            ],
            requirements: [ /* preenchido abaixo */ ],
            relations: [
              { id: "R1",  type: "DEPENDE_DE",    from: "US-001", to: "US-002", autor: "Fernando",  just: "O login pressupõe conta criada." },
              { id: "R2",  type: "RELACIONADO_A", from: "US-001", to: "US-003", autor: "Fernando",  just: "Fluxos de autenticação compartilham a política de sessão." },
              { id: "R3",  type: "DEPENDE_DE",    from: "US-005", to: "US-004", autor: "José Vítor", just: "Projetos existem dentro de workspaces." },
              { id: "R4",  type: "DEPENDE_DE",    from: "US-010", to: "US-009", autor: "Ana Clara", just: "Critérios estruturados dependem do editor." },
              { id: "R5",  type: "RELACIONADO_A", from: "US-016", to: "US-017", autor: "Fernando",  just: "Criação e visualização de relações." },
              { id: "R6",  type: "DEPENDE_DE",    from: "US-016", to: "US-009", autor: "Fernando",  just: "Relações referenciam requisitos criados no editor." },
              { id: "R7",  type: "DEPENDE_DE",    from: "US-028", to: "US-009", autor: "José Vítor", just: "A revisão avalia snapshots do editor." },
              { id: "R8",  type: "DEPENDE_DE",    from: "US-030", to: "US-028", autor: "Fernando",  just: "Baselines agrupam versões publicadas após revisão." },
              { id: "R9",  type: "BLOQUEIA",      from: "US-019", to: "US-022", autor: "José Vítor", just: "Vincular tarefas exige conexão configurada." },
              { id: "R10", type: "RELACIONADO_A", from: "US-023", to: "US-001", autor: "Fernando",  just: "Alternativa de autenticação, cancelada." },
            ],
          },
        },
        {
          id: "P2", name: "Portal do Parceiro", desc: "Área logada para lojistas consultarem vendas",
          archived: true,
          data: {
            project: { name: "Portal do Parceiro", code: "PAR", created: "12/08/2026" },
            seq: { req: 3, epico: 2, rel: 1 },
            epicos: [
              { id: "E1", name: "Onboarding",  open: true,  x: 130, y: 120 },
              { id: "E2", name: "Relatórios",  open: false, x: 480, y: 120 },
            ],
            requirements: [
              { id: "US-001", title: "Cadastro de lojista", epicoId: "E1", tipo: "História de usuário",
                situacao: "ATIVO", status: "PUBLICADA", pubVersion: 1, updated: "há 2 semanas", autor: "Ana Clara",
                origem: { tipo: "Interno", detalhe: "Criado no ATHENA em 12/08/2026 por Ana Clara" },
                conteudo: `<h2>Especificação</h2><p>Eu, como <strong>lojista</strong>, quero <strong>criar conta com CNPJ e e-mail</strong>, para <strong>acompanhar minhas vendas</strong>.</p>`,
                criterios: [] },
              { id: "US-002", title: "Dashboard de vendas", epicoId: "E1", tipo: "História de usuário",
                situacao: "ATIVO", status: "RASCUNHO", pubVersion: null, updated: "há 5 dias", autor: "Ana Clara",
                origem: { tipo: "Interno", detalhe: "Criado no ATHENA em 15/08/2026 por Ana Clara" },
                conteudo: `<h2>Especificação</h2><p>Eu, como <strong>lojista</strong>, quero <strong>visualizar o resumo diário de vendas</strong>, para <strong>acompanhar o faturamento</strong>.</p>`,
                criterios: [] },
              { id: "US-003", title: "Exportar relatórios em CSV", epicoId: "E2", tipo: "História de usuário",
                situacao: "ATIVO", status: "AGUARDANDO_REVISAO", pubVersion: null, updated: "há 1 dia", autor: "Ana Clara",
                origem: { tipo: "Interno", detalhe: "Criado no ATHENA em 20/08/2026 por Ana Clara" },
                conteudo: `<h2>Especificação</h2><p>Eu, como <strong>lojista</strong>, quero <strong>exportar relatórios em CSV</strong>, para <strong>analisar fora do portal</strong>.</p>`,
                criterios: [] },
            ],
            relations: [
              { id: "R1", type: "DEPENDE_DE", from: "US-002", to: "US-001", autor: "Ana Clara", just: "O dashboard exige lojista cadastrado." },
            ],
          },
        },
      ],
    },
    {
      id: "W2", name: "InfinitePay — Engenharia",
      projects: [
        {
          id: "P3", name: "App Mobile", desc: "Checkout one-click no app",
          archived: false,
          data: {
            project: { name: "App Mobile", code: "APP", created: "02/09/2026" },
            seq: { req: 1, epico: 0, rel: 0 },
            epicos: [
              { id: "E1", name: "Checkout", open: true, x: 140, y: 120 },
            ],
            requirements: [
              { id: "US-001", title: "Checkout em 1 clique", epicoId: "E1", tipo: "História de usuário",
                situacao: "ATIVO", status: "RASCUNHO", pubVersion: null, updated: "há 3 dias", autor: "Ana Clara",
                origem: { tipo: "Interno", detalhe: "Criado no ATHENA em 02/09/2026 por Ana Clara" },
                conteudo: `<h2>Especificação</h2><p>Eu, como <strong>cliente</strong>, quero <strong>finalizar compras com um toque</strong>, para <strong>reduzir o atrito no pagamento</strong>.</p>`,
                criterios: [] },
            ],
            relations: [],
          },
        },
      ],
    },
  ],
};

// ---------- Requisitos do projeto ATHENA (P1) ----------
(function seedAthena() {
  const d = DB.workspaces[0].projects[0].data;
  d.requirements = [
    {
      id: "US-001", title: "Acesso à conta (Login)", epicoId: "E1",
      tipo: "História de usuário", situacao: "ATIVO", status: "AGUARDANDO_REVISAO",
      pubVersion: 1, updated: "há 2 horas", autor: "Fernando",
      origem: { tipo: "Interno", detalhe: "Criado no ATHENA em 05/10/2026 por Fernando" },
      conteudo: `
        <h2>Especificação</h2>
        <p>Eu, como <strong>usuário registrado</strong>, quero <strong>autenticar-me com e-mail e senha</strong>, para <strong>acessar meus workspaces e projetos com segurança</strong>.</p>
        <p>O login emite um JWT mantido na memória do navegador; a renovação usa cookie protegido. Falhas de autenticação exibem mensagem genérica (“Credenciais inválidas”), sem revelar qual campo está incorreto.</p>
        <h2>Regras de negócio</h2>
        <ul><li>A autenticação falha se o e-mail não existir ou a senha estiver incorreta, com mensagem idêntica nos dois casos.</li><li>A opção “Lembrar este dispositivo” persiste a sessão de forma segura.</li></ul>`,
      criterios: [
        { ctx: "Credenciais válidas", dado: "que o usuário insere e-mail e senha válidos", quando: "clica em “Entrar”", entao: "a sessão é autenticada e ele é redirecionado à Visão Geral de Workspaces" },
        { ctx: "Sessão persistente", dado: "que “Lembrar este dispositivo” está marcado", quando: "o login é concluído", entao: "o token é armazenado de forma persistente" },
      ],
    },
    {
      id: "US-002", title: "Criação de conta (Registo)", epicoId: "E1",
      tipo: "História de usuário", situacao: "ATIVO", status: "PUBLICADA",
      pubVersion: 2, updated: "ontem", autor: "José Vítor",
      origem: { tipo: "Interno", detalhe: "Criado no ATHENA em 04/10/2026 por José Vítor" },
      conteudo: `
        <h2>Especificação</h2>
        <p>Eu, como <strong>novo usuário</strong>, quero <strong>criar conta com nome, e-mail e senha segura</strong>, para <strong>começar a utilizar a plataforma</strong>.</p>
        <p>O formulário inclui validação visual em tempo real dos requisitos de complexidade da senha e exige aceite dos Termos e da Política de privacidade.</p>
        <h2>Regras de negócio</h2>
        <ul><li>A senha cumpre 4 requisitos: mínimo de 8 caracteres, letras, números e caractere especial.</li><li>E-mail já registrado impede a criação da conta.</li></ul>`,
      criterios: [
        { ctx: "Senha válida", dado: "que a senha cumpre os 4 requisitos do checklist", quando: "o usuário digita", entao: "cada indicador correspondente fica verde com um check" },
      ],
    },
    {
      id: "US-003", title: "Recuperação de acesso e nova senha", epicoId: "E1",
      tipo: "História de usuário", situacao: "ATIVO", status: "RASCUNHO",
      pubVersion: 1, updated: "há 3 dias", autor: "Fernando",
      origem: { tipo: "Interno", detalhe: "Criado no ATHENA em 06/10/2026 por Fernando" },
      conteudo: `
        <h2>Especificação</h2>
        <p>Eu, como <strong>usuário que esqueceu a senha</strong>, quero <strong>solicitar um código por e-mail e redefinir credenciais</strong>, para <strong>recuperar acesso seguro à plataforma</strong>.</p>
        <p>Fluxo em duas etapas: “Recuperar acesso” (e-mail) e “Criar nova senha” (código de 6 dígitos + nova senha).</p>`,
      criterios: [
        { ctx: "Código válido", dado: "que o usuário insere o código recebido e senhas coincidentes", quando: "clica em “Redefinir senha”", entao: "a credencial é atualizada e ele é redirecionado ao Login com mensagem de sucesso" },
        { ctx: "Código expirado", dado: "que o tempo limite de 10 minutos expirou", quando: "o usuário tenta redefinir", entao: "o sistema retorna erro de expiração e oferece “Reenviar código”" },
      ],
    },
    {
      id: "US-023", title: "Login social via Google", epicoId: "E1",
      tipo: "História de usuário", situacao: "CANCELADO", status: "RASCUNHO",
      pubVersion: null, updated: "há 1 semana", autor: "Fernando",
      origem: { tipo: "Interno", detalhe: "Cancelado em 18/10/2026 — fora do escopo do MVP" },
      conteudo: `<p>Cancelada por decisão de escopo: autenticação social fica para evolução posterior do MVP.</p>`,
      criterios: [],
    },
    {
      id: "US-004", title: "Gestão de workspaces", epicoId: "E2",
      tipo: "História de usuário", situacao: "ATIVO", status: "PUBLICADA",
      pubVersion: 2, updated: "há 2 dias", autor: "José Vítor",
      origem: { tipo: "Interno", detalhe: "Criado no ATHENA em 07/10/2026 por José Vítor" },
      conteudo: `
        <h2>Especificação</h2>
        <p>Eu, como <strong>usuário do sistema</strong>, quero <strong>criar, editar e excluir workspaces</strong>, para <strong>organizar os projetos em áreas isoladas</strong>.</p>
        <h2>Regras de negócio</h2>
        <ul><li>Apenas o Owner do workspace pode renomeá-lo ou excluí-lo.</li><li>A exclusão exige confirmação digitando o nome exato.</li></ul>`,
      criterios: [
        { ctx: "Novo workspace", dado: "que o usuário clica em “+” na barra lateral", quando: "preenche o nome e clica em “Criar”", entao: "o modal fecha e o workspace aparece selecionado na barra lateral" },
      ],
    },
    {
      id: "US-005", title: "Gestão de projetos", epicoId: "E2",
      tipo: "História de usuário", situacao: "ATIVO", status: "RASCUNHO",
      pubVersion: 1, updated: "hoje", autor: "Ana Clara",
      origem: { tipo: "Interno", detalhe: "Criado no ATHENA em 08/10/2026 por Ana Clara" },
      conteudo: `
        <h2>Especificação</h2>
        <p>Eu, como <strong>usuário</strong>, quero <strong>criar, visualizar e acessar projetos do workspace</strong>, para <strong>gerenciar requisitos de cada produto</strong>.</p>
        <p>A aba “Projetos” lista os ativos; “Projetos arquivados” funciona como repositório secundário com empty state próprio.</p>`,
      criterios: [
        { ctx: "Abrir projeto", dado: "que o usuário clica em “Abrir” no card do projeto", quando: "o evento é processado", entao: "ele é redirecionado ao contexto interno daquele projeto" },
      ],
    },
    {
      id: "US-009", title: "Editor de requisitos (WYSIWYG)", epicoId: "E3",
      tipo: "História de usuário", situacao: "ATIVO", status: "APROVADA",
      pubVersion: 1, updated: "há 5 horas", autor: "Ana Clara",
      origem: { tipo: "Interno", detalhe: "Criado no ATHENA em 09/10/2026 por Ana Clara" },
      conteudo: `
        <h2>Especificação</h2>
        <p>Eu, como <strong>Editor</strong>, quero <strong>redigir o corpo do requisito com formatação avançada</strong>, para <strong>garantir legibilidade e estrutura das regras de negócio</strong>.</p>
        <h2>Campos e comportamento</h2>
        <ul><li>Barra de ferramentas fixa no topo durante o scroll (negrito, itálico, listas, tabelas, links).</li><li>Auto-save com debounce de ~2s e indicador “Salvo / Alterações pendentes”.</li><li>Título editável direto no cabeçalho do editor.</li></ul>`,
      criterios: [
        { ctx: "Auto-save", dado: "que o usuário altera o conteúdo e faz uma pausa na digitação", quando: "o debounce de ~2s dispara", entao: "o rascunho é salvo silenciosamente e o indicador muda para “Salvo”" },
      ],
    },
    {
      id: "US-010", title: "Estruturação de critérios de aceitação (Dado/Quando/Então)", epicoId: "E3",
      tipo: "História de usuário", situacao: "ATIVO", status: "RASCUNHO",
      pubVersion: null, updated: "há 1 dia", autor: "Ana Clara",
      origem: { tipo: "Interno", detalhe: "Criado no ATHENA em 09/10/2026 por Ana Clara" },
      conteudo: `
        <h2>Especificação</h2>
        <p>Eu, como <strong>usuário</strong>, quero <strong>registrar critérios de aceitação no formato Dado/Quando/Então</strong>, para <strong>alinhar os cenários ao comportamento esperado (BDD)</strong>.</p>
        <p>Blocos dinâmicos com “+ Critério”, remoção por bloco e persistência estruturada (JSON) separada do texto livre.</p>`,
      criterios: [],
    },
    {
      id: "US-016", title: "Criação de relações e rastreabilidade", epicoId: "E4",
      tipo: "História de usuário", situacao: "ATIVO", status: "PUBLICADA",
      pubVersion: 2, updated: "há 6 horas", autor: "Fernando",
      origem: { tipo: "Interno", detalhe: "Criado no ATHENA em 10/10/2026 por Fernando" },
      conteudo: `
        <h2>Especificação</h2>
        <p>Eu, como <strong>usuário</strong>, quero <strong>criar dependências explícitas entre duas User Stories</strong>, para <strong>mapear como as funcionalidades se afetam</strong>.</p>
        <h2>Regras de negócio</h2>
        <ul><li>Catálogo do MVP: DEPENDE_DE, BLOQUEIA (direcionais) e RELACIONADO_A (sem direção).</li><li>São proibidas autorrelações, duplicações equivalentes e ciclos de precedência.</li></ul>`,
      criterios: [
        { ctx: "Nova relação", dado: "que o usuário clica em “+ Conectar relação” e seleciona destino e semântica", quando: "confirma a criação", entao: "a relação é persistida no modelo oficial e contagens e mapa são atualizados" },
      ],
    },
    {
      id: "US-017", title: "Grafo vetorial de relações", epicoId: "E4",
      tipo: "História de usuário", situacao: "ATIVO", status: "EM_SUBMISSAO",
      pubVersion: null, updated: "agora", autor: "Ana Clara",
      origem: { tipo: "Interno", detalhe: "Criado no ATHENA em 10/10/2026 por Ana Clara" },
      conteudo: `
        <h2>Especificação</h2>
        <p>Eu, como <strong>usuário</strong>, quero <strong>visualizar as ligações e etiquetas de relação desenhadas entre cartões no mapa</strong>, para <strong>identificar gargalos e ordens de execução</strong>.</p>
        <p>DEPENDE_DE aponta do dependente ao pré-requisito; BLOQUEIA, do bloqueador ao bloqueado; RELACIONADO_A não tem seta.</p>`,
      criterios: [],
    },
    {
      id: "US-028", title: "Lotes de revisão (aprovação e rejeição)", epicoId: "E5",
      tipo: "História de usuário", situacao: "ATIVO", status: "AGUARDANDO_REVISAO",
      pubVersion: 1, updated: "há 4 horas", autor: "José Vítor",
      origem: { tipo: "Interno", detalhe: "Criado no ATHENA em 11/10/2026 por José Vítor" },
      conteudo: `
        <h2>Especificação</h2>
        <p>Eu, como <strong>Aprovador</strong>, quero <strong>avaliar lotes de revisão pendentes</strong>, para <strong>aprovar ou rejeitar candidatas antes da publicação</strong>.</p>
        <h2>Regras de negócio</h2>
        <ul><li>A interface exibe o snapshot submetido, bloqueado para edições.</li><li>Rejeição exige justificativa e devolve a candidata a Rascunho, preservando a tentativa.</li></ul>`,
      criterios: [
        { ctx: "Aprovar", dado: "que a candidata está AGUARDANDO_REVISAO", quando: "o Aprovador clica em “Aprovar”", entao: "o lote é encerrado e a candidata transita para APROVADA" },
      ],
    },
    {
      id: "US-030", title: "Baselines de conteúdo (congelamento de escopo)", epicoId: "E5",
      tipo: "História de usuário", situacao: "ATIVO", status: "RASCUNHO",
      pubVersion: null, updated: "há 2 dias", autor: "Fernando",
      origem: { tipo: "Interno", detalhe: "Criado no ATHENA em 12/10/2026 por Fernando" },
      conteudo: `
        <h2>Especificação</h2>
        <p>Eu, como <strong>Editor</strong>, quero <strong>agrupar versões publicadas em uma Baseline</strong>, para <strong>criar marcos históricos imutáveis do escopo</strong>.</p>
        <p>Ex.: “Fechamento da Release 1.0”. Baselines referenciam conjuntos imutáveis de versões e retratos.</p>`,
      criterios: [],
    },
    {
      id: "US-019", title: "Conexão com gestores de tarefas (GitHub / OpenProject)", epicoId: "E6",
      tipo: "História de usuário", situacao: "ATIVO", status: "PUBLICADA",
      pubVersion: 1, updated: "há 3 dias", autor: "José Vítor",
      origem: { tipo: "Interno", detalhe: "Criado no ATHENA em 13/10/2026 por José Vítor" },
      conteudo: `
        <h2>Especificação</h2>
        <p>Eu, como <strong>usuário</strong>, quero <strong>configurar a ligação com GitHub e OpenProject</strong>, para <strong>vincular User Stories a issues técnicas</strong>.</p>
        <p>Personal access token obrigatório para ativação; o botão “Conectar” só fica ativo com o token preenchido.</p>`,
      criterios: [
        { ctx: "Conectar GitHub", dado: "que o Owner insere um access token válido", quando: "clica em “Conectar GitHub”", entao: "o sistema valida as credenciais e o estado muda para “Conectado”" },
      ],
    },
    {
      id: "US-022", title: "Vincular tarefas externas ao requisito", epicoId: "E6",
      tipo: "História de usuário", situacao: "ATIVO", status: "RASCUNHO",
      pubVersion: null, updated: "há 1 dia", autor: "Ana Clara",
      origem: { tipo: "Interno", detalhe: "Criado no ATHENA em 13/10/2026 por Ana Clara" },
      conteudo: `
        <h2>Especificação</h2>
        <p>Eu, como <strong>usuário</strong>, quero <strong>vincular a User Story a tickets no GitHub/OpenProject a partir do editor</strong>, para <strong>manter rastreabilidade entre documentação e execução</strong>.</p>
        <p>Sem integrações configuradas, o dropdown “Destino” fica inativo com a mensagem “Nenhum destino autorizado”.</p>`,
      criterios: [],
    },
  ];
})();

// ---------- Contexto do projeto ativo (preenchido por loadProject) ----------
window.ATH = {
  projectId: null, workspaceId: null,
  project: null, epicos: [], requirements: [], relations: [], seq: { req: 0, epico: 0, rel: 0 },
  ui: {
    view: "visao", currentReq: null, prevView: "visao", search: "",
    filters: { ativo: true, cancelado: false, estado: "TODOS", pub: "TODOS", epico: "TODOS" },
    settingsTab: "pastas", editorDrawer: false,
  },
};

// ---------- Constantes de estado ----------
window.STATUS_LABEL = {
  RASCUNHO: "Rascunho",
  EM_SUBMISSAO: "Em submissão",
  AGUARDANDO_REVISAO: "Aguardando revisão",
  APROVADA: "Aprovada",
  PUBLICADA: "Publicada",
};

// Template "User Story completa" (US07)
window.TEMPLATE_US = `
  <h2>Especificação</h2>
  <p>Eu, como <strong>[tipo de usuário]</strong>, quero <strong>[objetivo]</strong>, para <strong>[benefício / motivação]</strong>.</p>
  <h2>Campos e comportamento</h2>
  <ul><li>[Descrever os campos e comportamentos esperados.]</li></ul>
  <h2>Regras de negócio</h2>
  <ul><li>[Descrever as regras aplicáveis.]</li></ul>
`;
