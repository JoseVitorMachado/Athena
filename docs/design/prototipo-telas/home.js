// ============================================================
// ATHENA — Workspaces, Projetos e Arquivados (US04–US06)
// + Modal "Trocar projeto" (US24) + loadProject
// ============================================================

function wsById(id) { return DB.workspaces.find(w => w.id === id); }
function projById(wsId, pId) { return wsById(wsId)?.projects.find(p => p.id === pId); }
function projMeta(p) { return `${p.data.requirements.length} requisito${p.data.requirements.length === 1 ? "" : "s"} · ${p.data.epicos.length} épicos`; }

// ---------- carrega projeto no contexto ATH ----------
function loadProject(projId, wsId) {
  const p = projById(wsId, projId);
  if (!p) return;
  DB.ui.homeWs = wsId;
  ATH.projectId = p.id;
  ATH.workspaceId = wsId;
  ATH.project = p.data.project;
  ATH.epicos = p.data.epicos;
  ATH.requirements = p.data.requirements;
  ATH.relations = p.data.relations;
  ATH.seq = p.data.seq;
  Object.assign(ATH.ui, {
    view: "visao", currentReq: null, prevView: "visao", search: "",
    filters: { ativo: true, cancelado: false, estado: "TODOS", pub: "TODOS", epico: "TODOS" },
    settingsTab: "pastas", editorDrawer: false,
  });
  MapView.sel = null;
  MapView._fitted = false;
}

// ---------- HOME ----------
function renderHome() {
  const ws = wsById(DB.ui.homeWs) || DB.workspaces[0];
  DB.ui.homeWs = ws.id;
  const q = DB.ui.wsSearch.toLowerCase();
  const active = ws.projects.filter(p => !p.archived && (!q || q === "" || p.name.toLowerCase().includes(DB.ui.projSearch.toLowerCase())));
  // filtro do ws usa wsSearch na sidebar; projeto usa projSearch
  const wsList = DB.workspaces.filter(w => !DB.ui.wsSearch || w.name.toLowerCase().includes(DB.ui.wsSearch.toLowerCase()));
  const archived = ws.projects.filter(p => p.archived);
  const tab = DB.ui.homeTab;
  const projQ = DB.ui.projSearch.toLowerCase();
  const projs = (tab === "arquivados" ? archived : ws.projects.filter(p => !p.archived))
    .filter(p => !projQ || p.name.toLowerCase().includes(projQ));

  const app = document.getElementById("app");
  app.innerHTML = `
  <div class="home">
    <aside class="wsbar">
      <div class="wsbar-head">
        <div class="brand"><span class="brand-mark">${icon("shield", 22)}</span><span class="brand-name sm">ATH<em>E</em>NA</span></div>
        <button class="iconbtn" id="homeAvatar" title="${esc(DB.session.user.name)}">${initialsOf(DB.session.user.name)}</button>
      </div>
      <div class="search sm">${icon("search", 13)}<input id="wsSearch" placeholder="Buscar workspace" value="${esc(DB.ui.wsSearch)}"></div>
      <div class="ws-list" id="wsList">
        ${wsList.length ? wsList.map(w => `
          <button class="ws-item${w.id === ws.id ? " active" : ""}" data-ws="${w.id}">
            ${icon("folder", 15)}<span class="ws-name">${esc(w.name)}</span>
            <span class="ws-count">${w.projects.filter(p => !p.archived).length}</span>
          </button>`).join("") : `<div class="ws-none">Nenhum workspace encontrado.</div>`}
      </div>
      <button class="btn btn-subtle btn-block" id="addWs">${icon("plus", 14)} Adicionar workspace</button>
    </aside>

    <main class="home-main">
      <header class="home-head">
        <div>
          <h1 class="ws-title">${esc(ws.name)}</h1>
          <div class="page-sub">Owner: <b>${esc(DB.session.user.name)}</b> · ${ws.projects.filter(p => !p.archived).length} projeto(s) ativo(s)</div>
        </div>
        <div class="actions">
          <button class="btn btn-ghost" id="wsMenu">${icon("dots", 15)} Opções do workspace</button>
        </div>
      </header>

      <nav class="home-tabs">
        <button class="htab${tab === "projetos" ? " active" : ""}" data-htab="projetos">Projetos</button>
        <button class="htab${tab === "arquivados" ? " active" : ""}" data-htab="arquivados">Projetos arquivados <span class="count-chip">${archived.length}</span></button>
      </nav>

      ${tab === "projetos" ? `
      <div class="home-toolbar">
        <div class="search">${icon("search", 14)}<input id="projSearch" placeholder="Buscar projeto…" value="${esc(DB.ui.projSearch)}"></div>
        <button class="btn btn-primary" id="newProj">${icon("plus", 14)} Novo projeto</button>
      </div>` : `
      <div class="arch-desc">Projetos arquivados em <b>${esc(ws.name)}</b> — ${archived.length} projeto(s) inativo(s).</div>`}

      <div class="proj-grid">
        ${projs.length ? projs.map(p => `
          <div class="proj-card${p.archived ? " archived" : ""}" data-proj="${p.id}">
            <div class="proj-top">
              ${icon("book", 16)}
              <span class="proj-name">${esc(p.name)}</span>
              <button class="iconbtn proj-menu" data-proj-menu="${p.id}" title="Opções">${icon("dots", 14)}</button>
            </div>
            <div class="proj-desc">${esc(p.desc || "Sem descrição.")}</div>
            <div class="proj-meta">${projMeta(p)}${p.archived ? ` · <span class="badge sit-CANCELADO"><span class="bdot"></span>Arquivado</span>` : ""}</div>
            <div class="proj-actions">
              ${p.archived
                ? `<button class="btn btn-subtle btn-sm" data-restore="${p.id}">${icon("back", 13)} Restaurar projeto</button>`
                : `<button class="btn btn-primary btn-sm" data-open="${p.id}">Abrir</button>`}
            </div>
          </div>`).join("")
        : `<div class="empty-table" style="grid-column:1/-1">
             <div class="big">🗄️</div>
             ${tab === "arquivados" ? "Nenhum projeto arquivado neste workspace." : "Nenhum projeto encontrado."}
           </div>`}
      </div>
    </main>
  </div>`;

  bindHome();
  const focusId = DB.ui.focusId;
  DB.ui.focusId = null;
  if (focusId) {
    const el = document.getElementById(focusId);
    if (el) { el.focus(); const v = el.value; el.setSelectionRange(v.length, v.length); }
  }
}

function bindHome() {
  const app = document.getElementById("app");

  // sidebar
  document.getElementById("homeAvatar").onclick = e => openUserMenu(e.currentTarget);
  document.getElementById("wsSearch").addEventListener("input", e => {
    DB.ui.wsSearch = e.target.value; DB.ui.focusId = "wsSearch"; renderHome();
  });
  document.getElementById("wsList").addEventListener("click", e => {
    const b = e.target.closest("[data-ws]");
    if (b) { DB.ui.homeWs = b.dataset.ws; DB.ui.projSearch = ""; renderHome(); }
  });
  document.getElementById("addWs").onclick = () => openNewWorkspaceModal();

  // header
  document.getElementById("wsMenu").onclick = e => openWorkspaceMenu(e.currentTarget);

  // tabs
  document.querySelectorAll("[data-htab]").forEach(t =>
    t.addEventListener("click", () => { DB.ui.homeTab = t.dataset.htab; DB.ui.projSearch = ""; renderHome(); }));

  // busca de projetos
  const ps = document.getElementById("projSearch");
  if (ps) ps.addEventListener("input", e => { DB.ui.projSearch = e.target.value; DB.ui.focusId = "projSearch"; renderHome(); });

  document.getElementById("newProj") && (document.getElementById("newProj").onclick = () => openNewProjectModal());

  // cards
  document.querySelectorAll("[data-open]").forEach(b => b.addEventListener("click", () => {
    loadProject(b.dataset.open, DB.ui.homeWs);
    DB.ui.view = "visao";
    render();
  }));
  document.querySelectorAll("[data-restore]").forEach(b => b.addEventListener("click", () => {
    const p = projById(DB.ui.homeWs, b.dataset.restore);
    p.archived = false;
    toast(`Projeto “${p.name}” restaurado para a aba de projetos ativos.`, "success");
    renderHome();
  }));
  document.querySelectorAll("[data-proj-menu]").forEach(b => b.addEventListener("click", e => {
    e.stopPropagation();
    const p = projById(DB.ui.homeWs, b.dataset.projMenu);
    openMenu(b, p.archived ? [
      { id: "res", icon: "back", label: "Restaurar projeto", onClick: () => { p.archived = false; toast("Projeto restaurado.", "success"); renderHome(); } },
      { id: "del", icon: "trash", label: "Excluir permanentemente", danger: true, onClick: () => confirmDeleteProject(p) },
    ] : [
      { id: "edit", icon: "edit", label: "Editar nome do projeto", onClick: () => openRenameProjectModal(p) },
      { id: "arch", icon: "trash", label: "Arquivar projeto", onClick: () => {
        confirmModal({
          title: "Arquivar projeto",
          message: `Arquivar <b>${esc(p.name)}</b>? Requisitos e documentações ficam em modo leitura até a restauração — projetos arquivados não aparecem na busca rápida nem no modal de troca de projeto.`,
          confirmLabel: "Arquivar",
          danger: true,
          onConfirm: () => { p.archived = true; toast(`Projeto “${p.name}” arquivado.`, "info"); renderHome(); },
        });
      } },
    ], { align: "right" });
  }));
}

// ---------- menu do usuário (reuso no home) ----------
function openUserMenu(anchor) {
  openMenu(anchor, [
    { type: "head", name: DB.session.user.name, email: DB.session.user.email },
    { type: "sep" },
    { type: "switch", id: "theme", label: "Modo escuro", checked: document.documentElement.dataset.theme === "dark", onChange: v => toggleTheme(v) },
    { id: "sair", label: "Sair", onClick: () => { DB.session.user = null; DB.ui.view = "home"; DB.ui.authView = "login"; render(); toast("Sessão encerrada.", "info"); } },
  ], { align: "right" });
}

// ---------- US04: workspace ----------
function openWorkspaceMenu(anchor) {
  const ws = wsById(DB.ui.homeWs);
  openMenu(anchor, [
    { id: "ren", icon: "edit", label: "Renomear workspace", onClick: () => openRenameWorkspaceModal(ws) },
    { id: "del", icon: "trash", label: "Excluir workspace", danger: true, onClick: () => openDeleteWorkspaceModal(ws) },
  ], { align: "right" });
}

function openNewWorkspaceModal() {
  const { el, close } = openModal(`
  <div class="modal">
    <div class="modal-head"><div class="modal-title">Adicionar workspace</div>
      <div class="modal-sub">Áreas de trabalho isoladas para organizar seus projetos.</div>
      <button class="iconbtn modal-x" data-close>${icon("x")}</button></div>
    <div class="modal-body">
      <div class="field"><label>Nome do workspace</label>
        <input class="input" id="nwName" placeholder="Ex.: InfinitePay — Engenharia">
        <div class="field-error" id="nwErr" style="display:none">O nome é obrigatório.</div></div>
    </div>
    <div class="modal-foot">
      <button class="btn btn-ghost" data-close>Cancelar</button>
      <button class="btn btn-primary" id="nwCreate">Criar</button>
    </div>
  </div>`);
  const inp = el.querySelector("#nwName");
  inp.focus();
  el.querySelector("#nwCreate").onclick = () => {
    const name = inp.value.trim();
    if (!name) { el.querySelector("#nwErr").style.display = "block"; return; }
    const id = "W" + (Date.now() % 100000);
    DB.workspaces.push({ id, name, projects: [] });
    DB.ui.homeWs = id;
    close();
    toast(`Workspace “${name}” criado e selecionado.`, "success");
    renderHome();
  };
}

function openRenameWorkspaceModal(ws) {
  const { el, close } = openModal(`
  <div class="modal">
    <div class="modal-head"><div class="modal-title">Renomear workspace</div>
      <button class="iconbtn modal-x" data-close>${icon("x")}</button></div>
    <div class="modal-body">
      <div class="field"><label>Nome do workspace</label><input class="input" id="rwName" value="${esc(ws.name)}"></div>
    </div>
    <div class="modal-foot">
      <button class="btn btn-ghost" data-close>Cancelar</button>
      <button class="btn btn-primary" id="rwSave">Salvar</button>
    </div>
  </div>`);
  const inp = el.querySelector("#rwName");
  inp.focus(); inp.select();
  el.querySelector("#rwSave").onclick = () => {
    const name = inp.value.trim();
    if (!name) return;
    ws.name = name;
    close();
    toast("Workspace renomeado.", "success");
    renderHome();
  };
}

function openDeleteWorkspaceModal(ws) {
  const { el, close } = openModal(`
  <div class="modal">
    <div class="modal-head"><div class="modal-title">Excluir workspace</div>
      <div class="modal-sub">Exclusão lógica: os projetos vinculados são marcados como excluídos. <b>Arquivos nos serviços conectados não serão removidos.</b></div>
      <button class="iconbtn modal-x" data-close>${icon("x")}</button></div>
    <div class="modal-body">
      <div class="field"><label>Digite <b>${esc(ws.name)}</b> para confirmar</label>
        <input class="input" id="dwName" placeholder="Nome exato do workspace">
      </div>
    </div>
    <div class="modal-foot">
      <button class="btn btn-ghost" data-close>Cancelar</button>
      <button class="btn btn-danger-solid" id="dwDelete" disabled>Excluir definitivamente</button>
    </div>
  </div>`);
  const inp = el.querySelector("#dwName"), btn = el.querySelector("#dwDelete");
  inp.focus();
  inp.addEventListener("input", () => { btn.disabled = inp.value.trim() !== ws.name; });
  btn.onclick = () => {
    DB.workspaces = DB.workspaces.filter(w => w.id !== ws.id);
    if (DB.ui.homeWs === ws.id) DB.ui.homeWs = DB.workspaces[0] ? DB.workspaces[0].id : null;
    close();
    toast("Workspace excluído (projetos em exclusão lógica).", "info");
    renderHome();
  };
}

// ---------- US05/US06: projetos ----------
function openNewProjectModal() {
  const ws = wsById(DB.ui.homeWs);
  const { el, close } = openModal(`
  <div class="modal">
    <div class="modal-head"><div class="modal-title">Novo projeto</div>
      <div class="modal-sub">O nome gera identificadores rastreáveis para os requisitos e deve ser único no workspace.</div>
      <button class="iconbtn modal-x" data-close>${icon("x")}</button></div>
    <div class="modal-body">
      <div class="field"><label>Nome do projeto</label>
        <input class="input" id="npName" placeholder="Ex.: Portal do Cliente">
        <div class="field-error" id="npErr" style="display:none"></div></div>
      <div class="field"><label>Descrição (opcional)</label>
        <input class="input" id="npDesc" placeholder="Uma linha sobre o objetivo do projeto"></div>
    </div>
    <div class="modal-foot">
      <button class="btn btn-ghost" data-close>Cancelar</button>
      <button class="btn btn-primary" id="npCreate">Criar projeto</button>
    </div>
  </div>`);
  const inp = el.querySelector("#npName");
  inp.focus();
  el.querySelector("#npCreate").onclick = () => {
    const name = inp.value.trim();
    const err = el.querySelector("#npErr");
    err.style.display = "none";
    if (!name) { err.textContent = "O nome do projeto é obrigatório."; err.style.display = "block"; return; }
    if (ws.projects.some(p => p.name.toLowerCase() === name.toLowerCase())) {
      err.textContent = "O nome do projeto deve ser único no workspace — já existe um projeto com esse nome."
      err.style.display = "block";
      return;
    }
    const id = "P" + (Date.now() % 100000);
    ws.projects.push({
      id, name, desc: el.querySelector("#npDesc").value.trim(), archived: false,
      data: { project: { name, code: name.slice(0, 3).toUpperCase(), created: "26/10/2026" }, seq: { req: 0, epico: 0, rel: 0 }, epicos: [], requirements: [], relations: [] },
    });
    close();
    toast(`Projeto “${name}” criado.`, "success");
    renderHome();
  };
}

function openRenameProjectModal(p) {
  const { el, close } = openModal(`
  <div class="modal">
    <div class="modal-head"><div class="modal-title">Editar nome do projeto</div>
      <button class="iconbtn modal-x" data-close>${icon("x")}</button></div>
    <div class="modal-body">
      <div class="field"><label>Nome do projeto</label><input class="input" id="rpName" value="${esc(p.name)}"></div>
    </div>
    <div class="modal-foot">
      <button class="btn btn-ghost" data-close>Cancelar</button>
      <button class="btn btn-primary" id="rpSave">Salvar</button>
    </div>
  </div>`);
  const inp = el.querySelector("#rpName");
  inp.focus(); inp.select();
  el.querySelector("#rpSave").onclick = () => {
    const name = inp.value.trim();
    if (!name) return;
    p.name = name;
    if (p.data) p.data.project.name = name;
    close();
    toast("Projeto renomeado.", "success");
    renderHome();
  };
}

function confirmDeleteProject(p) {
  const { el, close } = openModal(`
  <div class="modal">
    <div class="modal-head"><div class="modal-title">Excluir projeto permanentemente</div>
      <div class="modal-sub">Somente o Owner. A ação é irreversível e remove o conteúdo dos domínios após confirmação de quiescência. <b>Arquivos nos serviços conectados não serão removidos.</b></div>
      <button class="iconbtn modal-x" data-close>${icon("x")}</button></div>
    <div class="modal-body">
      <div class="field"><label>Digite <b>${esc(p.name)}</b> para confirmar</label>
        <input class="input" id="dpName" placeholder="Nome exato do projeto"></div>
    </div>
    <div class="modal-foot">
      <button class="btn btn-ghost" data-close>Cancelar</button>
      <button class="btn btn-danger-solid" id="dpDelete" disabled>Excluir permanentemente</button>
    </div>
  </div>`);
  const inp = el.querySelector("#dpName"), btn = el.querySelector("#dpDelete");
  inp.focus();
  inp.addEventListener("input", () => { btn.disabled = inp.value.trim() !== p.name; });
  btn.onclick = () => {
    const ws = wsById(DB.ui.homeWs);
    ws.projects = ws.projects.filter(x => x.id !== p.id);
    close();
    toast(`Projeto “${p.name}” excluído definitivamente.`, "info");
    renderHome();
  };
}

// ---------- US24: modal Trocar projeto ----------
function openSwitchProjectModal() {
  if (DB.ui.tpWs == null) DB.ui.tpWs = ATH.workspaceId || DB.ui.homeWs;
  const { el, close } = openModal(`
  <div class="modal lg">
    <div class="modal-head"><div class="modal-title">Trocar projeto</div>
      <div class="modal-sub">Escolha o workspace à esquerda e o projeto à direita.</div>
      <button class="iconbtn modal-x" data-close>${icon("x")}</button></div>
    <div class="modal-body">
      <div class="search tp-search">${icon("search", 14)}<input id="tpSearch" placeholder="Buscar projeto ou workspace…"></div>
      <div class="tp-cols">
        <div class="tp-col"><div class="tp-col-title">Workspaces</div><div class="tp-list" id="tpWsList"></div></div>
        <div class="tp-col"><div class="tp-col-title">Projetos</div><div class="tp-list" id="tpProjList"></div></div>
      </div>
    </div>
    <div class="modal-foot">
      <button class="btn btn-ghost left" id="tpManage">${icon("home", 14)} Gerenciar workspaces</button>
      <button class="btn btn-subtle" data-close>Fechar</button>
    </div>
  </div>`);

  function renderTpLists() {
    const q = (el.querySelector("#tpSearch").value || "").trim().toLowerCase();
    const wsList = DB.workspaces.filter(w => !q || w.name.toLowerCase().includes(q));
    el.querySelector("#tpWsList").innerHTML = wsList.length ? wsList.map(w => `
      <button class="tp-item${w.id === DB.ui.tpWs ? " selected" : ""}" data-tp-ws="${w.id}">
        ${w.id === ATH.workspaceId ? `<span class="tp-check">${icon("check", 12)}</span>` : `<span class="tp-check"></span>`}
        ${icon("folder", 14)}<span class="tp-name">${esc(w.name)}</span>
        <span class="ws-count">${w.projects.filter(p => !p.archived).length}</span>
      </button>`).join("") : `<div class="ws-none">Nada encontrado.</div>`;

    const ws = wsById(DB.ui.tpWs);
    const projs = ws ? ws.projects.filter(p => !p.archived && (!q || p.name.toLowerCase().includes(q))) : [];
    el.querySelector("#tpProjList").innerHTML = projs.length ? projs.map(p => `
      <button class="tp-item${ATH.projectId === p.id && ATH.workspaceId === DB.ui.tpWs ? " selected" : ""}" data-tp-proj="${p.id}">
        ${icon("book", 14)}<span class="tp-name">${esc(p.name)}</span>
        ${ATH.projectId === p.id && ATH.workspaceId === DB.ui.tpWs ? `<span class="tp-flag">Atual</span>` : ""}
      </button>`).join("") : `<div class="ws-none">Nenhum projeto ativo neste workspace.</div>`;
  }
  renderTpLists();

  el.querySelector("#tpWsList").addEventListener("click", e => {
    const b = e.target.closest("[data-tp-ws]");
    if (b) { DB.ui.tpWs = b.dataset.tpWs; renderTpLists(); }
  });
  el.querySelector("#tpProjList").addEventListener("click", e => {
    const b = e.target.closest("[data-tp-proj]");
    if (!b) return;
    loadProject(b.dataset.tpProj, DB.ui.tpWs);
    close();
    render();
  });
  el.querySelector("#tpSearch").addEventListener("input", renderTpLists);
  el.querySelector("#tpManage").onclick = () => { close(); DB.ui.view = "home"; render(); };
}
