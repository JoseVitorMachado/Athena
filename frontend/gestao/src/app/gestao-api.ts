import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, defer, finalize, of, shareReplay, switchMap, tap, throwError } from 'rxjs';

/**
 * Cliente do BFF Gestão. O caminho é sempre relativo à origem da página: o
 * navegador só conhece uma origem, e quem encaminha /api para o Gateway é o
 * nginx (compose) ou o Ingress (Kubernetes).
 */
const BASE = '/api/v1/gestao';
const AUTH_BASE = '/api/v1/autenticacao';

/** Cabeçalho em que o BFF informa qual instância respondeu. */
export const CABECALHO_INSTANCIA = 'X-Athena-Instancia';

export interface Versao {
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

export interface Sessao {
  accessToken: string;
  tokenType: 'Bearer';
  expiraEm: string;
  csrfToken: string;
}

export interface CredenciaisLogin {
  email: string;
  senha: string;
  lembrarDispositivo: boolean;
}

/** Visão composta pelo BFF: requisito (Requisitos) + lote e pendências (Revisões). */
export interface RequisitoGestao {
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
}

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
  private sessao: Sessao | null = null;
  private geracaoSessao = 0;
  private renovacao: Observable<Sessao> | null = null;

  autenticar(credenciais: CredenciaisLogin): Observable<Sessao> {
    return this.http.post<Sessao>(`${AUTH_BASE}/login`, credenciais, { withCredentials: true });
  }

  definirSessao(sessao: Sessao | null): void {
    this.sessao = sessao;
    this.geracaoSessao++;
    this.renovacao = null;
  }

  private sessaoExpirada(): HttpErrorResponse {
    return new HttpErrorResponse({ status: 401, error: { detail: 'Sessão expirada. Saia da conta e entre novamente.' } });
  }

  private renovarSessao(): Observable<Sessao> {
    if (this.renovacao) return this.renovacao;
    const sessao = this.sessao;
    if (!sessao) return throwError(() => this.sessaoExpirada());
    const geracao = this.geracaoSessao;
    // O cookie HttpOnly é rotacionado pelo backend. Requisições simultâneas
    // compartilham uma renovação para não reutilizar o mesmo refresh token.
    const renovacao = this.http.post<Sessao>(`${AUTH_BASE}/renovacao`, null, {
      withCredentials: true,
      headers: { 'X-CSRF-Token': sessao.csrfToken },
    }).pipe(
      tap((nova) => {
        if (geracao !== this.geracaoSessao) throw this.sessaoExpirada();
        this.sessao = nova;
      }),
      catchError((falha: HttpErrorResponse) => {
        if (geracao === this.geracaoSessao && (falha.status === 401 || falha.status === 403)) {
          this.definirSessao(null);
          return throwError(() => this.sessaoExpirada());
        }
        return throwError(() => falha);
      }),
      finalize(() => {
        if (this.renovacao === renovacao) this.renovacao = null;
      }),
      shareReplay({ bufferSize: 1, refCount: false }),
    );
    this.renovacao = renovacao;
    return renovacao;
  }

  private autenticada<T>(enviar: (opcoes: { headers: Record<string, string> }) => Observable<T>): Observable<T> {
    return defer(() => {
      const geracao = this.geracaoSessao;
      const sessao = this.sessao;
      if (!sessao) return throwError(() => this.sessaoExpirada());
      const pronta = Date.parse(sessao.expiraEm) > Date.now() + 30_000 ? of(sessao) : this.renovarSessao();
      const executar = (atual: Sessao) => enviar({ headers: { Authorization: `Bearer ${atual.accessToken}` } });
      return pronta.pipe(
        switchMap((usada) => executar(usada).pipe(
          catchError((falha: HttpErrorResponse) => {
            if (falha.status !== 401 || geracao !== this.geracaoSessao) return throwError(() => falha);
            // Outra requisição pode já ter renovado enquanto esta aguardava.
            const atual = this.sessao;
            const nova = atual && atual !== usada ? of(atual) : this.renovarSessao();
            // Repetir uma vez apenas; erros na repetição não entram neste catch.
            return nova.pipe(switchMap(executar));
          }),
        )),
        catchError((falha: HttpErrorResponse) => {
          if (falha.status === 401 && geracao === this.geracaoSessao) this.definirSessao(null);
          return throwError(() => falha);
        }),
      );
    });
  }

  listarProjetos(workspaceId: string): Observable<HttpResponse<Pagina<Projeto>>> {
    return this.autenticada((opcoes) => this.http.get<Pagina<Projeto>>(`${BASE}/projetos`, {
      params: { workspaceId, size: 100 },
      observe: 'response',
      ...opcoes,
    }));
  }

  criarProjeto(projeto: NovoProjeto): Observable<Projeto> {
    return this.autenticada((opcoes) => this.http.post<Projeto>(`${BASE}/projetos`, projeto, opcoes));
  }

  listarRequisitos(projetoId: string, situacao = 'ATIVO'): Observable<HttpResponse<Pagina<Requisito>>> {
    return this.autenticada((opcoes) => this.http.get<Pagina<Requisito>>(`${BASE}/projetos/${projetoId}/requisitos`, {
      params: { size: 100, situacao },
      observe: 'response',
      ...opcoes,
    }));
  }

  listarRelacoes(projetoId: string): Observable<HttpResponse<Pagina<Relacao>>> {
    return this.autenticada((opcoes) => this.http.get<Pagina<Relacao>>(`${BASE}/projetos/${projetoId}/relacoes`, {
      params: { size: 100 },
      observe: 'response',
      ...opcoes,
    }));
  }

  criarRelacao(projetoId: string, relacao: NovaRelacao): Observable<Relacao> {
    return this.autenticada((opcoes) => this.http.post<Relacao>(`${BASE}/projetos/${projetoId}/relacoes`, relacao, opcoes));
  }

  obterRequisito(projetoId: string, requisitoId: string): Observable<RequisitoGestao> {
    return this.autenticada((opcoes) => this.http.get<RequisitoGestao>(`${BASE}/projetos/${projetoId}/requisitos/${requisitoId}`, opcoes));
  }

  criarRequisito(projetoId: string, requisito: NovoRequisito): Observable<Requisito> {
    return this.autenticada((opcoes) => this.http.post<Requisito>(`${BASE}/projetos/${projetoId}/requisitos`, requisito, opcoes));
  }
}
