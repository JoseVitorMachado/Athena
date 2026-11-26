// ============================================================
// ATHENA — Tela "Mapa" (US14–US17)
// Quadro infinito com épicos (pastas), cartões de US,
// arestas rotuladas, painel lateral e conexão de relações.
// Depende dos helpers globais definidos em app.js (icon, esc,
// toast, openModal, badgeHTML, ...) — chamados só em runtime.
// ============================================================
const MapView = {
  view: { x: 30, y: 20, s: 0.9 },   // pan/zoom atuais
  sel: null,                        // id da US selecionada
  WORLD_W: 2400, WORLD_H: 1900,
  CARD_W: 232, CARD_H: 74, PAD: 16, HEAD: 42, GAP: 10,
};

// ---------- utilidades de layout ----------
function mReqsOf(epicoId) { return ATH.requirements.filter(r => r.epicoId === epicoId); }
function mCardRect(req) {
  const ep = ATH.epicos.find(e => e.id === req.epicoId);
  const idx = mReqsOf(ep.id).findIndex(r => r.id === req.id);
  return {
    x: ep.x + MapView.PAD,
    y: ep.y + MapView.HEAD + MapView.PAD + idx * (MapView.CARD_H + MapView.GAP),
    w: MapView.CARD_W, h: MapView.CARD_H,
  };
}
function folderSize(ep) {
  const n = Math.max(1, mReqsOf(ep.id).length);
  return {
    w: MapView.CARD_W + MapView.PAD * 2,
    h: MapView.HEAD + MapView.PAD + n * MapView.CARD_H + (n - 1) * MapView.GAP + MapView.PAD,
  };
}

// ---------- render principal ----------
function renderMapView() {
  const v = document.getElementById("view");
  v.className = "";
  v.innerHTML = `
  <div class="map-wrap">
    <div class="map-statusbar">
      <div class="map-statuschip"><span class="ldot"></span> Projeção atualizada <span id="mapAgo">· há instantes</span></div>
    </div>

    <div class="map-viewport" id="mapViewport">
      <div class="map-world" id="mapWorld">
        <svg class="map-edges" id="mapEdges" width="${MapView.WORLD_W}" height="${MapView.WORLD_H}"></svg>
        <div id="mapNodes"></div>
      </div>
    </div>

    <div class="zoom-ctrl">
      <button id="zoomOut" title="Reduzir">${icon("zoomOut")}</button>
      <span class="zl" id="zoomLevel">${Math.round(MapView.view.s * 100)}%</span>
      <button id="zoomIn" title="Ampliar">${icon("zoomIn")}</button>
      <span class="zsep"></span>
      <button id="zoomFit" title="Ajustar à tela">${icon("fit")}</button>
    </div>

    <aside class="mdrawer" id="mapDrawer"></aside>
  </div>
  <div class="map-tip" id="mapTip"></div>`;

  renderMapNodes();
  drawEdges();
  bindMapEvents();
  applyMapTransform();
  if (!MapView._fitted) fitMap();
  if (MapView.sel) { selectCard(MapView.sel, false); }
}

// ---------- nós (pastas + cartões) ----------
function renderMapNodes() {
  const nodes = document.getElementById("mapNodes");
  if (!nodes) return;
  let html = "";
  for (const ep of ATH.epicos) {
    const { w, h } = folderSize(ep);
    const reqs = mReqsOf(ep.id);
    let cards = "";
    for (const r of reqs) {
      const rect = mCardRect(r);
      const rel = relCount(r.id);
      cards += `
      <div class="mcard${r.situacao === "CANCELADO" ? " cancelled" : ""}${MapView.sel === r.id ? " selected" : ""}"
           data-id="${r.id}" style="left:${rect.x}px;top:${rect.y}px;--cw:${MapView.CARD_W}px">
        <div class="mcard-top"><span class="rid">${r.id}</span>${mcardBadge(r)}</div>
        <div class="mcard-title">${esc(r.title)}</div>
        <div class="mcard-foot">
          <span class="relcount">${icon("link")}${rel} relaç${rel === 1 ? "ão" : "ões"}</span>
          ${r.pubVersion ? `<span class="pub-tag">v${r.pubVersion} publicada</span>` : ""}
        </div>
      </div>`;
    }
    html += `
    <div class="folder" style="left:${ep.x}px;top:${ep.y}px;width:${w}px;height:${h}px">
      <div class="folder-head">${icon("folder")}<span class="folder-name">${esc(ep.name)}</span>
        <span class="folder-count count-chip">${reqs.length} US</span>
      </div>
    </div>${cards}`;
  }
  nodes.innerHTML = html;
}

function mcardBadge(r) {
  if (r.situacao === "CANCELADO") return `<span class="badge sit-CANCELADO"><span class="bdot"></span>Cancelada</span>`;
  return `<span class="badge st-${r.status}"><span class="bdot"></span>${STATUS_LABEL[r.status]}</span>`;
}

// ---------- arestas ----------
function edgeAnchorPoint(fromRect, toPt) {
  const cx = fromRect.x + fromRect.w / 2, cy = fromRect.y + fromRect.h / 2;
  const dx = toPt.x - cx, dy = toPt.y - cy;
  const m = 7; // margem para fora do cartão
  const sx = dx !== 0 ? (fromRect.w / 2 + m) / Math.abs(dx) : Infinity;
  const sy = dy !== 0 ? (fromRect.h / 2 + m) / Math.abs(dy) : Infinity;
  const t = Math.min(sx, sy, 1);
  return { x: cx + dx * t, y: cy + dy * t };
}

function drawEdges() {
  const svg = document.getElementById("mapEdges");
  if (!svg) return;
  let inner = `
    <defs>
      <marker id="arrow" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="7" markerHeight="7" orient="auto">
        <path d="M0 0L10 5L0 10z" fill="var(--edge)"/></marker>
      <marker id="arrowHot" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="8" markerHeight="8" orient="auto">
        <path d="M0 0L10 5L0 10z" fill="var(--edge-hot)"/></marker>
    </defs>`;

  for (const rel of ATH.relations) {
    const a = ATH.requirements.find(r => r.id === rel.from);
    const b = ATH.requirements.find(r => r.id === rel.to);
    if (!a || !b) continue;
    const ra = mCardRect(a), rb = mCardRect(b);
    const ca = { x: ra.x + ra.w / 2, y: ra.y + ra.h / 2 };
    const cb = { x: rb.x + rb.w / 2, y: rb.y + rb.h / 2 };
    const p1 = edgeAnchorPoint(ra, cb);
    const p2 = edgeAnchorPoint(rb, ca);
    const mx = (p1.x + p2.x) / 2, my = (p1.y + p2.y) / 2;
    const cancelled = a.situacao === "CANCELADO" || b.situacao === "CANCELADO";
    const hasArrow = rel.type !== "RELACIONADO_A";
    const labelW = rel.type.length * 7.2 + 18;

    inner += `
    <g class="edge${cancelled ? " edge-dashed" : ""}" data-rel="${rel.id}" data-from="${rel.from}" data-to="${rel.to}" data-type="${rel.type}">
      <path class="edge-hit" d="M ${p1.x} ${p1.y} L ${p2.x} ${p2.y}"/>
      <line class="edge-line" x1="${p1.x}" y1="${p1.y}" x2="${p2.x}" y2="${p2.y}"
        ${hasArrow ? 'marker-end="url(#arrow)"' : ""}/>
      <g class="edge-label" transform="translate(${mx}, ${my})">
        <rect x="${-labelW / 2}" y="-11" width="${labelW}" height="22" rx="11"/>
        <text y="0.5">${rel.type}</text>
      </g>
    </g>`;
  }
  svg.innerHTML = inner;
}

// ---------- eventos de pan / zoom / hover / seleção ----------
function bindMapEvents() {
  const vp = document.getElementById("mapViewport");
  const world = document.getElementById("mapWorld");
  let drag = null;

  vp.addEventListener("pointerdown", e => {
    if (e.target.closest(".mcard") || e.target.closest(".zoom-ctrl")) return;
    drag = { x0: e.clientX, y0: e.clientY, vx: MapView.view.x, vy: MapView.view.y };
    vp.classList.add("dragging");
    vp.setPointerCapture(e.pointerId);
  });
  vp.addEventListener("pointermove", e => {
    if (!drag) return;
    MapView.view.x = drag.vx + (e.clientX - drag.x0);
    MapView.view.y = drag.vy + (e.clientY - drag.y0);
    applyMapTransform();
  });
  const endDrag = () => { drag = null; vp.classList.remove("dragging"); };
  vp.addEventListener("pointerup", endDrag);
  vp.addEventListener("pointercancel", endDrag);

  // Zoom com roda, centrado no cursor
  vp.addEventListener("wheel", e => {
    e.preventDefault();
    const rect = vp.getBoundingClientRect();
    const px = e.clientX - rect.left, py = e.clientY - rect.top;
    const factor = e.deltaY < 0 ? 1.12 : 1 / 1.12;
    zoomAt(px, py, factor);
  }, { passive: false });

  document.getElementById("zoomIn").onclick = () => zoomCenter(1.2);
  document.getElementById("zoomOut").onclick = () => zoomCenter(1 / 1.2);
  document.getElementById("zoomFit").onclick = () => fitMap(true);

  // Hover em cartões → destaca arestas
  const nodes = document.getElementById("mapNodes");
  nodes.addEventListener("mouseover", e => {
    const card = e.target.closest(".mcard");
    if (!card) return;
    highlightEdges(card.dataset.id);
  });
  nodes.addEventListener("mouseout", e => {
    if (e.target.closest(".mcard")) clearEdgeHighlight();
  });
  nodes.addEventListener("dblclick", e => {
    const card = e.target.closest(".mcard");
    if (card) openReqInEditor(card.dataset.id);
  });
  nodes.addEventListener("click", e => {
    const card = e.target.closest(".mcard");
    if (card) selectCard(card.dataset.id);
  });

  // Hover em arestas → tooltip com leitura direcional
  const svg = document.getElementById("mapEdges");
  const tip = document.getElementById("mapTip");
  svg.addEventListener("mouseover", e => {
    const g = e.target.closest(".edge");
    if (!g) return;
    g.classList.add("hot");
    tip.textContent = relSentence(g.dataset.from, g.dataset.type, g.dataset.to);
    tip.classList.add("show");
  });
  svg.addEventListener("mousemove", e => { if (tip.classList.contains("show")) positionTip(e); });
  svg.addEventListener("mouseout", e => {
    const g = e.target.closest(".edge");
    if (g) { g.classList.remove("hot"); tip.classList.remove("show"); }
  });
}

function positionTip(e) {
  const tip = document.getElementById("mapTip");
  const pad = 14;
  let x = e.clientX + pad, y = e.clientY - pad - 8;
  const r = tip.getBoundingClientRect();
  if (x + r.width > window.innerWidth - 10) x = e.clientX - r.width - pad;
  tip.style.left = x + "px";
  tip.style.top = y + "px";
}

function highlightEdges(cardId) {
  document.querySelectorAll(".edge").forEach(g => {
    const touching = g.dataset.from === cardId || g.dataset.to === cardId;
    g.classList.toggle("hot", touching);
    g.classList.toggle("dimmed", !touching);
  });
}
function clearEdgeHighlight() {
  document.querySelectorAll(".edge").forEach(g => g.classList.remove("hot", "dimmed"));
}

// ---------- transform / zoom helpers ----------
function applyMapTransform() {
  const world = document.getElementById("mapWorld");
  if (!world) return;
  const { x, y, s } = MapView.view;
  world.style.transform = `translate(${x}px, ${y}px) scale(${s})`;
  const zl = document.getElementById("zoomLevel");
  if (zl) zl.textContent = Math.round(s * 100) + "%";
}
function zoomAt(px, py, factor) {
  const { x, y, s } = MapView.view;
  const ns = Math.min(2, Math.max(0.3, s * factor));
  const k = ns / s;
  MapView.view = { x: px - (px - x) * k, y: py - (py - y) * k, s: ns };
  applyMapTransform();
}
function zoomCenter(factor) {
  const vp = document.getElementById("mapViewport");
  const r = vp.getBoundingClientRect();
  zoomAt(r.width / 2, r.height / 2, factor);
}
function fitMap(animate) {
  const vp = document.getElementById("mapViewport");
  if (!vp) return;
  let minX = 1e9, minY = 1e9, maxX = -1e9, maxY = -1e9;
  for (const ep of ATH.epicos) {
    const { w, h } = folderSize(ep);
    minX = Math.min(minX, ep.x); minY = Math.min(minY, ep.y);
    maxX = Math.max(maxX, ep.x + w); maxY = Math.max(maxY, ep.y + h);
  }
  const r = vp.getBoundingClientRect();
  const pad = 70;
  const s = Math.min(2, Math.max(0.25, Math.min((r.width - pad * 2) / (maxX - minX), (r.height - pad * 2) / (maxY - minY))));
  const tx = pad - minX * s + (r.width - pad * 2 - (maxX - minX) * s) / 2;
  const ty = pad - minY * s + (r.height - pad * 2 - (maxY - minY) * s) / 2;
  const target = { x: tx, y: ty, s };
  MapView._fitted = true;
  if (animate) animateMapTo(target, 350);
  else { MapView.view = target; applyMapTransform(); }
}
function animateMapTo(target, ms) {
  const start = { ...MapView.view };
  const t0 = performance.now();
  function step(now) {
    const t = Math.min(1, (now - t0) / ms);
    const e = 1 - Math.pow(1 - t, 3);
    MapView.view = {
      x: start.x + (target.x - start.x) * e,
      y: start.y + (target.y - start.y) * e,
      s: start.s + (target.s - start.s) * e,
    };
    applyMapTransform();
    if (t < 1) requestAnimationFrame(step);
  }
  requestAnimationFrame(step);
}

function centerOnCard(reqId, animate) {
  const req = ATH.requirements.find(r => r.id === reqId);
  if (!req) return;
  const vp = document.getElementById("mapViewport");
  const r = vp.getBoundingClientRect();
  const rect = mCardRect(req);
  const cx = rect.x + rect.w / 2, cy = rect.y + rect.h / 2;
  const drawerW = document.getElementById("mapDrawer").classList.contains("open") ? 360 : 0;
  const s = MapView.view.s;
  const target = { x: (r.width - drawerW) / 2 - cx * s, y: r.height / 2 - cy * s, s };
  if (animate) animateMapTo(target, 420); else { MapView.view = target; applyMapTransform(); }
}

// ---------- seleção + gaveta (US15/US16) ----------
function selectCard(reqId, refocus) {
  MapView.sel = reqId;
  document.querySelectorAll(".mcard").forEach(c => c.classList.toggle("selected", c.dataset.id === reqId));
  renderMapDrawer();
  if (refocus) centerOnCard(reqId, true);
}

function renderMapDrawer() {
  const d = document.getElementById("mapDrawer");
  const req = ATH.requirements.find(r => r.id === MapView.sel);
  if (!req) { d.classList.remove("open"); return; }
  d.classList.add("open");

  const rels = relsOf(req.id);
  let relHtml = "";
  if (!rels.length) relHtml = `<div class="rel-empty">Nenhuma relação registrada.</div>`;
  for (const { rel, role } of rels) {
    relHtml += `
    <div class="rel-row">
      ${relReadChip(role, rel, req.id)}
      <button class="rm" data-rmrel="${rel.id}" title="Remover relação">${icon("x")}</button>
    </div>`;
  }

  let critHtml = "";
  if (!req.criterios.length) critHtml = `<div class="rel-empty">Nenhum critério registrado.</div>`;
  req.criterios.forEach(c => {
    critHtml += `
    <div class="crit-mini">
      <b>${esc(c.ctx || "Critério")}</b><br>
      <span>Dado</span> <em>${esc(c.dado)}</em><br>
      <span>Quando</span> <em>${esc(c.quando)}</em><br>
      <span>Então</span> <em>${esc(c.entao)}</em>
    </div>`;
  });

  d.innerHTML = `
    <div class="md-head">
      <div class="mrow">
        <span class="rid">${req.id}</span>
        ${mcardBadge(req)}
        ${req.pubVersion ? `<span class="pub-tag">v${req.pubVersion} publicada</span>` : `<span class="count-chip">sem publicação</span>`}
      </div>
      <h3>${esc(req.title)}</h3>
      <div class="mmeta">
        ${esc(req.tipo)} · Atualizada <b>${esc(req.updated)}</b> por <b>${esc(req.autor)}</b><br>
        Situação: <b>${req.situacao === "CANCELADO" ? "Cancelado" : "Ativo"}</b>
      </div>
      <button class="iconbtn md-close" id="mdClose">${icon("x")}</button>
    </div>
    <div class="md-body">
      <div class="macc open" id="accRel">
        <button class="acc-head">${icon("chevR")} Relações <span class="cnt">(${rels.length})</span><span class="add" id="btnConnect">+ Conectar relação</span></button>
        <div class="acc-body">${relHtml}</div>
      </div>
      <div class="macc" id="accCrit">
        <button class="acc-head">${icon("chevR")} Critérios de aceite <span class="cnt">(${req.criterios.length})</span></button>
        <div class="acc-body">${critHtml}</div>
      </div>
      <div class="macc" id="accOrig">
        <button class="acc-head">${icon("chevR")} Origem</button>
        <div class="acc-body">
          <div class="origin-mini"><b>${esc(req.origem.tipo)}</b><br>${esc(req.origem.detalhe)}</div>
        </div>
      </div>
    </div>
    <div class="md-foot">
      <button class="btn btn-ghost" id="btnCenter">${icon("crosshair")} Centralizar no mapa</button>
      <button class="btn btn-primary" id="btnOpen">${icon("open")} Abrir requisito</button>
    </div>`;

  d.querySelector("#mdClose").onclick = () => { d.classList.remove("open"); MapView.sel = null; document.querySelectorAll(".mcard.selected").forEach(c => c.classList.remove("selected")); };
  d.querySelector("#btnCenter").onclick = () => centerOnCard(req.id, true);
  d.querySelector("#btnOpen").onclick = () => openReqInEditor(req.id);
  d.querySelector("#btnConnect").onclick = e => { e.stopPropagation(); openConnectModal(req.id); };
  d.querySelectorAll(".acc-head").forEach(h => {
    h.addEventListener("click", ev => {
      if (ev.target.closest("#btnConnect")) return;
      h.parentElement.classList.toggle("open");
    });
  });
  d.querySelectorAll("[data-rmrel]").forEach(btn => {
    btn.onclick = () => {
      const id = btn.dataset.rmrel;
      const i = ATH.relations.findIndex(r => r.id === id);
      if (i > -1) ATH.relations.splice(i, 1);
      toast(`Relação removida`, "success");
      renderMapNodes(); drawEdges(); renderMapDrawer();
    };
  });
}

// Leitura dos extremos sem duplicar aresta (US16):
// Pré-requisito/Dependente e Bloqueia/Bloqueada por.
function relReadChip(role, rel, selfId) {
  const other = rel.from === selfId ? rel.to : rel.from;
  if (rel.type === "DEPENDE_DE") {
    return role === "out"
      ? `<span>Depende de</span> <span class="rid">${other}</span>`
      : `<span>É pré-requisito de</span> <span class="rid">${other}</span>`;
  }
  if (rel.type === "BLOQUEIA") {
    return role === "out"
      ? `<span class="sem">Bloqueia</span> <span class="rid">${other}</span>`
      : `<span>Bloqueada por</span> <span class="rid">${other}</span>`;
  }
  return `<span>Relacionada a</span> <span class="rid">${other}</span>`;
}

// ---------- modal "+ Conectar relação" (US16) ----------
function openConnectModal(fromId) {
  const from = ATH.requirements.find(r => r.id === fromId);
  const html = `
  <div class="modal md">
    <div class="modal-head">
      <div class="modal-title">Conectar relação</div>
      <div class="modal-sub">A partir de <b class="rid">${fromId}</b> — ${esc(from.title)}. Pesquise a US de destino e escolha a semântica da relação.</div>
      <button class="iconbtn modal-x" data-close>${icon("x")}</button>
    </div>
    <div class="modal-body">
      <div class="field" style="margin-bottom:10px">
        <label>Buscar US pelo código ou nome</label>
        <input class="input" id="relSearch" placeholder="Ex.: US-002 ou login" autocomplete="off">
      </div>
      <div class="search-list" id="relList"></div>
      <div id="relSemWrap" style="display:none">
        <div class="field" style="margin-top:16px">
          <label>Semântica da relação</label>
          <label class="radio-opt" data-type="DEPENDE_DE"><input type="radio" name="sem" value="DEPENDE_DE">
            <span class="rt"><b>DEPENDE_DE</b><span>Necessidade funcional — o destino é pré-requisito desta US.</span></span></label>
          <label class="radio-opt" data-type="BLOQUEIA"><input type="radio" name="sem" value="BLOQUEIA">
            <span class="rt"><b>BLOQUEIA</b><span>Impedimento de avanço — esta US bloqueia o destino.</span></span></label>
          <label class="radio-opt" data-type="RELACIONADO_A"><input type="radio" name="sem" value="RELACIONADO_A">
            <span class="rt"><b>RELACIONADO_A</b><span>Sem direção — as duas US se referem ao mesmo contexto.</span></span></label>
        </div>
        <div class="sem-preview" id="relPreview"></div>
      </div>
      <div class="field-error" id="relError" style="display:none;margin-top:8px"></div>
    </div>
    <div class="modal-foot">
      <span class="left field-hint">Leituras inversas não geram arestas duplicadas.</span>
      <button class="btn btn-ghost" data-close>Cancelar</button>
      <button class="btn btn-primary" id="relCreate" disabled>Conectar</button>
    </div>
  </div>`;

  const { el, close } = openModal(html);
  const search = el.querySelector("#relSearch");
  const list = el.querySelector("#relList");
  const semWrap = el.querySelector("#relSemWrap");
  const preview = el.querySelector("#relPreview");
  const errEl = el.querySelector("#relError");
  const createBtn = el.querySelector("#relCreate");
  let target = null, semType = null;

  function renderList() {
    const q = search.value.trim().toLowerCase();
    const cands = ATH.requirements.filter(r =>
      r.id !== fromId && (!q || r.id.toLowerCase().includes(q) || r.title.toLowerCase().includes(q)));
    if (!cands.length) { list.innerHTML = `<div class="sl-none">Nenhuma US encontrada.</div>`; return; }
    list.innerHTML = cands.map(r => `
      <button class="sl-item${target === r.id ? " selected" : ""}" data-us="${r.id}">
        <span class="rid">${r.id}</span><span class="st">${esc(r.title)}</span>
        <span class="badge st-${r.status}" style="font-size:10px"><span class="bdot"></span>${STATUS_LABEL[r.status]}</span>
      </button>`).join("");
  }
  renderList();

  search.oninput = () => renderList();
  list.onclick = e => {
    const item = e.target.closest(".sl-item");
    if (!item) return;
    target = item.dataset.us;
    renderList();
    semWrap.style.display = "block";
    updatePreview();
  };

  semWrap.addEventListener("change", () => {
    semType = semWrap.querySelector("input:checked")?.value || null;
    semWrap.querySelectorAll(".radio-opt").forEach(o => o.classList.toggle("checked", o.querySelector("input").checked));
    updatePreview();
  });

  function updatePreview() {
    createBtn.disabled = !(target && semType);
    errEl.style.display = "none";
    if (!target || !semType) { preview.innerHTML = ""; return; }
    const sentence = {
      DEPENDE_DE: `Esta US (<b>${fromId}</b>) <b>depende de</b> ${target} — ${target} é pré-requisito dela.`,
      BLOQUEIA: `Esta US (<b>${fromId}</b>) <b>bloqueia</b> ${target}.`,
      RELACIONADO_A: `Esta US (<b>${fromId}</b>) <b>está relacionada a</b> ${target}.`,
    }[semType];
    preview.innerHTML = sentence;
  }

  createBtn.onclick = () => {
    errEl.style.display = "none";
    // Validações US16/5.4: autorrelação, duplicata equivalente (qualquer ordem)
    if (target === fromId) return showError("Autorrelações são proibidas.");
    const dup = ATH.relations.some(r =>
      (r.from === fromId && r.to === target) || (r.from === target && r.to === fromId));
    if (dup) return showError("Já existe uma relação entre essas duas US (leituras inversas representam a mesma aresta).");
    const id = "R" + (++ATH.seq.rel);
    ATH.relations.push({ id, type: semType, from: fromId, to: target, autor: DB.session.user.name, just: "Criada no mapa." });
    toast(`Relação criada: ${fromId} ${semType} ${target}`, "success");
    close();
    renderMapNodes(); drawEdges(); renderMapDrawer();
  };
  function showError(msg) { errEl.textContent = msg; errEl.style.display = "block"; }
}

function openReqInEditor(reqId) {
  ATH.ui.prevView = "mapa";
  ATH.ui.currentReq = reqId;
  ATH.ui.view = "editor";
  render();
}
