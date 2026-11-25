import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { SessaoService } from './sessao';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const sessaoService = inject(SessaoService);
  const sessao = sessaoService.ler();
  const comToken = sessao && request.url.startsWith('/api/v1/') && !request.url.startsWith('/api/v1/autenticacao/') && !request.headers.has('Authorization')
    ? request.clone({ setHeaders: { Authorization: `Bearer ${sessao.accessToken}` } })
    : request;

  return next(comToken).pipe(catchError((erro: unknown) => {
    if (!(erro instanceof HttpErrorResponse) || erro.status !== 401 || request.url.includes('/api/v1/autenticacao/')) {
      return throwError(() => erro);
    }
    return sessaoService.renovar().pipe(
      switchMap(ok => {
        sessaoService.concluirRenovacao();
        if (!ok) return throwError(() => erro);
        const renovada = sessaoService.ler();
        return next(request.clone({ setHeaders: { Authorization: `Bearer ${renovada?.accessToken ?? ''}` } }));
      }),
      catchError(falha => { sessaoService.concluirRenovacao(); return throwError(() => falha); }),
    );
  }));
};
