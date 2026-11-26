// ============================================================
// ATHENA — Núcleo do protótipo
// Shell, Visão Geral (épicos), Requisitos (US18), Editor
// (US09–US13), Configurações → Pastas (US08), modais e toasts.
// ============================================================

// ---------- helpers DOM ----------
const $ = s => document.querySelector(s);
const $$ = s => [...document.querySelectorAll(s)];
function esc(s) {
  return String(s ?? "").replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

// ---------- ícones (feather-like) ----------
const IC = {};
function icon(name, w) {
  if (!IC[name]) return "";
  return IC[name].replace("<svg", `<svg class="ic" style="width:${w || 16}px;height:${w || 16}px"`);
}
(function buildIcons() {
  const wrap = (inner) => `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">${inner}</svg>`;
  IC.plus = wrap(`<path d="M12 5v14M5 12h14"/>`);
  IC.x = wrap(`<path d="M18 6L6 18M6 6l12 12"/>`);
  IC.search = wrap(`<circle cx="11" cy="11" r="7"/><path d="M21 21l-4.35-4.35"/>`);
  IC.bell = wrap(`<path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.7 21a2 2 0 0 1-3.4 0"/>`);
  IC.dots = `<svg viewBox="0 0 24 24" fill="currentColor"><circle cx="12" cy="5" r="1.8"/><circle cx="12" cy="12" r="1.8"/><circle cx="12" cy="19" r="1.8"/></svg>`;
  IC.folder = wrap(`<path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>`);
  IC.chev = wrap(`<path d="M9 18l6-6-6-6"/>`);
  IC.chevR = wrap(`<path d="M15 18l-6-6 6-6"/>`);
  IC.back = wrap(`<path d="M19 12H5M12 19l-7-7 7-7"/>`);
  IC.moon = wrap(`<path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z"/>`);
  IC.sun = wrap(`<circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/>`);
  IC.check = wrap(`<path d="M20 6L9 17l-5-5"/>`);
  IC.trash = wrap(`<path d="M3 6h18M8 6V4a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1v2m2 0v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"/>`);
  IC.edit = wrap(`<path d="M17 3a2.8 2.8 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5z"/>`);
  IC.link = wrap(`<path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>`);
  IC.crosshair = wrap(`<circle cx="12" cy="12" r="8"/><path d="M12 2v4M12 18v4M2 12h4M18 12h4"/>`);
  IC.zoomIn = wrap(`<circle cx="11" cy="11" r="7"/><path d="M21 21l-4.35-4.35M8 11h6M11 8v6"/>`);
  IC.zoomOut = wrap(`<circle cx="11" cy="11" r="7"/><path d="M21 21l-4.35-4.35M8 11h6"/>`);
  IC.fit = wrap(`<path d="M8 3H5a2 2 0 0 0-2 2v3m18 0V5a2 2 0 0 0-2-2h-3m0 18h3a2 2 0 0 0 2-2v-3M3 16v3a2 2 0 0 0 2 2h3"/>`);
  IC.open = wrap(`<path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/><path d="M15 3h6v6M10 14L21 3"/>`);
  IC.send = wrap(`<path d="M22 2L11 13M22 2l-7 20-4-9-9-4z"/>`);
  IC.sliders = wrap(`<path d="M4 21v-7M4 10V3M12 21v-9M12 8V3M20 21v-5M20 12V3M1 14h6M9 8h6M17 16h6"/>`);
  IC.shield = `<svg viewBox="0 0 24 24" fill="none"><path d="M12 2L4 5v6c0 5.25 3.4 9.7 8 11 4.6-1.3 8-5.75 8-11V5l-8-3z" fill="url(#goldgrad)" stroke="#F0C968" stroke-width="1.2"/><path d="M12 7.5l3 3.5-3 6-3-6 3-3.5z" fill="#231A06"/><defs><linearGradient id="goldgrad" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#F0C968"/><stop offset="1" stop-color="#B37F1E"/></linearGradient></defs></svg>`;
  IC.table = wrap(`<rect x="3" y="4" width="18" height="16" rx="2"/><path d="M3 10h18M9 10v10M15 10v10"/>`);
  IC.info = wrap(`<circle cx="12" cy="12" r="9"/><path d="M12 11v5M12 8h.01"/>`);
  IC.mail = wrap(`<rect x="2" y="4" width="20" height="16" rx="2"/><path d="M22 6l-10 7L2 6"/>`);
  IC.lock = wrap(`<rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>`);
  IC.user = wrap(`<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>`);
  IC.circle = wrap(`<circle cx="12" cy="12" r="9"/>`);
  IC.eye = wrap(`<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>`);
  IC.eyeOff = wrap(`<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><path d="M1 1l22 22"/>`);
  IC.home = wrap(`<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>`);
})();

// ---------- estado de dados ----------
const reqById = id => ATH.requirements.find(r => r.id === id);
const epicoById = id => ATH.epicos.find(e => e.id === id);
const reqsOf = epicoId => ATH.requirements.filter(r => r.epicoId === epicoId);
function relCount(reqId) { return ATH.relations.filter(r => r.from === reqId || r.to === reqId).length; }
function relsOf(reqId) {
  return ATH.relations
    .filter(r => r.from === reqId || r.to === reqId)
    .map(r => ({ rel: r, role: r.from === reqId ? "out" : "in" }));
}
function relSentence(from, type, to) {
  if (type === "DEPENDE_DE") return `${from} depende de ${to}`;
  if (type === "BLOQUEIA") return `${from} bloqueia ${to}`;
  return `${from} está relacionada a ${to}`;
}
function statusBadge(r) {
  if (r.situacao === "CANCELADO") return `<span class="badge sit-CANCELADO"><span class="bdot"></span>Cancelada</span>`;
  return `<span class="badge st-${r.status}"><span class="bdot"></span>${STATUS_LABEL[r.status]}</span>`;
}
function nextReqId() { return "US-" + String(++ATH.seq.req).padStart(3, "0"); }

// ---------- toasts ----------
function toast(msg, type) {
  const t = document.createElement("div");
  t.className = "toast" + (type ? " " + type : "");
  t.innerHTML = `${icon(type === "error" ? "info" : "check")}<span>${esc(msg)}</span>`;
  if (type === "error") t.querySelector("svg").style.color = "var(--red)";
  if (type === "info") t.querySelector("svg").style.color = "var(--blue)";
  $("#toast-root").appendChild(t);
  setTimeout(() => { t.classList.add("out"); setTimeout(() => t.remove(), 200); }, 3400);
}

// ---------- sistema de modais ----------
let _modalClose = null;
function openModal(html) {
  closeModal();
  const backdrop = document.createElement("div");
  backdrop.className = "modal-backdrop";
  backdrop.innerHTML = html;
  $("#modal-root").appendChild(backdrop);
  backdrop.addEventListener("mousedown", e => { if (e.target === backdrop) close(); });
  backdrop.querySelectorAll("[data-close]").forEach(b => b.addEventListener("click", close));
  const onKey = e => { if (e.key === "Escape") close(); };
  document.addEventListener("keydown", onKey, { once: false });
  function close() {
    document.removeEventListener("keydown", onKey);
    backdrop.remove();
    _modalClose = null;
  }
  _modalClose = close;
  return { el: backdrop, close };
}
function closeModal() { if (_modalClose) _modalClose(); }

function confirmModal({ title, message, confirmLabel, danger, onConfirm }) {
  const { el, close } = openModal(`
  <div class="modal">
    <div class="modal-head"><div class="modal-title">${esc(title)}</div>
      <button class="iconbtn modal-x" data-close>${icon("x")}</button></div>
    <div class="modal-body"><div class="modal-sub">${message}</div></div>
    <div class="modal-foot">
      <button class="btn btn-ghost" data-close>Cancelar</button>
      <button class="btn ${danger ? "btn-danger-solid" : "btn-primary"}" id="cfmOk">${esc(confirmLabel)}</button>
    </div>
  </div>`);
  el.querySelector("#cfmOk").onclick = () => { close(); onConfirm(); };
}

// ---------- menus dropdown ----------
function openMenu(anchor, items, opts) {
  closeMenu();
  const menu = document.createElement("div");
  menu.className = "menu";
  let html = "";
  for (const it of items) {
    if (it.type === "sep") { html += `<div class="menu-sep"></div>`; continue; }
    if (it.type === "head") { html += `<div class="menu-head"><div class="nm">${esc(it.name)}</div>${it.email ? `<div class="em">${esc(it.email)}</div>` : ""}</div>`; continue; }
    if (it.type === "switch") {
      html += `<div class="menu-item menu-switch"><span>${esc(it.label)}</span>
        <label class="switch"><input type="checkbox" ${it.checked ? "checked" : ""} data-menu-switch="${it.id}"><span class="knob"></span></label></div>`;
      continue;
    }
    html += `<button class="menu-item${it.danger ? " danger" : ""}${it.disabled ? " disabled" : ""}" data-menu-id="${it.id}">${it.icon ? icon(it.icon) : ""}<span>${esc(it.label)}</span></button>`;
  }
  menu.innerHTML = html;
  $("#menu-root").appendChild(menu);
  const r = anchor.getBoundingClientRect();
  const mw = menu.offsetWidth, mh = menu.offsetHeight;
  let x = opts && opts.align === "right" ? r.right - mw : r.left;
  let y = r.bottom + 6;
  if (opts && opts.openUp) y = r.top - mh - 6;
  if (y + mh > window.innerHeight - 8) y = Math.max(8, r.top - mh - 6);
  if (x + mw > window.innerWidth - 8) x = window.innerWidth - mw - 8;
  menu.style.left = x + "px";
  menu.style.top = y + "px";

  menu.addEventListener("click", e => {
    const sw = e.target.closest("[data-menu-switch]");
    if (sw) { it_dispatchSwitch(sw); return; }
    const btn = e.target.closest("[data-menu-id]");
    if (!btn || btn.classList.contains("disabled")) return;
    const item = items.find(i => i.id === btn.dataset.menuId);
    closeMenu();
    if (item && item.onClick) item.onClick();
  });
  function it_dispatchSwitch(swEl) {
    const item = items.find(i => i.id === swEl.dataset.menuSwitch);
    if (item && item.onChange) item.onChange(swEl.checked);
  }
  setTimeout(() => {
    document.addEventListener("mousedown", onOutside, { once: false });
  }, 0);
  function onOutside(e) { if (!menu.contains(e.target)) closeMenu(); }
  window._closeMenu = () => { document.removeEventListener("mousedown", onOutside); menu.remove(); };
}
function closeMenu() { if (window._closeMenu) window._closeMenu(); window._closeMenu = null; }

// ---------- shell ----------
function renderShell() {
  $("#app").innerHTML = `
  <header class="topbar">
    <div class="brand">
      <span class="brand-mark">${icon("shield", 26)}</span>
      <span class="brand-name">ATH<em>E</em>NA</span>
    </div>
    <button class="proj-chip" id="projChip" title="Trocar projeto (US24)"><span id="chipName">${esc(ATH.project ? ATH.project.name : "")}</span> ${icon("chev", 13)}</button>
    <button class="iconbtn" id="homeBtn" title="Voltar aos workspaces e projetos">${icon("home", 16)}</button>
    <nav class="tabs" id="mainTabs">
      <button class="tab" data-view="visao">Visão geral</button>
      <button class="tab" data-view="requisitos">Requisitos</button>
      <button class="tab" data-view="mapa">Mapa</button>
    </nav>
    <div class="topbar-right">
      <div class="search">${icon("search", 14)}<input id="globalSearch" placeholder="Buscar requisitos, épicos…"></div>
      <button class="iconbtn" id="bellBtn" title="Notificações">${icon("bell")}<span class="dot"></span></button>
      <button class="avatar" id="avatarBtn">${initialsOf(DB.session.user.name)}</button>
    </div>
  </header>
  <main id="view"></main>`;

  $("#mainTabs").addEventListener("click", e => {
    const t = e.target.closest(".tab");
    if (!t) return;
    ATH.ui.view = t.dataset.view;
    location.hash = t.dataset.view;
    render();
  });
  $("#projChip").onclick = () => openSwitchProjectModal();
  $("#homeBtn").onclick = () => { DB.ui.view = "home"; location.hash = ""; render(); };
  $("#globalSearch").addEventListener("input", e => {
    ATH.ui.search = e.target.value.trim().toLowerCase();
    if (ATH.ui.view === "visao" || ATH.ui.view === "requisitos") render();
  });
  $("#bellBtn").onclick = e => {
    e.stopPropagation();
    openMenu(e.currentTarget, [
      { type: "head", name: "Avisos", email: "2 não lidas" },
      { type: "sep" },
      { id: "n1", icon: "info", label: "Fernando solicitou acesso ao projeto", onClick: () => toast("Pedido de acesso → modal Membros e permissões (US26).", "info") },
      { id: "n2", icon: "check", label: "US-016 publicada na v2 por Fernando", onClick: () => toast("Notificação marcada como lida.", "success") },
    ]);
  };
  $("#avatarBtn").onclick = e => {
    e.stopPropagation();
    openMenu(e.currentTarget, [
      { type: "head", name: DB.session.user.name, email: DB.session.user.email },
      { type: "sep" },
      { id: "perfil", icon: "info", label: "Perfil", onClick: () => toast("Protótipo: perfil não faz parte deste épico.", "info") },
      { type: "switch", id: "theme", label: "Modo escuro", checked: document.documentElement.dataset.theme === "dark",
        onChange: v => toggleTheme(v) },
      { id: "sair", label: "Sair", onClick: () => { DB.session.user = null; DB.ui.view = "home"; DB.ui.authView = "login"; render(); toast("Sessão encerrada (JWT invalidado).", "info"); } },
    ], { align: "right" });
  };
}

function updateShellChrome() {
  const chip = $("#chipName");
  if (chip && ATH.project) chip.textContent = ATH.project.name;
  const av = $("#avatarBtn");
  if (av && DB.session.user) {
    const ini = initialsOf(DB.session.user.name);
    if (av.textContent !== ini) av.textContent = ini;
  }
}

function toggleTheme(dark) {
  document.documentElement.dataset.theme = dark ? "dark" : "light";
}

// ---------- dispatcher ----------
function render() {
  closeMenu();
  if (!DB.session.user) { renderAuth(); return; }
  if (DB.ui.view === "home") { renderHome(); return; }
  // contexto de projeto — shell persiste entre renders (mantém foco da busca)
  if (!$("#mainTabs")) renderShell(); else updateShellChrome();
  $$("#mainTabs .tab").forEach(t => t.classList.toggle("active", t.dataset.view === ATH.ui.view));
  // Substitui o #view por um clone vazio: remove listeners antigos e evita
  // handlers duplicados a cada render (cloneNode não copia listeners).
  const oldView = $("#view");
  const v = oldView.cloneNode(false);
  oldView.replaceWith(v);
  if (ATH.ui.view === "mapa") { renderMapView(); return; }
  v.className = "page";
  if (ATH.ui.view === "visao") { v.innerHTML = visaoHTML(); bindVisao(); }
  else if (ATH.ui.view === "requisitos") { v.innerHTML = requisitosHTML(); bindRequisitos(); }
  else if (ATH.ui.view === "editor") { v.innerHTML = editorHTML(); bindEditor(); }
}

// ============================================================
// VISÃO GERAL — épicos (US08) + novos épicos/requisitos (US07)
// ============================================================
function visaoHTML() {
  const total = ATH.requirements.length;
  const q = ATH.ui.search;

  const cards = ATH.epicos.map(ep => {
    let reqs = reqsOf(ep.id);
    const showEpico = !q || ep.name.toLowerCase().includes(q);
    const matched = !q ? reqs : reqs.filter(r => r.id.toLowerCase().includes(q) || r.title.toLowerCase().includes(q));
    const visible = showEpico || matched.length;
    if (!visible) return "";

    const rows = matched.filter(r => r.situacao === "ATIVO").map(r => `
      <div class="req-row${r.situacao === "CANCELADO" ? " cancelled" : ""}" data-open-req="${r.id}">
        <span class="rid">${r.id}</span>
        <span class="rt">${esc(r.title)}</span>
        ${statusBadge(r)}
      </div>`).join("");

    return `
    <div class="epico-card${ep.open ? " open" : ""}" data-epico="${ep.id}">
      <div class="epico-head" data-toggle-epico="${ep.id}">
        <svg class="chev ic" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18l6-6-6-6"/></svg>
        ${icon("folder", 17)}
        <span class="epico-name">${esc(ep.name)}</span>
        <span class="count-chip">${reqs.length} US</span>
        <button class="mini iconbtn" data-add-req="${ep.id}" title="Novo requisito neste épico">${icon("plus", 15)}</button>
        <button class="mini iconbtn" data-epico-menu="${ep.id}" title="Opções do épico">${icon("dots", 15)}</button>
      </div>
      <div class="epico-body${ep.open ? " open" : ""}"><div>
        <div class="epico-list">
          ${rows || (reqs.length ? `<div class="empty-epico">${q ? "Nenhuma US corresponde à busca." : "Nenhuma US ativa neste épico."}</div>` : `<div class="empty-epico">Épico vazio —<button data-add-req="${ep.id}">adicionar a primeira US</button></div>`)}
        </div>
      </div></div>
    </div>`;
  }).join("");

  return `
  <div class="wrap-wide">
    <div class="page-head">
      <div class="page-title">
        <div>
          <h1>Visão geral</h1>
          <div class="page-sub"><b>${ATH.project.name}</b> · ${total} requisitos em ${ATH.epicos.length} épicos · ${ATH.relations.length} relações ativas</div>
        </div>
      </div>
      <div class="actions">
        <button class="btn btn-ghost" data-open-settings="pastas">${icon("sliders")} Configurações</button>
        <button class="btn btn-gold" data-new-epico>${icon("plus")} Novo Épico</button>
        <button class="btn btn-primary" data-new-req>${icon("plus")} Novo requisito</button>
      </div>
    </div>
    <div class="epico-grid">${cards || `<div class="empty-epico" style="grid-column:1/-1">Nenhum épico ou requisito corresponde à busca.</div>`}</div>
  </div>`;
}

function bindVisao() {
  const v = $("#view");
  v.addEventListener("click", e => {
    const tog = e.target.closest("[data-toggle-epico]");
    const addReq = e.target.closest("[data-add-req]");
    const epMenu = e.target.closest("[data-epico-menu]");
    const openReq = e.target.closest("[data-open-req]");
    const newEp = e.target.closest("[data-new-epico]");
    const newReq = e.target.closest("[data-new-req]");
    const settings = e.target.closest("[data-open-settings]");

    if (addReq) { e.stopPropagation(); openNewReqModal(addReq.dataset.addReq); return; }
    if (epMenu) { e.stopPropagation(); openEpicoMenu(epMenu); return; }
    if (openReq) { openReqInEditorFrom(openReq.dataset.openReq); return; }
    if (newEp) { openNewEpicoModal(); return; }
    if (newReq) { openNewReqModal(null); return; }
    if (settings) { openSettingsModal("pastas"); return; }
    if (tog) {
      const ep = epicoById(tog.dataset.toggleEpico);
      ep.open = !ep.open;
      const card = tog.closest(".epico-card");
      card.classList.toggle("open", ep.open);
      card.querySelector(".epico-body").classList.toggle("open", ep.open);
    }
  });
}

function openEpicoMenu(anchor) {
  const ep = epicoById(anchor.dataset.epicoMenu);
  openMenu(anchor, [
    { id: "add", icon: "plus", label: "Novo requisito neste épico", onClick: () => openNewReqModal(ep.id) },
    { type: "sep" },
    { id: "ren", icon: "edit", label: "Renomear épico", onClick: () => openSettingsModal("pastas") },
    { id: "del", icon: "trash", label: "Excluir épico", danger: true, onClick: () => openSettingsModal("pastas") },
  ], { align: "right", openUp: false });
}

function openReqInEditorFrom(reqId) {
  ATH.ui.prevView = ATH.ui.view;
  ATH.ui.currentReq = reqId;
  ATH.ui.view = "editor";
  render();
}

// ---------- Modal: Novo Épico (US08) ----------
function openNewEpicoModal() {
  const { el, close } = openModal(`
  <div class="modal">
    <div class="modal-head">
      <div class="modal-title">Novo Épico</div>
      <div class="modal-sub">Agrupe histórias de usuário e requisitos relacionados em um mesmo contexto.</div>
      <button class="iconbtn modal-x" data-close>${icon("x")}</button>
    </div>
    <div class="modal-body">
      <div class="field">
        <label>Nome do épico</label>
        <input class="input" id="epName" placeholder="Ex.: Pagamentos e Cash-out" autocomplete="off">
        <div class="field-error" id="epErr" style="display:none">O nome do épico é obrigatório.</div>
      </div>
    </div>
    <div class="modal-foot">
      <button class="btn btn-ghost" data-close>Cancelar</button>
      <button class="btn btn-primary" id="epCreate">Criar</button>
    </div>
  </div>`);
  const input = el.querySelector("#epName");
  input.focus();
  input.addEventListener("input", () => { el.querySelector("#epErr").style.display = "none"; input.classList.remove("invalid"); });
  input.addEventListener("keydown", e => { if (e.key === "Enter") create(); });
  el.querySelector("#epCreate").onclick = create;
  function create() {
    const name = input.value.trim();
    if (!name) { el.querySelector("#epErr").style.display = "block"; input.classList.add("invalid"); input.focus(); return; }
    const maxY = Math.max(...ATH.epicos.map(e => e.y));
    const id = "E" + (++ATH.seq.epico);
    ATH.epicos.push({ id, name, open: false, x: 140, y: maxY + 320 });
    close();
    toast(`Épico “${name}” criado — 0 US`, "success");
    if (ATH.ui.view === "mapa") { renderMapNodes(); drawEdges(); }
    render();
  }
}

// ---------- Modal: Adicionar Requisito (US07) ----------
function openNewReqModal(epicoId) {
  const defaultEpico = epicoId || (ATH.epicos[0] && ATH.epicos[0].id);
  const { el, close } = openModal(`
  <div class="modal">
    <div class="modal-head">
      <div class="modal-title">Adicionar requisito</div>
      <div class="modal-sub">Defina o título, o épico de destino e como inicializar o editor.</div>
      <button class="iconbtn modal-x" data-close>${icon("x")}</button>
    </div>
    <div class="modal-body">
      <div class="field">
        <label>Título</label>
        <input class="input" id="rqTitle" placeholder="Ex.: Acesso à conta (Login)" autocomplete="off">
        <div class="field-error" id="rqErr" style="display:none">O título é obrigatório.</div>
      </div>
      <div class="field">
        <label>Pasta / Épico</label>
        <select class="select" id="rqEpico">
          ${ATH.epicos.map(e => `<option value="${e.id}"${e.id === defaultEpico ? " selected" : ""}>${esc(e.name)}</option>`).join("")}
        </select>
      </div>
      <div class="field">
        <label>Começar com</label>
        <label class="radio-opt checked"><input type="radio" name="start" value="blank" checked>
          <span class="rt"><b>Em branco</b><span>Editor vazio, sem template.</span></span></label>
        <label class="radio-opt"><input type="radio" name="start" value="template">
          <span class="rt"><b>User Story completa</b><span>Pré-preenchida com cabeçalhos e seções: Especificação, Campos e comportamento, Regras de negócio.</span></span></label>
      </div>
    </div>
    <div class="modal-foot">
      <button class="btn btn-ghost" data-close>Cancelar</button>
      <button class="btn btn-primary" id="rqCreate">Adicionar</button>
    </div>
  </div>`);

  const title = el.querySelector("#rqTitle");
  title.focus();
  el.querySelectorAll(".radio-opt").forEach(o =>
    o.addEventListener("change", () => el.querySelectorAll(".radio-opt").forEach(x => x.classList.toggle("checked", x.querySelector("input").checked))));
  el.querySelector("#rqCreate").onclick = () => {
    const t = title.value.trim();
    if (!t) { el.querySelector("#rqErr").style.display = "block"; title.classList.add("invalid"); title.focus(); return; }
    const epico = el.querySelector("#rqEpico").value;
    const template = el.querySelector("input[name=start]:checked").value === "template";
    const id = nextReqId();
    ATH.requirements.push({
      id, title: t, epicoId: epico, tipo: "História de usuário", situacao: "ATIVO", status: "RASCUNHO",
      pubVersion: null, updated: "agora", autor: DB.session.user.name,
      origem: { tipo: "Interno", detalhe: `Criado no ATHENA em 26/10/2026 por ${DB.session.user.name}` },
      conteudo: template ? TEMPLATE_US : `<p>Descreva o requisito aqui…</p>`,
      criterios: [],
    });
    const ep = epicoById(epico);
    ep.open = true;
    close();
    toast(`${id} criada em “${ep.name}”`, "success");
    openReqInEditorFrom(id);
  };
}

// ============================================================
// REQUISITOS — listagem e filtros (US18)
// ============================================================
function requisitosHTML() {
  const f = ATH.ui.filters;
  const q = ATH.ui.search;

  const rows = ATH.requirements.filter(r => {
    if (f.ativo || f.cancelado) {
      if (r.situacao === "ATIVO" && !f.ativo) return false;
      if (r.situacao === "CANCELADO" && !f.cancelado) return false;
    } else return false;
    if (f.estado !== "TODOS" && r.status !== f.estado) return false;
    if (f.pub === "PUBLICADA" && !r.pubVersion) return false;
    if (f.pub === "NAO" && r.pubVersion) return false;
    if (f.epico !== "TODOS" && r.epicoId !== f.epico) return false;
    if (q && !(r.id.toLowerCase().includes(q) || r.title.toLowerCase().includes(q))) return false;
    return true;
  });

  const table = rows.map(r => `
    <tr class="${r.situacao === "CANCELADO" ? "cancelled" : ""}" data-open-req="${r.id}">
      <td><span class="rid">${r.id}</span></td>
      <td class="rt-cell">${esc(r.title)}</td>
      <td style="color:var(--muted)">${esc(epicoById(r.epicoId).name)}</td>
      <td>${r.situacao === "ATIVO" ? `<span class="badge sit-ATIVO"><span class="bdot"></span>Ativo</span>` : `<span class="badge sit-CANCELADO"><span class="bdot"></span>Cancelada</span>`}</td>
      <td>${statusBadge(r)}</td>
      <td>${r.pubVersion ? `<span class="pub-tag">v${r.pubVersion}</span>` : `<span style="color:var(--faint)">—</span>`}</td>
      <td style="color:var(--muted);white-space:nowrap">${esc(r.updated)}</td>
    </tr>`).join("");

  return `
  <div class="wrap-wide">
    <div class="page-head">
      <div class="page-title"><div>
        <h1>Requisitos</h1>
        <div class="page-sub">Listagem centralizada do projeto <b>${esc(ATH.project.name)}</b> · ${rows.length} de ${ATH.requirements.length}</div>
      </div></div>
      <div class="actions"><button class="btn btn-primary" data-new-req>${icon("plus")} Novo requisito</button></div>
    </div>

    <div class="filters-bar">
      <span class="filters-label">Situação</span>
      <button class="filter-chip${f.ativo ? " on" : ""}" data-fchip="ativo"><span class="bdot"></span>ATIVO</button>
      <button class="filter-chip${f.cancelado ? " on" : ""}" data-fchip="cancelado"><span class="bdot"></span>CANCELADO</button>
      <span class="filters-label">Estado da candidata</span>
      <select class="select" data-fsel="estado">
        <option value="TODOS">Todos</option>
        ${Object.keys(STATUS_LABEL).map(k => `<option value="${k}"${f.estado === k ? " selected" : ""}>${STATUS_LABEL[k]}</option>`).join("")}
      </select>
      <span class="filters-label">Publicação</span>
      <select class="select" data-fsel="pub">
        <option value="TODOS">Todas</option>
        <option value="PUBLICADA"${f.pub === "PUBLICADA" ? " selected" : ""}>Com versão publicada</option>
        <option value="NAO"${f.pub === "NAO" ? " selected" : ""}>Sem publicação</option>
      </select>
      <span class="filters-label">Épico</span>
      <select class="select" data-fsel="epico">
        <option value="TODOS">Todos</option>
        ${ATH.epicos.map(e => `<option value="${e.id}"${f.epico === e.id ? " selected" : ""}>${esc(e.name)}</option>`).join("")}
      </select>
    </div>

    <div class="table-wrap">
      <table class="req-table">
        <thead><tr><th>ID</th><th>Título</th><th>Épico</th><th>Situação</th><th>Estado da candidata</th><th>Versão publicada</th><th>Última atualização</th></tr></thead>
        <tbody>${table || `<tr><td colspan="7"><div class="empty-table"><div class="big">🏛️</div>Nenhuma história de usuário encontrada para os filtros aplicados.</div></td></tr>`}</tbody>
      </table>
    </div>
  </div>`;
}

function bindRequisitos() {
  const v = $("#view");
  v.addEventListener("click", e => {
    const chip = e.target.closest("[data-fchip]");
    if (chip) {
      ATH.ui.filters[chip.dataset.fchip] = !ATH.ui.filters[chip.dataset.fchip];
      render();
      return;
    }
    const openReq = e.target.closest("[data-open-req]");
    if (openReq) { openReqInEditorFrom(openReq.dataset.openReq); return; }
    const newReq = e.target.closest("[data-new-req]");
    if (newReq) openNewReqModal(null);
  });
  v.addEventListener("change", e => {
    const sel = e.target.closest("[data-fsel]");
    if (sel) { ATH.ui.filters[sel.dataset.fsel] = sel.value; render(); }
  });
}

// ============================================================
// EDITOR (US09–US13) + gaveta Detalhes (US11)
// ============================================================
function editorHTML() {
  const r = reqById(ATH.ui.currentReq);
  if (!r) { ATH.ui.view = "visao"; return visaoHTML(); }
  // Edição livre em RASCUNHO; em APROVADA/PUBLICADA a edição é permitida,
  // mas invalida a candidata (nova candidata em Rascunho — seções 5.2/US13).
  // Em EM_SUBMISSAO/AGUARDANDO_REVISAO o snapshot fica bloqueado.
  const canEdit = ["RASCUNHO", "PUBLICADA", "APROVADA"].includes(r.status);

  const critBlocks = r.criterios.map((c, i) => `
    <div class="crit-block" data-crit="${i}">
      <button class="crit-rm" data-crit-rm="${i}" title="Remover critério">${icon("x", 14)}</button>
      <input class="ctx" value="${esc(c.ctx)}" placeholder="Nome do contexto" data-crit-ctx="${i}">
      <div class="crit-fields">
        <div class="crit-line"><label>Dado</label><input value="${esc(c.dado)}" placeholder="que…" data-crit-f="${i}|dado"></div>
        <div class="crit-line"><label>Quando</label><input value="${esc(c.quando)}" placeholder="…" data-crit-f="${i}|quando"></div>
        <div class="crit-line"><label>Então</label><input value="${esc(c.entao)}" placeholder="…" data-crit-f="${i}|entao"></div>
      </div>
    </div>`).join("");

  return `
  <div class="editor">
    <div class="e-col">
      <div class="e-top">
        <button class="iconbtn" id="edBack" title="Voltar">${icon("back")}</button>
        <input class="e-title-input" id="edTitle" value="${esc(r.title)}" title="Editar título">
        <span class="save-state" id="saveState"><span class="pulse"></span>Salvo</span>
        <button class="btn btn-ghost btn-sm" id="edDrawerBtn">${icon("sliders")} Detalhes</button>
        <span id="lifecycleBtns">${lifecycleBtn(r)}</span>
        <button class="iconbtn" id="edMore" title="Mais ações">${icon("dots")}</button>
      </div>
      <div class="doc-scroll" id="docScroll">
        <div class="e-toolbar" id="edToolbar">
          <button class="tool-btn" data-cmd="bold"><b>B</b></button>
          <button class="tool-btn" data-cmd="italic"><i>I</i></button>
          <button class="tool-btn" data-cmd="underline"><u>U</u></button>
          <span class="tool-sep"></span>
          <button class="tool-btn" data-block="h2"><span class="tg">H2</span></button>
          <button class="tool-btn" data-block="p"><span class="tg">¶</span></button>
          <span class="tool-sep"></span>
          <button class="tool-btn" data-cmd="insertUnorderedList"><span class="tg">• —</span></button>
          <button class="tool-btn" data-cmd="insertOrderedList"><span class="tg">1. —</span></button>
          <span class="tool-sep"></span>
          <button class="tool-btn" data-cmd="createLink">${icon("link", 14)}</button>
          <button class="tool-btn" id="insertTable">${icon("table", 14)}</button>
          <span class="tool-sep"></span>
          <button class="tool-btn" data-cmd="undo">${icon("back", 13)}</button>
          <button class="tool-btn" data-cmd="redo" style="transform:scaleX(-1)">${icon("back", 13)}</button>
        </div>
        <div class="doc-page" id="docPage" contenteditable="true" ${canEdit ? "" : 'data-locked="1"'}>${r.conteudo}</div>
        <div class="criteria${r.criterios.length ? "" : ""}" id="critSec">
          <div class="crit-head" id="critHead">
            <svg class="chev ic" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 18l6-6-6-6"/></svg>
            <h3>Critérios de aceite</h3>
            <span class="count-chip">${r.criterios.length}</span>
            <button class="btn btn-gold btn-sm" id="addCrit">+ Critério</button>
          </div>
          <div class="crit-body" id="critBody">${critBlocks || `<div class="rel-empty">Nenhum critério ainda. Use “+ Critério” para registrar cenários Dado/Quando/Então.</div>`}</div>
        </div>
      </div>
    </div>
    <aside class="edrawer${ATH.ui.editorDrawer ? " open" : ""}" id="edDrawer">${drawerHTML(r)}</aside>
  </div>`;
}

function lifecycleBtn(r) {
  if (r.situacao === "CANCELADO") return `<span class="badge sit-CANCELADO"><span class="bdot"></span>Cancelada</span>`;
  if (r.status === "RASCUNHO") return `<button class="btn btn-primary btn-sm" data-action="submit">${icon("send", 13)} Submeter revisão</button>`;
  if (r.status === "EM_SUBMISSAO") return `<span class="badge st-EM_SUBMISSAO"><span class="bdot"></span>SAGA em execução…</span>`;
  if (r.status === "AGUARDANDO_REVISAO") return `<button class="btn btn-gold btn-sm" data-action="approve">${icon("check", 13)} Aprovar</button><button class="btn btn-ghost btn-sm" data-action="reject">Rejeitar</button>`;
  if (r.status === "APROVADA") return `<button class="btn btn-primary btn-sm" data-action="publish">${icon("open", 13)} Publicar v${(r.pubVersion || 0) + 1}</button>`;
  if (r.status === "PUBLICADA") return `<span class="pub-tag">v${r.pubVersion} publicada</span>`;
  return "";
}

function drawerHTML(r) {
  return `
    <h4>Estado</h4>
    <div class="kv"><span class="k">Situação</span><span class="v">${r.situacao === "ATIVO" ? "Ativo" : "Cancelado"}</span></div>
    <div class="kv"><span class="k">Candidata</span><span class="v">${statusBadge(r)}</span></div>
    <div class="kv"><span class="k">Versão publicada</span><span class="v">${r.pubVersion ? "v" + r.pubVersion : "—"}</span></div>
    <h4>Metadados</h4>
    <div class="kv"><span class="k">Tipo</span>
      <select class="select" id="dwTipo" style="width:170px">
        ${["História de usuário", "Regra de negócio", "Requisito não funcional"].map(t => `<option${r.tipo === t ? " selected" : ""}>${t}</option>`).join("")}
      </select></div>
    <div class="kv"><span class="k">Pasta</span>
      <select class="select" id="dwPasta" style="width:170px">
        ${ATH.epicos.map(e => `<option value="${e.id}"${e.id === r.epicoId ? " selected" : ""}>${esc(e.name)}</option>`).join("")}
      </select></div>
    <div class="kv"><span class="k">Responsável</span><span class="v">${esc(r.autor)}</span></div>
    <div class="kv"><span class="k">Origem</span><span class="v">${esc(r.origem.tipo)}</span></div>
    <h4>Ações do ciclo de vida</h4>
    <div class="actions-col">
      ${r.situacao === "CANCELADO"
        ? `<button class="btn btn-subtle btn-sm btn-block" data-action="reactivate">Reativar requisito</button>`
        : `<button class="btn btn-danger btn-sm btn-block" data-action="cancel">Cancelar US</button>`}
    </div>
    <div class="flow-note">
      <b>Fluxo da candidata:</b> Rascunho → Em submissão → Aguardando revisão → Aprovada → Publicada.<br>
      A situação ATIVO/CANCELADO é independente. Editar uma candidata aprovada exige nova revisão.
    </div>`;
}

function bindEditor() {
  const v = $("#view");
  const r = reqById(ATH.ui.currentReq);
  if (!r) return;
  let saveTimer = null;

  const doc = $("#docPage");
  const title = $("#edTitle");
  const saveEl = $("#saveState");

  function markPending() {
    clearTimeout(saveTimer);
    saveEl.classList.add("pending");
    saveEl.innerHTML = `<span class="pulse"></span>Alterações pendentes…`;
    saveTimer = setTimeout(saveNow, 1500);
  }
  function saveNow() {
    clearTimeout(saveTimer);
    r.conteudo = doc.innerHTML;
    r.updated = "agora";
    saveEl.classList.remove("pending");
    saveEl.innerHTML = `<span class="pulse"></span>Salvo`;
    toast("Rascunho salvo automaticamente", "success");
  }

  // US13/5.2: editar PUBLICADA/APROVADA invalida e devolve a Rascunho (uma vez por candidata)
  let invalidated = false;
  doc.addEventListener("input", () => {
    if (!invalidated && (r.status === "PUBLICADA" || r.status === "APROVADA")) {
      invalidated = true;
      r.status = "RASCUNHO";
      toast("Alteração invalidou a candidata — nova candidata em Rascunho", "info");
      const lc = $("#lifecycleBtns");
      if (lc) lc.innerHTML = lifecycleBtn(r);
    }
    markPending();
  });
  if (doc.dataset.locked) doc.addEventListener("focus", () => { doc.blur(); toast("Conteúdo bloqueado: candidata em submissão/revisão (snapshot protegido).", "info"); });

  title.addEventListener("input", markPending);
  title.addEventListener("blur", () => { if (title.value.trim() !== r.title) { r.title = title.value.trim() || r.title; toast("Título atualizado", "success"); } });

  // Toolbar
  $("#edToolbar").addEventListener("mousedown", e => e.preventDefault());
  $("#edToolbar").addEventListener("click", e => {
    const b = e.target.closest(".tool-btn");
    if (!b) return;
    doc.focus();
    if (b.dataset.cmd === "createLink") {
      const url = prompt("URL do link:", "https://");
      if (url) document.execCommand("createLink", false, url);
      return;
    }
    if (b.dataset.block) { document.execCommand("formatBlock", false, b.dataset.block); return; }
    if (b.id === "insertTable") {
      document.execCommand("insertHTML", false, `<table><tr><th>Campo</th><th>Comportamento</th></tr><tr><td>…</td><td>…</td></tr></table><p></p>`);
      markPending();
      return;
    }
    document.execCommand(b.dataset.cmd, false, null);
    markPending();
  });

  // Critérios (US10)
  $("#critHead").addEventListener("click", e => {
    if (e.target.closest("#addCrit")) return;
    $("#critSec").classList.toggle("open");
  });
  $("#addCrit").onclick = () => {
    r.criterios.push({ ctx: "Novo critério", dado: "", quando: "", entao: "" });
    renderEditorOnlyChrome(r); // não: recarrega tudo, mas perde foco do editor; optamos por reconstruir critérios
  };

  $("#critBody").addEventListener("input", e => {
    const t = e.target;
    if (t.dataset.critCtx !== undefined) { r.criterios[+t.dataset.critCtx].ctx = t.value; }
    if (t.dataset.critF) {
      const [i, k] = t.dataset.critF.split("|");
      r.criterios[+i][k] = t.value;
    }
    markPending();
  });
  $("#critBody").addEventListener("click", e => {
    const rm = e.target.closest("[data-crit-rm]");
    if (rm) { r.criterios.splice(+rm.dataset.critRm, 1); renderEditorOnlyChrome(r); toast("Critério removido", "success"); }
  });

  // Gaveta detalhes (US11)
  $("#edDrawerBtn").onclick = () => { ATH.ui.editorDrawer = !ATH.ui.editorDrawer; $("#edDrawer").classList.toggle("open", ATH.ui.editorDrawer); };
  $("#edBack").onclick = () => { ATH.ui.view = ATH.ui.prevView; render(); };

  $("#dwPasta") && ($("#dwPasta").onchange = e => {
    const ep = epicoById(e.target.value);
    const old = epicoById(r.epicoId);
    r.epicoId = e.target.value;
    toast(`US movida de “${old.name}” para “${ep.name}”`, "success");
  });
  $("#dwTipo") && ($("#dwTipo").onchange = e => { r.tipo = e.target.value; toast("Tipo atualizado", "success"); });

  // Menu "..." (US13)
  $("#edMore").onclick = e => {
    openMenu(e.currentTarget, [
      { id: "det", icon: "sliders", label: "Detalhes", onClick: () => { ATH.ui.editorDrawer = true; $("#edDrawer").classList.add("open"); } },
      { type: "sep" },
      r.situacao === "CANCELADO"
        ? { id: "reat", icon: "check", label: "Reativar US", onClick: () => doAction("reactivate", r) }
        : { id: "cancel", icon: "trash", label: "Cancelar US", danger: true, onClick: () => doAction("cancel", r) },
    ], { align: "right" });
  };

  // Ações do ciclo de vida (delegação)
  v.addEventListener("click", e => {
    const a = e.target.closest("[data-action]");
    if (a) doAction(a.dataset.action, r);
  });
}

function renderEditorChromeOnly(r) {
  // Reconstrói apenas critérios (evita perder o scroll do editor)
  const sec = $("#critSec");
  const body = $("#critBody");
  sec.querySelector(".count-chip").textContent = r.criterios.length;
  body.innerHTML = r.criterios.map((c, i) => `
    <div class="crit-block" data-crit="${i}">
      <button class="crit-rm" data-crit-rm="${i}" title="Remover critério">${icon("x", 14)}</button>
      <input class="ctx" value="${esc(c.ctx)}" placeholder="Nome do contexto" data-crit-ctx="${i}">
      <div class="crit-fields">
        <div class="crit-line"><label>Dado</label><input value="${esc(c.dado)}" placeholder="que…" data-crit-f="${i}|dado"></div>
        <div class="crit-line"><label>Quando</label><input value="${esc(c.quando)}" placeholder="…" data-crit-f="${i}|quando"></div>
        <div class="crit-line"><label>Então</label><input value="${esc(c.entao)}" placeholder="…" data-crit-f="${i}|entao"></div>
      </div>
    </div>`).join("") || `<div class="rel-empty">Nenhum critério ainda. Use “+ Critério” para registrar cenários Dado/Quando/Então.</div>`;
}

function renderEditorOnlyChrome(r) { renderEditorChromeOnly(r); }

// ---------- ações do ciclo de vida ----------
function doAction(action, r) {
  if (action === "submit") {
    r.status = "EM_SUBMISSAO";
    render();
    toast("SAGA de submissão iniciada: registrando retrato de rastreabilidade…", "info");
    setTimeout(() => {
      r.status = "AGUARDANDO_REVISAO";
      if (ATH.ui.view === "editor" && ATH.ui.currentReq === r.id) render();
      toast("SAGA concluída — lote de revisão aberto (AGUARDANDO_REVISAO)", "success");
    }, 1600);
  }
  if (action === "approve") {
    r.status = "APROVADA";
    render();
    toast(`Candidata de ${r.id} aprovada — elegível para publicação explícita`, "success");
  }
  if (action === "reject") {
    const { el, close } = openModal(`
    <div class="modal">
      <div class="modal-head"><div class="modal-title">Rejeitar candidata</div>
        <div class="modal-sub">A decisão é registrada com justificativa. A candidata volta ao estado <b>Rascunho</b>, preservando o conteúdo para correção.</div>
        <button class="iconbtn modal-x" data-close>${icon("x")}</button></div>
      <div class="modal-body">
        <div class="field"><label>Justificativa</label>
          <textarea class="input" id="rejWhy" rows="3" placeholder="Ex.: regra de negócio ambígua no item 2…"></textarea></div>
      </div>
      <div class="modal-foot">
        <button class="btn btn-ghost" data-close>Cancelar</button>
        <button class="btn btn-danger-solid" id="rejOk" disabled>Rejeitar</button>
      </div>
    </div>`);
    const ta = el.querySelector("#rejWhy"), ok = el.querySelector("#rejOk");
    ta.oninput = () => { ok.disabled = !ta.value.trim(); };
    ok.onclick = () => {
      r.status = "RASCUNHO";
      close();
      render();
      toast("Candidata devolvida a Rascunho — tentativa preservada no histórico", "info");
    };
  }
  if (action === "publish") {
    confirmModal({
      title: `Publicar ${r.id}`,
      message: `A versão ${(r.pubVersion || 0) + 1} substitui a atual no <b>Livro de Requisitos</b>, com o retrato aprovado de relações. A versão anterior permanece no histórico.`,
      confirmLabel: "Publicar versão",
      onConfirm: () => {
        r.pubVersion = (r.pubVersion || 0) + 1;
        r.status = "PUBLICADA";
        render();
        toast(`Versão v${r.pubVersion} de ${r.id} publicada no Livro`, "success");
      },
    });
  }
  if (action === "cancel") {
    confirmModal({
      title: "Cancelar US",
      message: `Tem certeza de que deseja cancelar esta US? Ela sai das listagens ativas e do Livro corrente, mas <b>poderá ser encontrada na aba Requisitos</b> com a tag “Cancelada”, preservando histórico, versões e relações.`,
      confirmLabel: "Cancelar US",
      danger: true,
      onConfirm: () => {
        r.situacao = "CANCELADO";
        render();
        toast(`${r.id} cancelada — histórico e relações preservados`, "info");
      },
    });
  }
  if (action === "reactivate") {
    r.situacao = "ATIVO";
    r.status = "RASCUNHO";
    render();
    toast(`${r.id} reativada — nova candidata em Rascunho (retorno ao Livro exige aprovação e publicação)`, "success");
  }
}

// ============================================================
// CONFIGURAÇÕES → PASTAS (US08) — renomear/excluir épicos
// ============================================================
function openSettingsModal(tab) {
  ATH.ui.settingsTab = tab || "pastas";
  const { el, close } = openModal(`
  <div class="modal lg" id="settingsModal">
    <div class="modal-head">
      <div class="modal-title">Configurações do projeto</div>
      <button class="iconbtn modal-x" data-close>${icon("x")}</button>
    </div>
    <div class="modal-tabs">
      <button class="tab" data-stab="geral">Geral</button>
      <button class="tab" data-stab="pastas">Pastas</button>
      <button class="tab" data-stab="membros">Membros</button>
    </div>
    <div class="modal-body" id="settingsBody" style="min-height:280px"></div>
    <div class="modal-foot"><button class="btn btn-subtle" data-close>Concluir</button></div>
  </div>`);

  const body = el.querySelector("#settingsBody");
  function renderTab() {
    el.querySelectorAll(".modal-tabs .tab").forEach(t => t.classList.toggle("active", t.dataset.stab === ATH.ui.settingsTab));
    const t = ATH.ui.settingsTab;
    if (t === "geral") body.innerHTML = settingsGeralHTML();
    if (t === "pastas") body.innerHTML = settingsPastasHTML();
    if (t === "membros") body.innerHTML = settingsMembrosHTML();
  }
  renderTab();
  el.querySelector(".modal-tabs").addEventListener("click", e => {
    const b = e.target.closest("[data-stab]");
    if (b) { ATH.ui.settingsTab = b.dataset.stab; renderTab(); }
  });

  // Delegação de eventos do corpo
  body.addEventListener("click", e => {
    const ren = e.target.closest("[data-ren-epico]");
    const del = e.target.closest("[data-del-epico]");
    const add = e.target.closest("[data-add-pasta]");
    if (ren) { startRename(ren.dataset.renEpico, ren); return; }
    if (del) { tryDelete(del.dataset.delEpico); return; }
    if (add) { close(); openNewEpicoModal(); return; }
  });
  body.addEventListener("click", e => {
    const save = e.target.closest("[data-save-ren]");
    if (save) {
      const ep = epicoById(save.dataset.saveRen);
      const input = body.querySelector(`[data-ren-input="${ep.id}"]`);
      const name = input.value.trim();
      if (name) { ep.name = name; toast(`Épico renomeado para “${name}”`, "success"); }
      renderTab();
      if (ATH.ui.view === "mapa") { renderMapNodes(); drawEdges(); }
      else if (ATH.ui.view === "visao") render();
    }
  });

  function startRename(epId, anchor) {
    const ep = epicoById(epId);
    const row = anchor.closest(".pasta-row");
    row.innerHTML = `
      ${icon("folder")}<span class="picon"></span>
      <input class="input" data-ren-input="${ep.id}" value="${esc(ep.name)}">
      <span class="pcount">${reqsOf(ep.id).length} US</span>
      <button class="btn btn-primary btn-sm" data-save-ren="${ep.id}">Salvar</button>`;
    const inp = row.querySelector("input");
    inp.focus();
    inp.select();
    inp.addEventListener("keydown", ev => { if (ev.key === "Enter") row.querySelector("[data-save-ren]").click(); });
  }

  function tryDelete(epId) {
    const ep = epicoById(epId);
    const n = reqsOf(ep.id).length;
    if (n > 0) {
      toast(`O épico “${ep.name}” contém ${n} requisito(s). Mova-os antes de excluir.`, "error");
      return;
    }
    confirmModal({
      title: "Excluir épico",
      message: `Excluir o épico vazio <b>${esc(ep.name)}</b>?`,
      confirmLabel: "Excluir",
      danger: true,
      onConfirm: () => {
        ATH.epicos = ATH.epicos.filter(x => x.id !== ep.id);
        renderTab();
        if (ATH.ui.view === "visao") render();
        if (ATH.ui.view === "mapa") { renderMapNodes(); drawEdges(); }
        toast(`Épico “${ep.name}” excluído`, "success");
      },
    });
  }
}

function settingsGeralHTML() {
  return `
  <div class="field"><label>Nome do projeto</label><input class="input" value="${esc(ATH.project.name)}"></div>
  <div class="field"><label>Código (usado nos identificadores dos requisitos)</label><input class="input" value="${esc(ATH.project.code)}" disabled></div>
  <div class="field-hint">O nome do projeto deve ser único no workspace. O código gera identificadores rastreáveis (REQ-42 → US-042).</div>`;
}

function settingsPastasHTML() {
  const rows = ATH.epicos.map(ep => `
    <div class="pasta-row" data-epico-row="${ep.id}">
      <span class="picon">${icon("folder")}</span>
      <span class="pname">${esc(ep.name)}</span>
      <span class="pcount">${reqsOf(ep.id).length} US</span>
      <button class="iconbtn" data-ren-epico="${ep.id}" title="Renomear">${icon("edit", 14)}</button>
      <button class="iconbtn danger" data-del-epico="${ep.id}" title="Excluir">${icon("trash", 14)}</button>
    </div>`).join("");
  return `
  <div class="field-hint" style="margin-bottom:12px">Épicos agrupam histórias de usuário e requisitos. Não é possível excluir um épico que contém requisitos.</div>
  ${rows}
  <button class="btn btn-gold btn-block" data-add-pasta style="margin-top:8px">${icon("plus")} Novo Épico</button>`;
}

function settingsMembrosHTML() {
  const members = [
    { n: "Ana Clara", e: "ana@athena.dev", r: "Owner", you: true },
    { n: "Fernando Scarabeli", e: "fernando@athena.dev", r: "Aprovador" },
    { n: "José Vítor Machado", e: "jose@athena.dev", r: "Editor" },
    { n: "Thomas", e: "thomas@athena.dev", r: "Repórter" },
  ];
  const roles = ["Leitor", "Repórter", "Editor", "Aprovador", "Mantenedor", "Owner"];
  return `
  <div class="field-hint" style="margin-bottom:10px">Papéis cumulativos por projeto: Leitor → Repórter → Editor → Aprovador → Mantenedor → Owner.</div>
  ${members.map(m => `
    <div class="membro-row">
      <span class="avatar">${m.n.split(" ").map(x => x[0]).slice(0, 2).join("")}</span>
      <span class="mname">${esc(m.n)} ${m.you ? '<span class="you">· Você</span>' : ""}<br><span style="font-weight:400;font-size:12px;color:var(--faint)">${esc(m.e)}</span></span>
      <select class="select">${roles.map(r => `<option${r === m.r ? " selected" : ""}>${r}</option>`).join("")}</select>
    </div>`).join("")}`;
}

// ============================================================
// BOOT — deep-links (#visao | #requisitos | #mapa | #editor/US-XXX)
// ficam pendentes até o login (a sessão é em memória)
// ============================================================
(function bootFromHash() {
  const h = location.hash.replace("#", "");
  if (h.startsWith("editor/") || ["visao", "requisitos", "mapa"].includes(h)) DB.pendingHash = h;
})();
render();
