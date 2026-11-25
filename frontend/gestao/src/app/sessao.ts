import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, map, Observable, of, shareReplay, tap } from 'rxjs';

export interface Sessao {
  accessToken: string;
  tokenType: 'Bearer';
  expiraEm: string;
  csrfToken: string;
}

const STORAGE_KEY = 'athena.sessao';

@Injectable({ providedIn: 'root' })
export class SessaoService {
  private readonly http = inject(HttpClient);
  private refreshEmAndamento: Observable<boolean> | null = null;

  ler(): Sessao | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return null;
      const sessao = JSON.parse(raw) as Sessao;
      if (!sessao.accessToken || !sessao.csrfToken) return null;
      return sessao;
    } catch {
      return null;
    }
  }

  salvar(sessao: Sessao): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(sessao));
  }

  limpar(): void {
    localStorage.removeItem(STORAGE_KEY);
  }

  renovar(): Observable<boolean> {
    if (!this.refreshEmAndamento) {
      const csrfToken = this.ler()?.csrfToken ?? '';
      this.refreshEmAndamento = this.http.post<Sessao>('/api/v1/autenticacao/renovacao', null, {
        withCredentials: true,
        headers: csrfToken ? { 'X-CSRF-Token': csrfToken } : {},
      }).pipe(
        tap(sessao => this.salvar(sessao)),
        map(() => true),
        catchError(() => { this.limpar(); window.dispatchEvent(new Event('athena:session-expired')); return of(false); }),
        shareReplay({ bufferSize: 1, refCount: false }),
      );
    }
    return this.refreshEmAndamento;
  }

  encerrar(): Observable<void> {
    const csrfToken = this.ler()?.csrfToken ?? '';
    return this.http.post<void>('/api/v1/autenticacao/logout', null, {
      withCredentials: true,
      headers: csrfToken ? { 'X-CSRF-Token': csrfToken } : {},
    }).pipe(tap(() => this.limpar()), catchError(() => { this.limpar(); return of(undefined); }));
  }

  concluirRenovacao(): void {
    this.refreshEmAndamento = null;
  }
}
