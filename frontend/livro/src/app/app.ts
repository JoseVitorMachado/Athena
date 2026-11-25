import { DatePipe, Location } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EpicoLivro, LivroApi, ProjetoLivro, Referencia, RequisitoLivro } from './livro-api';
import { htmlPublicado, normalizarBusca, textoPublicado } from './conteudo-livro';
import { HttpClient } from '@angular/common/http';
import { Sessao, SessaoService } from './sessao';

const SEM_EPICO = 'sem-epico';

@Component({
  imports: [DatePipe, FormsModule],
  selector: 'app-root',
  host: { '[class.dark-theme]': 'modoEscuro()', '(document:keydown)': 'atalhoBusca($event)' },
  templateUrl: './app.html',
})
export class App {
  private readonly api = inject(LivroApi);
  private readonly location = inject(Location);
  private leitura = 0;
  private catalogo = 0;
  private readonly http = inject(HttpClient);
  private readonly sessaoService = inject(SessaoService);

  protected readonly modoEscuro = signal(localStorage.getItem('athena.tema') === 'escuro');
  protected readonly autenticado = signal(!!this.sessaoService.ler());
  protected readonly email = signal('');
  protected readonly senha = signal('');
  protected readonly erroLogin = signal('');
  protected readonly entrando = signal(false);
  protected readonly lembrarDispositivo = signal(false);
  protected readonly recuperacao = signal(false);
  protected readonly codigoRecuperacao = signal('');
  protected readonly novaSenha = signal('');
  protected readonly mensagemRecuperacao = signal('');

  protected readonly requisitos = signal<RequisitoLivro[]>([]);
  protected readonly projetos = signal<ProjetoLivro[]>([]);
  protected readonly projetoId = signal('');
  protected readonly projetoAtual = computed(() => this.projetos().find(projeto => projeto.projetoId === this.projetoId()));
  protected readonly epicos = signal<EpicoLivro[]>([]);
  protected readonly carregandoProjetos = signal(false);
  protected readonly carregando = signal(false);
  protected readonly carregandoLeitura = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly erroLeitura = signal('');
  protected readonly avisoEpicos = signal('');
  protected readonly pagina = signal<'inicio' | 'busca' | 'catalogo' | 'requisito'>('inicio');
  protected readonly consulta = signal('');
  protected readonly termoAplicado = signal('');
  protected readonly epico = signal('');
  protected readonly campoBusca = signal<'todos' | 'titulo' | 'conteudo'>('todos');
  protected readonly ordenacao = signal<'relevancia' | 'titulo' | 'recente'>('relevancia');
  protected readonly selecionado = signal<RequisitoLivro | null>(null);
  protected readonly requisitoId = signal('');
  protected readonly feedbackLink = signal('');
  protected readonly conteudoSelecionado = computed(() => htmlPublicado(this.selecionado()?.conteudo ?? ''));
  protected readonly grupos = computed(() => {
    const grupos = new Map<string, { id: string; titulo: string; quantidade: number }>();
    for (const requisito of this.requisitos()) {
      const id = requisito.epicoId ?? SEM_EPICO;
      const existente = grupos.get(id);
      if (existente) existente.quantidade++;
      else grupos.set(id, { id, titulo: this.epicoFor(requisito), quantidade: 1 });
    }
    return Array.from(grupos.values()).sort((a, b) => a.titulo.localeCompare(b.titulo, 'pt-BR'));
  });
  protected readonly recentes = computed(() => this.requisitos().filter(item => item.publicadoEm)
    .sort((a, b) => this.dataPublicacao(b) - this.dataPublicacao(a)).slice(0, 3));
  protected readonly vizinhos = computed(() => {
    const itens = this.requisitos();
    const indice = itens.findIndex(item => item.requisitoId === this.requisitoId());
    return { anterior: indice > 0 ? itens[indice - 1] : undefined, proximo: indice >= 0 ? itens[indice + 1] : undefined };
  });
  private readonly indiceBusca = computed(() => this.requisitos().map(item => {
    const resumo = textoPublicado(htmlPublicado(item.conteudo));
    return {
      item, resumo,
      titulo: normalizarBusca(`${item.codigo} ${item.titulo}`),
      corpo: normalizarBusca(`${resumo} ${item.criteriosAceite.join(' ')}`),
    };
  }));
  protected readonly resultados = computed(() => {
    const termo = normalizarBusca(this.termoAplicado());
    const campo = this.campoBusca();
    const itens = this.indiceBusca().filter(({ item, titulo, corpo }) =>
      (!this.epico() || (item.epicoId ?? SEM_EPICO) === this.epico()) &&
      (!termo || (campo !== 'conteudo' && titulo.includes(termo)) || (campo !== 'titulo' && corpo.includes(termo))),
    );
    if (this.ordenacao() === 'titulo') return itens.sort((a, b) => a.item.titulo.localeCompare(b.item.titulo, 'pt-BR'));
    if (this.ordenacao() === 'recente') return itens.sort((a, b) => this.dataPublicacao(b.item) - this.dataPublicacao(a.item));
    return termo && campo === 'todos'
      ? itens.sort((a, b) => Number(b.titulo.includes(termo)) - Number(a.titulo.includes(termo))) : itens;
  });

  constructor() {
    effect(() => localStorage.setItem('athena.tema', this.modoEscuro() ? 'escuro' : 'claro'));
    window.addEventListener('athena:session-expired', () => this.limparSessao());
    this.location.subscribe(() => {
      if (this.autenticado()) this.restaurarRota();
    });
    if (this.autenticado()) this.carregarProjetos();
  }

  protected entrar(): void {
    this.erroLogin.set('');
    this.entrando.set(true);
    this.http.post<Sessao>('/api/v1/autenticacao/login', {
      email: this.email().trim(), senha: this.senha(), lembrarDispositivo: this.lembrarDispositivo(),
    }, { withCredentials: true }).subscribe({
      next: sessao => {
        this.sessaoService.salvar(sessao);
        this.autenticado.set(true);
        this.entrando.set(false);
        this.senha.set('');
        this.carregarProjetos();
      },
      error: (falha: HttpErrorResponse) => {
        const problema = falha.error as { detail?: string } | null;
        this.erroLogin.set(problema?.detail ?? 'Não foi possível entrar. Confira o e-mail e a senha.');
        this.entrando.set(false);
      },
    });
  }

  protected sair(): void {
    this.sessaoService.encerrar().subscribe(() => this.limparSessao());
  }

  protected solicitarRecuperacao(): void {
    this.erroLogin.set('');
    this.http.post<{ mensagem: string }>('/api/v1/autenticacao/recuperacao', { email: this.email().trim() }).subscribe({
      next: resposta => { this.mensagemRecuperacao.set(resposta.mensagem); this.recuperacao.set(true); },
      error: (falha: HttpErrorResponse) => this.erroLogin.set((falha.error as { detail?: string } | null)?.detail ?? 'Não foi possível solicitar a recuperação.'),
    });
  }

  protected concluirRecuperacao(): void {
    this.erroLogin.set('');
    this.http.post<void>('/api/v1/autenticacao/recuperacao/conclusao', {
      email: this.email().trim(), codigo: this.codigoRecuperacao(), novaSenha: this.novaSenha(),
    }).subscribe({
      next: () => { this.recuperacao.set(false); this.codigoRecuperacao.set(''); this.novaSenha.set(''); this.senha.set(''); this.mensagemRecuperacao.set('Senha redefinida. Entre com a nova senha.'); },
      error: (falha: HttpErrorResponse) => this.erroLogin.set((falha.error as { detail?: string } | null)?.detail ?? 'Não foi possível redefinir a senha.'),
    });
  }

  private limparSessao(): void {
    this.catalogo++;
    this.leitura++;
    this.autenticado.set(false);
    this.carregandoProjetos.set(false);
    this.projetos.set([]);
    this.projetoId.set('');
    this.requisitos.set([]);
    this.epicos.set([]);
    this.selecionado.set(null);
    this.erro.set(null);
  }

  protected carregarProjetos(): void {
    this.carregandoProjetos.set(true);
    this.erro.set(null);
    this.api.projetos().subscribe({
      next: projetos => {
        if (!this.autenticado()) return;
        this.projetos.set(projetos.sort((a, b) =>
          a.workspaceNome.localeCompare(b.workspaceNome, 'pt-BR') || a.nome.localeCompare(b.nome, 'pt-BR')));
        this.carregandoProjetos.set(false);
        this.restaurarRota();
      },
      error: falha => {
        if (!this.autenticado()) return;
        this.erro.set(this.mensagemErro(falha, 'Não foi possível carregar os projetos.'));
        this.carregandoProjetos.set(false);
      },
    });
  }

  private restaurarRota(): void {
    const [caminho, query] = this.location.path().split('?');
    const partes = caminho.split('/').filter(Boolean);
    const [prefixo, projetoId, secao, requisitoId] = partes;
    if (!projetoId) {
      const primeiro = this.projetos()[0];
      if (primeiro) this.selecionarProjeto(primeiro.projetoId, true);
      return;
    }
    const rotaValida = prefixo === 'projetos' && (
      partes.length === 2 || ((secao === 'busca' || secao === 'requisitos') && partes.length === 3) ||
      (secao === 'requisitos' && partes.length === 4 && /^[a-f0-9-]{36}$/i.test(requisitoId))
    );
    if (!rotaValida || !this.projetos().some(projeto => projeto.projetoId === projetoId)) {
      this.catalogo++;
      this.leitura++;
      this.projetoId.set('');
      this.requisitos.set([]);
      this.epicos.set([]);
      this.selecionado.set(null);
      this.erro.set('Este Livro não está disponível para sua conta. Selecione um projeto para continuar.');
      return;
    }
    if (this.projetoId() !== projetoId) this.ativarProjeto(projetoId);
    else if (this.pagina() === 'requisito' && !requisitoId) this.carregarCatalogo();
    if (secao === 'requisitos' && requisitoId) {
      this.carregarLeitura(requisitoId);
    } else if (secao === 'busca') {
      this.leitura++;
      const filtros = new URLSearchParams(query);
      const campo = filtros.get('em');
      const ordem = filtros.get('ordem');
      const termo = filtros.get('q') ?? '';
      this.consulta.set(termo);
      this.termoAplicado.set(termo);
      this.epico.set(filtros.get('epico') ?? '');
      this.campoBusca.set(campo === 'titulo' || campo === 'conteudo' ? campo : 'todos');
      this.ordenacao.set(ordem === 'titulo' || ordem === 'recente' ? ordem : 'relevancia');
      this.pagina.set('busca');
    } else {
      this.leitura++;
      this.pagina.set(secao === 'requisitos' ? 'catalogo' : 'inicio');
    }
    window.scrollTo(0, 0);
  }

  protected selecionarProjeto(projetoId: string, substituir = false): void {
    if (!this.projetos().some(projeto => projeto.projetoId === projetoId)) return;
    this.navegar(`/projetos/${projetoId}`, substituir);
  }

  private ativarProjeto(projetoId: string): void {
    this.leitura++;
    this.projetoId.set(projetoId);
    this.requisitos.set([]);
    this.epicos.set([]);
    this.avisoEpicos.set('');
    this.consulta.set('');
    this.termoAplicado.set('');
    this.epico.set('');
    this.carregarCatalogo();
  }

  protected carregarCatalogo(): void {
    const projeto = this.projetoId();
    if (!projeto) return;
    const numero = ++this.catalogo;
    this.carregando.set(true);
    this.erro.set(null);
    this.avisoEpicos.set('');
    this.api.listar(projeto).subscribe({
      next: requisitos => {
        if (numero !== this.catalogo) return;
        this.requisitos.set(requisitos);
        this.carregando.set(false);
      },
      error: falha => {
        if (numero !== this.catalogo) return;
        this.requisitos.set([]);
        this.erro.set(this.mensagemErro(falha, 'Não foi possível carregar as publicações.'));
        this.carregando.set(false);
      },
    });
    this.api.epicos(projeto).subscribe({
      next: epicos => { if (numero === this.catalogo) this.epicos.set(epicos); },
      error: () => { if (numero === this.catalogo) this.avisoEpicos.set('Os nomes dos épicos estão indisponíveis.'); },
    });
  }

  protected inicio(): void {
    if (this.projetoId()) this.navegar(`/projetos/${this.projetoId()}`);
  }

  protected abrirCatalogo(): void {
    if (this.projetoId()) this.navegar(`/projetos/${this.projetoId()}/requisitos`);
  }

  protected buscar(): void {
    this.termoAplicado.set(this.consulta().trim());
    this.atualizarBusca();
  }

  protected buscarPorEpico(epico: string): void {
    this.consulta.set('');
    this.termoAplicado.set('');
    this.epico.set(epico);
    this.campoBusca.set('todos');
    this.ordenacao.set('relevancia');
    this.atualizarBusca();
  }

  protected atualizarBusca(): void {
    if (!this.projetoId()) return;
    const params = new URLSearchParams();
    if (this.termoAplicado()) params.set('q', this.termoAplicado());
    if (this.epico()) params.set('epico', this.epico());
    if (this.campoBusca() !== 'todos') params.set('em', this.campoBusca());
    if (this.ordenacao() !== 'relevancia') params.set('ordem', this.ordenacao());
    this.navegar(`/projetos/${this.projetoId()}/busca${params.size ? '?' + params.toString() : ''}`);
  }

  protected abrir(requisitoId: string): void {
    this.navegar(`/projetos/${this.projetoId()}/requisitos/${requisitoId}`);
  }

  private navegar(url: string, substituir = false): void {
    const [caminho, query] = url.split('?');
    if (url !== this.location.path()) {
      if (substituir) this.location.replaceState(caminho, query);
      else this.location.go(caminho, query);
    }
    this.restaurarRota();
  }

  protected carregarLeitura(requisitoId = this.requisitoId()): void {
    const numero = ++this.leitura;
    this.requisitoId.set(requisitoId);
    this.pagina.set('requisito');
    this.carregandoLeitura.set(true);
    this.selecionado.set(null);
    this.erroLeitura.set('');
    this.feedbackLink.set('');
    this.api.obter(this.projetoId(), requisitoId).subscribe({
      next: requisito => {
        if (numero !== this.leitura) return;
        this.selecionado.set(requisito);
        this.carregandoLeitura.set(false);
      },
      error: falha => {
        if (numero !== this.leitura) return;
        this.carregandoLeitura.set(false);
        if (falha.status === 404) this.requisitos.update(itens => itens.filter(item => item.requisitoId !== requisitoId));
        this.erroLeitura.set(falha.status === 404
          ? 'Este requisito não está disponível no Livro corrente.'
          : this.mensagemErro(falha, 'Não foi possível carregar a publicação.'));
      },
    });
  }

  protected rotulo(ref: Referencia): string {
    if (!ref.disponivel || ref.cancelado) return 'Referência indisponível';
    if (ref.tipo === 'DEPENDE_DE') return ref.direcao === 'ENTRADA' ? 'É pré-requisito de' : 'Depende de';
    if (ref.tipo === 'BLOQUEIA') return ref.direcao === 'ENTRADA' ? 'É bloqueado por' : 'Bloqueia';
    return 'Relacionado a';
  }

  protected async copiarLink(): Promise<void> {
    try {
      await navigator.clipboard.writeText(window.location.href);
      this.feedbackLink.set('Link copiado');
    } catch {
      this.feedbackLink.set('Copie o endereço na barra do navegador.');
    }
  }

  protected epicoFor(item: RequisitoLivro): string {
    if (!item.epicoId) return 'Sem épico';
    return this.epicos().find(epico => epico.epicoId === item.epicoId)?.titulo ?? 'Épico indisponível';
  }

  protected irParaSecao(evento: MouseEvent, id: string): void {
    evento.preventDefault();
    document.getElementById(id)?.scrollIntoView();
  }

  protected atalhoBusca(evento: KeyboardEvent): void {
    if (!this.projetoId() || !(evento.ctrlKey || evento.metaKey) || evento.key.toLowerCase() !== 'k') return;
    const busca = document.querySelector<HTMLInputElement>('.global-search input');
    if (busca) { evento.preventDefault(); busca.focus(); }
  }

  private dataPublicacao(requisito: RequisitoLivro): number {
    return Date.parse(requisito.publicadoEm ?? '') || 0;
  }

  private mensagemErro(falha: HttpErrorResponse, fallback: string): string {
    return (falha.error as { detail?: string } | null)?.detail ?? fallback;
  }
}
