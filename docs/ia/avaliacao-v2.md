# Avaliação ampliada de IA — 25/11/2026

Esta rodada implementa os três refinamentos: análise do delta com justificativa
por alvo, ampliação dos dados publicados e avaliação acompanhada de rubrica humana.
A revisão qualitativa abaixo foi feita com auxílio do Codex; **a revisão humana
do grupo permanece pendente**. Nenhuma sugestão do LLM foi aplicada ao domínio.

## Dados e execução

O projeto local de demonstração passou de dois para **dez requisitos publicados**.
Os oito novos são fixtures fictícias identificadas por `[IA-V2-...]`, não uma
declaração de que suas funcionalidades foram implementadas no ATHENA. A carga
passou pelas APIs de criação, relações, submissão, revisão e publicação, aguardou
as transições assíncronas e verificou a indexação pelo MCP. Foi repetida sem criar
duplicatas. Os três requisitos originais foram preservados.

[Manifesto](evidencias/v2/dataset/manifesto.json),
[verificação do backend](evidencias/v2/dataset/integracao.json),
[perguntas](evidencias/v2/dataset/perguntas.json) e
[cenários de impacto](evidencias/v2/dataset/impactos.json).

| Código nesta execução | Conteúdo da fixture |
|---|---|
| REQ-4 | Preservar códigos e relações em todos os formatos de exportação |
| REQ-5 | Validar vínculo ativo na solicitação e no download |
| REQ-6 | Registrar usuário, projeto, horário, formato e versões na auditoria |
| REQ-7 | Prévia textual independente do formato; candidata só muda o título |
| REQ-8 | Notificar apenas depois da conclusão da exportação |
| REQ-9 | Recuperação de senha independente da exportação |
| REQ-10 | Tema individual sem efeito nos arquivos exportados |
| REQ-11 | Filtros sem alteração dos dados; candidata igual à publicada |

REQ-1 e REQ-3 continuam publicados; REQ-2 continua disponível pela API/mapa,
mas fora do índice RAG. Os códigos novos são alocados pelo backend; em outro
banco, use o manifesto em vez de presumir os mesmos números.

O modelo continua `qwen2.5:3b-instruct-q4_K_M`, manifesto
`357c53fb659c5076de1d65ccb0b397446227b71a42be9d1603d46168015c9e4b`,
executado localmente em CPU pelo Ollama. Temperatura 0, seed 42, contexto 4096,
saída máxima 512 tokens, timeout 20 s por tentativa e até duas tentativas.
Os cenários de impacto precederam a comparação de perguntas, mantendo o modelo
carregado. Há uma amostra por pergunta/modo, com ordem alternada. O modo sem RAG
não lê API/MCP; gabaritos não são enviados ao modelo. Os prompts de impacto foram
refinados usando estes controles: este conjunto não é uma validação independente
nem permite estimar qualidade geral, p95 ou desempenho concorrente em produção.

## Perguntas com e sem RAG

[Tabela completa](evidencias/v2/perguntas/comparacao.md),
[CSV](evidencias/v2/perguntas/comparacao.csv) e JSONs por operação na mesma pasta.

As **24 operações terminaram com geração válida**, uma tentativa cada. Nos onze
casos com uma fonte esperada identificada, o MCP recuperou essa fonte: 11/11.
P12 avalia informação ausente e não tem fonte esperada específica. As referências
estruturadas das respostas com RAG resolvem para fontes recuperadas. Esses são
resultados operacionais/de recuperação; não são uma taxa de acerto semântico.

| Caso | Com RAG — análise assistida | Sem RAG — análise assistida |
|---|---|---|
| P1 — exportação e preservação | Respondeu as duas partes, incluindo preservar requisitos. | A resposta sugere falta de especificação no REQ-1, embora só saiba que não recebeu evidências. |
| P2 — formatos publicados | Reconheceu que REQ-1 não especifica formatos; a justificativa mistura o marcador da fixture de prévia com REQ-1. | Reconheceu informação insuficiente. |
| P3 — latência | Recuperou p95 ≤ 1 s e 20 usuários simultâneos. | Reconheceu informação insuficiente. |
| P4 — campos das relações | Enumerou origem, destino e tipo; não repetiu o erro anterior de negar esses campos. | Reconheceu informação insuficiente. |
| P5 — download após remoção | Negou com apoio na regra de vínculo ativo e no arquivo anteriormente gerado. | Respondeu “Não”, mas admite na justificativa que não tem informação: conclusão sem apoio. |
| P6 — auditoria | Enumerou usuário, projeto, horário UTC, formato e versões. | Reconheceu ausência de informação específica. |
| P7 — prévia e formato | Resposta “Não” compatível com a fonte, mas justificativa incorreta: diz que a fonte não trata de formato, embora declare independência explicitamente. | Reconheceu informação insuficiente. |
| P8 — notificação | Indicou emissão após geração completa do arquivo. | Reconheceu informação insuficiente. |
| P9 — token | Indicou 15 minutos e uso único. | Reconheceu informação insuficiente. |
| P10 — tema | Explicou que não muda arquivos nem preferências de outros usuários. | Reconheceu informação insuficiente. |
| P11 — filtros | Explicou que não mudam conteúdo/estado. | Admitiu insuficiência, mas introduziu uma menção indevida a REQ-1 na justificativa. |
| P12 — DOCX/500 páginas | Não inferiu formato nem prazo sem evidência. | Admitiu insuficiência; descreveu evidências como se houvesse documentos, embora o contexto fosse vazio. |

O prompt de perguntas foi separado das instruções de impacto e passou a pedir
explicitamente todas as partes da pergunta. Os diagnósticos anteriores de
[relações](evidencias/v2/diagnosticos/pergunta-relacoes-antes.json) e
[auditoria](evidencias/v2/diagnosticos/pergunta-auditoria-antes.json) preservam
respostas erradas mesmo com a fonte correta em primeiro lugar. Logo, recuperar
o documento certo era necessário, mas não suficiente. Persistem problemas de
justificativa em P2/P7: **não apresentar esta rodada como 100% correta**.

Médias por operação, em ms; intervalo observado do total entre parênteses:

| Modo | API | MCP | LLM | Total | Entrada / saída em tokens | USD referência |
|---|---:|---:|---:|---:|---:|---:|
| Com RAG | 18,10 | 26,58 | 11.553,78 | 11.603,34 (9.815–13.309) | 981,75 / 110,42 | 0,00056937 |
| Sem RAG | 0 | 0 | 4.256,23 | 4.260,55 (2.985–7.724) | 442,33 / 70,92 | 0,00029040 |

Mantida essa distribuição de tokens, 1.000 perguntas com RAG correspondem a
US$ 0,56937 de referência e 10.000 a US$ 5,6937. Sem RAG, US$ 0,29040 e US$ 2,904.
Isso não estima throughput, latência concorrente nem custo real do computador.

## Análise de impacto

O cliente calcula o diff de título, conteúdo e critérios em Python. Para cada
alvo recuperado, uma triagem de vínculo funcional precede a geração de riscos.
Vínculos independentes/incertos ficam registrados e não seguem para a etapa
que formula hipóteses de falha. Para os diretos, o LLM escolhe um trecho literal do alvo e produz mudança,
efeito/risco, verificação e classificação. A gramática restringe as frases e
referências a evidências existentes. Isso valida a transcrição e a identidade
das fontes, não o raciocínio. Revisar pode significar conferir testes da nova
variante, mesmo quando o texto do requisito relacionado continua válido.

Todos os alvos analisados ficam visíveis, incluindo os classificados como
`SEM_IMPACTO_IDENTIFICADO` e `INDETERMINADO`. Somente `REVISAR` com geração válida
entra em `sugestoes`, sempre `PENDENTE_HUMANO`. Sem delta, há retorno determinístico
sem LLM. Em falha parcial, conclusões válidas são preservadas e os alvos com falha
ficam indeterminados. Tempos e tokens incluem todas as chamadas, não apenas a última.

| Cenário | Resultado observado | Tempo total | Chamadas LLM | USD referência |
|---|---|---:|---:|---:|
| [I1 — PDF/Markdown](evidencias/v2/impactos/I1-com_rag.json) | 4 sugestões em 8 alvos: REQ-4, REQ-5, REQ-6 e REQ-8 | 126,59 s | 12 | 0,00815160 |
| [I2 — só título](evidencias/v2/impactos/I2-com_rag.json) | 0 sugestões funcionais; conferência editorial pendente | 0,173 s | 0 | 0 |
| [I3 — campos iguais](evidencias/v2/impactos/I3-com_rag.json) | 0 sugestões; comparação determinística | 0,091 s | 0 | 0 |

Em I1, os dois alvos positivos autorados (REQ-4 e REQ-6) foram sugeridos. Os
controles negativos **efetivamente analisados**, REQ-7 e REQ-9, foram classificados
como independentes. REQ-10 e REQ-11 não entraram nessa análise e não contam como
rejeições corretas. REQ-2 e REQ-3, classificados como plausíveis no roteiro,
não foram sugeridos. Isso exige conferência humana, especialmente porque o mapa
relaciona a exportação à meta de tempo do REQ-3.

As sugestões de REQ-4 e REQ-5 propõem testar preservação de rastreabilidade nos
dois formatos e autorização para membros inativos. Há, porém, erros que a
validação estrutural não resolve: a justificativa de REQ-6 chama o registro do
motivo de falha de “nova exigência”, mas isso já estava no alvo; a mudança do
principal é adicionar PDF/Markdown. Para a revisão humana, a conferência relevante
é verificar se o campo de formato e as versões exportadas são registrados
corretamente nos novos caminhos. Em REQ-8, testar a conclusão antes da notificação
é pertinente, mas a justificativa produzida não explica bem o risco específico
dos novos formatos. **Os alvos sugeridos não tornam todas as justificativas corretas.**

I2 é tratado por uma regra explícita da aplicação: título isolado não aciona
inferência automática de impacto funcional. O resultado pede conferir se a nova
redação contradiz o conteúdo/critérios ou afeta referências ao título anterior.
Isso não prova equivalência semântica. I3 dispensa o LLM porque os três campos
comparados são idênticos. Estes dois controles foram reexecutados após a última
mudança, restrita ao tratamento editorial; o caminho funcional medido em I1 foi
preservado. Os JSONs mantêm seus horários/UUIDs reais, e os arquivos `codigo-*.sha256`
registram o código das execuções.

O preço da análise detalhada foi maior: oito triagens e quatro gerações de risco,
13.591 tokens de entrada e 1.697 de saída, sem retry nessa amostra. A duração LLM
foi 126,18 s, além de 354 ms de API e 27 ms de MCP. O timeout de 20 s vale por
tentativa de cada etapa, não por operação completa. **Reserve cerca de dois
minutos para I1 nesta máquina**, ou apresente o JSON salvo junto de uma execução
curta de pergunta. A mesma distribuição daria US$ 8,1516 por 1.000 análises na
simulação de preço, sem representar cobrança real ou previsão de concorrência.

Os diagnósticos anteriores foram preservados:

- [Análise genérica](evidencias/v2/diagnosticos/impacto-generico.json): sugeria todos os oito alvos, inclusive os independentes.
- [Transcrição rejeitada](evidencias/v2/diagnosticos/impacto-transcricao-rejeitada.json): frase atribuída ao alvo não era literal; motivou restringir a escolha de trechos reais.
- [Análise conservadora](evidencias/v2/diagnosticos/impacto-conservador.json): confundia texto ainda válido com ausência de necessidade de rever testes.
- [Riscos sem triagem](evidencias/v2/diagnosticos/impacto-risco-sem-triagem.json): voltou a sugerir todos os alvos; registra também timeout seguido de retry bem-sucedido. Motivou separar vínculo funcional de hipótese de falha.
- [Título antes da regra editorial](evidencias/v2/diagnosticos/titulo-antes-da-regra-editorial.json): sugeriu REQ-4/REQ-6 mesmo com conteúdo e critérios iguais. Motivou a conferência editorial determinística.

Essas iterações mostram o valor dos controles e também limitam a interpretação:
o conjunto serviu ao desenvolvimento. Para medir generalização, o grupo deve
criar novos casos sem ajustar o prompt a partir das respostas e fazer a revisão humana.

## Revisão humana e verificações

Foram geradas fichas vazias para
[perguntas](evidencias/v2/perguntas/revisao-humana.csv) e
[impactos](evidencias/v2/impactos/revisao-humana.csv), com
[rubrica de notas 0–2](evidencias/v2/perguntas/RUBRICA.md). O grupo deve ler cada
JSON e preencher correção, fundamentação, tratamento da informação ausente,
observações, revisor e data. O comando `consolidar-revisao` valida o formato e o
vínculo com a operação, informa pendências e calcula médias por modo. Não autentica
quem preencheu a ficha e não modifica os resultados brutos.

**26 testes automatizados passaram**, cobrindo também diff, ausência de mudança,
fontes das duas versões e do alvo, citação inventada, texto JSON com aspas/quebras,
falha parcial, soma de tempos/tokens, controle negativo, triagem sem geração de
risco para alvos independentes, regra editorial, ficha pendente e autoria,
reconhecimento de relações simétricas e recusa de sobrescrever fixtures divergentes.
O build do cliente e sua verificação de saúde no Compose também passaram.

O custo registrado é uma simulação com os tokens informados pelo Ollama e os
preços de referência configurados (US$ 0,40/1,60 por milhão de entrada/saída), não
gasto de uma API. Tokenizadores diferem e energia/hardware não foram medidos.
Tentativas interrompidas mantêm custo total `null` quando o consumo é desconhecido.

Para reproduzir a carga, as 24 respostas e os controles, consulte o
[guia do cliente](../../clientes/agente-ia/README.md#ampliar-a-demonstração-e-executar-os-controles).
A aceitação do cliente externo pelo professor e a avaliação humana continuam
pendentes. A aprovação/publicação assistida pela IA com confirmação humana é um
extra distinto da carga determinística de fixtures e não foi implementada aqui.
