import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

/** Caminho relativo à origem: o nginx (ou o Ingress) encaminha /api ao Gateway. */
const BASE = '/api/v1/livro';

export const CABECALHO_INSTANCIA = 'X-Athena-Instancia';

export type Referencia =
  | { disponivel: true; requisitoId: string; codigo: string; titulo: string; cancelado: boolean; tipo: string }
  | { disponivel: false };

export interface RequisitoLivro {
  requisitoId: string;
  codigo: string;
  titulo: string;
  versaoPublicada: number;
  conteudo: string;
  criteriosAceite: string[];
  referencias: Referencia[];
  avisos: string[];
}

export interface Pagina<T> {
  items: T[];
  totalElements: number;
}

@Injectable({ providedIn: 'root' })
export class LivroApi {
  private readonly http = inject(HttpClient);

  listar(projetoId: string): Observable<HttpResponse<Pagina<RequisitoLivro>>> {
    return this.http.get<Pagina<RequisitoLivro>>(`${BASE}/projetos/${projetoId}/livro/requisitos`, {
      params: { size: 100 },
      observe: 'response',
    });
  }
}
