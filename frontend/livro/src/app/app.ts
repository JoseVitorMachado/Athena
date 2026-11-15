import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { CABECALHO_INSTANCIA, LivroApi, Referencia, RequisitoLivro } from './livro-api';

const PROJETO_DEMO = '22222222-2222-4222-8222-222222222222';

/** Leitura da relação a partir do requisito exibido (livro §5.4). */
const ROTULOS: Record<string, string> = {
  DEPENDE_DE: 'Depende de / é pré-requisito de',
  BLOQUEIA: 'Bloqueia / é bloqueado por',
  RELACIONADO_A: 'Relacionado a',
};

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
})
export class App {
  private readonly api = inject(LivroApi);

  protected readonly requisitos = signal<RequisitoLivro[]>([]);
  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly instancia = signal<string | null>(null);

  constructor() {
    this.carregar();
  }

  protected carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.api.listar(PROJETO_DEMO).subscribe({
      next: (resposta) => {
        this.requisitos.set(resposta.body?.items ?? []);
        this.instancia.set(resposta.headers.get(CABECALHO_INSTANCIA));
        this.carregando.set(false);
      },
      error: (falha: HttpErrorResponse) => {
        const problema = falha.error as { detail?: string; title?: string } | null;
        this.erro.set(`${problema?.detail ?? problema?.title ?? 'Livro indisponível.'} (HTTP ${falha.status})`);
        this.carregando.set(false);
      },
    });
  }

  protected rotulo(referencia: Referencia): string {
    return referencia.disponivel ? (ROTULOS[referencia.tipo] ?? referencia.tipo) : '';
  }
}
