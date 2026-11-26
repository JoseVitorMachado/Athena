// Smoke test funcional do protótipo ATHENA via jsdom
// Fluxo completo: US01–US03 (auth) → US04–US06 (workspaces/projetos) → US07–US18 (épico)
const fs = require("fs");
const path = require("path");
const { JSDOM } = require("jsdom");

const ROOT = "/Users/anaclaragomes/Projects/athena-prototype";
let html = fs.readFileSync(path.join(ROOT, "index.html"), "utf8");
html = html.replace(/<link[^>]*>/g, "");
for (const f of ["data.js", "map.js", "auth.js", "home.js", "app.js"]) {
  const code = fs.readFileSync(path.join(ROOT, f), "utf8");
  html = html.replace(`<script src="${f}"></script>`, () => `<script>\n${code}\n</script>`);
}

const dom = new JSDOM(html, { runScripts: "dangerously", pretendToBeVisual: true });
const w = dom.window;
const d = w.document;
w.document.execCommand = () => true;

let pass = 0, fail = 0;
function check(name, cond) {
  if (cond) { pass++; console.log("  ✓ " + name); }
  else { fail++; console.log("  ✗ FALHOU: " + name); }
}
function q(sel) { return d.querySelector(sel); }
function qa(sel) { return [...d.querySelectorAll(sel)]; }
function click(el) { el && el.dispatchEvent(new w.MouseEvent("click", { bubbles: true })); }
function submit(form) { form.dispatchEvent(new w.Event("submit", { bubbles: true, cancelable: true })); }
function input(el, val) { el.value = val; el.dispatchEvent(new w.Event("input", { bubbles: true })); }
const wait = ms => new Promise(r => setTimeout(r, ms));

(async () => {
  await wait(60);

  console.log("\n[A] Login (US02)");
  check("tela de login renderizada", !!q("#loginForm"));
  check("marca ATHENA presente", !!q(".brand-name"));
  check("checkbox 'Lembrar este dispositivo' (desmarcado)", !!q("#liRemember") && !q("#liRemember").checked);
  check("links para recuperação e criar conta", !!qa("[data-auth]").find(a => a.dataset.auth === "recover") && !!qa("[data-auth]").find(a => a.dataset.auth === "signup"));
  submit(q("#loginForm")); // vazio
  check("submissão vazia bloqueada", !!q("#liError") && q("#liError").style.display === "block");
  input(q("#liEmail"), "errado@exemplo.com"); input(q("#liPass"), "senhaerrada");
  submit(q("#loginForm"));
  await wait(900);
  check("credenciais inválidas → mensagem genérica", q("#liError").textContent.includes("Credenciais inválidas"));
  input(q("#liEmail"), "anagomes@athena.dev"); input(q("#liPass"), "Athena@2026");
  submit(q("#loginForm"));
  await wait(900);
  check("login ok → home de workspaces", !!q(".home") && !!q(".ws-list"));
  check("dois workspaces listados", qa(".ws-item").length === 2);
  check("workspace ativo: W1 (UFLA)", qa(".ws-item")[0].classList.contains("active"));
  check("tab Projetos com 2 ativos + 1 arquivado (contador)", q("[data-htab='arquivados']").textContent.includes("1"));
  check("cards de projeto ativo renderizados", qa(".proj-card").length === 1 && qa(".proj-card")[0].textContent.includes("Plataforma ATHENA"));
  check("projeto arquivado fora da aba Projetos", !qa(".proj-card").some(c => c.textContent.includes("Portal do Parceiro")));

  console.log("\n[B] Abrir projeto → contexto do épico");
  click(qa("[data-open]").find(b => b.dataset.open === "P1"));
  check("shell do projeto com tabs Visão geral/Requisitos/Mapa", !!q("#mainTabs") && q("#mainTabs").textContent.includes("Mapa"));
  check("chip mostra o projeto atual", q("#chipName").textContent.includes("Plataforma ATHENA"));
  check("6 cartões de épico", qa(".epico-card").length === 6);
  click(q("#homeBtn"));
  check("botão home volta aos workspaces", !!q(".home"));

  console.log("\n[C] Logout e Criar conta (US01)");
  click(q("#homeAvatar"));
  await wait(30);
  check("menu do usuário com Modo escuro e Sair", qa(".menu-item").some(m => m.textContent.includes("Modo escuro")) && !!q("[data-menu-id='sair']"));
  click(q("[data-menu-id='sair']"));
  check("sair volta ao login", !!q("#loginForm") && !w.DB.session.user);
  click(qa("[data-auth]").find(a => a.dataset.auth === "signup"));
  check("tela criar conta com checklist de senha", !!q("#signupForm") && qa(".pw-item").length === 4);
  input(q("#suPass"), "Senha123");
  check("checklist parcial (3 de 4 ok, sem especial)", qa(".pw-item.ok").length === 3);
  input(q("#suPass"), "Senha123!");
  check("checklist completo (4 de 4)", qa(".pw-item.ok").length === 4);
  submit(q("#signupForm")); // nome vazio
  check("nome obrigatório bloqueia", q("#suError").textContent.includes("nome"));
  input(q("#suName"), "Maria Teste");
  input(q("#suEmail"), "anagomes@athena.dev"); // já registrado
  input(q("#suPass2"), "Senha123!");
  submit(q("#signupForm"));
  check("e-mail já registrado é recusado", q("#suError").textContent.includes("registrado"));
  input(q("#suEmail"), "maria@teste.dev");
  input(q("#suPass2"), "OutraSenha9");
  submit(q("#signupForm"));
  check("senhas divergentes bloqueiam", q("#suError").textContent.includes("coincidem"));
  input(q("#suPass2"), "Senha123!");
  submit(q("#signupForm"));
  check("termos não aceitos bloqueiam", q("#suError").textContent.includes("Termos"));
  click(q("#suTerms"));
  submit(q("#signupForm"));
  check("conta criada → sessão automática → home", w.DB.session.user && w.DB.session.user.name === "Maria Teste" && !!q(".home"));
  check("novo usuário pode logar depois", w.DB.users.length === 2);
  click(q("#homeAvatar")); await wait(30); click(q("[data-menu-id='sair']"));

  console.log("\n[D] Recuperação de acesso e nova senha (US03)");
  click(qa("[data-auth]").find(a => a.dataset.auth === "recover"));
  check("tela recuperar acesso", !!q("#recForm"));
  submit(q("#recForm"));
  check("e-mail vazio bloqueado", q("#recError").style.display === "block");
  input(q("#recEmail"), "anagomes@athena.dev");
  submit(q("#recForm"));
  await wait(1000);
  check("mensagem padronizada exibida (sem revelar existência da conta)", !!q("#recSuccess") && q("#recSuccess").textContent.includes("Se houver uma conta"));
  await wait(1500);
  check("avança para Criar nova senha (6 caixas)", qa(".code-box").length === 6);
  check("botão Redefinir desabilitado até preencher", q("#rsSubmit").disabled);
  const boxes = qa(".code-box");
  boxes.forEach((b, i) => input(b, "0"));
  input(q("#rsPass"), "NovaSenha@1"); input(q("#rsPass2"), "NovaSenha@1");
  check("botão habilita com código + senhas", !q("#rsSubmit").disabled);
  // código errado
  submit(q("#resetForm"));
  check("código errado recusado", q("#rsError").textContent.includes("Código inválido"));
  const code = w.DB.devCode;
  code.split("").forEach((c, i) => input(boxes[i], c));
  input(q("#rsPass"), "NovaSenha@1"); input(q("#rsPass2"), "NovaSenha@1");
  submit(q("#resetForm"));
  check("senha redefinida → volta ao login", q("#loginForm") && w.DB.users.find(u => u.email === "anagomes@athena.dev").password === "NovaSenha@1");
  // login com a senha nova
  input(q("#liEmail"), "anagomes@athena.dev"); input(q("#liPass"), "NovaSenha@1");
  submit(q("#loginForm"));
  await wait(900);
  check("login com senha redefinida funciona", !!q(".home"));

  console.log("\n[E] Workspaces (US04)");
  const wsQ = q("#wsSearch");
  input(wsQ, "InfinitePay");
  check("busca de workspace filtra em tempo real", qa(".ws-item").length === 1);
  input(wsQ, "");
  click(q("#addWs"));
  check("modal adicionar workspace", !!q("#nwName"));
  click(q("#nwCreate"));
  check("validação: nome obrigatório", q("#nwErr").style.display === "block");
  input(q("#nwName"), "UFLA — Engenharia");
  click(q("#nwCreate"));
  check("workspace criado e selecionado (vazio)", qa(".ws-item").length === 3 && q(".ws-item.active").textContent.includes("UFLA — Engenharia"));
  click(q("#wsMenu"));
  await wait(30);
  click(qa(".menu-item").find(m => m.textContent.includes("Renomear")));
  input(q("#rwName"), "UFLA — Engenharia Civil");
  click(q("#rwSave"));
  check("workspace renomeado na sidebar", q(".ws-item.active").textContent.includes("Engenharia Civil"));
  click(q("#wsMenu")); await wait(30);
  click(qa(".menu-item").find(m => m.textContent.includes("Excluir workspace")));
  check("modal exige digitar nome exato (botão desabilitado)", !!q("#dwName") && q("#dwDelete").disabled);
  input(q("#dwName"), "nome errado");
  check("nome diferente mantém botão desabilitado", q("#dwDelete").disabled);
  input(q("#dwName"), "UFLA — Engenharia Civil");
  check("nome exato habilita exclusão", !q("#dwDelete").disabled);
  click(q("#dwDelete"));
  check("workspace removido da lista", qa(".ws-item").length === 2);

  console.log("\n[F] Projetos (US05/US06)");
  click(qa(".ws-item")[0]); // W1
  click(q("#newProj"));
  check("modal novo projeto", !!q("#npName"));
  input(q("#npName"), "Plataforma ATHENA"); // duplicado no workspace
  click(q("#npCreate"));
  check("nome único por workspace é exigido", q("#npErr").textContent.includes("único") || q("#npErr").textContent.includes("mesmo nome"));
  input(q("#npName"), "Portal do Cliente");
  input(q("#npDesc"), "Área do cliente final");
  click(q("#npCreate"));
  check("projeto criado aparece com 0 requisitos", qa(".proj-card").some(c => c.textContent.includes("Portal do Cliente") && c.textContent.includes("0 requisito")));
  // arquivar
  const cardPC = qa(".proj-card").find(c => c.textContent.includes("Portal do Cliente"));
  click(cardPC.querySelector("[data-proj-menu]"));
  await wait(30);
  click(qa(".menu-item").find(m => m.textContent.includes("Arquivar")));
  await wait(30);
  click(q("#cfmOk"));
  check("projeto arquivado sai da aba ativa", !qa(".proj-card").some(c => c.textContent.includes("Portal do Cliente")));
  click(q("[data-htab='arquivados']"));
  check("contador de arquivados = 2", q("[data-htab='arquivados']").textContent.includes("2"));
  check("descrição contextual do workspace", q(".arch-desc").textContent.includes("UFLA — Sistemas Distribuídos"));
  const cardArq = qa(".proj-card").find(c => c.textContent.includes("Portal do Cliente"));
  check("card arquivado com botão Restaurar", !!cardArq.querySelector("[data-restore]"));
  // excluir permanentemente com confirmação por nome
  click(cardArq.querySelector("[data-proj-menu]"));
  await wait(30);
  click(qa(".menu-item").find(m => m.textContent.includes("Excluir permanentemente")));
  check("exclusão exige nome exato", !!q("#dpDelete") && q("#dpDelete").disabled);
  input(q("#dpName"), "Portal do Cliente");
  click(q("#dpDelete"));
  check("exclusão permanente aplicada (contador = 1)", q("[data-htab='arquivados']").textContent.includes("1"));
  // restaurar o Portal do Parceiro
  const cardPP = qa(".proj-card").find(c => c.textContent.includes("Portal do Parceiro"));
  click(cardPP.querySelector("[data-restore]"));
  check("restaurado volta à aba Projetos", q(".home-tabs").textContent.includes("arquivados") && q("[data-htab='arquivados']").textContent.includes("0"));
  click(q("[data-htab='projetos']"));
  check("Portal do Parceiro restaurado listado", qa(".proj-card").some(c => c.textContent.includes("Portal do Parceiro")));
  // re-arquivar para manter o estado do seed para os próximos testes
  const cardPP2 = qa(".proj-card").find(c => c.textContent.includes("Portal do Parceiro"));
  click(cardPP2.querySelector("[data-proj-menu]")); await wait(30);
  click(qa(".menu-item").find(m => m.textContent.includes("Arquivar"))); await wait(30);
  click(q("#cfmOk"));

  console.log("\n[G] Trocar projeto (US24)");
  const cardP1 = qa(".proj-card").find(c => c.textContent.includes("Plataforma ATHENA"));
  click(cardP1.querySelector("[data-open]"));
  check("voltou ao projeto ATHENA", q("#chipName").textContent.includes("Plataforma ATHENA"));
  click(q("#projChip"));
  check("modal trocar projeto com duas colunas", !!q("#tpWsList") && !!q("#tpProjList"));
  check("flag 'Atual' no projeto corrente", !!q(".tp-flag"));
  input(q("#tpSearch"), "InfinitePay");
  await wait(30);
  click(qa("[data-tp-ws]")[0]);
  input(q("#tpSearch"), "");
  await wait(30);
  check("coluna direita lista projetos do workspace selecionado", q("#tpProjList").textContent.includes("App Mobile"));
  click(qa("[data-tp-proj]")[0]);
  check("contexto muda para App Mobile (isolamento de dados)", q("#chipName").textContent.includes("App Mobile") && w.ATH.requirements.length === 1);
  click(q("#mainTabs .tab[data-view='mapa']") || qa("#mainTabs .tab")[2]);
  check("mapa do App Mobile: 1 cartão, 0 arestas", qa(".mcard").length === 1 && qa(".edge").length === 0);
  click(q("#projChip"));
  input(q("#tpSearch"), "UFLA");
  await wait(30);
  click(qa("[data-tp-ws]")[0]);
  input(q("#tpSearch"), "");
  await wait(30);
  const tpP1 = qa("[data-tp-proj]").find(b => b.textContent.includes("Plataforma ATHENA"));
  click(tpP1);
  check("troca de volta para Plataforma ATHENA", w.ATH.requirements.length === 14 && qa(".epico-card").length === 6);

  console.log("\n[H] Visão Geral (US08)");
  check("contagem de US no épico E1 (4 US)", qa(".epico-card")[0].textContent.includes("4 US"));
  check("US cancelada não aparece em épicos ativos", !qa(".epico-card")[0].textContent.includes("US-023"));
  check("expansão: E1 aberto lista US-001", qa(".epico-card")[0].textContent.includes("US-001"));
  const e2head = qa(".epico-card")[1].querySelector("[data-toggle-epico]");
  click(e2head);
  check("E2 aberto após clique", qa(".epico-card")[1].classList.contains("open"));
  click(e2head);
  check("E2 fechado após segundo clique", !qa(".epico-card")[1].classList.contains("open"));

  console.log("\n[I] Modal Novo Épico (US08)");
  click(q("[data-new-epico]"));
  check("modal aberto", !!q("#epName"));
  input(q("#epName"), "");
  click(q("#epCreate"));
  check("validação: nome obrigatório", q("#epErr").style.display === "block");
  input(q("#epName"), "Pagamentos e Cash-out");
  click(q("#epCreate"));
  check("épico criado aparece com 0 US", qa(".epico-card").length === 7 && d.body.textContent.includes("Pagamentos e Cash-out"));

  console.log("\n[J] Modal Adicionar Requisito (US07)");
  click(q("[data-new-req]"));
  check("modal aberto com select de épico", !!q("#rqEpico"));
  check("radio 'User Story completa' presente", !!qa("input[name=start]")[1]);
  input(q("#rqTitle"), "Cobrança por boleto");
  qa("input[name=start]")[1].click();
  click(q("#rqCreate"));
  check("redirecionado ao editor com template", !!q("#docPage") && q("#docPage").textContent.includes("Especificação"));
  check("nova US em rascunho", w.ATH.requirements.some(r => r.title === "Cobrança por boleto" && r.status === "RASCUNHO"));

  console.log("\n[K] Editor e ciclo de vida (US09–US13)");
  check("título editável", !!q("#edTitle"));
  check("indicador de salvamento", q("#saveState").textContent.includes("Salvo"));
  click(q("#addCrit"));
  check("novo bloco Dado/Quando/Então adicionado", qa(".crit-block").length === 1);
  click(q("[data-crit-rm]"));
  check("critério removido", qa(".crit-block").length === 0);
  check("gaveta detalhes com dropdown de pasta", !!q("#dwPasta"));
  click(q("[data-action='submit']"));
  check("candidata em EM_SUBMISSAO (SAGA)", w.ATH.requirements.find(r => r.title === "Cobrança por boleto").status === "EM_SUBMISSAO");
  await wait(1800);
  check("após SAGA: AGUARDANDO_REVISAO", w.ATH.requirements.find(r => r.title === "Cobrança por boleto").status === "AGUARDANDO_REVISAO");
  click(q("[data-action='approve']"));
  check("candidata APROVADA", w.ATH.requirements.find(r => r.title === "Cobrança por boleto").status === "APROVADA");
  click(q("[data-action='publish']"));
  await wait(50);
  click(q("#cfmOk"));
  check("PUBLICADA com v1", (() => { const r = w.ATH.requirements.find(r => r.title === "Cobrança por boleto"); return r.status === "PUBLICADA" && r.pubVersion === 1; })());
  q("#docPage").dispatchEvent(new w.Event("input", { bubbles: true }));
  check("edição invalida PUBLICADA → RASCUNHO", w.ATH.requirements.find(r => r.title === "Cobrança por boleto").status === "RASCUNHO");

  console.log("\n[L] Aba Requisitos (US18)");
  click(qa("#mainTabs .tab")[1]);
  check("tabela com colunas esperadas", q("table.req-table").textContent.includes("Estado da candidata") && q("table.req-table").textContent.includes("Versão publicada"));
  check("linhas = 14 ativas (13 originais + nova) sem cancelada", qa("table.req-table tbody tr[data-open-req]").length === 14);
  check("US-023 (cancelada) fora por padrão", !qa("table.req-table")[0].textContent.includes("US-023"));
  click(qa(".filter-chip")[0]);
  click(qa(".filter-chip")[1]);
  check("ATIVO off + CANCELADO on → só canceladas", qa("table.req-table tbody tr[data-open-req]").length === 1);
  click(qa(".filter-chip")[1]);
  check("estado vazio exibido (ambos off)", d.body.textContent.includes("Nenhuma história de usuário encontrada para os filtros aplicados"));
  click(qa(".filter-chip")[0]); // volta ao padrão

  console.log("\n[M] Mapa (US14–US17)");
  click(qa("#mainTabs .tab")[2]);
  check("pastas de épicos no canvas (7)", qa(".folder").length === 7);
  check("cartões de US renderizados (15)", qa(".mcard").length === 15);
  check("arestas SVG desenhadas (10 relações)", qa(".edge").length === 10);
  check("cartão cancelado com estilo distinto", qa(".mcard.cancelled").length === 1);
  const card001 = qa(".mcard").find(m => m.dataset.id === "US-001");
  click(card001);
  check("gaveta abre com detalhes", q("#mapDrawer").classList.contains("open"));
  check("acordeões Relações/Critérios/Origem", !!q("#accRel") && !!q("#accCrit") && !!q("#accOrig"));
  check("leituras dos extremos (sem duplicar arestas)", q("#mapDrawer").textContent.includes("Depende de") && q("#mapDrawer").textContent.includes("Relacionada a"));

  console.log("\n[N] Conectar relação (US16)");
  const card010 = qa(".mcard").find(m => m.dataset.id === "US-010");
  click(card010);
  click(q("#btnConnect"));
  check("modal de conexão aberto", !!q("#relSearch"));
  input(q("#relSearch"), "US-009");
  click(qa(".sl-item")[0]);
  qa("#relSemWrap input[name=sem]")[0].click();
  click(q("#relCreate"));
  check("duplicata rejeitada", w.ATH.relations.length === 10 && q("#relError").style.display === "block");
  input(q("#relSearch"), "US-030");
  click(qa(".sl-item")[0]);
  qa("#relSemWrap input[name=sem]")[2].click();
  check("preview de leitura direcional", q("#relPreview").textContent.includes("está relacionada a US-030"));
  click(q("#relCreate"));
  check("relação criada (11 arestas)", w.ATH.relations.length === 11 && qa(".edge").length === 11);
  check("contagem de relações do cartão atualizada", qa(".mcard").find(m => m.dataset.id === "US-010").textContent.includes("2 relaç"));
  const before = w.ATH.relations.length;
  click(q("[data-rmrel]"));
  check("relação removida", w.ATH.relations.length === before - 1);
  click(q("#btnOpen"));
  check("abre editor a partir do mapa", q("#docPage") && w.ATH.ui.currentReq === "US-010");
  click(q("#edBack"));
  check("voltar retorna ao mapa com gaveta", qa(".mcard").length === 15 && q("#mapDrawer").classList.contains("open"));

  console.log("\n[O] Configurações → Pastas (US08) + US11/US13");
  click(qa("#mainTabs .tab")[0]);
  const e7card = qa(".epico-card").find(c => c.textContent.includes("Pagamentos e Cash-out"));
  click(e7card.querySelector("[data-add-req]"));
  input(q("#rqTitle"), "Reembolso via Pix");
  click(q("#rqCreate"));
  check("US criada dentro do épico novo (pasta pré-preenchida)", w.ATH.requirements.find(r => r.title === "Reembolso via Pix").epicoId === "E7");
  click(q("#edBack"));
  click(q("[data-open-settings]"));
  check("modal configurações na aba Pastas", !!q(".pasta-row"));
  const e7row = qa(".pasta-row").find(r => r.textContent.includes("Pagamentos e Cash-out"));
  check("novo épico listado (1 US)", e7row.textContent.includes("1 US"));
  click(e7row.querySelector("[data-del-epico]"));
  await wait(50);
  check("exclusão bloqueada com itens dependentes", qa(".pasta-row").length === 7 && d.body.textContent.includes("contém"));
  click(e7row.querySelector("[data-ren-epico]"));
  const inp = q("[data-ren-input]");
  check("input de renomeação", !!inp);
  input(inp, "Pagamentos");
  click(q("[data-save-ren]"));
  check("épico renomeado", qa(".pasta-row").some(r => r.textContent.includes("Pagamentos")));
  click(q("[data-close]"));
  click(qa("#mainTabs .tab")[1]);
  const row010 = qa("tr[data-open-req]").find(r => r.textContent.includes("US-010"));
  click(row010.querySelector("td") || row010);
  const pasta = q("#dwPasta");
  pasta.value = "E5"; pasta.dispatchEvent(new w.Event("change", { bubbles: true }));
  check("US movida de épico (US11)", w.ATH.requirements.find(r => r.id === "US-010").epicoId === "E5");
  click(q("[data-action='cancel']"));
  await wait(50);
  click(q("#cfmOk"));
  check("US-010 CANCELADA", w.ATH.requirements.find(r => r.id === "US-010").situacao === "CANCELADO");
  click(q("#edBack"));
  check("de volta à listagem", !!q("table.req-table"));
  click(qa(".filter-chip")[0]); click(qa(".filter-chip")[1]);
  check("US-010 aparece com tag Cancelada", qa("tr[data-open-req]").some(r => r.textContent.includes("US-010") && r.textContent.includes("Cancelada")));

  console.log(`\n========== RESULTADO: ${pass} ✓ / ${fail} ✗ ==========`);
  process.exit(fail ? 1 : 0);
})().catch(e => { console.error("ERRO NO TESTE:", e); process.exit(1); });
