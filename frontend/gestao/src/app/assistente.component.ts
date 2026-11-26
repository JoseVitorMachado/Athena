import { Component, OnChanges, OnDestroy, SimpleChanges, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { GestaoApi, Requisito, RespostaAssistente } from './gestao-api';

interface Mensagem {
  pergunta: string;
  resultado?: RespostaAssistente;
  erro?: string;
}

@Component({
  selector: 'app-assistente',
  imports: [FormsModule],
  templateUrl: './assistente.component.html',
  styleUrl: './assistente.component.css',
})
export class AssistenteComponent implements OnChanges, OnDestroy {
  readonly projetoId = input.required<string>();
  readonly requisitos = input<Requisito[]>([]);
  readonly abrirRequisito = output<string>();

  private readonly api = inject(GestaoApi);
  private pedido?: Subscription;
  private relogio?: ReturnType<typeof setInterval>;

  protected readonly aberto = signal(false);
  protected readonly pergunta = signal('');
  protected readonly segundos = signal(0);
  protected readonly carregando = signal(false);
  protected readonly mensagens = signal<Mensagem[]>([]);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['projetoId']) this.limpar();
  }

  ngOnDestroy(): void {
    this.limpar();
  }

  protected enviar(): void {
    const pergunta = this.pergunta().trim();
    if (!pergunta || pergunta.length > 8000 || this.carregando()) return;
    this.pergunta.set('');
    this.carregando.set(true);
    this.segundos.set(0);
    this.mensagens.update(lista => [...lista, { pergunta }]);
    const inicio = Date.now();
    this.relogio = setInterval(() => this.segundos.set(Math.floor((Date.now() - inicio) / 1000)), 1000);
    this.pedido = this.api.perguntarAssistente(this.projetoId(), pergunta).subscribe({
      next: resultado => this.concluir({ pergunta, resultado }),
      error: (falha: HttpErrorResponse) => this.concluir({ pergunta, erro: this.descrever(falha.status) }),
    });
  }

  protected tecla(evento: KeyboardEvent): void {
    if (evento.key === 'Enter' && !evento.shiftKey && !evento.isComposing) {
      evento.preventDefault();
      this.enviar();
    }
  }

  protected temRequisito(id: string): boolean {
    return this.requisitos().some(requisito => requisito.requisitoId === id);
  }

  protected abrirFonte(id: string): void {
    this.aberto.set(false);
    this.abrirRequisito.emit(id);
  }

  /** Substitui a última mensagem (a que aguardava resposta) e encerra o carregamento. */
  private concluir(mensagem: Mensagem): void {
    clearInterval(this.relogio);
    this.mensagens.update(lista => [...lista.slice(0, -1), mensagem]);
    this.carregando.set(false);
  }

  private descrever(status: number): string {
    if (status === 429) return 'O assistente está ocupado. Tente novamente em instantes.';
    if (status === 403) return 'Você não tem acesso a esta consulta.';
    if (status === 503 || status === 504) return 'O assistente está indisponível. Tente novamente em instantes.';
    return 'Não foi possível consultar o assistente. Tente novamente.';
  }

  private limpar(): void {
    this.pedido?.unsubscribe();
    clearInterval(this.relogio);
    this.mensagens.set([]);
    this.pergunta.set('');
    this.carregando.set(false);
    this.aberto.set(false);
  }
}
