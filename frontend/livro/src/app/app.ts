import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CABECALHO_INSTANCIA, LivroApi, Referencia, RequisitoLivro } from './livro-api';

const PROJETO_DEMO = '22222222-2222-4222-8222-222222222222';

/** Leitura da relação a partir do requisito exibido (livro §5.4). */
const ROTULOS: Record<string, string> = {
  DEPENDE_DE: 'Depende de / é pré-requisito de',
  BLOQUEIA: 'Bloqueia / é bloqueado por',
  RELACIONADO_A: 'Relacionado a',
};

@Component({
  imports: [FormsModule],
  selector: 'app-root',
  templateUrl: './app.html',
})
export class App {
  private readonly api = inject(LivroApi);

  protected readonly requisitos = signal<RequisitoLivro[]>([]);
  protected readonly carregando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly instancia = signal<string | null>(null);
  protected readonly pagina = signal<'inicio' | 'busca' | 'requisito'>('inicio');
  protected readonly consulta = signal('');
  protected readonly termoAplicado = signal('');
  protected readonly epico = signal('Todos');
  protected readonly ordenacao = signal('Relevância');
  protected readonly selecionado = signal<RequisitoLivro | null>(null);
  protected readonly copiado = signal(false);
  protected readonly resultados = computed(() => {
    const termo = this.termoAplicado().trim().toLocaleLowerCase('pt-BR');
    let items = this.requisitos().filter((item) => {
      const byTerm =
        !termo ||
        `${item.codigo} ${item.titulo} ${item.conteudo} ${item.criteriosAceite.join(' ')}`
          .toLocaleLowerCase('pt-BR')
          .includes(termo);
      const byEpic = this.epico() === 'Todos' || this.epicoFor(item) === this.epico();
      return byTerm && byEpic;
    });
    if (this.ordenacao() === 'Título')
      items = [...items].sort((a, b) => a.titulo.localeCompare(b.titulo, 'pt-BR'));
    return items;
  });

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
        this.erro.set(
          `${problema?.detail ?? problema?.title ?? 'Livro indisponível.'} (HTTP ${falha.status})`,
        );
        this.carregando.set(false);
      },
    });
  }

  protected rotulo(referencia: Referencia): string {
    return referencia.disponivel ? (ROTULOS[referencia.tipo] ?? referencia.tipo) : '';
  }

  protected buscar(): void {
    this.termoAplicado.set(this.consulta());
    this.pagina.set('busca');
    this.selecionado.set(null);
  }

  protected abrir(requisito: RequisitoLivro): void {
    this.selecionado.set(requisito);
    this.pagina.set('requisito');
  }

  protected async copiarLink(): Promise<void> {
    try {
      await navigator.clipboard.writeText(window.location.href);
    } catch {
      /* clipboard may be unavailable in non-secure contexts */
    }
    this.copiado.set(true);
    window.setTimeout(() => this.copiado.set(false), 1800);
  }

  protected epicoFor(item: RequisitoLivro): string {
    if (/US-00[123]/.test(item.codigo)) return 'Autenticação e Conta';
    if (/US-00[45]/.test(item.codigo)) return 'Organização de projetos';
    if (/US-00[89]|US-010/.test(item.codigo)) return 'Editor de Requisitos';
    if (/US-016|US-017/.test(item.codigo)) return 'Rastreabilidade';
    if (/US-028|US-030/.test(item.codigo)) return 'Revisão e Publicação';
    if (/US-019|US-022/.test(item.codigo)) return 'Integrações';
    return 'Plataforma Athena';
  }
}
