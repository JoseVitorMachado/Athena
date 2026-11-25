# Comparação com e sem RAG

Execuções reais; avaliação humana ainda pendente. Falhas/degradações não equivalem a respostas do LLM.

| Pergunta | Modo | Estado | Resposta | Fontes | MCP ms | LLM ms | Total ms | USD referência |
|---|---|---|---|---|---:|---:|---:|---:|
| P1 | sem_rag | ok | REQ-1 não possui especificações ou detalhes suficientes para fornecer uma descrição de funcionalidade ou preservação de requisitos. | [] | 0 | 7716.79 | 7724.35 | 0.000292 |
| P1 | com_rag | ok | A versão publicada do REQ-1 [REQ-1] descreve que o sistema deve permitir exportar o documento de requisitos. Critérios de aceite para este requisito incluem que a exportação preserva os requisitos. | ["E2"] | 26.52 | 12931.75 | 13009.63 | 0.0006136 |
| P2 | com_rag | ok | A versão publicada do REQ-1 garante que o sistema exporta o documento de requisitos sem especificar quais formatos de arquivo são garantidos. A única garantia é que a exportação preserva os requisitos. | ["E2"] | 30.24 | 11374.14 | 11425.96 | 0.0005716 |
| P2 | sem_rag | ok | Não há informação suficiente nas evidências fornecidas para listar quais formatos de arquivo são garantidos pelo REQ-1. | [] | 0 | 3973.8 | 3977.76 | 0.0002936 |
| P3 | sem_rag | ok | Não há informação suficiente nas evidências fornecidas para responder a essa pergunta. | [] | 0 | 4369.96 | 4373.83 | 0.0003092 |
| P3 | com_rag | ok | O REQ-3 define que o p95 das consultas comuns deve ficar em até 1 segundo no cenário de referência. Deve ser medido com 20 usuários simultâneos. | ["E1"] | 25.39 | 13265.98 | 13309.13 | 0.0005888 |
| P4 | com_rag | ok | Os códigos de origem e destino e o tipo da cada relação devem ser preservados na exportação de documentos. | ["E1", "E2"] | 24.72 | 12609.47 | 12652.59 | 0.0006428 |
| P4 | sem_rag | ok | Não há informação suficiente nas evidências fornecidas para responder a essa pergunta. | [] | 0 | 4351.11 | 4355.41 | 0.0002896 |
| P5 | sem_rag | ok | Não | [] | 0 | 3481.77 | 3486.02 | 0.000278 |
| P5 | com_rag | ok | Não | ["E1"] | 25.58 | 11648.87 | 11694.31 | 0.00055 |
| P6 | com_rag | ok | Na auditoria de uma exportação concluída, devem ser registradas as seguintes informações: usuário solicitante, projeto, horário UTC, formato do arquivo e identificadores das versões exportadas. | ["E1"] | 26.64 | 12824.02 | 12871.28 | 0.00059 |
| P6 | sem_rag | ok | Nenhuma informação específica sobre o que deve ser registrado na auditoria de uma exportação concluída foi fornecida nas evidências. | [] | 0 | 4228.32 | 4232.32 | 0.0002976 |
| P7 | sem_rag | ok | Não há informação suficiente nas evidências fornecidas para responder a essa pergunta. | [] | 0 | 3638.18 | 3642.4 | 0.0002844 |
| P7 | com_rag | ok | Não | ["E1", "E2"] | 24.4 | 11606.16 | 11648.41 | 0.0005624 |
| P8 | com_rag | ok | A notificação de sucesso deve ser enviada após a geração completa do arquivo de exportação. | ["E1"] | 23.76 | 10299.48 | 10345.87 | 0.000526 |
| P8 | sem_rag | ok | Não há informação suficiente nas evidências fornecidas para responder a essa pergunta. | [] | 0 | 3737.87 | 3741.92 | 0.0002848 |
| P9 | sem_rag | ok | Não há informação suficiente nas evidências fornecidas para responder a essa pergunta. | [] | 0 | 3435.11 | 3439.3 | 0.0002792 |
| P9 | com_rag | ok | O token de recuperação de senha tem uma validade de 15 minutos. Ele não pode ser reutilizado após ser consumido. | ["E1"] | 29.42 | 11306.48 | 11356.77 | 0.0005608 |
| P10 | com_rag | ok | A alteração para o tema escuro não modifica arquivos exportados e não afeta a preferência de outros usuários. | ["E1"] | 27.71 | 9768.61 | 9819.06 | 0.000524 |
| P10 | sem_rag | ok | Não há informação suficiente nas evidências fornecidas para responder a essa pergunta. | [] | 0 | 4583.36 | 4587.18 | 0.0003052 |
| P11 | sem_rag | ok | Não há informação suficiente nas evidências fornecidas para responder a essa pergunta. | [] | 0 | 2981.13 | 2984.65 | 0.0002596 |
| P11 | com_rag | ok | Não, a filtragem por código e situação não modifica o conteúdo ou o estado dos requisitos. | ["E1"] | 26.07 | 11246.01 | 11291.89 | 0.000556 |
| P12 | com_rag | ok | Não há informação suficiente nas evidências fornecidas para responder a essa pergunta. | ["E1"] | 28.49 | 9764.34 | 9815.18 | 0.0005464 |
| P12 | sem_rag | ok | Não há informação suficiente nas evidências fornecidas. | [] | 0 | 4577.35 | 4581.42 | 0.0003116 |

O CSV e os JSONs preservam os trechos, perguntas, justificativas, versões e métricas completas.
Rubrica humana: correção (0–2), apoio nas fontes (0–2), reconhecimento de informação ausente (0–2).
Compare também citações inventadas e cobertura. Não use presença de palavras-chave como prova de qualidade.
Custo é uma simulação de referência, não gasto efetivo. null indica consumo incompleto em alguma tentativa.
