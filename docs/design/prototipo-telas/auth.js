// ============================================================
// ATHENA — Autenticação (US01–US03)
// Criar conta, Login, Recuperar acesso e Criar nova senha.
// ============================================================

// ---------- helpers ----------
function pwChecks(pw) {
  pw = pw || "";
  return [
    { label: "8 caracteres",    ok: pw.length >= 8 },
    { label: "Letras",          ok: /[a-zA-Z]/.test(pw) },
    { label: "Números",         ok: /\d/.test(pw) },
    { label: "Caractere especial", ok: /[^A-Za-z0-9]/.test(pw) },
  ];
}
function pwCheckHTML(pw) {
  return pwChecks(pw).map(c =>
    `<div class="pw-item${c.ok ? " ok" : ""}">${icon(c.ok ? "check" : "circle", 13)}<span>${c.label}</span></div>`).join("");
}
function initialsOf(name) {
  return name.trim().split(/\s+/).map(p => p[0]).slice(0, 2).join("").toUpperCase();
}
function validEmail(v) { return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v || ""); }
function goToHome() {
  DB.ui.view = "home";
  DB.ui.homeWs = DB.workspaces.some(w => w.id === DB.ui.homeWs) ? DB.ui.homeWs : DB.workspaces[0].id;
  render();
}

// ---------- router de telas de auth ----------
function renderAuth() {
  const v = DB.ui.authView;
  const app = document.getElementById("app");
  app.innerHTML = `
  <div class="auth-wrap">
    <div class="auth-card">
      <div class="auth-brand">
        <span class="brand-mark">${icon("shield", 34)}</span>
        <div class="brand-name">ATH<em>E</em>NA</div>
        <div class="auth-tag">Gestão e rastreabilidade de requisitos</div>
      </div>
      <div id="authBody">${authViewHTML(v)}</div>
    </div>
    <div class="auth-footnote">Protótipo acadêmico — UFLA · Sistemas Distribuídos · ATHENA</div>
  </div>`;
  bindAuth(v);
}

function authViewHTML(v) {
  if (v === "signup") return authSignupHTML();
  if (v === "recover") return authRecoverHTML();
  if (v === "reset") return authResetHTML();
  return authLoginHTML();
}

// ---------- US02: Login ----------
function authLoginHTML() {
  return `
  <h2 class="auth-title">Entrar</h2>
  <p class="auth-sub">Acesse seus workspaces e projetos.</p>
  <form id="loginForm" novalidate>
    <div class="field"><label>E-mail</label>
      <div class="input-icon">${icon("mail", 14)}<input class="input" id="liEmail" type="email" placeholder="voce@empresa.com" autocomplete="off"></div>
    </div>
    <div class="field"><label>Senha</label>
      <div class="input-icon pw-wrap">
        ${icon("lock", 14)}
        <input class="input" id="liPass" type="password" placeholder="••••••••">
        <button type="button" class="eye" data-eye="liPass">${icon("eye", 15)}</button>
      </div>
    </div>
    <div class="auth-row">
      <label class="check-line"><input type="checkbox" id="liRemember"><span>Lembrar este dispositivo</span></label>
      <a class="auth-link" data-auth="recover">Esqueci minha senha</a>
    </div>
    <div class="field-error auth-error" id="liError" style="display:none"></div>
    <button class="btn btn-primary btn-block" id="liSubmit" type="submit">Entrar</button>
  </form>
  <div class="auth-alt">Não tem conta? <a data-auth="signup">Criar conta</a></div>
  <div class="demo-hint">${icon("info", 13)} Protótipo — conta demo: <b>anagomes@athena.dev</b> · senha <b>Athena@2026</b></div>`;
}

function bindLogin(el) {
  el.querySelectorAll("[data-auth]").forEach(a => a.addEventListener("click", e => { e.preventDefault(); DB.ui.authView = a.dataset.auth; renderAuth(); }));
  bindEyes(el);
  el.querySelector("#loginForm").addEventListener("submit", e => {
    e.preventDefault();
    const email = el.querySelector("#liEmail").value.trim();
    const pass = el.querySelector("#liPass").value;
    const err = el.querySelector("#liError");
    err.style.display = "none";
    if (!validEmail(email) || !pass) { showAuthError(err, "Informe e-mail e senha."); return; }
    const btn = el.querySelector("#liSubmit");
    btn.disabled = true; btn.textContent = "Entrando…";
    setTimeout(() => {
      const u = DB.users.find(u => u.email.toLowerCase() === email.toLowerCase());
      if (!u || u.password !== pass) {
        btn.disabled = false; btn.textContent = "Entrar";
        showAuthError(err, "Credenciais inválidas.");
        return;
      }
      DB.session.user = u;
      if (el.querySelector("#liRemember").checked) toast("Dispositivo lembrado — sessão persistente (protótipo).", "info");
      toast(`Bem-vinda, ${u.name.split(" ")[0]}!`, "success");
      applyPendingHashOrHome();
    }, 650);
  });
}

function applyPendingHashOrHome() {
  const h = DB.pendingHash;
  DB.pendingHash = null;
  if (h && h.startsWith("editor/")) {
    const id = h.split("/")[1];
    loadProject(DB.workspaces[0].projects[0].id, DB.workspaces[0].id);
    if (reqById(id)) { DB.ui.view = "editor"; DB.ui.currentReq = id; DB.ui.prevView = "visao"; render(); return; }
  } else if (["mapa", "requisitos"].includes(h)) {
    loadProject(DB.workspaces[0].projects[0].id, DB.workspaces[0].id);
    DB.ui.view = h; render(); return;
  }
  goToHome();
}

// ---------- US01: Criar conta ----------
function authSignupHTML() {
  return `
  <h2 class="auth-title">Criar conta</h2>
  <p class="auth-sub">Registe-se para começar a utilizar o ATHENA.</p>
  <form id="signupForm" novalidate>
    <div class="field"><label>Nome completo</label>
      <div class="input-icon">${icon("user", 14)}<input class="input" id="suName" type="text" placeholder="Digite seu nome"></div>
    </div>
    <div class="field"><label>E-mail</label>
      <div class="input-icon">${icon("mail", 14)}<input class="input" id="suEmail" type="email" placeholder="voce@empresa.com" autocomplete="off"></div>
    </div>
    <div class="field"><label>Senha</label>
      <div class="input-icon pw-wrap">${icon("lock", 14)}
        <input class="input" id="suPass" type="password" placeholder="Crie uma senha segura">
        <button type="button" class="eye" data-eye="suPass">${icon("eye", 15)}</button>
      </div>
      <div class="pw-check" id="suCheck">${pwCheckHTML("")}</div>
    </div>
    <div class="field"><label>Confirmar senha</label>
      <div class="input-icon pw-wrap">${icon("lock", 14)}
        <input class="input" id="suPass2" type="password" placeholder="Repita a senha">
        <button type="button" class="eye" data-eye="suPass2">${icon("eye", 15)}</button>
      </div>
    </div>
    <label class="check-line terms"><input type="checkbox" id="suTerms">
      <span>Li e aceito os <a href="#" id="lnkTermos">Termos de Serviço</a> e a <a href="#" id="lnkPriv">Política de privacidade</a>.</span></label>
    <div class="field-error auth-error" id="suError" style="display:none"></div>
    <button class="btn btn-primary btn-block" id="suSubmit" type="submit">Criar conta</button>
  </form>
  <div class="auth-alt">Já tem conta? <a data-auth="login">Entrar</a></div>`;
}

function bindSignup(el) {
  bindEyes(el);
  el.querySelectorAll("[data-auth]").forEach(a => a.addEventListener("click", e => { e.preventDefault(); DB.ui.authView = a.dataset.auth; renderAuth(); }));
  el.querySelector("#suPass").addEventListener("input", e => {
    el.querySelector("#suCheck").innerHTML = pwCheckHTML(e.target.value);
  });
  el.querySelector("#lnkTermos").onclick = e => { e.preventDefault(); toast("Documento legal (protótipo).", "info"); };
  el.querySelector("#lnkPriv").onclick = e => { e.preventDefault(); toast("Documento legal (protótipo).", "info"); };
  el.querySelector("#signupForm").addEventListener("submit", e => {
    e.preventDefault();
    const name = el.querySelector("#suName").value.trim();
    const email = el.querySelector("#suEmail").value.trim();
    const p1 = el.querySelector("#suPass").value, p2 = el.querySelector("#suPass2").value;
    const err = el.querySelector("#suError");
    err.style.display = "none";
    if (!name) return showAuthError(err, "Informe seu nome completo.");
    if (!validEmail(email)) return showAuthError(err, "Informe um e-mail válido.");
    if (DB.users.some(u => u.email.toLowerCase() === email.toLowerCase()))
      return showAuthError(err, "Este e-mail já está registrado na base de dados.");
    if (pwChecks(p1).some(c => !c.ok)) return showAuthError(err, "A senha não cumpre todos os requisitos.");
    if (p1 !== p2) return showAuthError(err, "As senhas não coincidem.");
    if (!el.querySelector("#suTerms").checked) return showAuthError(err, "É preciso aceitar os Termos e a Política de privacidade.");
    const u = { name, email, password: p1 };
    DB.users.push(u);
    DB.session.user = u;
    toast("Conta criada. Sessão iniciada automaticamente.", "success");
    goToHome();
  });
}

// ---------- US03-A: Recuperar acesso ----------
function authRecoverHTML() {
  return `
  <h2 class="auth-title">Recuperar acesso</h2>
  <p class="auth-sub">Informe o e-mail associado à conta para acionar o envio de um código.</p>
  <form id="recForm" novalidate>
    <div class="field"><label>E-mail</label>
      <div class="input-icon">${icon("mail", 14)}<input class="input" id="recEmail" type="email" placeholder="voce@empresa.com" autocomplete="off"></div>
      <div class="field-error auth-error" id="recError" style="display:none"></div>
    </div>
    <div class="auth-success" id="recSuccess" style="display:none">
      ${icon("check", 14)} Se houver uma conta com esse e-mail, enviaremos um código para redefinir sua senha.
    </div>
    <button class="btn btn-primary btn-block" id="recSubmit" type="submit">Enviar código</button>
  </form>
  <div class="auth-alt"><a data-auth="login">← Voltar para entrar</a></div>`;
}

function bindRecover(el) {
  el.querySelectorAll("[data-auth]").forEach(a => a.addEventListener("click", e => { e.preventDefault(); DB.ui.authView = a.dataset.auth; renderAuth(); }));
  el.querySelector("#recForm").addEventListener("submit", e => {
    e.preventDefault();
    const email = el.querySelector("#recEmail").value.trim();
    const err = el.querySelector("#recError");
    err.style.display = "none";
    if (!validEmail(email)) return showAuthError(err, "Informe um e-mail válido.");
    const btn = el.querySelector("#recSubmit");
    btn.disabled = true; btn.textContent = "Enviando…";
    setTimeout(() => {
      DB.devCode = String(Math.floor(100000 + Math.random() * 900000));
      DB.devEmail = email;
      el.querySelector("#recSuccess").style.display = "block";
      btn.textContent = "Código enviado ✓";
      toast(`Protótipo: seu código é ${DB.devCode} (em produção chegaria por e-mail).`, "info");
      setTimeout(() => { DB.ui.authView = "reset"; renderAuth(); }, 1400);
    }, 700);
  });
}

// ---------- US03-B: Criar nova senha ----------
function authResetHTML() {
  return `
  <h2 class="auth-title">Criar nova senha</h2>
  <p class="auth-sub">Valide o código de 6 dígitos recebido e defina a nova senha.</p>
  <div class="code-inputs" id="codeInputs">
    ${[0, 1, 2, 3, 4, 5].map(i => `<input class="code-box" data-i="${i}" maxlength="1" inputmode="numeric" type="text">`).join("")}
  </div>
  <form id="resetForm" novalidate>
    <div class="field"><label>Nova senha</label>
      <div class="input-icon pw-wrap">${icon("lock", 14)}
        <input class="input" id="rsPass" type="password" placeholder="Digite sua nova senha">
        <button type="button" class="eye" data-eye="rsPass">${icon("eye", 15)}</button>
      </div>
      <div class="pw-check" id="rsCheck">${pwCheckHTML("")}</div>
    </div>
    <div class="field"><label>Confirmar senha</label>
      <div class="input-icon pw-wrap">${icon("lock", 14)}
        <input class="input" id="rsPass2" type="password" placeholder="Confirme sua senha">
        <button type="button" class="eye" data-eye="rsPass2">${icon("eye", 15)}</button>
      </div>
    </div>
    <div class="field-error auth-error" id="rsError" style="display:none"></div>
    <button class="btn btn-primary btn-block" id="rsSubmit" type="submit" disabled>Redefinir senha</button>
  </form>
  <div class="auth-links-row">
    <a class="auth-link" id="rsOther">Usar outro e-mail</a>
    <a class="auth-link" id="rsResend">Reenviar código</a>
    <a class="auth-link" id="rsBack">Voltar para entrar</a>
  </div>`;
}

function bindReset(el) {
  bindEyes(el);
  const boxes = [...el.querySelectorAll(".code-box")];
  const form = el.querySelector("#resetForm");
  const err = el.querySelector("#rsError");
  err.style.display = "none";

  function code() { return boxes.map(b => b.value).join(""); }
  function updateBtn() {
    const p1 = el.querySelector("#rsPass").value, p2 = el.querySelector("#rsPass2").value;
    el.querySelector("#rsSubmit").disabled = !(code().length === 6 && p1 && p2);
  }
  boxes.forEach((b, i) => {
    b.addEventListener("input", () => {
      b.value = b.value.replace(/\D/g, "").slice(0, 1);
      if (b.value && i < 5) boxes[i + 1].focus();
      err.style.display = "none";
      updateBtn();
    });
    b.addEventListener("keydown", e => {
      if (e.key === "Backspace" && !b.value && i > 0) { boxes[i - 1].focus(); }
    });
  });
  boxes[0].addEventListener("paste", e => {
    e.preventDefault();
    const digits = (e.clipboardData.getData("text") || "").replace(/\D/g, "").slice(0, 6).split("");
    digits.forEach((d, k) => { if (boxes[k]) boxes[k].value = d; });
    boxes[Math.min(digits.length, 5)].focus();
    updateBtn();
  });
  el.querySelector("#rsPass").addEventListener("input", e => {
    el.querySelector("#rsCheck").innerHTML = pwCheckHTML(e.target.value);
    updateBtn();
  });
  el.querySelector("#rsPass2").addEventListener("input", updateBtn);

  el.querySelector("#rsOther").onclick = e => { e.preventDefault(); DB.ui.authView = "recover"; renderAuth(); };
  el.querySelector("#rsBack").onclick = e => { e.preventDefault(); DB.ui.authView = "login"; renderAuth(); };
  el.querySelector("#rsResend").onclick = e => {
    e.preventDefault();
    DB.devCode = String(Math.floor(100000 + Math.random() * 900000));
    toast(`Protótipo: novo código é ${DB.devCode}.`, "info");
  };

  form.addEventListener("submit", e => {
    e.preventDefault();
    const p1 = el.querySelector("#rsPass").value, p2 = el.querySelector("#rsPass2").value;
    err.style.display = "none";
    if (code() !== DB.devCode) return showAuthError(err, "Código inválido ou expirado. Use “Reenviar código”.");
    if (pwChecks(p1).some(c => !c.ok)) return showAuthError(err, "A nova senha deve cumprir a política de complexidade.");
    if (p1 !== p2) return showAuthError(err, "As senhas não coincidem.");
    const u = DB.users.find(u => u.email.toLowerCase() === (DB.devEmail || "").toLowerCase());
    if (u) u.password = p1;
    toast("Senha redefinida com sucesso. Faça login com a nova senha.", "success");
    DB.ui.authView = "login"; renderAuth();
  });
}

// ---------- comum ----------
function bindAuth(v) {
  const body = document.getElementById("authBody");
  if (v === "signup") bindSignup(body);
  else if (v === "recover") bindRecover(body);
  else if (v === "reset") bindReset(body);
  else bindLogin(body);
  const f = body.querySelector("input:not([type=checkbox])");
  if (f) f.focus();
}
function bindEyes(scope) {
  scope.querySelectorAll("[data-eye]").forEach(b => b.addEventListener("click", () => {
    const inp = scope.querySelector("#" + b.dataset.eye);
    const showing = inp.type === "text";
    inp.type = showing ? "password" : "text";
    b.innerHTML = icon(showing ? "eyeOff" : "eye", 15);
  }));
}
function showAuthError(el, msg) {
  el.innerHTML = `${icon("info", 13)} ${esc(msg)}`;
  el.style.display = "block";
}
