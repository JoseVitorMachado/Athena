import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { SessaoService } from './sessao';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(SessaoService);
  const sessao = auth.ler();
  const requestAuth = sessao && request.url.startsWith('/api/v1/') && !request.url.startsWith('/api/v1/autenticacao/') && !request.headers.has('Authorization')
    ? request.clone({ setHeaders: { Authorization: `Bearer ${sessao.accessToken}` } }) : request;
  return next(requestAuth).pipe(catchError((erro: unknown) => {
    if (!(erro instanceof HttpErrorResponse) || erro.status !== 401 || request.url.includes('/api/v1/autenticacao/')) return throwError(() => erro);
    return auth.renovar().pipe(switchMap(ok => {
      auth.concluirRenovacao();
      if (!ok) return throwError(() => erro);
      const token = auth.ler()?.accessToken;
      return next(request.clone({ setHeaders: token ? { Authorization: `Bearer ${token}` } : {} }));
    }), catchError(falha => { auth.concluirRenovacao(); return throwError(() => falha); }));
  }));
};
