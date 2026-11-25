import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, switchMap } from 'rxjs';
import { Sessao, SessaoService } from './sessao';

/**
 * Cliente do BFF Gestão. O caminho é sempre relativo à origem da página: o
 * navegador só conhece uma origem, e quem encaminha /api para o Gateway é o
 * nginx (compose) ou o Ingress (Kubernetes).
 */
const BASE = '/api/v1/gestao';
const AUTH_BASE = '/api/v1/autenticacao';

/** Cabeçalho em que o BFF informa qual instância respondeu. */
export const CABECALHO_INSTANCIA = 'X-Athena-Instancia';

export interface LoteRevisao {
  submissaoId: string;
  versaoId: string;
  hashConteudo: string;
}

export interface Versao {
  titulo?: string;
  hashConteudo?: string;
  versaoId: string;
  numero: number;
  estadoVersao: string;
  conteudo: string;
  criteriosAceite: string[];
}

export interface Requisito {
  requisitoId: string;
  projetoId: string;
  codigo: string;
  titulo: string;
  tipo?: string;
  epicoId?: string;
  responsavelId?: string;
  situacao: string;
  candidata?: Versao;
  versaoPublicadaId?: string;
  atualizadoEm: string;
}

export interface Projeto {
  projetoId: string;
  workspaceId: string;
  nome: string;
  descricao: string;
  arquivado: boolean;
  ownerId: string;
  atualizadoEm: string;
}

export interface Workspace { workspaceId: string; nome: string; ownerId: string; atualizadoEm: string; }
export interface Notificacao { notificacaoId: string; mensagem: string; lida: boolean; atualizadoEm: string; tipo: string; projetoId?: string; link?: string; }
export interface Membro { membroId: string; usuarioId: string; papel: string; atualizadoEm: string; nome: string; email: string; }
export interface Convite { conviteId: string; projetoId: string; email: string; papel: string; aceito: boolean; atualizadoEm: string; projetoNome?: string; }
export interface Baseline { baselineId: string; nome: string; atualizadoEm?: string; quantidade?: number; }
export interface VersaoRequisito { versaoId: string; numero: number; estadoVersao: string; titulo: string; atualizadoEm: string; }
export interface Usuario { usuarioId: string; nome: string; email: string; }

export interface NovoProjeto {
  workspaceId: string;
  nome: string;
  descricao: string;
}

export interface Relacao {
  relacaoId: string;
  projetoId: string;
  origemId: string;
  destinoId: string;
  tipo: string;
  justificativa: string;
  atualizadoEm: string;
}

export interface NovaRelacao {
  origemId: string;
  destinoId: string;
  tipo: 'DEPENDE_DE' | 'BLOQUEIA' | 'RELACIONADO_A';
  justificativa: string;
}

export interface Pagina<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export type { Sessao } from './sessao';

export interface CredenciaisLogin {
  email: string;
  senha: string;
  lembrarDispositivo: boolean;
}

/** Visão composta pelo BFF: requisito (Requisitos) + lote e pendências (Revisões). */
export interface RequisitoGestao {
  epicoId?: string;
  tipo?: string;
  responsavelId?: string;
  versaoPublicadaId?: string;
  versaoId?: string;
  requisitoId: string;
  codigo: string;
  titulo: string;
  situacao: string;
  versaoEmEdicao?: number;
  estadoVersao?: string;
  loteId?: string;
  pendenciasAbertas?: number;
  atualizadoEm: string;
  avisos: string[];
  _actions?: Record<string, AcaoRecurso>;
  _links?: Record<string, { href: string }>;
}

export interface AcaoRecurso { href: string; method: string; contentType: string; campos: string[]; precondicoes: string[]; }

export interface NovoRequisito {
  documentoId: string;
  epicoId?: string;
  titulo: string;
  conteudo: string;
  criteriosAceite: string[];
}

@Injectable({ providedIn: 'root' })
export class GestaoApi {
  private readonly http = inject(HttpClient);
  private readonly sessao = inject(SessaoService);
  private accessToken: string | null = null;

  cadastrar(nome: string, email: string, senha: string): Observable<Usuario> {
    return this.http.post<Usuario>(`${AUTH_BASE}/cadastro`, { nome, email, senha });
  }

  autenticar(credenciais: CredenciaisLogin): Observable<Sessao> {
    return this.http.post<Sessao>(`${AUTH_BASE}/login`, credenciais, { withCredentials: true });
  }

  obterUsuarioAtual(): Observable<Usuario> {
    return this.http.get<Usuario>('/api/v1/usuarios/me', this.opcoesAutenticadas());
  }

  solicitarRecuperacao(email: string): Observable<{ mensagem: string }> {
    return this.http.post<{ mensagem: string }>(`${AUTH_BASE}/recuperacao`, { email });
  }

  concluirRecuperacao(email: string, codigo: string, novaSenha: string): Observable<void> {
    return this.http.post<void>(`${AUTH_BASE}/recuperacao/conclusao`, { email, codigo, novaSenha });
  }

  definirSessao(sessao: Sessao | null): void {
    this.accessToken = sessao?.accessToken ?? null;
    if (sessao) this.sessao.salvar(sessao);
    else this.sessao.limpar();
  }

  private opcoesAutenticadas(): { headers?: Record<string, string> } {
    const token = this.sessao.ler()?.accessToken ?? this.accessToken;
    return token ? { headers: { Authorization: `Bearer ${token}` } } : {};
  }

  listarWorkspaces(): Observable<Pagina<Workspace>> {
    return this.http.get<Pagina<Workspace>>(`${BASE}/workspaces`, { params: { size: 100 }, ...this.opcoesAutenticadas() });
  }

  obterWorkspace(workspaceId: string): Observable<HttpResponse<Workspace>> {
    return this.http.get<Workspace>(`${BASE}/workspaces/${workspaceId}`, { observe: 'response', ...this.opcoesAutenticadas() });
  }

  criarWorkspace(nome: string): Observable<Workspace> {
    return this.http.post<Workspace>(`${BASE}/workspaces`, { nome }, this.opcoesAutenticadas());
  }

  editarWorkspace(workspaceId: string, nome: string, etag: string): Observable<Workspace> {
    return this.http.patch<Workspace>(`${BASE}/workspaces/${workspaceId}`, { nome }, {
      ...this.opcoesAutenticadas(), headers: { ...this.opcoesAutenticadas().headers, 'If-Match': etag },
    });
  }

  excluirWorkspace(workspaceId: string, nome: string, etag: string): Observable<unknown> {
    return this.http.post(`${BASE}/workspaces/${workspaceId}/exclusoes`, { confirmacaoNome: nome }, {
      ...this.opcoesAutenticadas(), headers: { ...this.opcoesAutenticadas().headers, 'If-Match': etag, 'Idempotency-Key': crypto.randomUUID() },
    });
  }

  listarProjetos(workspaceId: string, arquivado = false): Observable<HttpResponse<Pagina<Projeto>>> {
    return this.http.get<Pagina<Projeto>>(`${BASE}/projetos`, {
      params: { workspaceId, size: 100, arquivado },
      observe: 'response',
      ...this.opcoesAutenticadas(),
    });
  }

  criarProjeto(projeto: NovoProjeto): Observable<Projeto> {
    return this.http.post<Projeto>(`${BASE}/projetos`, projeto, this.opcoesAutenticadas());
  }

  editarProjeto(projetoId: string, patch: { nome: string; descricao: string }, etag: string): Observable<Projeto> {
    return this.http.patch<Projeto>(`${BASE}/projetos/${projetoId}`, patch, { ...this.opcoesAutenticadas(), headers: { ...this.opcoesAutenticadas().headers, 'If-Match': etag } });
  }

  obterProjeto(projetoId: string): Observable<HttpResponse<Projeto>> {
    return this.http.get<Projeto>(`${BASE}/projetos/${projetoId}`, { observe: 'response', ...this.opcoesAutenticadas() });
  }

  arquivarProjeto(projetoId: string, etag: string): Observable<Projeto> {
    return this.http.post<Projeto>(`${BASE}/projetos/${projetoId}/arquivamentos`, {}, {
      ...this.opcoesAutenticadas(), headers: { ...this.opcoesAutenticadas().headers, 'If-Match': etag, 'Idempotency-Key': crypto.randomUUID() },
    });
  }

  restaurarProjeto(projetoId: string, etag: string): Observable<Projeto> {
    return this.http.post<Projeto>(`${BASE}/projetos/${projetoId}/restauracoes`, {}, {
      ...this.opcoesAutenticadas(), headers: { ...this.opcoesAutenticadas().headers, 'If-Match': etag, 'Idempotency-Key': crypto.randomUUID() },
    });
  }

  excluirProjeto(projetoId: string, confirmacaoNome: string, etag: string): Observable<unknown> {
    return this.http.post(`${BASE}/projetos/${projetoId}/exclusoes`, { confirmacaoNome }, {
      ...this.opcoesAutenticadas(), headers: { ...this.opcoesAutenticadas().headers, 'If-Match': etag, 'Idempotency-Key': crypto.randomUUID() },
      observe: 'response',
    });
  }

  listarRequisitos(projetoId: string, situacao = 'ATIVO', filtros: { q?: string; estadoVersao?: string; tipo?: string; epicoId?: string; responsavelId?: string } = {}): Observable<HttpResponse<Pagina<Requisito>>> {
    let params = new HttpParams().set('size', 100).set('situacao', situacao);
    for (const [key, value] of Object.entries(filtros)) if (value) params = params.set(key, value);
    return this.http.get<Pagina<Requisito>>(`${BASE}/projetos/${projetoId}/requisitos`, {
      params,
      observe: 'response',
      ...this.opcoesAutenticadas(),
    });
  }

  listarEpicos(projetoId: string): Observable<HttpResponse<Pagina<{ epicoId: string; titulo: string }>>> {
    return this.http.get<Pagina<{ epicoId: string; titulo: string }>>(`${BASE}/projetos/${projetoId}/epicos`, { params: { size: 100 }, observe: 'response', ...this.opcoesAutenticadas() });
  }

  criarEpico(projetoId: string, nome: string): Observable<{ epicoId: string; titulo: string }> {
    return this.http.post<{ epicoId: string; titulo: string }>(`${BASE}/projetos/${projetoId}/epicos`, { titulo: nome }, this.opcoesAutenticadas());
  }

  obterEpico(projetoId: string, epicoId: string): Observable<HttpResponse<{ epicoId: string; titulo: string; descricao?: string }>> {
    return this.http.get<{ epicoId: string; titulo: string; descricao?: string }>(`${BASE}/projetos/${projetoId}/epicos/${epicoId}`, { observe: 'response', ...this.opcoesAutenticadas() });
  }

  editarEpico(projetoId: string, epicoId: string, titulo: string, etag: string): Observable<{ epicoId: string; titulo: string }> {
    return this.http.patch<{ epicoId: string; titulo: string }>(`${BASE}/projetos/${projetoId}/epicos/${epicoId}`, { titulo }, { ...this.opcoesAutenticadas(), headers: { ...this.opcoesAutenticadas().headers, 'If-Match': etag } });
  }

  excluirEpico(projetoId: string, epicoId: string, etag: string): Observable<void> {
    const headers = { ...this.opcoesAutenticadas().headers, 'If-Match': etag };
    return this.http.delete<void>(`${BASE}/projetos/${projetoId}/epicos/${epicoId}`, { headers });
  }

  listarMembrosProjeto(projetoId: string): Observable<HttpResponse<Pagina<Membro>>> {
    return this.http.get<Pagina<Membro>>(`${BASE}/projetos/${projetoId}/membros`, { params: { size: 100 }, observe: 'response', ...this.opcoesAutenticadas() });
  }

  convidarMembro(projetoId: string, email: string, papel: string): Observable<unknown> {
    return this.http.post(`${BASE}/projetos/${projetoId}/convites`, { email, papel }, this.opcoesAutenticadas());
  }

  editarPapelMembro(projetoId: string, membroId: string, papel: string, etag?: string): Observable<Membro> {
    const headers = { ...this.opcoesAutenticadas().headers, ...(etag ? { 'If-Match': etag } : {}) };
    return this.http.patch<Membro>(`${BASE}/projetos/${projetoId}/membros/${membroId}`, { papel }, { headers });
  }

  removerMembro(projetoId: string, membroId: string, etag?: string): Observable<void> {
    const headers = { ...this.opcoesAutenticadas().headers, ...(etag ? { 'If-Match': etag } : {}), 'Idempotency-Key': crypto.randomUUID() };
    return this.http.delete<void>(`${BASE}/projetos/${projetoId}/membros/${membroId}`, { headers });
  }

  listarBaselines(projetoId: string): Observable<Pagina<Baseline>> {
    return this.http.get<Pagina<Baseline>>(`${BASE}/projetos/${projetoId}/baselines`, { params: { size: 100 }, ...this.opcoesAutenticadas() });
  }

  criarBaseline(projetoId: string, nome: string): Observable<Baseline> {
    return this.http.post<Baseline>(`${BASE}/projetos/${projetoId}/baselines`, { nome, versoes: [] }, this.opcoesAutenticadas());
  }

  compararBaseline(projetoId: string, baselineId: string): Observable<unknown> {
    return this.http.get(`${BASE}/projetos/${projetoId}/baselines/${baselineId}/comparacao`, this.opcoesAutenticadas());
  }

  listarHistoricoRequisito(projetoId: string, requisitoId: string): Observable<Pagina<Record<string, unknown>>> {
    return this.http.get<Pagina<Record<string, unknown>>>(`${BASE}/projetos/${projetoId}/requisitos/${requisitoId}/historico`, { params: { size: 100 }, ...this.opcoesAutenticadas() });
  }

  listarVersoesRequisito(projetoId: string, requisitoId: string): Observable<Pagina<VersaoRequisito>> {
    return this.http.get<Pagina<VersaoRequisito>>(`${BASE}/projetos/${projetoId}/requisitos/${requisitoId}/versoes`, { params: { size: 100 }, ...this.opcoesAutenticadas() });
  }

  compararVersoesRequisito(projetoId: string, requisitoId: string, versaoId: string): Observable<unknown> {
    return this.http.get(`${BASE}/projetos/${projetoId}/requisitos/${requisitoId}/versoes/${versaoId}/diferencas`, this.opcoesAutenticadas());
  }

  listarConvitesRecebidos(): Observable<Pagina<Convite>> {
    return this.http.get<Pagina<Convite>>(`${BASE}/convites`, { params: { size: 100 }, ...this.opcoesAutenticadas() });
  }

  aceitarConvite(conviteId: string): Observable<Membro> {
    return this.http.post<Membro>(`${BASE}/convites/${conviteId}/aceite`, null, this.opcoesAutenticadas());
  }

  solicitarTransferenciaPropriedade(projetoId: string, destinatarioId: string, etag: string): Observable<unknown> {
    return this.http.post(`${BASE}/projetos/${projetoId}/transferencias-propriedade`, { destinatarioId, confirmacao: true }, {
      ...this.opcoesAutenticadas(), headers: { ...this.opcoesAutenticadas().headers, 'If-Match': etag, 'Idempotency-Key': crypto.randomUUID() },
    });
  }

  listarNotificacoes(): Observable<Pagina<Notificacao>> {
    return this.http.get<Pagina<Notificacao>>(`${BASE}/notificacoes`, { params: { size: 100 }, ...this.opcoesAutenticadas() });
  }

  marcarNotificacaoLida(id: string): Observable<Notificacao> {
    return this.http.patch<Notificacao>(`${BASE}/notificacoes/${id}`, { lida: true }, this.opcoesAutenticadas());
  }

  listarRelacoes(projetoId: string): Observable<HttpResponse<Pagina<Relacao>>> {
    return this.http.get<Pagina<Relacao>>(`${BASE}/projetos/${projetoId}/relacoes`, {
      params: { size: 100 },
      observe: 'response',
      ...this.opcoesAutenticadas(),
    });
  }

  criarRelacao(projetoId: string, relacao: NovaRelacao): Observable<Relacao> {
    return this.http.post<Relacao>(`${BASE}/projetos/${projetoId}/relacoes`, relacao, this.opcoesAutenticadas());
  }

  obterRequisito(projetoId: string, requisitoId: string): Observable<HttpResponse<RequisitoGestao>> {
    return this.http.get<RequisitoGestao>(`${BASE}/projetos/${projetoId}/requisitos/${requisitoId}`, { observe: 'response', ...this.opcoesAutenticadas() });
  }

  executarAcao(acao: AcaoRecurso, body: Record<string, unknown>, etag?: string): Observable<unknown> {
    const headers: Record<string, string> = { ...this.opcoesAutenticadas().headers };
    if (acao.precondicoes.some(condicao => condicao.startsWith('If-Match')) && etag) headers['If-Match'] = etag;
    if (acao.precondicoes.includes('Idempotency-Key')) headers['Idempotency-Key'] = crypto.randomUUID();
    return this.http.request(acao.method, acao.href, { ...(acao.method === 'GET' ? {} : { body }), headers, observe: 'response' });
  }

  removerRelacao(projetoId: string, relacaoId: string): Observable<void> {
    const href = `${BASE}/projetos/${projetoId}/relacoes/${relacaoId}`;
    return this.carregarRecurso<Relacao>(href).pipe(switchMap(resposta => {
      const etag = resposta.headers.get('ETag');
      if (!etag) throw new Error('A API não retornou a revisão da relação.');
      const headers = { ...this.opcoesAutenticadas().headers, 'If-Match': etag };
      return this.http.delete<void>(href, { headers });
    }));
  }

  carregarRecurso<T>(href: string): Observable<HttpResponse<T>> {
    return this.http.get<T>(href, { observe: 'response', ...this.opcoesAutenticadas() });
  }

  obterEtag(href: string): Observable<HttpResponse<unknown>> {
    return this.http.get<unknown>(href, { observe: 'response', ...this.opcoesAutenticadas() });
  }

  criarRequisito(projetoId: string, requisito: NovoRequisito): Observable<Requisito> {
    return this.http.post<Requisito>(`${BASE}/projetos/${projetoId}/requisitos`, requisito, this.opcoesAutenticadas());
  }
}
