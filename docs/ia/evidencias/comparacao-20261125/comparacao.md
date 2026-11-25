# Comparação com e sem RAG

Execuções reais; avaliação humana ainda pendente. Falhas/degradações não equivalem a respostas do LLM.

| Pergunta | Modo | Estado | Resposta | Fontes | MCP ms | LLM ms | Total ms | USD referência |
|---|---|---|---|---|---:|---:|---:|---:|
| P1 | sem_rag | ok | Nenhuma funcionalidade de exportação está descrita na versão publicada do REQ-1. | [] | 0 | 4031.59 | 4038.68 | 0.0003372 |
| P1 | com_rag | ok | A funcionalidade de exportação descrita na versão publicada do REQ-1 é a exportação de documento de requisitos. | ["E1"] | 24.79 | 6429.85 | 6610.32 | 0.00044 |
| P2 | com_rag | ok | Nenhuma garantia de formato de arquivo exportável está presente na versão publicada de REQ-1. | ["E1", "E2"] | 29.66 | 6063.29 | 6115.96 | 0.000414 |
| P2 | sem_rag | ok | Não há evidências para identificar formatos de arquivo garantidos pela versão publicada do REQ-1. | [] | 0 | 3966.59 | 3970.16 | 0.0003448 |
| P3 | sem_rag | ok | REQ-3 não estabelece limite de tempo ou percentil para consultas comuns. | [] | 0 | 4502.67 | 4506.22 | 0.0003524 |
| P3 | com_rag | ok | REQ-3 estabelece um limite de tempo de até 1 segundo para consultas comuns e um percentil p95 de até 1 segundo. | ["E1"] | 26.02 | 6191.06 | 6240.02 | 0.0004344 |
| P4 | com_rag | ok | REQ-3 exige 20 usuários simultâneos no cenário de medição. | ["E1"] | 26.77 | 6986.46 | 7044.71 | 0.000434 |
| P4 | sem_rag | ok | REQ-3 não menciona quantos usuários simultâneos são necessários. | [] | 0 | 3766.85 | 3770.6 | 0.0003408 |
| P5 | sem_rag | ok | O conteúdo publicado não menciona o tempo máximo para gerar um PDF de 500 páginas. | [] | 0 | 3944.98 | 3948.9 | 0.0003364 |
| P5 | com_rag | ok | REQ-3 não menciona tempo de geração de PDF. | ["E1"] | 27.94 | 5066.39 | 5118.34 | 0.000396 |

O CSV e os JSONs preservam os trechos, perguntas, justificativas, versões e métricas completas.
Rubrica humana: correção (0–2), apoio nas fontes (0–2), reconhecimento de informação ausente (0–2).
Compare também citações inventadas e cobertura. Não use presença de palavras-chave como prova de qualidade.
Custo é uma simulação de referência, não gasto efetivo. null indica consumo incompleto em alguma tentativa.
