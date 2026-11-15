import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CABECALHO_INSTANCIA, GestaoApi, Requisito, RequisitoGestao } from './gestao-api';

/** Projeto e documento dos dados de demonstração dos serviços. */
const PROJETO_DEMO = '22222222-2222-4222-8222-222222222222';
const DOCUMENTO_DEMO = '44444444-4444-4444-8444-444444444444';

@Component({
  imports: [DatePipe, FormsModule],
  selector: 'app-root',
  templateUrl: './app.html',
})
export class App {
  private readonly api = inject(GestaoApi);

  protected readonly projetoId = signal(PROJETO_DEMO);
  protected readonly requisitos = signal<Requisito[]>([]);
  protected readonly total = signal(0);
  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly instancia = signal<string | null>(null);
  protected readonly detalhe = signal<RequisitoGestao | null>(null);

  protected readonly titulo = signal('');
  protected readonly conteudo = signal('');
  protected readonly criterios = signal('');
  protected readonly salvando = signal(false);

  constructor() {
    this.carregar();
  }

  protected carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.api.listarRequisitos(this.projetoId()).subscribe({
      next: (resposta) => {
        this.requisitos.set(resposta.body?.items ?? []);
        this.total.set(resposta.body?.totalElements ?? 0);
        this.instancia.set(resposta.headers.get(CABECALHO_INSTANCIA));
        this.carregando.set(false);
      },
      error: (falha: HttpErrorResponse) => {
        this.erro.set(this.descrever(falha));
        this.carregando.set(false);
      },
    });
  }

  protected abrir(requisito: Requisito): void {
    this.api.obterRequisito(this.projetoId(), requisito.requisitoId).subscribe({
      next: (visao) => this.detalhe.set(visao),
      error: (falha: HttpErrorResponse) => this.erro.set(this.descrever(falha)),
    });
  }

  protected criar(): void {
    this.salvando.set(true);
    this.erro.set(null);
    const criteriosAceite = this.criterios()
      .split('\n')
      .map((linha) => linha.trim())
      .filter((linha) => linha.length > 0);
    this.api
      .criarRequisito(this.projetoId(), {
        documentoId: DOCUMENTO_DEMO,
        titulo: this.titulo().trim(),
        conteudo: this.conteudo().trim(),
        criteriosAceite,
      })
      .subscribe({
        next: () => {
          this.titulo.set('');
          this.conteudo.set('');
          this.criterios.set('');
          this.salvando.set(false);
          this.carregar();
        },
        error: (falha: HttpErrorResponse) => {
          this.erro.set(this.descrever(falha));
          this.salvando.set(false);
        },
      });
  }

  protected podeCriar(): boolean {
    return this.titulo().trim().length > 0 && this.conteudo().trim().length > 0 && !this.salvando();
  }

  /** Os erros da API seguem Problem Details (RFC 9457): usa detail ou title quando existem. */
  private descrever(falha: HttpErrorResponse): string {
    const problema = falha.error as { detail?: string; title?: string } | null;
    const texto = problema?.detail ?? problema?.title ?? 'Não foi possível falar com o servidor.';
    return `${texto} (HTTP ${falha.status})`;
  }
}
