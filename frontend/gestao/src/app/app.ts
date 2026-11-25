import { DatePipe, JsonPipe, KeyValuePipe, Location, NgTemplateOutlet } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, forkJoin, switchMap } from 'rxjs';
import { SessaoService } from './sessao';
import { caminhoGestao, lerRotaGestao, PaginaProjeto, RotaGestao } from './gestao-rotas';
import {
  CABECALHO_INSTANCIA,
  AcaoRecurso,
  LoteRevisao,
  Versao,
  GestaoApi,
  Convite,
  Baseline,
  VersaoRequisito,
  Membro,
  Notificacao,
  NovaRelacao,
  Projeto,
  Relacao,
  Requisito,
  RequisitoGestao,
} from './gestao-api';

const MAPA = { colunas: 3, larguraGrupo: 474, distanciaGrupo: 108, margem: 16, cabecalho: 50, larguraCartao: 212, alturaCartao: 108, distanciaCartao: 16 };

const DOCUMENTO_DEMO = '44444444-4444-4444-8444-444444444444';
@Component({
  imports: [DatePipe, JsonPipe, KeyValuePipe, NgTemplateOutlet, FormsModule],
  host: {
    '[class.dark-theme]': 'modoEscuro()',
    '(document:click)': 'fecharMenuContaAoClicarFora($event)',
  },
  selector: 'app-root',
  templateUrl: './app.html',
})
export class App {
  private readonly api = inject(GestaoApi);
  private readonly sessaoService = inject(SessaoService);
  private readonly location = inject(Location);
  private urlAtual = this.location.path();
  private rotaPendente = lerRotaGestao(this.urlAtual);
  private numeroNavegacao = 0;

  constructor() {
    this.location.subscribe(() => {
      const url = this.location.path();
      if (!this.podeSairDoEditor()) {
        this.location.go(this.urlAtual);
        return;
      }
      const rota = lerRotaGestao(url);
      if (!this.emailAtual()) {
        this.rotaPendente = rota;
        if (!this.entrando()) this.registrarRota({ tela: 'login' }, true);
        return;
      }
      this.urlAtual = url;
      this.aplicarRota(rota ?? { tela: 'workspaces' });
    });
    effect(() => localStorage.setItem('athena.tema', this.modoEscuro() ? 'escuro' : 'claro'));
    window.addEventListener('athena:session-expired', () => {
      this.api.definirSessao(null);
      this.limparContextoWorkspaces();
      this.emailAtual.set('');
      this.nomeConta.set('');
      this.perfilAberto.set(false);
      this.senha.set('');
      this.tela.set('login');
      this.registrarRota({ tela: 'login' }, true);
    });
    if (this.sessaoService.ler()) {
      this.entrando.set(true);
      this.api.obterUsuarioAtual().subscribe({
        next: usuario => { this.nomeConta.set(usuario.nome); this.email.set(usuario.email); this.emailAtual.set(usuario.email); this.entrando.set(false); this.iniciarNavegacaoAutenticada(); },
        error: falha => { this.api.definirSessao(null); this.entrando.set(false); this.erroLogin.set(this.detalheErro(falha, 'Entre novamente para continuar.')); this.registrarRota({ tela: 'login' }, true); },
      });
    } else {
      this.registrarRota({ tela: 'login' }, true);
    }
  }

  private registrarRota(rota: RotaGestao, substituir = false): void {
    const caminho = caminhoGestao(rota);
    if (this.location.path() !== caminho) {
      if (substituir) this.location.replaceState(caminho);
      else this.location.go(caminho);
    }
    this.urlAtual = caminho;
    this.numeroNavegacao++;
  }

  private iniciarNavegacaoAutenticada(): void {
    const rota = this.rotaPendente;
    this.rotaPendente = null;
    this.tela.set('workspaces');
    this.carregarWorkspaces(rota?.tela === 'login' ? null : rota);
  }

  private aplicarRota(rota: RotaGestao): void {
    this.editorAlterado.set(false);
    this.acaoSelecionada.set(null);
    this.modalTrocarProjeto.set(false);
    this.fecharDetalhes();
    const numero = ++this.numeroNavegacao;
    if (rota.tela === 'login') {
      this.aplicarRota({ tela: 'workspaces' });
      return;
    }
    if (rota.tela === 'workspaces') {
      this.tela.set('workspaces');
      const workspace = rota.workspaceId
        ? this.workspacesDemo().find(item => item.id === rota.workspaceId)
        : this.workspacesDemo()[0];
      if (workspace) this.abrirWorkspace(workspace.id, workspace.nome, true);
      else {
        this.limparSelecaoWorkspace();
        this.registrarRota({ tela: 'workspaces' }, true);
        if (rota.workspaceId) this.erroLogin.set('Workspace não encontrado ou sem acesso.');
      }
      return;
    }
    if (this.tela() === 'projeto' && this.projetoId() === rota.projetoId) {
      this.exibirRotaProjeto(rota);
      return;
    }
    this.api.obterProjeto(rota.projetoId).subscribe({
      next: resposta => {
        if (numero !== this.numeroNavegacao) return;
        if (!resposta.body || resposta.body.arquivado) {
          this.falhaRota('Este projeto está indisponível.');
          return;
        }
        this.abrirProjeto(resposta.body, false);
        this.exibirRotaProjeto(rota);
      },
      error: falha => {
        if (numero === this.numeroNavegacao) this.falhaRota(this.detalheErro(falha, 'Projeto não encontrado ou sem acesso.'));
      },
    });
  }

  private falhaRota(mensagem: string): void {
    this.aplicarRota({ tela: 'workspaces' });
    this.erroLogin.set(mensagem);
  }

  private exibirRotaProjeto(rota: Extract<RotaGestao, { tela: 'projeto' }>): void {
    this.registrarRota(rota, true);
    this.pagina.set(rota.pagina);
    if (rota.pagina === 'mapa') this.abrirMapa();
    if (!rota.requisitoId) return;
    this.retornoEditor = 'requisitos';
    this.editorDetalhes.set(false);
    this.editorMenu.set(false);
    this.pagina.set('editor');
    this.abrir({
      requisitoId: rota.requisitoId, projetoId: rota.projetoId,
      titulo: '', codigo: '', situacao: '', atualizadoEm: '',
    });
  }

  private registrarPagina(pagina: PaginaProjeto, requisitoId?: string): void {
    this.registrarRota({ tela: 'projeto', projetoId: this.projetoId(), pagina, requisitoId });
  }

  protected readonly membrosProjeto = signal<Membro[]>([]);
  protected readonly etagMembros = signal<string | null>(null);
  protected readonly conviteAberto = signal(false);
  protected readonly conviteEmail = signal('');
  protected readonly convitePapel = signal('Editor');
  protected readonly convitesRecebidos = signal<Convite[]>([]);
  protected readonly transferenciaAberta = signal(false);
  protected readonly destinatarioTransferencia = signal('');
  protected readonly emailAtual = signal('');
  protected readonly nomeConta = signal('');
  protected readonly iniciaisConta = computed(() => {
    const nome = this.nomeConta().trim();
    const partes = nome ? nome.split(/\s+/) : this.emailAtual().split('@')[0].split(/[._+-]+/).filter(Boolean);
    const iniciais = partes.length > 1 ? partes.slice(0, 2).map(parte => parte[0]).join('') : (partes[0] ?? '').slice(0, 2);
    return iniciais.toLocaleUpperCase('pt-BR');
  });
  protected get membrosDemo() {
    return this.membrosProjeto().map(membro => ({
      ...membro,
      iniciais: membro.nome.split(/\s+/).slice(0, 2).map(parte => parte[0] ?? '').join('').toLocaleUpperCase('pt-BR'),
      nome: membro.nome,
      email: membro.email,
      papel: membro.papel,
    }));
  }

  protected readonly tela = signal<'login' | 'workspaces' | 'projeto'>('login');
  protected readonly email = signal('');
  protected readonly senha = signal('');
  protected readonly lembrarDispositivo = signal(false);
  protected readonly senhaVisivel = signal(false);
  protected readonly erroLogin = signal('');
  protected readonly entrando = signal(false);
  protected readonly avisoLogin = signal('');
  protected readonly recuperacaoAberta = signal(false);
  protected readonly recuperacaoEtapa = signal<'email' | 'codigo'>('email');
  protected readonly recuperacaoEmail = signal('');
  protected readonly recuperacaoCodigo = signal('');
  protected readonly novaSenha = signal('');
  protected readonly recuperacaoErro = signal('');
  protected readonly recuperacaoMensagem = signal('');
  protected readonly recuperacaoEnviando = signal(false);
  protected readonly buscaWorkspace = signal('');
  protected readonly workspacesDemo = signal<{ id: string; nome: string }[]>([]);
  protected readonly carregandoWorkspaces = signal(false);
  protected readonly workspaceId = signal('');
  protected readonly workspaceNome = signal('');
  protected readonly abaWorkspace = signal<'ativos' | 'arquivados'>('ativos');
  protected readonly buscaProjeto = signal('');
  protected readonly projetos = signal<Projeto[]>([]);
  protected readonly estatisticasProjetos = signal<Record<string, { requisitos: number; epicos: number }>>({});
  protected readonly projetosArquivadosLocal = signal<string[]>([]);
  protected readonly menuProjetoId = signal<string | null>(null);
  protected readonly avisoLocal = signal('');
  protected readonly nomeNovoProjeto = signal('');
  protected readonly descricaoNovoProjeto = signal('');
  protected readonly modalNovoProjeto = signal(false);
  protected readonly projetoParaExcluir = signal<Projeto | null>(null);
  protected readonly confirmacaoExcluirProjeto = signal('');
  protected readonly baselinesAbertas = signal(false);
  protected readonly baselines = signal<Baseline[]>([]);
  protected readonly nomeBaseline = signal('');
  protected readonly comparacaoBaseline = signal('');
  protected readonly complementoRequisito = signal<'historico' | 'versoes' | null>(null);
  protected readonly historicoRequisito = signal<Record<string, unknown>[]>([]);
  protected readonly versoesRequisito = signal<VersaoRequisito[]>([]);
  protected readonly comparacaoVersao = signal('');
  protected readonly modalTrocarProjeto = signal(false);
  protected readonly workspaceMenuAberto = signal(false);
  protected readonly notificacoesAbertas = signal(false);
  protected readonly notificacoes = signal<Notificacao[]>([]);
  protected readonly workspaceNomeDraft = signal('');
  protected readonly modalWorkspace = signal<'adicionar' | 'renomear' | 'excluir' | null>(null);
  protected readonly workspaceErro = signal('');
  protected readonly criandoWorkspace = signal(false);
  protected readonly zoomMapa = signal(0.82);
  protected readonly arrastandoMapa = signal(false);
  protected readonly requisitoSobPonteiro = signal<string | null>(null);
  private readonly centroMapa = signal<string | null>(null);
  private readonly panMapa = signal({ x: 0, y: 0 });
  private gestoMapa?: { id: number; x: number; y: number; panX: number; panY: number };
  protected readonly mapaRaizId = signal<string | null>(null);
  protected readonly contaMenuAberto = signal(false);
  protected readonly perfilAberto = signal(false);
  protected readonly modoEscuro = signal(localStorage.getItem('athena.tema') !== 'claro');
  protected readonly cadastroAberto = signal(false);
  protected readonly cadastroNome = signal('');
  protected readonly cadastroErro = signal('');
  protected readonly cadastrando = signal(false);
  protected readonly confirmacaoSenha = signal('');
  protected readonly requisitosSenha = computed(() => {
    const senha = this.novaSenha();
    return [
      { nome: '8 caracteres', valido: senha.length >= 8 },
      { nome: 'Letras', valido: /\p{L}/u.test(senha) },
      { nome: 'Números', valido: /\p{Nd}/u.test(senha) },
      { nome: 'Caractere especial', valido: /[^\p{L}\p{Nd}\s]/u.test(senha) },
    ];
  });
  protected readonly senhaNovaValida = computed(() =>
    this.requisitosSenha().every(requisito => requisito.valido) && this.novaSenha() === this.confirmacaoSenha()
  );
  protected readonly configuracoesAbertas = signal(false);
  protected readonly abaConfiguracoes = signal<'geral' | 'pastas' | 'membros'>('geral');

  protected readonly projetoId = signal('22222222-2222-4222-8222-222222222222');
  protected readonly projetoNome = signal('ATHENA');
  protected readonly descricaoProjeto = signal('');
  protected readonly projetoEditando = signal(false);
  protected readonly nomeProjetoDraft = signal('');
  protected readonly descricaoProjetoDraft = signal('');
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
  protected readonly etagRequisito = signal<string | null>(null);
  protected readonly acaoSelecionada = signal<{ nome: string; recurso: AcaoRecurso } | null>(null);
  protected readonly corpoAcao = signal('{}');
  protected readonly erroAcao = signal('');
  protected readonly executandoAcao = signal(false);
  protected readonly requisitoDetalhe = signal<Requisito | null>(null);
  protected readonly titulo = signal('');
  protected readonly menuEpico = signal<string | null>(null);
  protected readonly epicoSelecionado = signal('');
  protected readonly conteudo = signal('');
  protected readonly criterios = signal('');
  protected readonly salvando = signal(false);
  protected readonly pagina = signal<'visao' | 'requisitos' | 'mapa' | 'editor'>('visao');
  protected readonly busca = signal('');
  protected readonly novoAberto = signal(false);
  protected readonly templateCompleto = signal(false);
  protected readonly filtroSituacao = signal<'ATIVO' | 'CANCELADO' | 'TODOS'>('ATIVO');
  protected readonly filtroEstado = signal('Todos');
  protected readonly filtroPublicacao = signal('Todas');
  protected readonly filtroEpico = signal('Todos');
  protected readonly filtroTipo = signal('');
  protected readonly filtroResponsavel = signal('');
  protected readonly epicos = signal<string[]>([]);
  protected readonly epicosRemotos = signal<{ epicoId: string; titulo: string }[]>([]);
  protected readonly epicosAbertos = signal<string[]>([]);
  protected readonly nomeEpico = signal('');
  protected readonly modalNovoEpico = signal(false);
  protected readonly filtrados = computed(() => {
    const termo = this.busca().trim().toLocaleLowerCase('pt-BR');
    return this.requisitos().filter((item) => {
      const texto = `${item.codigo} ${item.titulo} ${item.tipo ?? ''}`.toLocaleLowerCase('pt-BR');
      const tipoOk = !this.filtroTipo() || item.tipo === this.filtroTipo();
      const responsavelOk = !this.filtroResponsavel() || item.responsavelId === this.filtroResponsavel();
      const situacaoOk = this.filtroSituacao() === 'TODOS' || item.situacao === this.filtroSituacao();
      const estado = item.candidata?.estadoVersao ?? 'SEM_CANDIDATA';
      const estadoOk = this.filtroEstado() === 'Todos' || estado === this.filtroEstado();
      const pub = item.versaoPublicadaId ? 'PUBLICADA' : 'NAO_PUBLICADA';
      const pubOk = this.filtroPublicacao() === 'Todas' || pub === this.filtroPublicacao();
      const epicoOk = this.filtroEpico() === 'Todos' || (this.filtroEpico() === 'Sem épico' ? !item.epicoId : item.epicoId === this.filtroEpico());
      return texto.includes(termo) && tipoOk && responsavelOk && situacaoOk && estadoOk && pubOk && epicoOk;
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
  protected readonly relacoesVisiveis = computed(() => this.relacoes().filter((relacao) =>
    this.requisitos().some((item) => item.requisitoId === relacao.origemId)
    && this.requisitos().some((item) => item.requisitoId === relacao.destinoId)));

  protected readonly layoutMapa = computed(() => {
    const { colunas, larguraGrupo, distanciaGrupo, margem, cabecalho, larguraCartao, alturaCartao, distanciaCartao } = MAPA;
    const grupos = this.requisitosPorEpico().filter(grupo => grupo.itens.length || grupo.nome !== 'Sem épico');
    const posicoes = new Map<string, { x: number; y: number }>();
    let topo = 0;
    let alturaLinha = 0;
    const caixas = grupos.map((grupo, indice) => {
      if (indice && indice % colunas === 0) {
        topo += alturaLinha + distanciaGrupo;
        alturaLinha = 0;
      }
      const x = indice % colunas * (larguraGrupo + distanciaGrupo);
      const linhas = Math.ceil(grupo.itens.length / 2);
      const altura = cabecalho + 2 * margem + Math.max(alturaCartao, linhas * (alturaCartao + distanciaCartao) - distanciaCartao);
      alturaLinha = Math.max(alturaLinha, altura);
      const cartoes = grupo.itens.map((requisito, i) => {
        const esquerda = margem + (i % 2) * (larguraCartao + distanciaCartao);
        const acima = cabecalho + margem + Math.floor(i / 2) * (alturaCartao + distanciaCartao);
        posicoes.set(requisito.requisitoId, { x: x + esquerda + larguraCartao / 2, y: topo + acima + alturaCartao / 2 });
        return { requisito, esquerda, acima };
      });
      return { nome: grupo.nome, x, y: topo, altura, cartoes };
    });
    return {
      caixas, posicoes, larguraGrupo, larguraCartao, alturaCartao,
      largura: Math.max(1, Math.min(grupos.length, colunas)) * (larguraGrupo + distanciaGrupo) - distanciaGrupo,
      altura: Math.max(1, topo + alturaLinha),
    };
  });

  protected readonly conexoesMapa = computed(() => {
    const { posicoes, larguraCartao, alturaCartao } = this.layoutMapa();
    return this.relacoesVisiveis().flatMap(relacao => {
      const inicio = posicoes.get(this.origemVisual(relacao));
      const fim = posicoes.get(this.destinoVisual(relacao));
      if (!inicio || !fim || inicio === fim) return [];
      const vertical = inicio.x === fim.x || (inicio.y !== fim.y && Math.abs(fim.y - inicio.y) > Math.abs(fim.x - inicio.x));
      const sinal = Math.sign(vertical ? fim.y - inicio.y : fim.x - inicio.x);
      const deslocamentoX = vertical ? 0 : sinal * (larguraCartao / 2 + 4);
      const deslocamentoY = vertical ? sinal * (alturaCartao / 2 + 4) : 0;
      const x1 = inicio.x + deslocamentoX;
      const y1 = inicio.y + deslocamentoY;
      const x2 = fim.x - deslocamentoX;
      const y2 = fim.y - deslocamentoY;
      const x = (x1 + x2) / 2;
      const y = (y1 + y2) / 2;
      const caminho = vertical
        ? `M ${x1} ${y1} C ${x1} ${y}, ${x2} ${y}, ${x2} ${y2}`
        : `M ${x1} ${y1} C ${x} ${y1}, ${x} ${y2}, ${x2} ${y2}`;
      return [{ relacao, caminho, x, y, rotulo: this.rotuloRelacao(relacao) }];
    });
  });

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
    this.erroLogin.set('');
    this.avisoLogin.set('');
    this.entrando.set(true);
    this.api.autenticar({ email: this.email().trim(), senha: this.senha(), lembrarDispositivo: this.lembrarDispositivo() }).subscribe({
      next: (sessao) => {
        this.api.definirSessao(sessao);
        const email = this.email().trim();
        this.emailAtual.set(email);
        this.api.obterUsuarioAtual().subscribe({
          next: usuario => { if (this.emailAtual() === email) this.nomeConta.set(usuario.nome); },
          error: () => { /* O e-mail já identifica a conta no menu. */ },
        });
        this.entrando.set(false);
        this.iniciarNavegacaoAutenticada();
      },
      error: (falha: HttpErrorResponse) => {
        const problema = falha.error as { detail?: string; codigo?: string } | null;
        this.erroLogin.set(problema?.detail ?? 'Não foi possível entrar. Confira o e-mail e a senha.');
        this.entrando.set(false);
      },
    });
  }

  private limparSelecaoWorkspace(): void {
    this.workspaceId.set('');
    this.workspaceNome.set('');
    this.projetos.set([]);
    this.abaWorkspace.set('ativos');
    this.buscaProjeto.set('');
    this.workspaceMenuAberto.set(false);
    this.modalNovoProjeto.set(false);
  }

  private limparContextoWorkspaces(): void {
    this.limparSelecaoWorkspace();
    this.workspacesDemo.set([]);
    this.convitesRecebidos.set([]);
    this.buscaWorkspace.set('');
    this.modalWorkspace.set(null);
    this.avisoLocal.set('');
    this.carregandoWorkspaces.set(false);
  }

  private carregarWorkspaces(rotaInicial?: RotaGestao | null): void {
    const conta = this.emailAtual();
    this.carregandoWorkspaces.set(true);
    this.carregarConvites();
    this.api.listarWorkspaces().subscribe({
      next: pagina => {
        if (this.emailAtual() !== conta) return;
        this.carregandoWorkspaces.set(false);
        const workspaces = pagina.items.map(item => ({ id: item.workspaceId, nome: item.nome }));
        this.workspacesDemo.set(workspaces);
        if (rotaInicial !== undefined) {
          this.aplicarRota(rotaInicial ?? { tela: 'workspaces' });
          return;
        }
        if (!workspaces.length) {
          this.limparSelecaoWorkspace();
          this.registrarRota({ tela: 'workspaces' }, true);
          return;
        }
        const atual = workspaces.find(item => item.id === this.workspaceId()) ?? workspaces[0];
        this.abrirWorkspace(atual.id, atual.nome, true);
      },
      error: falha => {
        if (this.emailAtual() !== conta) return;
        this.carregandoWorkspaces.set(false);
        this.erroLogin.set(this.detalheErro(falha, 'Não foi possível carregar os workspaces.'));
      },
    });
  }

  private carregarConvites(): void {
    const conta = this.emailAtual();
    this.api.listarConvitesRecebidos().subscribe({
      next: pagina => { if (this.emailAtual() === conta) this.convitesRecebidos.set(pagina.items); },
      error: falha => {
        if (this.emailAtual() === conta) this.erroLogin.set(this.detalheErro(falha, 'Não foi possível carregar os convites.'));
      },
    });
  }

  protected aceitarConvite(convite: Convite): void {
    this.api.aceitarConvite(convite.conviteId).subscribe({
      next: () => { this.carregarConvites(); this.carregarWorkspaces(); this.avisoLocal.set(`Convite para ${convite.projetoNome ?? 'o projeto'} aceito.`); },
      error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível aceitar o convite.')),
    });
  }

  protected membroDaConta(membro: Membro): boolean {
    return membro.email.toLowerCase() === this.emailAtual().toLowerCase();
  }

  protected podeTransferirPropriedade(): boolean {
    return this.membrosProjeto().some(membro => this.membroDaConta(membro) && membro.papel === 'Owner');
  }

  protected readonly papeisParaConceder = computed(() => {
    const papel = this.membrosProjeto().find(membro => this.membroDaConta(membro))?.papel;
    const papeis = ['Leitor', 'Repórter', 'Editor', 'Aprovador'];
    if (papel === 'Owner') return [...papeis, 'Mantenedor'];
    return papel === 'Mantenedor' ? papeis : [];
  });

  protected readonly erroConvite = signal('');
  protected readonly enviandoConvite = signal(false);

  protected abrirConvite(): void {
    this.erroConvite.set('');
    this.convitePapel.set('Editor');
    this.conviteAberto.set(true);
  }

  protected transferirPropriedade(): void {
    const destinatarioId = this.destinatarioTransferencia();
    if (!destinatarioId) return;
    this.api.obterProjeto(this.projetoId()).subscribe({
      next: response => {
        const etag = response.headers.get('ETag');
        if (!etag) { this.erroLogin.set('A API não retornou o ETag do projeto.'); return; }
        this.api.solicitarTransferenciaPropriedade(this.projetoId(), destinatarioId, etag).subscribe({
          next: () => { this.transferenciaAberta.set(false); this.destinatarioTransferencia.set(''); this.avisoLocal.set('Transferência solicitada. O novo proprietário precisa aceitar o convite.'); },
          error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível transferir a propriedade.')),
        });
      }, error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível carregar a revisão do projeto.')),
    });
  }

  protected convidarMembro(): void {
    if (this.enviandoConvite() || !this.papeisParaConceder().includes(this.convitePapel())) return;
    this.enviandoConvite.set(true);
    this.erroConvite.set('');
    this.api.convidarMembro(this.projetoId(), this.conviteEmail().trim(), this.convitePapel()).subscribe({
      next: () => {
        this.enviandoConvite.set(false);
        this.conviteAberto.set(false);
        this.conviteEmail.set('');
        this.avisoLocal.set('Convite enviado. O acesso depende da aceitação.');
      },
      error: falha => {
        this.enviandoConvite.set(false);
        this.erroConvite.set(this.detalheErro(falha, 'Não foi possível enviar o convite.'));
      },
    });
  }

  protected abrirBaselines(): void {
    this.baselinesAbertas.set(true);
    this.comparacaoBaseline.set('');
    this.api.listarBaselines(this.projetoId()).subscribe({
      next: pagina => this.baselines.set(pagina.items),
      error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível carregar as baselines.')),
    });
  }

  protected criarBaseline(): void {
    if (!this.nomeBaseline().trim()) return;
    this.api.criarBaseline(this.projetoId(), this.nomeBaseline().trim()).subscribe({
      next: baseline => { this.baselines.update(rows => [...rows, baseline]); this.nomeBaseline.set(''); },
      error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível criar a baseline.')),
    });
  }

  protected compararBaseline(baseline: Baseline): void {
    this.api.compararBaseline(this.projetoId(), baseline.baselineId).subscribe({
      next: comparacao => this.comparacaoBaseline.set(JSON.stringify(comparacao, null, 2)),
      error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível comparar a baseline.')),
    });
  }

  protected alterarPapelMembro(membro: Membro, papel: string): void {
    const papeis = this.papeisParaConceder();
    if (this.membroDaConta(membro) || !papeis.includes(membro.papel) || !papeis.includes(papel)) return;
    if (!this.etagMembros()) {
      this.erroLogin.set('A API não retornou o ETag da lista de membros. Atualize a lista antes de alterar papéis.');
      return;
    }
    this.api.editarPapelMembro(this.projetoId(), membro.membroId, papel, this.etagMembros() ?? undefined).subscribe({
      next: atualizado => this.membrosProjeto.update(rows => rows.map(item => item.membroId === atualizado.membroId ? atualizado : item)),
      error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível atualizar o papel do membro.')),
    });
  }

  private detalheErro(falha: unknown, fallback: string): string {
    const problema = falha instanceof HttpErrorResponse ? falha.error as { detail?: string } | null : null;
    return problema?.detail ?? fallback;
  }

  protected readonly workspaceDaTroca = signal('');
  protected readonly buscaTroca = signal('');
  protected readonly projetosDaTroca = signal<Projeto[]>([]);
  protected readonly carregandoTroca = signal(false);
  protected readonly erroTroca = signal('');
  protected readonly workspacesDaTroca = computed(() => this.workspacesDemo().filter(workspace =>
    this.correspondeBuscaTroca(workspace.nome) || this.projetosDaTroca().some(projeto =>
      projeto.workspaceId === workspace.id && this.correspondeBuscaTroca(projeto.nome))
  ));
  protected readonly projetosFiltradosDaTroca = computed(() => {
    const workspace = this.workspacesDemo().find(item => item.id === this.workspaceDaTroca());
    return this.projetosDaTroca().filter(projeto =>
      this.correspondeBuscaTroca(projeto.nome) || this.correspondeBuscaTroca(workspace?.nome ?? ''));
  });

  private correspondeBuscaTroca(nome: string): boolean {
    return nome.toLocaleLowerCase('pt-BR').includes(this.buscaTroca().trim().toLocaleLowerCase('pt-BR'));
  }

  protected abrirTrocaProjeto(): void {
    if (!this.podeSairDoEditor()) return;
    this.buscaTroca.set('');
    this.modalTrocarProjeto.set(true);
    this.alternarWorkspace(this.workspaceId());
  }

  protected alternarWorkspace(id: string): void {
    this.workspaceDaTroca.set(id);
    this.projetosDaTroca.set([]);
    this.erroTroca.set('');
    this.carregandoTroca.set(true);
    this.api.listarProjetos(id, false).subscribe({
      next: resposta => {
        if (this.workspaceDaTroca() !== id) return;
        this.projetosDaTroca.set(resposta.body?.items ?? []);
        this.carregandoTroca.set(false);
      },
      error: falha => {
        if (this.workspaceDaTroca() !== id) return;
        this.erroTroca.set(this.detalheErro(falha, 'Não foi possível carregar os projetos.'));
        this.carregandoTroca.set(false);
      },
    });
  }

  protected renomearWorkspace(): void {
    const nome = this.workspaceNomeDraft().trim();
    const id = this.workspaceId();
    if (!nome || !id) return;
    this.workspaceErro.set('');
    this.api.obterWorkspace(id).subscribe({
      next: response => {
        const etag = response.headers.get('ETag');
        if (!etag) { this.workspaceErro.set('A API não retornou o ETag do workspace.'); return; }
        this.api.editarWorkspace(id, nome, etag).subscribe({
          next: () => { this.workspaceNome.set(nome); this.workspacesDemo.update(rows => rows.map(row => row.id === id ? { ...row, nome } : row)); this.modalWorkspace.set(null); this.workspaceMenuAberto.set(false); },
          error: falha => this.workspaceErro.set(this.detalheErro(falha, 'Não foi possível renomear o workspace.')),
        });
      }, error: falha => this.workspaceErro.set(this.detalheErro(falha, 'Não foi possível carregar o workspace.')),
    });
  }

  protected abrirConfiguracoesWorkspace(): void {
    this.workspaceMenuAberto.set(false);
    this.workspaceErro.set('');
    this.workspaceNomeDraft.set(this.workspaceNome());
    this.modalWorkspace.set('renomear');
  }

  protected iniciarWorkspace(): void {
    this.modalWorkspace.set('adicionar');
    this.workspaceNomeDraft.set('');
    this.workspaceErro.set('');
  }

  protected readonly confirmacaoExcluirWorkspace = signal('');

  protected abrirExclusaoWorkspace(): void {
    this.workspaceMenuAberto.set(false);
    this.workspaceErro.set('');
    this.confirmacaoExcluirWorkspace.set('');
    this.modalWorkspace.set('excluir');
  }

  protected excluirWorkspaceDemo(): void {
    const removido = this.workspaceId();
    const confirmacao = this.confirmacaoExcluirWorkspace();
    if (!removido || confirmacao !== this.workspaceNome()) return;
    this.workspaceErro.set('');
    this.api.obterWorkspace(removido).subscribe({
      next: response => {
        const etag = response.headers.get('ETag');
        if (!etag) { this.workspaceErro.set('A API não retornou o ETag do workspace.'); return; }
        this.api.excluirWorkspace(removido, confirmacao, etag).subscribe({
          next: () => {
            this.workspacesDemo.update(rows => rows.filter(row => row.id !== removido));
            this.modalWorkspace.set(null); this.workspaceMenuAberto.set(false);
            const proximo = this.workspacesDemo()[0];
            if (proximo) this.abrirWorkspace(proximo.id, proximo.nome); else { this.limparSelecaoWorkspace(); this.registrarRota({ tela: 'workspaces' }, true); }
          }, error: falha => this.workspaceErro.set(this.detalheErro(falha, 'Não foi possível excluir o workspace.')),
        });
      }, error: falha => this.workspaceErro.set(this.detalheErro(falha, 'Não foi possível carregar o workspace.')),
    });
  }

  protected salvarWorkspaceDemo(): void {
    const nome = this.workspaceNomeDraft().trim();
    if (!nome || this.criandoWorkspace()) return;
    this.workspaceErro.set('');
    this.criandoWorkspace.set(true);
    this.api.criarWorkspace(nome).subscribe({
      next: workspace => { this.workspacesDemo.update(rows => [...rows, { id: workspace.workspaceId, nome: workspace.nome }]); this.criandoWorkspace.set(false); this.modalWorkspace.set(null); this.abrirWorkspace(workspace.workspaceId, workspace.nome); },
      error: falha => { this.workspaceErro.set(this.detalheErro(falha, 'Não foi possível criar o workspace.')); this.criandoWorkspace.set(false); },
    });
  }

  protected recuperarSenha(): void {
    this.recuperacaoEmail.set(this.email());
    this.limparNovaSenha();
    this.recuperacaoCodigo.set('');
    this.recuperacaoEtapa.set('email');
    this.recuperacaoErro.set('');
    this.recuperacaoMensagem.set('');
    this.recuperacaoAberta.set(true);
  }

  protected enviarCodigoRecuperacao(): void {
    if (this.recuperacaoEnviando() || !this.recuperacaoEmail().trim()) return;
    this.recuperacaoEnviando.set(true);
    this.recuperacaoErro.set('');
    this.api.solicitarRecuperacao(this.recuperacaoEmail().trim()).subscribe({
      next: resposta => { this.recuperacaoCodigo.set(''); this.recuperacaoMensagem.set(resposta.mensagem); this.recuperacaoEtapa.set('codigo'); this.recuperacaoEnviando.set(false); },
      error: falha => { this.recuperacaoErro.set(this.detalheErro(falha, 'Não foi possível solicitar a recuperação.')); this.recuperacaoEnviando.set(false); },
    });
  }

  protected concluirRecuperacao(): void {
    if (this.recuperacaoEnviando() || !this.senhaNovaValida() || !/^\d{6}$/.test(this.recuperacaoCodigo())) return;
    this.recuperacaoEnviando.set(true);
    this.recuperacaoErro.set('');
    this.api.concluirRecuperacao(this.recuperacaoEmail().trim(), this.recuperacaoCodigo(), this.novaSenha()).subscribe({
      next: () => { this.recuperacaoEnviando.set(false); this.recuperacaoAberta.set(false); this.email.set(this.recuperacaoEmail().trim()); this.limparNovaSenha(); this.avisoLogin.set('Senha redefinida. Entre com sua nova senha.'); this.senha.set(''); },
      error: falha => { this.recuperacaoErro.set(this.detalheErro(falha, 'Não foi possível redefinir a senha.')); this.recuperacaoEnviando.set(false); },
    });
  }

  protected criarConta(): void {
    this.cadastroErro.set('');
    this.cadastroNome.set('');
    this.limparNovaSenha();
    this.cadastroAberto.set(true);
  }

  protected cadastrar(): void {
    if (this.cadastrando() || !this.cadastroNome().trim() || !this.senhaNovaValida()) return;
    this.cadastrando.set(true);
    this.cadastroErro.set('');
    this.api.cadastrar(this.cadastroNome().trim(), this.email().trim(), this.novaSenha()).subscribe({
      next: () => {
        this.senha.set(this.novaSenha());
        this.limparNovaSenha();
        this.cadastrando.set(false);
        this.cadastroAberto.set(false);
        this.entrar();
      },
      error: falha => {
        this.cadastroErro.set(this.detalheErro(falha, 'Não foi possível criar a conta.'));
        this.cadastrando.set(false);
      },
    });
  }

  private limparNovaSenha(): void {
    this.novaSenha.set('');
    this.confirmacaoSenha.set('');
  }

  protected sair(): void {
    this.sessaoService.encerrar().subscribe(() => {
      this.api.definirSessao(null);
      this.limparContextoWorkspaces();
      this.emailAtual.set('');
      this.nomeConta.set('');
      this.perfilAberto.set(false);
      this.tela.set('login');
      this.rotaPendente = null;
      this.registrarRota({ tela: 'login' }, true);
      this.senha.set('');
      this.contaMenuAberto.set(false);
      this.detalhe.set(null);
    });
  }

  protected fecharMenuContaAoClicarFora(evento: MouseEvent): void {
    if (evento.target instanceof Element && !evento.target.closest('.epic-options')) {
      this.menuEpico.set(null);
      this.editorMenu.set(false);
    }
    if (evento.target instanceof Element && !evento.target.closest('.account-menu, [aria-label="Menu da conta"]')) {
      this.contaMenuAberto.set(false);
    }
  }

  protected alternarNotificacoes(): void {
    const abrindo = !this.notificacoesAbertas();
    this.notificacoesAbertas.set(abrindo);
    if (abrindo) this.api.listarNotificacoes().subscribe({
      next: pagina => this.notificacoes.set([...pagina.items].sort((a, b) => Date.parse(b.atualizadoEm) - Date.parse(a.atualizadoEm))),
      error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível carregar as notificações.')),
    });
  }

  protected marcarNotificacaoLida(item: Notificacao): void {
    if (item.lida) return;
    this.api.marcarNotificacaoLida(item.notificacaoId).subscribe({
      next: atualizada => this.notificacoes.update(rows => rows.map(row => row.notificacaoId === atualizada.notificacaoId ? atualizada : row)),
      error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível marcar a notificação como lida.')),
    });
  }

  protected naoLidasNotificacoes(): number {
    return this.notificacoes().filter(item => !item.lida).length;
  }

  protected abrirWorkspace(id: string, nome: string, substituir = false): void {
    this.registrarRota({ tela: 'workspaces', workspaceId: id || undefined }, substituir);
    this.workspaceId.set(id);
    this.workspaceNome.set(nome);
    this.abaWorkspace.set('ativos');
    this.buscaProjeto.set('');
    this.tela.set('workspaces');
    this.carregarProjetos();
  }

  protected carregarProjetos(arquivado = this.abaWorkspace() === 'arquivados'): void {
    const id = this.workspaceId();
    if (!id) return;
    this.api.listarProjetos(id, arquivado).subscribe({
      next: resposta => {
        if (this.workspaceId() === id && arquivado === (this.abaWorkspace() === 'arquivados')) {
          this.projetos.set(resposta.body?.items ?? []);
        }
      },
      error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível carregar os projetos.')),
    });
  }

  protected criarProjeto(): void {
    if (!this.workspaceId() || !this.nomeNovoProjeto().trim()) return;
    this.salvando.set(true);
    this.api.criarProjeto({ workspaceId: this.workspaceId(), nome: this.nomeNovoProjeto().trim(), descricao: this.descricaoNovoProjeto().trim() }).subscribe({
      next: projeto => {
        this.projetos.update(projetos => [projeto, ...projetos]);
        this.estatisticasProjetos.update(stats => ({ ...stats, [projeto.projetoId]: { requisitos: 0, epicos: 0 } }));
        this.nomeNovoProjeto.set(''); this.descricaoNovoProjeto.set(''); this.modalNovoProjeto.set(false); this.salvando.set(false);
      },
      error: falha => { this.erroLogin.set(this.detalheErro(falha, 'Não foi possível criar o projeto.')); this.salvando.set(false); },
    });
  }

  protected abrirProjeto(projeto: Projeto, registrar = true): void {
    if (registrar) this.registrarRota({ tela: 'projeto', projetoId: projeto.projetoId, pagina: 'visao' });
    this.workspaceId.set(projeto.workspaceId);
    this.workspaceNome.set(this.workspacesDemo().find(item => item.id === projeto.workspaceId)?.nome ?? '');
    this.projetoId.set(projeto.projetoId);
    this.projetoNome.set(projeto.nome);
    this.descricaoProjeto.set(projeto.descricao ?? '');
    this.fecharDetalhes();
    this.requisitos.set([]);
    this.relacoes.set([]);
    this.epicos.set([]);
    this.epicosRemotos.set([]);
    this.epicosAbertos.set([]);
    this.menuEpico.set(null);
    this.membrosProjeto.set([]);
    this.etagMembros.set(null);
    this.busca.set('');
    this.filtroSituacao.set('ATIVO');
    this.filtroEstado.set('Todos');
    this.filtroPublicacao.set('Todas');
    this.filtroEpico.set('Todos');
    this.filtroTipo.set('');
    this.filtroResponsavel.set('');
    this.total.set(0);
    this.tela.set('projeto');
    this.pagina.set('visao');
    this.carregar();
    this.api.listarEpicos(projeto.projetoId).subscribe({
      next: response => {
        if (this.projetoId() !== projeto.projetoId) return;
        const rows = response.body?.items ?? [];
        this.epicosRemotos.set(rows);
        this.epicos.set(rows.map(item => item.titulo));
      },
      error: falha => this.erro.set(this.descrever(falha)),
    });
    this.api.listarMembrosProjeto(projeto.projetoId).subscribe({
      next: response => {
        if (this.projetoId() !== projeto.projetoId) return;
        this.membrosProjeto.set(response.body?.items ?? []);
        this.etagMembros.set(response.headers.get('ETag'));
      },
      error: falha => this.erro.set(this.descrever(falha)),
    });
  }

  protected abrirEdicaoProjeto(): void {
    this.nomeProjetoDraft.set(this.projetoNome());
    this.descricaoProjetoDraft.set(this.descricaoProjeto());
    this.projetoEditando.set(true);
  }

  protected salvarEdicaoProjeto(): void {
    const nome = this.nomeProjetoDraft().trim();
    if (!nome) return;
    this.api.obterProjeto(this.projetoId()).subscribe({
      next: response => {
        const etag = response.headers.get('ETag');
        if (!etag) { this.erroLogin.set('A API não retornou o ETag do projeto.'); return; }
        this.api.editarProjeto(this.projetoId(), { nome, descricao: this.descricaoProjetoDraft().trim() }, etag).subscribe({
          next: atualizado => { this.projetoNome.set(atualizado.nome); this.descricaoProjeto.set(atualizado.descricao ?? ''); this.projetoEditando.set(false); this.projetos.update(rows => rows.map(row => row.projetoId === atualizado.projetoId ? atualizado : row)); },
          error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível editar o projeto.')),
        });
      }, error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível carregar a revisão do projeto.')),
    });
  }

  protected alterarArquivo(projeto: Projeto): void {
    const arquivado = projeto.arquivado || this.projetosArquivadosLocal().includes(projeto.projetoId);
    this.menuProjetoId.set(null);
    this.api.obterProjeto(projeto.projetoId).subscribe({
      next: response => {
        const etag = response.headers.get('ETag');
        if (!etag) { this.erroLogin.set('A API não retornou o ETag do projeto. Atualize a tela e tente novamente.'); return; }
        const alteracao = arquivado ? this.api.restaurarProjeto(projeto.projetoId, etag) : this.api.arquivarProjeto(projeto.projetoId, etag);
        alteracao.subscribe({
          next: () => { this.carregarProjetos(this.abaWorkspace() === 'arquivados'); this.avisoLocal.set(arquivado ? 'Projeto restaurado.' : 'Projeto arquivado.'); },
          error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível alterar o projeto.')),
        });
      },
      error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível carregar a revisão do projeto.')),
    });
  }

  protected solicitarExclusaoProjeto(): void {
    const projeto = this.projetoParaExcluir();
    if (!projeto || this.confirmacaoExcluirProjeto() !== projeto.nome) return;
    this.api.obterProjeto(projeto.projetoId).subscribe({
      next: response => {
        const etag = response.headers.get('ETag');
        if (!etag) { this.erroLogin.set('A API não retornou o ETag do projeto.'); return; }
        this.api.excluirProjeto(projeto.projetoId, this.confirmacaoExcluirProjeto(), etag).subscribe({
          next: () => {
            this.projetoParaExcluir.set(null); this.confirmacaoExcluirProjeto.set('');
            this.avisoLocal.set('Pedido de exclusão recebido. O processamento pode levar alguns instantes.');
            this.carregarProjetos(this.abaWorkspace() === 'arquivados');
          }, error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível solicitar a exclusão.')),
        });
      }, error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível carregar a revisão do projeto.')),
    });
  }

  protected projetoArquivado(projeto: Projeto): boolean {
    return projeto.arquivado || this.projetosArquivadosLocal().includes(projeto.projetoId);
  }

  protected voltarAosWorkspaces(): void {
    if (!this.podeSairDoEditor()) return;
    this.modalTrocarProjeto.set(false);
    this.contaMenuAberto.set(false);
    this.abrirWorkspace(this.workspaceId(), this.workspaceNome());
  }

  protected trocarProjeto(projeto: Projeto): void {
    this.abrirProjeto(projeto);
    this.modalTrocarProjeto.set(false);
  }

  protected carregar(): void {
    const projetoId = this.projetoId();
    this.carregando.set(true);
    this.erro.set(null);
    const situacoes = this.filtroSituacao() === 'TODOS' ? ['ATIVO', 'CANCELADO'] : [this.filtroSituacao()];
    const filtros = {
      q: this.busca().trim() || undefined,
      estadoVersao: this.filtroEstado() === 'Todos' ? undefined : this.filtroEstado(),
      tipo: this.filtroTipo() || undefined,
      epicoId: this.filtroEpico() === 'Todos' || this.filtroEpico() === 'Sem épico' ? undefined : this.filtroEpico(),
      responsavelId: this.filtroResponsavel() || undefined,
    };
    forkJoin(situacoes.map((situacao) => this.api.listarRequisitos(projetoId, situacao, filtros))).subscribe({
      next: (resposta) => {
        if (this.projetoId() !== projetoId) return;
        this.requisitos.set(resposta.flatMap((pagina) => pagina.body?.items ?? []));
        this.total.set(resposta.reduce((soma, pagina) => soma + (pagina.body?.totalElements ?? 0), 0));
        this.instancia.set(resposta[0]?.headers.get(CABECALHO_INSTANCIA) ?? null);
        this.carregando.set(false);
      },
      error: (falha: HttpErrorResponse) => {
        if (this.projetoId() !== projetoId) return;
        this.erro.set(this.descrever(falha));
        this.carregando.set(false);
      },
    });
  }

  protected labelEpico(id: string): string {
    return this.epicosRemotos().find(item => item.epicoId === id)?.titulo ?? id;
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
    const projetoId = this.projetoId();
    this.detalhe.set(null);
    this.etagRequisito.set(null);
    this.conectarAberto.set(false);
    this.mapaRaizId.set(requisito.requisitoId);
    this.requisitoDetalhe.set(requisito);
    this.api.listarRelacoes(this.projetoId()).subscribe({
      next: (resposta) => this.relacoes.set(resposta.body?.items ?? []),
      error: (falha: HttpErrorResponse) => this.erroRelacoes.set(this.descrever(falha)),
    });
    this.api.obterRequisito(this.projetoId(), requisito.requisitoId).subscribe({
      next: response => {
        if (this.projetoId() !== projetoId || this.requisitoDetalhe()?.requisitoId !== requisito.requisitoId) return;
        this.detalhe.set(response.body);
        if (response.body && !requisito.codigo) {
          this.requisitoDetalhe.set({ ...requisito, ...response.body });
        }
        this.etagRequisito.set(response.headers.get('ETag'));
        if (this.pagina() === 'editor' && response.body) this.carregarEditor(response.body);
      },
      error: (falha: HttpErrorResponse) => this.erro.set(this.descrever(falha)),
    });
  }

  protected readonly editorDetalhes = signal(false);
  protected readonly editorMenu = signal(false);
  protected readonly editorTitulo = signal('');
  protected readonly gruposFormatacao: { comando: string; nome: string; texto: string; valor?: string }[][] = [
    [{ comando: 'bold', nome: 'Negrito', texto: 'B' }, { comando: 'italic', nome: 'Itálico', texto: 'I' }, { comando: 'underline', nome: 'Sublinhado', texto: 'U' }],
    [{ comando: 'formatBlock', nome: 'Título de seção', texto: 'H2', valor: 'h2' }, { comando: 'formatBlock', nome: 'Parágrafo', texto: '¶', valor: 'p' }],
    [{ comando: 'insertUnorderedList', nome: 'Lista com marcadores', texto: '•' }, { comando: 'insertOrderedList', nome: 'Lista numerada', texto: '1.' }],
  ];
  protected readonly editorLinkAberto = signal(false);
  protected readonly enderecoEditorLink = signal('');
  private selecaoEditorLink?: Range;
  protected readonly editorConteudo = signal('');
  protected readonly editorCriterios = signal<string[]>([]);
  protected readonly editorCriteriosAbertos = signal(false);
  protected readonly editorVersao = signal<Versao | null>(null);
  protected readonly editorErro = signal('');
  protected readonly editorCarregando = signal(false);
  protected readonly editorSalvando = signal(false);
  protected readonly editorAlterado = signal(false);
  protected readonly editorTextoAlterado = signal(false);
  protected readonly decisaoEditor = signal<'APROVAR' | 'REJEITAR' | null>(null);
  protected readonly justificativaDecisao = signal('');
  private readonly etagEditor = signal<string | undefined>(undefined);
  private retornoEditor: 'visao' | 'requisitos' | 'mapa' = 'mapa';

  protected abrirEditor(): void {
    const visao = this.detalhe();
    if (!visao) return;
    const pagina = this.pagina();
    if (pagina !== 'editor') this.retornoEditor = pagina;
    this.editorDetalhes.set(false);
    this.editorMenu.set(false);
    this.pagina.set('editor');
    this.registrarPagina('requisitos', visao.requisitoId);
    this.carregarEditor(visao);
  }

  private podeSairDoEditor(): boolean {
    if (this.pagina() !== 'editor') return true;
    if (this.editorSalvando() || this.executandoAcao()) return false;
    return !this.editorAlterado() || window.confirm('Sair sem salvar as alterações deste requisito?');
  }

  protected navegarProjeto(pagina: 'visao' | 'requisitos' | 'mapa'): void {
    if (!this.podeSairDoEditor()) return;
    this.editorAlterado.set(false);
    this.registrarPagina(pagina);
    if (pagina === 'mapa') {
      this.abrirMapa();
      return;
    }
    this.fecharDetalhes();
    this.pagina.set(pagina);
  }

  protected buscarNoProjeto(termo: string): void {
    if (this.pagina() === 'editor' && !this.podeSairDoEditor()) return;
    this.busca.set(termo);
    this.registrarPagina('requisitos');
    this.editorAlterado.set(false);
    if (this.pagina() === 'editor') this.fecharDetalhes();
    this.pagina.set('requisitos');
  }

  protected voltarDoEditor(): void {
    if (!this.podeSairDoEditor()) return;
    this.editorAlterado.set(false);
    this.registrarPagina(this.retornoEditor);
    this.pagina.set(this.retornoEditor);
    if (this.retornoEditor !== 'mapa') this.fecharDetalhes();
  }

  private carregarEditor(visao: RequisitoGestao): void {
    this.editorErro.set('');
    this.editorLinkAberto.set(false);
    this.editorAlterado.set(false);
    this.editorTextoAlterado.set(false);
    this.editorVersao.set(null);
    this.etagEditor.set(undefined);
    this.editorTitulo.set(visao.titulo);
    this.editorConteudo.set('');
    this.editorCriterios.set([]);
    this.editorCriteriosAbertos.set(false);
    const publicada = visao.versaoPublicadaId;
    const versoes = visao._links?.['versoes']?.href
      ?? `/api/v1/gestao/projetos/${this.projetoId()}/requisitos/${visao.requisitoId}/versoes`;
    const versaoId = visao.versaoId ?? publicada;
    const link = visao._links?.['candidata']?.href ?? (versaoId ? `${versoes}/${versaoId}` : undefined);
    this.editorCarregando.set(!!link);
    if (!link) return;
    this.api.carregarRecurso<Versao>(link).subscribe({
      next: resposta => {
        if (this.pagina() !== 'editor' || this.detalhe() !== visao) return;
        const versao = resposta.body;
        this.editorVersao.set(versao);
        this.etagEditor.set(resposta.headers.get('ETag') ?? undefined);
        this.editorTitulo.set(versao?.titulo ?? visao.titulo);
        this.editorConteudo.set(this.htmlDoRequisito(versao?.conteudo ?? ''));
        this.editorCriterios.set(versao?.criteriosAceite ?? []);
        this.editorCarregando.set(false);
      },
      error: falha => {
        if (this.detalhe() !== visao) return;
        this.editorCarregando.set(false);
        this.editorErro.set(this.detalheErro(falha, 'Não foi possível carregar a versão.'));
      },
    });
  }

  private htmlDoRequisito(conteudo: string): string {
    if (/^\s*<(?:p|div|h[1-6]|ul|ol|strong|b|i|em|u|br|pre|table|span|blockquote|a)\b/i.test(conteudo)) return conteudo;
    return conteudo.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
      .split('\n').map(linha => linha.startsWith('## ') ? `<h2>${linha.slice(3)}</h2>` : `<div>${linha || '<br>'}</div>`).join('');
  }

  protected abrirLinkEditor(editor: HTMLElement): void {
    if (!editor.isContentEditable) return;
    const selecao = window.getSelection();
    this.selecaoEditorLink = selecao?.rangeCount && editor.contains(selecao.anchorNode)
      ? selecao.getRangeAt(0).cloneRange() : undefined;
    this.enderecoEditorLink.set('');
    this.editorLinkAberto.set(true);
  }

  protected readonly editorLinkValido = computed(() => {
    try {
      return ['http:', 'https:', 'mailto:'].includes(new URL(this.enderecoEditorLink().trim()).protocol);
    } catch {
      return false;
    }
  });

  protected inserirLinkEditor(editor: HTMLElement): void {
    if (!this.editorLinkValido() || !editor.isContentEditable) return;
    editor.focus();
    const selecao = window.getSelection();
    if (this.selecaoEditorLink && editor.contains(this.selecaoEditorLink.commonAncestorContainer)) {
      selecao?.removeAllRanges();
      selecao?.addRange(this.selecaoEditorLink);
    }
    const endereco = this.enderecoEditorLink().trim();
    if (selecao?.isCollapsed) {
      const link = document.createElement('a');
      link.href = endereco;
      link.textContent = endereco;
      this.formatarRequisito(editor, 'insertHTML', link.outerHTML);
    } else {
      this.formatarRequisito(editor, 'createLink', endereco);
    }
    this.editorLinkAberto.set(false);
    this.selecaoEditorLink = undefined;
  }

  protected inserirTabelaEditor(editor: HTMLElement): void {
    this.formatarRequisito(editor, 'insertHTML', '<table><tbody><tr><td> </td><td> </td></tr><tr><td> </td><td> </td></tr></tbody></table><p><br></p>');
  }

  protected formatarRequisito(editor: HTMLElement, comando: string, valor?: string): void {
    editor.focus();
    if (editor.isContentEditable && document.execCommand(comando, false, valor)) this.marcarTextoEditor();
  }

  protected marcarTextoEditor(): void {
    this.editorTextoAlterado.set(true);
    this.editorAlterado.set(true);
  }

  protected partesCriterio(criterio: string): RegExpMatchArray | null {
    return criterio.match(/^Dado:? ([^\n]*)\nQuando:? ([^\n]*)\nEntão:? ([^\n]*)$/i);
  }

  protected editarParteCriterio(indice: number, parte: number, valor: string): void {
    const dados = this.partesCriterio(this.editorCriterios()[indice])?.slice(1);
    if (!dados) return;
    dados[parte] = valor;
    this.atualizarCriterio(indice, `Dado: ${dados[0]}\nQuando: ${dados[1]}\nEntão: ${dados[2]}`);
  }

  protected readonly criteriosEditorValidos = computed(() => this.editorCriterios().every(criterio => {
    const partes = this.partesCriterio(criterio);
    return !partes || partes.slice(1).every(parte => parte.trim());
  }));

  protected atualizarCriterio(indice: number, criterio: string): void {
    this.editorCriterios.update(lista => lista.map((item, i) => i === indice ? criterio : item));
    this.editorAlterado.set(true);
  }

  protected adicionarCriterioEditor(): void {
    this.editorCriteriosAbertos.set(true);
    this.editorCriterios.update(lista => [...lista, 'Dado: \nQuando: \nEntão: ']);
    this.editorAlterado.set(true);
  }

  protected removerCriterioEditor(indice: number): void {
    this.editorCriterios.update(lista => lista.filter((_, i) => i !== indice));
    this.editorAlterado.set(true);
  }

  protected nomeResponsavelEditor(id?: string): string {
    if (!id) return 'Sem responsável';
    return this.membrosProjeto().find(membro => membro.usuarioId === id)?.nome ?? 'Responsável atribuído';
  }

  protected salvarEditor(editor: HTMLElement): void {
    const acao = this.detalhe()?._actions?.['editar-candidata'];
    const etag = this.etagEditor();
    const titulo = this.editorTitulo().trim();
    if (!acao || !etag || !titulo || !this.editorAlterado() || this.editorSalvando() || !this.criteriosEditorValidos()) return;
    const conteudo = this.editorTextoAlterado() ? editor.innerHTML : this.editorVersao()?.conteudo;
    this.editorSalvando.set(true);
    this.editorErro.set('');
    this.api.executarAcao(acao, { titulo, conteudo, criteriosAceite: this.editorCriterios().map(item => item.trim()).filter(Boolean) }, etag)
      .pipe(finalize(() => this.editorSalvando.set(false))).subscribe({
        next: () => {
          this.editorAlterado.set(false);
          const requisito = this.requisitoDetalhe();
          if (requisito) this.abrir(requisito);
          this.carregar();
        },
        error: falha => this.editorErro.set(this.detalheErro(falha, 'Não foi possível salvar o requisito.')),
      });
  }

  protected salvarMetadadoEditor(campo: 'epicoId' | 'tipo', valor: string): void {
    const visao = this.detalhe();
    const acao = visao?._actions?.['editar-metadados'];
    const etag = this.etagRequisito();
    if (!visao || !acao || !etag || this.editorSalvando() || this.editorAlterado()) return;
    this.editorSalvando.set(true);
    this.editorErro.set('');
    this.api.executarAcao(acao, { [campo]: campo === 'epicoId' ? valor || null : valor }, etag)
      .pipe(finalize(() => this.editorSalvando.set(false))).subscribe({
        next: () => {
          const requisito = this.requisitoDetalhe();
          if (requisito) this.abrir(requisito);
          this.carregar();
        },
        error: falha => this.editorErro.set(this.detalheErro(falha, 'Não foi possível atualizar os metadados.')),
      });
  }

  protected prepararDecisao(decisao: 'APROVAR' | 'REJEITAR'): void {
    const acao = this.detalhe()?._actions?.['decidir'];
    if (!acao || !this.editorVersao()) return;
    this.prepararAcao('decidir', acao);
    this.decisaoEditor.set(decisao);
    this.justificativaDecisao.set('');
  }

  protected prepararAcao(nome: string, recurso: AcaoRecurso): void {
    this.editorMenu.set(false);
    this.decisaoEditor.set(null);
    this.erroAcao.set('');
    const versaoId = this.editorVersao()?.versaoId;
    this.corpoAcao.set(JSON.stringify(nome === 'cancelar' ? { confirmacao: true } : nome === 'submeter' && versaoId ? { versaoId } : {}));
    this.acaoSelecionada.set({ nome, recurso });
  }

  protected carregarHistoricoRequisito(): void {
    const requisito = this.requisitoDetalhe();
    if (!requisito) return;
    this.complementoRequisito.set('historico');
    this.api.listarHistoricoRequisito(this.projetoId(), requisito.requisitoId).subscribe({
      next: pagina => this.historicoRequisito.set(pagina.items),
      error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível carregar o histórico.')),
    });
  }

  protected carregarVersoesRequisito(): void {
    const requisito = this.requisitoDetalhe();
    if (!requisito) return;
    this.complementoRequisito.set('versoes');
    this.comparacaoVersao.set('');
    this.api.listarVersoesRequisito(this.projetoId(), requisito.requisitoId).subscribe({
      next: pagina => this.versoesRequisito.set(pagina.items),
      error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível carregar as versões.')),
    });
  }

  protected compararVersao(versao: VersaoRequisito): void {
    const requisito = this.requisitoDetalhe();
    if (!requisito) return;
    this.api.compararVersoesRequisito(this.projetoId(), requisito.requisitoId, versao.versaoId).subscribe({
      next: resultado => this.comparacaoVersao.set(JSON.stringify(resultado, null, 2)),
      error: falha => this.erroLogin.set(this.detalheErro(falha, 'Não foi possível comparar as versões.')),
    });
  }

  protected fecharModalAcao(): void {
    if (!this.executandoAcao()) this.acaoSelecionada.set(null);
  }

  protected confirmarAcao(): void {
    const selecionada = this.acaoSelecionada();
    if (!selecionada || this.executandoAcao()) return;
    const decisao = this.decisaoEditor();
    if (decisao === 'REJEITAR' && !this.justificativaDecisao().trim()) return;
    let body: Record<string, unknown>;
    try {
      body = JSON.parse(this.corpoAcao()) as Record<string, unknown>;
      if (!body || Array.isArray(body) || typeof body !== 'object') throw new Error();
    } catch {
      this.erroAcao.set('Informe os dados da ação como um objeto JSON válido.');
      return;
    }
    this.executandoAcao.set(true);
    this.erroAcao.set('');
    const links = this.detalhe()?._links ?? {};
    const usaRevisao = selecionada.nome === 'decidir';
    const usaCandidata = ['editar-candidata', 'submeter', 'publicar'].includes(selecionada.nome);
    const linkVersao = usaRevisao
      ? links['revisao']?.href
      : usaCandidata
        ? links['candidata']?.href ?? `/api/v1/gestao/projetos/${this.projetoId()}/requisitos/${this.requisitoDetalhe()?.requisitoId}/candidata`
        : undefined;
    const executar = (etag?: string) => this.api.executarAcao(selecionada.recurso, body, etag ?? this.etagRequisito() ?? undefined).subscribe({
      next: () => {
        this.acaoSelecionada.set(null);
        this.executandoAcao.set(false);
        this.carregar();
        const requisito = this.requisitoDetalhe();
        if (requisito) this.abrir(requisito);
      },
      error: (falha: HttpErrorResponse) => { this.erroAcao.set(this.descrever(falha)); this.executandoAcao.set(false); },
    });
    if (usaRevisao || usaCandidata) {
      if (!linkVersao) {
        this.erroAcao.set('A API não informou o link da revisão necessária para esta ação.');
        this.executandoAcao.set(false);
        return;
      }
      this.api.carregarRecurso<LoteRevisao>(linkVersao).subscribe({
        next: response => {
          const etag = response.headers.get('ETag');
          if (!etag) { this.erroAcao.set('A API não retornou o ETag da versão que será alterada.'); this.executandoAcao.set(false); return; }
          if (usaRevisao && decisao) {
            const lote = response.body;
            if (!lote) {
              this.erroAcao.set('A API não retornou o lote de revisão.');
              this.executandoAcao.set(false);
              return;
            }
            const { submissaoId, versaoId, hashConteudo } = lote;
            const versao = this.editorVersao();
            if (versao?.versaoId !== versaoId || versao.hashConteudo !== hashConteudo) {
              this.erroAcao.set('O lote mudou. Reabra o requisito e confira a versão antes de decidir.');
              this.executandoAcao.set(false);
              return;
            }
            body = { decisao, submissaoId, versaoId, hashConteudo, ...(decisao === 'REJEITAR' ? { justificativa: this.justificativaDecisao().trim() } : {}) };
          }
          executar(etag);
        },
        error: (falha: HttpErrorResponse) => { this.erroAcao.set(this.descrever(falha)); this.executandoAcao.set(false); },
      });
    } else executar(this.etagRequisito() ?? undefined);
  }

  protected fecharDetalhes(): void {
    this.detalhe.set(null);
    this.requisitoDetalhe.set(null);
  }

  protected zoom(delta: number): void {
    this.zoomMapa.update((valor) => Math.min(1.4, Math.max(0.4, Math.round((valor + delta) * 100) / 100)));
  }

  protected centralizarMapa(requisitoId: string | null = null, viewport?: HTMLElement): void {
    this.mapaRaizId.set(requisitoId);
    this.centroMapa.set(requisitoId);
    this.panMapa.set({ x: 0, y: 0 });
    const { largura, altura } = this.layoutMapa();
    const escala = viewport ? Math.min((viewport.clientWidth - 52) / largura, (viewport.clientHeight - 100) / altura) : 0.82;
    this.zoomMapa.set(requisitoId ? Math.max(0.82, this.zoomMapa()) : Math.max(0.4, Math.min(1, escala)));
  }

  protected iniciarPanMapa(evento: PointerEvent): void {
    if (!evento.isPrimary || evento.button !== 0 || !(evento.target instanceof Element) ||
      evento.target.closest('button') || !(evento.currentTarget instanceof HTMLElement)) return;
    const { x, y } = this.panMapa();
    this.gestoMapa = { id: evento.pointerId, x: evento.clientX, y: evento.clientY, panX: x, panY: y };
    this.arrastandoMapa.set(true);
    evento.currentTarget.setPointerCapture(evento.pointerId);
  }

  protected moverPanMapa(evento: PointerEvent): void {
    const gesto = this.gestoMapa;
    if (!gesto || gesto.id !== evento.pointerId) return;
    this.panMapa.set({ x: gesto.panX + evento.clientX - gesto.x, y: gesto.panY + evento.clientY - gesto.y });
  }

  protected encerrarPanMapa(evento: PointerEvent): void {
    if (this.gestoMapa?.id !== evento.pointerId) return;
    this.gestoMapa = undefined;
    this.arrastandoMapa.set(false);
  }

  protected origemVisual(relacao: Relacao): string {
    return relacao.tipo === 'DEPENDE_DE' ? relacao.destinoId : relacao.origemId;
  }

  protected destinoVisual(relacao: Relacao): string {
    return relacao.tipo === 'DEPENDE_DE' ? relacao.origemId : relacao.destinoId;
  }

  protected rotuloRelacao(relacao: Relacao): string {
    if (relacao.tipo === 'DEPENDE_DE') return 'Pré-requisito';
    if (relacao.tipo === 'BLOQUEIA') return 'Bloqueia';
    return 'Relacionado';
  }

  protected relacaoDestacada(relacao: Relacao): boolean {
    const raiz = this.requisitoSobPonteiro() ?? this.mapaRaizId();
    return !!raiz && (relacao.origemId === raiz || relacao.destinoId === raiz);
  }

  protected deslocamentoMapa(): string {
    const { largura, altura, posicoes } = this.layoutMapa();
    const centro = posicoes.get(this.centroMapa() ?? '');
    const pan = this.panMapa();
    const escala = this.zoomMapa();
    const x = pan.x + (centro ? (largura / 2 - centro.x) * escala : 0);
    const y = pan.y + (centro ? (altura / 2 - centro.y) * escala : 0);
    return `translate(${x}px, ${y}px) scale(${escala})`;
  }

  protected centralizarRequisitoNoMapa(requisitoId: string): void {
    this.fecharDetalhes();
    this.registrarPagina('mapa');
    this.pagina.set('mapa');
    this.centralizarMapa(requisitoId);
  }

  protected estadoNoMapa(requisito: Requisito): string {
    if (requisito.situacao === 'CANCELADO') return 'Cancelada';
    const estado = requisito.candidata?.estadoVersao;
    return ({
      RASCUNHO: 'Rascunho', EM_SUBMISSAO: 'Em submissão', AGUARDANDO_REVISAO: 'Em revisão',
      APROVADA: 'Aprovada', PUBLICADA: 'Publicada',
    } as Record<string, string>)[estado ?? ''] ?? (requisito.versaoPublicadaId ? 'Publicada' : 'Sem candidata');
  }

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
  protected rotuloRelacaoNoPainel(relacao: Relacao, requisitoId: string): string {
    const origem = relacao.origemId === requisitoId;
    if (relacao.tipo === 'DEPENDE_DE') return origem ? 'Depende de' : 'É pré-requisito de';
    if (relacao.tipo === 'BLOQUEIA') return origem ? 'Bloqueia' : 'É bloqueado por';
    return 'Relacionado a';
  }

  protected removerRelacaoDoMapa(relacao: Relacao): void {
    if (!this.detalhe()?._actions?.['editar-metadados'] || !window.confirm('Remover esta relação do mapa?')) return;
    const projetoId = this.projetoId();
    this.erroRelacoes.set(null);
    this.api.removerRelacao(projetoId, relacao.relacaoId).subscribe({
      next: () => {
        if (this.projetoId() === projetoId) this.relacoes.update(lista => lista.filter(item => item.relacaoId !== relacao.relacaoId));
      },
      error: falha => {
        if (this.projetoId() === projetoId) this.erroRelacoes.set(falha instanceof HttpErrorResponse ? this.detalheErro(falha, 'Não foi possível remover a relação.') : falha.message);
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

  protected abrirNovoRequisito(nomeEpico?: string): void {
    this.menuEpico.set(null);
    this.epicoSelecionado.set(this.epicosRemotos().find(epico => epico.titulo === nomeEpico)?.epicoId ?? '');
    this.novoAberto.set(true);
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
      next: requisito => {
        this.titulo.set('');
        this.epicoSelecionado.set('');
        this.conteudo.set('');
        this.criterios.set('');
        this.novoAberto.set(false);
        this.salvando.set(false);
        this.registrarPagina('requisitos');
        this.pagina.set('requisitos');
        this.abrir(requisito);
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
    this.api.criarEpico(this.projetoId(), nome).subscribe({
      next: epico => { this.epicosRemotos.update(rows => [...rows, epico]); this.epicos.update(rows => [...rows, epico.titulo]); this.nomeEpico.set(''); this.modalNovoEpico.set(false); },
      error: falha => this.erro.set(this.descrever(falha)),
    });
  }

  protected readonly epicoEmRenomeacao = signal<{ epicoId: string; titulo: string } | null>(null);
  protected readonly novoNomeEpico = signal('');
  protected readonly erroRenomearEpico = signal('');
  protected readonly renomeandoEpico = signal(false);

  protected editarEpico(nomeAtual: string): void {
    this.menuEpico.set(null);
    const epico = this.epicosRemotos().find(item => item.titulo === nomeAtual);
    if (!epico) return;
    this.novoNomeEpico.set(epico.titulo);
    this.erroRenomearEpico.set('');
    this.epicoEmRenomeacao.set(epico);
  }

  protected fecharRenomearEpico(): void {
    if (!this.renomeandoEpico()) this.epicoEmRenomeacao.set(null);
  }

  protected salvarNomeEpico(): void {
    const epico = this.epicoEmRenomeacao();
    const titulo = this.novoNomeEpico().trim();
    if (!epico || !titulo || titulo === epico.titulo || this.renomeandoEpico()) return;

    const projetoId = this.projetoId();
    this.erroRenomearEpico.set('');
    this.renomeandoEpico.set(true);
    this.api.obterEpico(projetoId, epico.epicoId).pipe(
      switchMap(resposta => {
        const etag = resposta.headers.get('ETag');
        if (!etag) throw new Error('A API não retornou a revisão do épico.');
        return this.api.editarEpico(projetoId, epico.epicoId, titulo, etag);
      }),
      finalize(() => this.renomeandoEpico.set(false)),
    ).subscribe({
      next: atualizado => {
        this.epicosRemotos.update(lista => lista.map(item => item.epicoId === atualizado.epicoId ? atualizado : item));
        this.epicos.update(lista => lista.map(nome => nome === epico.titulo ? atualizado.titulo : nome));
        this.epicosAbertos.update(lista => lista.map(nome => nome === epico.titulo ? atualizado.titulo : nome));
        this.epicoEmRenomeacao.set(null);
      },
      error: falha => this.erroRenomearEpico.set(falha instanceof HttpErrorResponse
        ? this.detalheErro(falha, 'Não foi possível renomear o épico.')
        : falha.message),
    });
  }

  protected excluirEpico(nome: string): void {
    this.menuEpico.set(null);
    const epico = this.epicosRemotos().find(item => item.titulo === nome);
    if (!epico) return;
    if (this.requisitosDoEpico(nome).length) {
      this.erro.set('Mova os requisitos ativos para outro épico antes de excluir este épico.');
      return;
    }
    if (!window.confirm(`Excluir o épico “${nome}”?`)) return;

    const projetoId = this.projetoId();
    this.erro.set(null);
    this.api.obterEpico(projetoId, epico.epicoId).pipe(
      switchMap(resposta => {
        const etag = resposta.headers.get('ETag');
        if (!etag) throw new Error('A API não retornou a revisão do épico.');
        return this.api.excluirEpico(projetoId, epico.epicoId, etag);
      }),
    ).subscribe({
      next: () => {
        if (this.projetoId() !== projetoId) return;
        this.epicosRemotos.update(lista => lista.filter(item => item.epicoId !== epico.epicoId));
        this.epicos.update(lista => lista.filter(item => item !== nome));
        this.epicosAbertos.update(lista => lista.filter(item => item !== nome));
        if (this.filtroEpico() === epico.epicoId) this.filtroEpico.set('Todos');
        this.carregar();
      },
      error: falha => {
        if (this.projetoId() !== projetoId) return;
        this.erro.set(falha instanceof HttpErrorResponse ? this.detalheErro(falha, 'Não foi possível excluir o épico.') : falha.message);
      },
    });
  }

  protected toggleEpico(nome: string): void {
    this.epicosAbertos.update((abertos) => abertos.includes(nome)
      ? abertos.filter((epico) => epico !== nome)
      : [...abertos, nome]);
  }

  protected requisitosDoEpico(nome: string): Requisito[] {
    return this.requisitos().filter(item => item.situacao === 'ATIVO' && this.epicoDo(item) === nome);
  }

  protected epicoDo(requisito: Requisito): string {
    const raw = requisito.epicoId;
    if (raw) return this.labelEpico(raw);
    return 'Sem épico';
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
    const problema = falha.error as { detail?: string; title?: string } | null;
    return problema?.detail ?? problema?.title ?? 'Não foi possível falar com o servidor.';
  }
}
