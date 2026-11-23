import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

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
  private accessToken: string | null = null;

  autenticar(credenciais: CredenciaisLogin): Observable<Sessao> {
    return this.http.post<Sessao>(`${AUTH_BASE}/login`, credenciais, { withCredentials: true });
  }

  definirSessao(sessao: Sessao | null): void {
    this.accessToken = sessao?.accessToken ?? null;
  }

  private opcoesAutenticadas(): { headers?: Record<string, string> } {
    return this.accessToken ? { headers: { Authorization: `Bearer ${this.accessToken}` } } : {};
  }

  listarProjetos(workspaceId: string): Observable<HttpResponse<Pagina<Projeto>>> {
    return this.http.get<Pagina<Projeto>>(`${BASE}/projetos`, {
      params: { workspaceId, size: 100 },
      observe: 'response',
      ...this.opcoesAutenticadas(),
    });
  }

  criarProjeto(projeto: NovoProjeto): Observable<Projeto> {
    return this.http.post<Projeto>(`${BASE}/projetos`, projeto, this.opcoesAutenticadas());
  }

  listarRequisitos(projetoId: string, situacao = 'ATIVO'): Observable<HttpResponse<Pagina<Requisito>>> {
    return this.http.get<Pagina<Requisito>>(`${BASE}/projetos/${projetoId}/requisitos`, {
      params: { size: 100, situacao },
      observe: 'response',
      ...this.opcoesAutenticadas(),
    });
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

  obterRequisito(projetoId: string, requisitoId: string): Observable<RequisitoGestao> {
    return this.http.get<RequisitoGestao>(`${BASE}/projetos/${projetoId}/requisitos/${requisitoId}`, this.opcoesAutenticadas());
  }

  criarRequisito(projetoId: string, requisito: NovoRequisito): Observable<Requisito> {
    return this.http.post<Requisito>(`${BASE}/projetos/${projetoId}/requisitos`, requisito, this.opcoesAutenticadas());
  }
}
