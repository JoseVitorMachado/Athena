import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import {
  CABECALHO_INSTANCIA,
  GestaoApi,
  NovaRelacao,
  Projeto,
  Relacao,
  Requisito,
  RequisitoGestao,
} from './gestao-api';

/** Dados locais usados pelo protótipo visual de workspaces. */
const WORKSPACE_UFLA = '66666666-6666-4666-8666-666666666666';
const WORKSPACE_INFINITEPAY = '77777777-7777-4777-8777-777777777777';
const DOCUMENTO_DEMO = '44444444-4444-4444-8444-444444444444';
const EPICOS = [
  'Autenticação e Conta',
  'Organização de projetos',
  'Editor de Requisitos',
  'Rastreabilidade',
  'Revisão e Publicação',
  'Integrações',
];

@Component({
  imports: [DatePipe, FormsModule],
  selector: 'app-root',
  templateUrl: './app.html',
})
export class App {
  private readonly api = inject(GestaoApi);
  protected readonly membrosDemo = [
    { iniciais: 'AC', nome: 'Ana Clara · Você', email: 'ana@athena.dev', papel: 'Owner' },
    { iniciais: 'FS', nome: 'Fernando Scarabeli', email: 'fernando@athena.dev', papel: 'Aprovador' },
    { iniciais: 'JV', nome: 'José Vítor Machado', email: 'jose@athena.dev', papel: 'Editor' },
    { iniciais: 'T', nome: 'Thomas', email: 'thomas@athena.dev', papel: 'Repórter' },
  ];

  protected readonly tela = signal<'login' | 'workspaces' | 'projeto'>('login');
  protected readonly email = signal('');
  protected readonly senha = signal('');
  protected readonly lembrarDispositivo = signal(false);
  protected readonly senhaVisivel = signal(false);
  protected readonly erroLogin = signal('');
  protected readonly entrando = signal(false);
  protected readonly avisoLogin = signal('');
  protected readonly buscaWorkspace = signal('');
  protected readonly workspacesDemo = signal([
    { id: WORKSPACE_UFLA, nome: 'UFLA — Sistemas Distribuídos' },
    { id: WORKSPACE_INFINITEPAY, nome: 'InfinitePay — Engenharia' },
  ]);
  protected readonly workspaceId = signal(WORKSPACE_INFINITEPAY);
  protected readonly workspaceNome = signal('InfinitePay — Engenharia');
  protected readonly abaWorkspace = signal<'ativos' | 'arquivados'>('ativos');
  protected readonly buscaProjeto = signal('');
  protected readonly projetos = signal<Projeto[]>([
    { projetoId: '88888888-8888-4888-8888-888888888888', workspaceId: WORKSPACE_INFINITEPAY, nome: 'App Mobile', descricao: 'Checkout one-click no app', arquivado: false, ownerId: '55555555-5555-4555-8555-555555555555', atualizadoEm: new Date().toISOString() },
    { projetoId: '22222222-2222-4222-8222-222222222222', workspaceId: WORKSPACE_UFLA, nome: 'Plataforma ATHENA', descricao: 'Gestão, rastreabilidade e revisão de requisitos', arquivado: false, ownerId: '55555555-5555-4555-8555-555555555555', atualizadoEm: new Date().toISOString() },
    { projetoId: '99999999-9999-4999-8999-999999999999', workspaceId: WORKSPACE_UFLA, nome: 'Portal do Parceiro', descricao: 'Área logada para lojistas consultarem vendas', arquivado: true, ownerId: '55555555-5555-4555-8555-555555555555', atualizadoEm: new Date().toISOString() },
  ]);
  protected readonly estatisticasProjetos = signal<Record<string, { requisitos: number; epicos: number }>>({
    '88888888-8888-4888-8888-888888888888': { requisitos: 1, epicos: 1 },
    '22222222-2222-4222-8222-222222222222': { requisitos: 14, epicos: 6 },
    '99999999-9999-4999-8999-999999999999': { requisitos: 3, epicos: 2 },
  });
  protected readonly projetosArquivadosLocal = signal<string[]>([]);
  protected readonly menuProjetoId = signal<string | null>(null);
  protected readonly avisoLocal = signal('');
  protected readonly nomeNovoProjeto = signal('');
  protected readonly descricaoNovoProjeto = signal('');
  protected readonly modalNovoProjeto = signal(false);
  protected readonly modalTrocarProjeto = signal(false);
  protected readonly workspaceMenuAberto = signal(false);
  protected readonly notificacoesAbertas = signal(false);
  protected readonly notificacoesLidas = signal(false);
  protected readonly workspaceNomeDraft = signal('');
  protected readonly modalWorkspace = signal<'adicionar' | 'renomear' | 'excluir' | null>(null);
  protected readonly zoomMapa = signal(0.62);
  protected readonly mapaCentralizado = signal(false);
  protected readonly mapaRaizId = signal<string | null>(null);
  protected readonly contaMenuAberto = signal(false);
  protected readonly perfilAberto = signal(false);
  protected readonly modoEscuro = signal(true);
  protected readonly configuracoesAbertas = signal(false);
  protected readonly abaConfiguracoes = signal<'geral' | 'pastas' | 'membros'>('geral');

  protected readonly projetoId = signal('22222222-2222-4222-8222-222222222222');
  protected readonly projetoNome = signal('ATHENA');
  protected readonly requisitos = signal<Requisito[]>([]);
  protected readonly relacoes = signal<Relacao[]>([]);
  protected readonly erroRelacoes = signal<string | null>(null);
  protected readonly conectarAberto = signal(false);
  protected readonly requisitoDestino = signal('');
  protected readonly tipoNovaRelacao = signal<NovaRelacao['tipo']>('DEPENDE_DE');
  protected readonly justificativaRelacao = signal('');
  protected readonly salvandoRelacao = signal(false);
  protected readonly erroCriacaoRelacao = signal('');
  protected readonly total = signal(0);
  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly instancia = signal<string | null>(null);
  protected readonly detalhe = signal<RequisitoGestao | null>(null);
  protected readonly requisitoDetalhe = signal<Requisito | null>(null);
  protected readonly titulo = signal('');
  protected readonly epicoSelecionado = signal('');
  protected readonly conteudo = signal('');
  protected readonly criterios = signal('');
  protected readonly salvando = signal(false);
  protected readonly pagina = signal<'visao' | 'requisitos' | 'mapa'>('visao');
  protected readonly busca = signal('');
  protected readonly novoAberto = signal(false);
  protected readonly templateCompleto = signal(false);
  protected readonly filtroSituacao = signal<'ATIVO' | 'CANCELADO' | 'TODOS'>('ATIVO');
  protected readonly filtroEstado = signal('Todos');
  protected readonly filtroPublicacao = signal('Todas');
  protected readonly filtroEpico = signal('Todos');
  protected readonly epicos = signal(EPICOS);
  protected readonly epicosAbertos = signal<string[]>([EPICOS[0], EPICOS[3]]);
  protected readonly nomeEpico = signal('');
  protected readonly modalNovoEpico = signal(false);
  protected readonly filtrados = computed(() => {
    const termo = this.busca().trim().toLocaleLowerCase('pt-BR');
    return this.requisitos().filter((item) => {
      const texto = `${item.codigo} ${item.titulo} ${item.tipo ?? ''}`.toLocaleLowerCase('pt-BR');
      const situacaoOk = this.filtroSituacao() === 'TODOS' || item.situacao === this.filtroSituacao();
      const estado = item.candidata?.estadoVersao ?? 'SEM_CANDIDATA';
      const estadoOk = this.filtroEstado() === 'Todos' || estado === this.filtroEstado();
      const pub = item.versaoPublicadaId ? 'PUBLICADA' : 'NAO_PUBLICADA';
      const pubOk = this.filtroPublicacao() === 'Todas' || pub === this.filtroPublicacao();
      const epicoOk = this.filtroEpico() === 'Todos' || this.epicoDo(item) === this.filtroEpico();
      return texto.includes(termo) && situacaoOk && estadoOk && pubOk && epicoOk;
    });
  });
  protected readonly emRevisao = computed(
    () => this.requisitos().filter((item) => item.candidata?.estadoVersao?.includes('REVISAO')).length,
  );
  protected readonly projetosVisiveis = computed(() => {
    const termo = this.buscaProjeto().trim().toLocaleLowerCase('pt-BR');
    return this.projetos().filter((projeto) => {
      const pertenceAoWorkspace = projeto.workspaceId === this.workspaceId();
      const arquivado = projeto.arquivado || this.projetosArquivadosLocal().includes(projeto.projetoId);
      const estadoCorreto = this.abaWorkspace() === 'arquivados' ? arquivado : !arquivado;
      return pertenceAoWorkspace && estadoCorreto && projeto.nome.toLocaleLowerCase('pt-BR').includes(termo);
    });
  });

  protected readonly totalEpicos = computed(() => this.epicos().length);
  protected readonly requisitosPorEpico = computed(() => {
    const nomes = [...this.epicos(), ...new Set(this.requisitos()
      .filter((item) => !!item.epicoId)
      .map((item) => this.epicoDo(item))
      .filter((nome) => !this.epicos().includes(nome))), 'Sem épico'];
    return nomes.map((nome) => ({ nome, itens: this.requisitos().filter((item) => this.epicoDo(item) === nome) }));
  });
  protected readonly epicosComId = computed(() => [...new Set(this.requisitos().map((item) => item.epicoId).filter((id): id is string => !!id))]);
  protected readonly relacoesVisiveis = computed(() => this.relacoes().filter((relacao) =>
    this.requisitos().some((item) => item.requisitoId === relacao.origemId)
    && this.requisitos().some((item) => item.requisitoId === relacao.destinoId)));

  protected get workspaceUFLA(): string {
    return WORKSPACE_UFLA;
  }

  protected get workspaceInfinitePay(): string {
    return WORKSPACE_INFINITEPAY;
  }

  protected get workspaceEhUFLA(): boolean {
    return this.workspaceId() === WORKSPACE_UFLA;
  }

  protected workspaceMatch(nome: string): boolean {
    return nome.toLocaleLowerCase('pt-BR').includes(this.buscaWorkspace().trim().toLocaleLowerCase('pt-BR'));
  }

  protected totalProjetosWorkspace(workspaceId: string, arquivados = false): number {
    return this.projetos().filter((projeto) => {
      const arquivado = projeto.arquivado || this.projetosArquivadosLocal().includes(projeto.projetoId);
      return projeto.workspaceId === workspaceId && (arquivados ? arquivado : !arquivado);
    }).length;
  }

  protected entrar(): void {
    if (this.entrando()) return;
    this.erroLogin.set('');
    this.avisoLogin.set('');
    this.entrando.set(true);
    this.api.autenticar({
      email: this.email().trim(),
      senha: this.senha(),
      lembrarDispositivo: this.lembrarDispositivo(),
    }).subscribe({
      next: (sessao) => {
        this.api.definirSessao(sessao);
        this.email.set(this.email().trim());
        this.senha.set('');
        this.entrando.set(false);
        this.abrirWorkspace(WORKSPACE_UFLA, 'UFLA — Sistemas Distribuídos');
      },
      error: (falha: HttpErrorResponse) => {
        this.api.definirSessao(null);
        this.entrando.set(false);
        this.erroLogin.set(falha.status === 401
          ? 'E-mail ou senha inválidos. Confira os dados e tente novamente.'
          : this.descrever(falha));
      },
    });
  }

  protected alternarWorkspace(id: string, nome: string): void {
    this.workspaceId.set(id);
    this.workspaceNome.set(nome);
    this.abaWorkspace.set('ativos');
    this.buscaProjeto.set('');
  }

  protected renomearWorkspace(): void {
    const nome = this.workspaceNomeDraft().trim();
    if (nome) {
      this.workspaceNome.set(nome);
      this.workspacesDemo.update((workspaces) => workspaces.map((workspace) => workspace.id === this.workspaceId() ? { ...workspace, nome } : workspace));
    }
    this.modalWorkspace.set(null);
    this.workspaceMenuAberto.set(false);
  }

  protected abrirConfiguracoesWorkspace(): void {
    this.workspaceMenuAberto.set(false);
    this.workspaceNomeDraft.set(this.workspaceNome());
    this.modalWorkspace.set('renomear');
  }

  protected iniciarWorkspace(): void {
    this.modalWorkspace.set('adicionar');
    this.workspaceNomeDraft.set('');
  }

  protected excluirWorkspaceDemo(): void {
    const removido = this.workspaceId();
    this.workspacesDemo.update((workspaces) => workspaces.filter((workspace) => workspace.id !== removido));
    this.modalWorkspace.set(null);
    this.workspaceMenuAberto.set(false);
    const proximo = this.workspacesDemo()[0] ?? { id: WORKSPACE_UFLA, nome: 'UFLA — Sistemas Distribuídos' };
    if (!this.workspacesDemo().length) this.workspacesDemo.set([proximo]);
    this.abrirWorkspace(proximo.id, proximo.nome);
  }

  protected salvarWorkspaceDemo(): void {
    const nome = this.workspaceNomeDraft().trim();
    if (!nome) return;
    const id = crypto.randomUUID();
    this.workspacesDemo.update((workspaces) => [...workspaces, { id, nome }]);
    this.workspaceId.set(id);
    this.workspaceNome.set(nome);
    this.tela.set('workspaces');
    this.modalWorkspace.set(null);
  }

  protected recuperarSenha(): void {
    this.avisoLogin.set('A recuperação de senha estará disponível em uma próxima etapa do protótipo.');
  }

  protected criarConta(): void {
    this.avisoLogin.set('O cadastro estará disponível em uma próxima etapa do protótipo.');
  }

  protected sair(): void {
    this.api.definirSessao(null);
    this.tela.set('login');
    this.senha.set('');
    this.contaMenuAberto.set(false);
    this.detalhe.set(null);
    this.requisitoDetalhe.set(null);
    this.requisitos.set([]);
    this.relacoes.set([]);
    this.total.set(0);
    this.erro.set(null);
  }

  protected abrirWorkspace(id: string, nome: string): void {
    this.workspaceId.set(id);
    this.workspaceNome.set(nome);
    this.abaWorkspace.set('ativos');
    this.buscaProjeto.set('');
    this.tela.set('workspaces');
  }

  protected criarProjeto(): void {
    if (!this.nomeNovoProjeto().trim()) return;
    this.salvando.set(true);
    const projeto: Projeto = {
      projetoId: crypto.randomUUID(),
      workspaceId: this.workspaceId(),
      nome: this.nomeNovoProjeto().trim(),
      descricao: this.descricaoNovoProjeto().trim(),
      arquivado: false,
      ownerId: '55555555-5555-4555-8555-555555555555',
      atualizadoEm: new Date().toISOString(),
    };
    this.projetos.update((projetos) => [projeto, ...projetos]);
    this.estatisticasProjetos.update((stats) => ({ ...stats, [projeto.projetoId]: { requisitos: 0, epicos: 0 } }));
    this.nomeNovoProjeto.set('');
    this.descricaoNovoProjeto.set('');
    this.modalNovoProjeto.set(false);
    this.salvando.set(false);
  }

  protected abrirProjeto(projeto: Projeto): void {
    this.projetoId.set(projeto.projetoId);
    this.projetoNome.set(projeto.nome);
    this.tela.set('projeto');
    this.pagina.set('visao');
    this.carregar();
  }

  protected alterarArquivo(projeto: Projeto): void {
    const arquivado = projeto.arquivado || this.projetosArquivadosLocal().includes(projeto.projetoId);
    this.projetosArquivadosLocal.update((ids) => arquivado
      ? ids.filter((id) => id !== projeto.projetoId)
      : [...ids, projeto.projetoId]);
    this.menuProjetoId.set(null);
    this.avisoLocal.set(arquivado
      ? 'Projeto restaurado na demonstração.'
      : 'Projeto arquivado na demonstração.');
  }

  protected projetoArquivado(projeto: Projeto): boolean {
    return projeto.arquivado || this.projetosArquivadosLocal().includes(projeto.projetoId);
  }

  protected trocarProjeto(projeto: Projeto): void {
    this.abrirProjeto(projeto);
    this.modalTrocarProjeto.set(false);
  }

  protected carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    const situacoes = this.filtroSituacao() === 'TODOS' ? ['ATIVO', 'CANCELADO'] : [this.filtroSituacao()];
    forkJoin(situacoes.map((situacao) => this.api.listarRequisitos(this.projetoId(), situacao))).subscribe({
      next: (resposta) => {
        this.requisitos.set(resposta.flatMap((pagina) => pagina.body?.items ?? []));
        this.total.set(resposta.reduce((soma, pagina) => soma + (pagina.body?.totalElements ?? 0), 0));
        this.instancia.set(resposta[0]?.headers.get(CABECALHO_INSTANCIA) ?? null);
        this.carregando.set(false);
      },
      error: (falha: HttpErrorResponse) => {
        this.erro.set(this.descrever(falha));
        this.carregando.set(false);
      },
    });
  }

  protected filtrarSituacao(situacao: 'ATIVO' | 'CANCELADO' | 'TODOS'): void {
    this.filtroSituacao.set(situacao);
    this.carregar();
  }

  protected abrirMapa(): void {
    this.pagina.set('mapa');
    this.centralizarMapa();
    this.detalhe.set(null);
    this.requisitoDetalhe.set(null);
    this.erroRelacoes.set(null);
    this.api.listarRelacoes(this.projetoId()).subscribe({
      next: (resposta) => this.relacoes.set(resposta.body?.items ?? []),
      error: (falha: HttpErrorResponse) => this.erroRelacoes.set(this.descrever(falha)),
    });
  }

  protected codigoDo(requisitoId: string): string {
    return this.requisitos().find((requisito) => requisito.requisitoId === requisitoId)?.codigo ?? requisitoId.slice(0, 8);
  }

  protected abrir(requisito: Requisito): void {
    this.mapaRaizId.set(requisito.requisitoId);
    this.requisitoDetalhe.set(requisito);
    this.api.listarRelacoes(this.projetoId()).subscribe({
      next: (resposta) => this.relacoes.set(resposta.body?.items ?? []),
      error: (falha: HttpErrorResponse) => this.erroRelacoes.set(this.descrever(falha)),
    });
    this.api.obterRequisito(this.projetoId(), requisito.requisitoId).subscribe({
      next: (visao) => this.detalhe.set(visao),
      error: (falha: HttpErrorResponse) => this.erro.set(this.descrever(falha)),
    });
  }

  protected fecharDetalhes(): void {
    this.detalhe.set(null);
    this.requisitoDetalhe.set(null);
  }

  protected zoom(delta: number): void {
    this.zoomMapa.update((valor) => Math.min(1.4, Math.max(0.4, Math.round((valor + delta) * 100) / 100)));
  }

  protected centralizarMapa(requisitoId: string | null = null): void {
    this.mapaRaizId.set(requisitoId);
    this.mapaCentralizado.set(true);
    this.zoomMapa.set(0.62);
  }

  protected sugestaoEpico(requisito: Requisito): string {
    return this.epicoSugerido(requisito);
  }

  protected epicoSugerido(requisito: Requisito): string {
    return this.epicoDo(requisito);
  }

  protected origemVisual(relacao: Relacao): string {
    return relacao.tipo === 'DEPENDE_DE' ? relacao.destinoId : relacao.origemId;
  }

  protected destinoVisual(relacao: Relacao): string {
    return relacao.tipo === 'DEPENDE_DE' ? relacao.origemId : relacao.destinoId;
  }

  protected rotuloRelacao(relacao: Relacao): string {
    if (relacao.tipo === 'DEPENDE_DE') return 'PRÉ-REQUISITO PARA';
    if (relacao.tipo === 'BLOQUEIA') return 'BLOQUEIA';
    return 'RELACIONADO';
  }

  protected relacaoDestacada(relacao: Relacao): boolean {
    const raiz = this.mapaRaizId();
    return !!raiz && (relacao.origemId === raiz || relacao.destinoId === raiz);
  }

  protected deslocamentoMapa(): string {
    const raiz = this.mapaRaizId();
    if (!raiz) return 'translate(0px, 0px)';
    const escala = this.zoomMapa();
    return `translate(${Math.round((725 - this.xDo(raiz)) * escala)}px, ${Math.round((675 - this.yDo(raiz)) * escala)}px)`;
  }

  protected centralizarRequisitoNoMapa(requisitoId: string): void {
    this.detalhe.set(null);
    this.requisitoDetalhe.set(null);
    this.pagina.set('mapa');
    this.centralizarMapa(requisitoId);
  }

  protected xDo(requisitoId: string): number {
    const requisito = this.requisitos().find((item) => item.requisitoId === requisitoId);
    const epico = requisito ? this.epicoSugerido(requisito) : 'Sem épico';
    const grupo = Math.max(0, this.requisitosPorEpico().findIndex((item) => item.nome === epico));
    const coluna = grupo % 3;
    const itens = this.requisitosPorEpico().find((item) => item.nome === epico)?.itens ?? [];
    const index = Math.max(0, itens.findIndex((item) => item.requisitoId === requisitoId));
    return coluna * 480 + 16 + (index % 2) * 220 + 102;
  }

  protected yDo(requisitoId: string): number {
    const requisito = this.requisitos().find((item) => item.requisitoId === requisitoId);
    const epico = requisito ? this.epicoSugerido(requisito) : 'Sem épico';
    const grupo = Math.max(0, this.requisitosPorEpico().findIndex((item) => item.nome === epico));
    const row = Math.floor(grupo / 3);
    const itens = this.requisitosPorEpico().find((item) => item.nome === epico)?.itens ?? [];
    const index = Math.max(0, itens.findIndex((item) => item.requisitoId === requisitoId));
    return row * 450 + 48 + 16 + Math.trunc(index / 2) * 112 + 48;
  }

  protected cardTop(index: number): number { return 64 + Math.trunc(index / 2) * 112; }
  protected grupoLeft(index: number): number { return (index % 3) * 480; }
  protected grupoTop(index: number): number { return Math.floor(index / 3) * 450; }
  protected zoomPercent(): number { return Math.round(this.zoomMapa() * 100); }
  protected relacaoCount(requisitoId: string): number {
    return this.relacoes().filter((relacao) => relacao.origemId === requisitoId || relacao.destinoId === requisitoId).length;
  }
  protected relacoesDo(requisitoId: string): Relacao[] {
    return this.relacoes().filter((relacao) => relacao.origemId === requisitoId || relacao.destinoId === requisitoId);
  }
  protected outrosRequisitos(requisitoId: string): Requisito[] {
    return this.requisitos().filter((item) => item.requisitoId !== requisitoId);
  }
  protected criarRelacao(visao: RequisitoGestao): void {
    if (!this.requisitoDestino()) return;
    this.salvandoRelacao.set(true);
    this.erroCriacaoRelacao.set('');
    const relacao: NovaRelacao = {
      origemId: visao.requisitoId,
      destinoId: this.requisitoDestino(),
      tipo: this.tipoNovaRelacao(),
      justificativa: this.justificativaRelacao().trim(),
    };
    this.api.criarRelacao(this.projetoId(), relacao).subscribe({
      next: () => {
        this.api.listarRelacoes(this.projetoId()).subscribe({
          next: (resposta) => {
            this.relacoes.set(resposta.body?.items ?? []);
            this.conectarAberto.set(false);
            this.requisitoDestino.set('');
            this.justificativaRelacao.set('');
            this.salvandoRelacao.set(false);
          },
          error: (falha: HttpErrorResponse) => {
            this.erroCriacaoRelacao.set(this.descrever(falha));
            this.salvandoRelacao.set(false);
          },
        });
      },
      error: (falha: HttpErrorResponse) => {
        this.erroCriacaoRelacao.set(this.descrever(falha));
        this.salvandoRelacao.set(false);
      },
    });
  }
  protected abrirRelacao(relacao: Relacao, currentId: string): void {
    const outroId = relacao.origemId === currentId ? relacao.destinoId : relacao.origemId;
    const requisito = this.requisitos().find((item) => item.requisitoId === outroId);
    if (requisito) this.abrir(requisito);
  }

  protected criteriosDetalhe(): string[] {
    const conteudo = this.requisitoDetalhe()?.candidata?.criteriosAceite;
    return Array.isArray(conteudo) ? conteudo : [];
  }

  protected criar(): void {
    this.salvando.set(true);
    this.erro.set(null);
    const criteriosAceite = this.criterios().split('\n').map((linha) => linha.trim()).filter(Boolean);
    const corpo = this.templateCompleto()
      ? `${this.conteudo().trim()}\n\n## Especificação\n\n## Campos e comportamento\n\n## Regras de negócio\n\n## Critérios de aceite\n`
      : this.conteudo().trim();
    this.api.criarRequisito(this.projetoId(), {
      documentoId: DOCUMENTO_DEMO,
      ...(this.epicoSelecionado() ? { epicoId: this.epicoSelecionado() } : {}),
      titulo: this.titulo().trim(),
      conteudo: corpo,
      criteriosAceite,
    }).subscribe({
      next: () => {
        this.titulo.set('');
        this.epicoSelecionado.set('');
        this.conteudo.set('');
        this.criterios.set('');
        this.novoAberto.set(false);
        this.salvando.set(false);
        this.carregar();
      },
      error: (falha: HttpErrorResponse) => {
        this.erro.set(this.descrever(falha));
        this.salvando.set(false);
      },
    });
  }

  protected adicionarEpico(): void {
    const nome = this.nomeEpico().trim();
    if (!nome || this.epicos().includes(nome)) return;
    this.epicos.update((epicos) => [...epicos, nome]);
    this.nomeEpico.set('');
    this.modalNovoEpico.set(false);
  }

  protected toggleEpico(nome: string): void {
    this.epicosAbertos.update((abertos) => abertos.includes(nome)
      ? abertos.filter((epico) => epico !== nome)
      : [...abertos, nome]);
  }

  protected requisitosDoEpico(nome: string): Requisito[] {
    return this.filtrados().filter((item) => this.epicoDo(item) === nome);
  }

  protected epicoDo(requisito: Requisito): string {
    const raw = requisito.epicoId;
    if (raw) return this.epicos().find((epico) => epico === raw) ?? `Épico ${raw.slice(0, 8)}`;
    return 'Sem épico';
  }

  protected rotuloEpico(epicoId: string): string {
    return `Épico ${epicoId.slice(0, 8)}`;
  }

  protected status(requisito: Requisito): string {
    return requisito.candidata?.estadoVersao?.replaceAll('_', ' ') ?? 'Sem candidata';
  }

  protected publicado(requisito: Requisito): string {
    return requisito.versaoPublicadaId
      ? requisito.candidata?.estadoVersao === 'PUBLICADA' ? `v${requisito.candidata.numero}` : 'Publicada'
      : '—';
  }

  protected podeCriar(): boolean {
    return this.titulo().trim().length > 0 && this.conteudo().trim().length > 0 && !this.salvando();
  }

  private descrever(falha: HttpErrorResponse): string {
    if (falha.status === 401) return 'Sessão expirada. Saia da conta e entre novamente.';
    const problema = falha.error as { detail?: string; title?: string } | null;
    const texto = problema?.detail ?? problema?.title ?? 'Não foi possível falar com o servidor.';
    return `${texto} (HTTP ${falha.status})`;
  }
}
