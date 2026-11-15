import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

/**
 * Cliente do BFF Gestão. O caminho é sempre relativo à origem da página: o
 * navegador só conhece uma origem, e quem encaminha /api para o Gateway é o
 * nginx (compose) ou o Ingress (Kubernetes).
 */
const BASE = '/api/v1/gestao';

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
  situacao: string;
  candidata?: Versao;
  atualizadoEm: string;
}

export interface Pagina<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
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
  titulo: string;
  conteudo: string;
  criteriosAceite: string[];
}

@Injectable({ providedIn: 'root' })
export class GestaoApi {
  private readonly http = inject(HttpClient);

  listarRequisitos(projetoId: string): Observable<HttpResponse<Pagina<Requisito>>> {
    return this.http.get<Pagina<Requisito>>(`${BASE}/projetos/${projetoId}/requisitos`, {
      params: { size: 50 },
      observe: 'response',
    });
  }

  obterRequisito(projetoId: string, requisitoId: string): Observable<RequisitoGestao> {
    return this.http.get<RequisitoGestao>(`${BASE}/projetos/${projetoId}/requisitos/${requisitoId}`);
  }

  criarRequisito(projetoId: string, requisito: NovoRequisito): Observable<Requisito> {
    return this.http.post<Requisito>(`${BASE}/projetos/${projetoId}/requisitos`, requisito);
  }
}
