# Demonstração de IA — execução local de 25/11/2026

Este é o relatório histórico da primeira rodada, com dois requisitos publicados.
A [avaliação ampliada e os refinamentos](avaliacao-v2.md) registram o estado mais
recente: dez publicados, doze perguntas, controles negativos e ficha de revisão humana.

Foi implementado o cliente Python com chain LangChain fixa, tool MCP real,
análise de impacto, comparação com/sem RAG, timeout/retry, fallback e métricas.
O guia de execução está em [clientes/agente-ia/README.md](../../clientes/agente-ia/README.md).

Este relatório analisa execuções reais no projeto de demonstração. A análise
qualitativa é assistida e deve ser revisada pelo grupo; a coluna de avaliação
humana nos resultados permanece `PENDENTE`.

## Ambiente e método

- Python 3.12.12; LangChain 1.5.2; langchain-ollama 1.2.0; Ollama 0.40.18.
- Qwen `qwen2.5:3b-instruct-q4_K_M`, manifesto
  `357c53fb659c5076de1d65ccb0b397446227b71a42be9d1603d46168015c9e4b`.
- AMD Ryzen 5 5600GT, 12 CPUs lógicas; aproximadamente 13 GiB de memória total
  visível ao sistema operacional. Ollama confirmou **100% CPU**, contexto 4096, modelo
  carregado com cerca de 2,2 GB. Isso não é um requisito mínimo universal.
- ATHENA completo em Docker Compose, com os serviços de domínio em execução.
- Temperatura 0, seed 42, saída máxima 512 tokens, timeout 20 segundos por tentativa,
  até duas tentativas totais e backoff inicial de 2 segundos.
- Uma execução por pergunta/modo, com ordem alternada; modelo aquecido antes da
  comparação. O modo sem RAG não recebe dados da API nem do MCP. Gabaritos não
  entram no prompt. Não há cache de respostas no cliente; a reutilização de
  prefixos pelo Ollama pode influenciar o tempo.
- Base pequena: apenas REQ-1 e REQ-3 publicados. Cinco perguntas não são cinco
  documentos. Os resultados não constituem benchmark geral nem SLA.

## Comparação das dez respostas

Dados completos: [tabela Markdown](evidencias/comparacao-20261125/comparacao.md),
[CSV](evidencias/comparacao-20261125/comparacao.csv) e os dez JSONs na mesma pasta.
Todas as dez operações terminaram com `estado=ok`; as cinco com RAG citaram fontes
recuperadas. Isso demonstra sucesso operacional e referências existentes, não
garante sozinho correção semântica.

| Pergunta | Sem RAG | Com RAG | Análise assistida |
|---|---|---|---|
| P1 — funcionalidade publicada de exportação | Negou que a versão descrevesse exportação. | Identificou exportação do documento e citou REQ-1. | RAG recuperou o fato; a resposta ainda omitiu o critério de preservação dos requisitos. |
| P2 — formatos garantidos na publicada | Reconheceu falta de evidências. | Informou ausência de formatos especificados e citou os trechos. | Não confundiu candidata PDF/Markdown com a publicada. A citação adicional de REQ-3 é pouco útil. |
| P3 — tempo e percentil | Negou que REQ-3 estabelecesse limite ou percentil. | Respondeu até 1 segundo, p95, com REQ-3. | RAG corrigiu uma negativa sem fundamento. A formulação pode ser mais direta: p95 ≤ 1 s. |
| P4 — usuários simultâneos | Afirmou que REQ-3 não mencionava a quantidade. | Respondeu 20 usuários e citou REQ-3. | O dado recuperado sustentou a resposta. |
| P5 — PDF de 500 páginas | Afirmou ausência de prazo sem conhecer o conteúdo. | Distinguiu a meta de consultas de um prazo de geração de PDF. | A conclusão é compatível com a base. Sem RAG, afirmar ausência no documento também é uma afirmação não fundamentada. |

Os resultados mostram ganho de acesso a fatos próprios do projeto nas três
perguntas factuais. Também mostram por que apenas pedir ao modelo para admitir
falta de dados é insuficiente: no modo sem RAG houve negativas falsas. As perguntas
P2/P5 avaliam ausência de informação, mas não ampliam a diversidade do corpus.

## Latência e custo de referência

Médias das cinco operações de cada modo, em milissegundos. API inclui autenticação
e inicialização MCP; LLM inclui geração, tentativas e espera, se houver. O total
também inclui montagem e validação. Não houve retry nesta comparação final.

| Modo | API | MCP | LLM | Total | Intervalo total | Tokens entrada / saída |
|---|---:|---:|---:|---:|---:|---:|
| Com RAG | 46,76 | 27,04 | 6.147,41 | 6.225,87 | 5.118,34–7.044,71 | 744,8 / 78,6 |
| Sem RAG | 0 | 0 | 4.042,54 | 4.046,91 | 3.770,60–4.506,22 | 587,8 / 67,0 |

Simulação com preços históricos publicados de GPT-4.1 mini: US$ 0,40 por milhão
de tokens de entrada e US$ 1,60 por milhão de saída. Fonte e ressalvas de
tokenização estão no guia do cliente. O LLM executado é local e não houve chamada
ou cobrança dessa API. Energia e hardware não foram medidos.

| Operação | USD / operação | USD / 1.000 operações | USD / 10.000 operações |
|---|---:|---:|---:|
| Pergunta com RAG, média | 0,00042368 | 0,42368 | 4,23680 |
| Pergunta sem RAG, média | 0,00034232 | 0,34232 | 3,42320 |
| Impacto do REQ-1, uma amostra | 0,00077200 | 0,77200 | 7,72000 |

A projeção de custo assume a mesma distribuição de tokens. Não estima throughput
ou latência concorrente. Cinco amostras por modo não sustentam um p95 de produção.

## Análise de impacto

[Resultado completo](evidencias/impacto.json): REQ-1 v1 publicada comparada à
candidata v2 (PDF e Markdown), com mapa e conteúdo real de REQ-2/REQ-3. A tool MCP
também foi chamada; trechos de versões já obtidas integralmente pela API foram
deduplicados antes do prompt, preservando os metadados na resposta.

O modelo sugeriu revisão de REQ-2 pela dependência da exportação em relação às
relações registradas, e de REQ-3 pela meta de tempo associada à exportação. Cada
item cita uma fonte do alvo e fica `PENDENTE_HUMANO`. Nenhuma relação foi criada,
nenhuma revisão foi decidida e nenhuma versão foi publicada.

Esta execução durou 10.131,74 ms: API 253,58 ms, MCP 32,73 ms e LLM 9.837,32 ms;
1.318 tokens de entrada e 153 de saída. As justificativas ainda são curtas e
próximas das descrições do mapa; o grupo deve detalhar, por exemplo, como preservar
relações em cada formato e como medir seu efeito sobre o desempenho.

## Falhas induzidas e recuperação

| Ensaio | Resultado observado | Evidência |
|---|---|---|
| Parar somente Ollama | Duas tentativas, backoff de 2 s, fallback com dois trechos, cerca de 2,16 s no total. | [falha-ollama.json](evidencias/falha-ollama.json) |
| Religar e aquecer Ollama | Voltou a responder, uma tentativa, cerca de 9,60 s. | [recuperacao.json](evidencias/recuperacao.json) |
| Timeout de 0,001 s | Duas tentativas, fallback com dois trechos, cerca de 2,14 s. | [timeout.json](evidencias/timeout.json) |

Transcrições: [parada e recuperação](evidencias/athena-demo-falha.log) e
[timeout](evidencias/athena-timeout.log). O modelo foi religado ao fim do ensaio.
Nas tentativas interrompidas, consumo total é desconhecido: o custo de referência
fica `null`, não zero. A resposta degradada exibe evidências sem fabricar análise.

## Verificação e limitações

- **16 testes automatizados passaram**, incluindo refresh/CSRF, sessão expirada,
  HTTP 401, erro MCP em HTTP 200, ausência de recuperação no baseline, timeout,
  backoff, fallback, tokens, gramática de fontes, impacto e geração dos relatórios.
- Login, renovação e MCP reais funcionaram; um projeto sem acesso devolveu
  `-32003`: [evidência do backend](evidencias/integracao-backend.json).
- O Compose completo passou pelo `--wait`, com 23 serviços em execução e a tarefa
  de modelo terminada com sucesso: [estado final](evidencias/servicos-finais.txt).
- Um ensaio inicial retornou resumo de impacto sem itens estruturados:
  [diagnóstico anterior à correção](evidencias/diagnostico-impacto-inicial.json).
  O cliente passou a validar consistência do resumo, restringir referências por
  alvo e rejeitar respostas com contexto que omitam citações. O diagnóstico não
  representa a implementação final.
- O MVP não implementa o extra de aprovação/publicação assistida nem OAuth do
  agente. Mantém o JWT do usuário, como o backend atual.
- Confirmar com o professor a aceitação do cliente externo. Ampliar a base
  publicada com o colega e repetir a avaliação antes da apresentação.
