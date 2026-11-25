import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { EMPTY, Observable, expand, from, mergeMap, reduce, switchMap } from 'rxjs';

const LIVRO = '/api/v1/livro';
const GESTAO = '/api/v1/gestao';

export type Referencia =
  | { disponivel: true; requisitoId: string; codigo: string; titulo: string; cancelado: boolean; tipo: string; direcao?: 'ENTRADA' | 'SAIDA' }
  | { disponivel: false };

export interface RequisitoLivro {
  requisitoId: string;
  codigo: string;
  titulo: string;
  epicoId?: string;
  publicadoEm?: string;
  versaoPublicada: number;
  conteudo: string;
  criteriosAceite: string[];
  referencias: Referencia[];
  avisos: string[];
}

export interface ProjetoLivro { projetoId: string; nome: string; workspaceNome: string }
export interface EpicoLivro { epicoId: string; titulo: string }

interface Pagina<T> { items: T[]; totalPages: number }

@Injectable({ providedIn: 'root' })
export class LivroApi {
  private readonly http = inject(HttpClient);

  listar(projetoId: string): Observable<RequisitoLivro[]> {
    return this.listarPaginas<RequisitoLivro>(this.colecao(projetoId), {}, 20);
  }

  obter(projetoId: string, requisitoId: string): Observable<RequisitoLivro> {
    return this.http.get<RequisitoLivro>(`${this.colecao(projetoId)}/${requisitoId}`);
  }

  epicos(projetoId: string): Observable<EpicoLivro[]> {
    return this.listarPaginas<EpicoLivro>(`${GESTAO}/projetos/${projetoId}/epicos`);
  }

  projetos(): Observable<ProjetoLivro[]> {
    return this.listarPaginas<{ workspaceId: string; nome: string }>(`${GESTAO}/workspaces`).pipe(
      switchMap(workspaces => from(workspaces).pipe(
        mergeMap(workspace => this.listarPaginas<{ projetoId: string; nome: string }>(
          `${GESTAO}/projetos`, { workspaceId: workspace.workspaceId, arquivado: 'false' },
        ).pipe(
          mergeMap(projetos => from(projetos.map(projeto => ({ ...projeto, workspaceNome: workspace.nome })))),
        ), 4),
        reduce((projetos, projeto) => [...projetos, projeto], [] as ProjetoLivro[]),
      )),
    );
  }

  private colecao(projetoId: string): string {
    return `${LIVRO}/projetos/${projetoId}/livro/requisitos`;
  }

  private listarPaginas<T>(url: string, filtros: Record<string, string> = {}, size = 100): Observable<T[]> {
    const carregar = (page: number) => this.http.get<Pagina<T>>(url, { params: { ...filtros, page, size } });
    return carregar(0).pipe(
      expand((pagina, indice) => indice + 1 < pagina.totalPages ? carregar(indice + 1) : EMPTY),
      reduce((itens, pagina) => itens.concat(pagina.items), [] as T[]),
    );
  }
}
