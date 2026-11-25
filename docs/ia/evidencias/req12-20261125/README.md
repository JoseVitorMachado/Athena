# Teste de impacto do REQ-12 — 25/11/2026

Inspeção assistida pelo Codex. A revisão humana permanece pendente; os CSVs não têm notas nem autoria preenchidas.

O requisito foi publicado pela usuária com limite de 1 a 100. Os testes criaram uma candidata v2 e alteraram somente essa candidata pela API, com ETag. Nenhuma publicação, aprovação ou relação foi criada durante estes testes.

## Cenários definidos antes da execução

- Sem alteração: mesmos título, conteúdo e critérios; esperado zero sugestões e zero chamadas LLM.
- Limite de 100 para 1000: revisar a validação de limites e a preservação de todos os itens na exportação (REQ-1).
- Descarte proposital: acima de 100, exportar só os primeiros 100, omitir os demais e suas relações e permitir contagem de relações menor que na seleção original. Conflita com a preservação do REQ-1 e, explicitamente, com o critério de contagem do REQ-4.
- Controle independente: recuperação de senha (REQ-9) e tema da interface (REQ-10) não devem ser sugeridos. Este controle usa alvos fixos consultados pela API e o snapshot da alteração de descarte; testa a classificação, não a recuperação MCP.

As expectativas estão em `casos.json`; não foram enviadas ao LLM. A análise principal usa as duas versões via API, vizinhos do mapa e cinco trechos do MCP. Não varre todos os requisitos do projeto.

## Execuções preservadas

| Código | Cenário | Estado técnico | Sugestões | Tempo total (s) | Tentativas LLM | Falhas recuperadas ou degradadas | Custo de referência total (USD) |
|---|---|---|---|---:|---:|---:|---:|
| antes | controle-sem-mudanca | ok | Nenhuma | 0.25 | 0 | 0 | 0.0 |
| antes | limite-1000 | ok | REQ-1, REQ-5 | 103.80 | 8 | 2 | Indeterminado |
| antes | conflito-descarte | ok | REQ-1, REQ-6 | 112.97 | 8 | 2 | Indeterminado |
| depois | controle-sem-mudanca | ok | Nenhuma | 0.25 | 0 | 0 | 0.0 |
| depois | limite-1000 | ok | REQ-1, REQ-5, REQ-6 | 103.74 | 8 | 1 | Indeterminado |
| depois | conflito-descarte | ok | REQ-1, REQ-4, REQ-5, REQ-6 | 105.11 | 8 | 0 | 0.0068168 |
| depois | controle-negativos | ok | REQ-9 | 26.99 | 3 | 0 | 0.0021708 |

`estado=ok` informa que a geração/validação técnica concluiu; não certifica a correção semântica. Houve timeouts de 20 segundos com retry e backoff. Onde faltam tokens das tentativas interrompidas, o custo total fica indeterminado; os JSONs preservam separadamente o custo dos tokens observados. Modelo local: nenhum desses valores representa cobrança de API.

Modelo: qwen2.5:3b-instruct-q4_K_M, temperatura 0, seed 42, contexto 4096, saída até 512 tokens, duas tentativas por etapa. Resultados individuais não são um benchmark de desempenho: cache e ordem de execução podem afetar os tempos.

## O que foi corrigido

Na primeira execução de descarte, o REQ-4 foi classificado como independente e a única citação da triagem era seu título. O cliente agora exclui cabeçalhos MCP das frases aceitas como evidência, orienta a comparação de todas as obrigações e critérios e identifica contradições como vínculo direto. A entrada de impacto mostra conteúdo e critérios, com antes/depois explícitos, sem repetir os mesmos parágrafos num segundo diff. O diff completo continua salvo no resultado.

A mudança foi validada por 37 testes automatizados. Nenhum código de requisito ou resposta esperada foi codificado na lógica de classificação. As saídas anteriores não foram substituídas; o patch e os hashes permitem auditar a alteração.

## Limitações encontradas na inspeção assistida

- O controle independente falhou para o REQ-9: a IA afirmou uma mudança na validade do token de senha, inexistente no diff, e o sugeriu para revisão. O REQ-10 ficou fora das sugestões, mas a justificativa disse que o alvo não menciona exportações, embora seu texto diga que o tema não altera os arquivos exportados. A classificação de um alvo pode estar certa com argumento errado.
- No cenário ajustado de limite 1000, o REQ-5 recebeu uma alegação incorreta de que a alteração permite membros inativos. O texto alterado não muda permissões. Isso é uma inferência sem apoio, não um conflito demonstrado.
- No mesmo cenário, a sugestão para o REQ-6 pede testar 1001 itens sem explicitar que essa seleção continua proibida pela candidata. O teste precisa distinguir tentativa bloqueada de exportação concluída.
- Antes do ajuste, a verificação sugerida para o REQ-1 no descarte misturava incluir todos os requisitos com excluir relações dos itens descartados. Essa conclusão não deve ser aceita automaticamente.
- Na pergunta enviada pela usuária, os números 1 e 100 estão corretos, mas ambos vêm do REQ-12. Atribuir o mínimo ao REQ-1 foi erro de fundamentação.
- No descarte ajustado, a triagem do REQ-6 diz que registrar versões exportadas é uma obrigação nova, mas ela já existia no alvo. Esse argumento está incorreto.
- Alguns campos continuam longos, cortados ou misturam o texto do alvo com a mudança do principal; por exemplo, mudanca do REQ-4. A sinalização do alvo não equivale à aprovação de todos os campos da análise.
- Alvos não recuperados não contam como rejeições corretas. Falhas, casos indeterminados e controles isolados devem ser relatados separadamente.

## Análises após o ajuste

### controle-sem-mudanca

Nenhuma análise funcional necessária.

### limite-1000

- **REQ-1 — REVISAR**: O sistema deve permitir exportar o documento de requisitos.
  - Conferência proposta pelo modelo: Conferir se a exportação preserva requisitos e se há testes adequados para a nova limitação de 1 a 1000 requisitos.
- **REQ-5 — REVISAR**: O trecho alterado permite que membros inativos também possam solicitar exportação, o que pode violar a obrigação de somente membros ativos.
  - Conferência proposta pelo modelo: Testar se membros inativos ainda são negados ao solicitar exportação após a mudança.
- **REQ-6 — REVISAR**: A obrigação de registrar usuário solicitante, projeto, horário UTC, formato e versões exportadas pode precisar ser testada novamente para garantir que ainda são registrados corretamente.
  - Conferência proposta pelo modelo: Testar exportações com 1001 requisitos para confirmar que todos os campos são registrados corretamente.
- **REQ-11 — SEM_IMPACTO_IDENTIFICADO**: O trecho altera a forma como os requisitos são filtrados, mas não afeta o conteúdo ou estado dos requisitos.
  - Conferência proposta pelo modelo: Confirmar manualmente o vínculo com a parte alterada, usando as versões e a evidência do alvo.

### conflito-descarte

- **REQ-1 — REVISAR**: O sistema pode descartar requisitos e relações associadas, o que pode violar a obrigação de exportar o documento completo.
  - Conferência proposta pelo modelo: Testar se o sistema exporta todos os requisitos selecionados mesmo quando mais de 100 são selecionados.
- **REQ-4 — REVISAR**: A obrigação de REQ-4 pode exigir revisão de testes para garantir que a contagem de relações exportadas coincida com a seleção de origem mesmo quando houver mais de 100 requisitos selecionados.
  - Conferência proposta pelo modelo: Testar a contagem de relações exportadas para garantir que coincida com a seleção de origem quando mais de 100 requisitos forem selecionados.
- **REQ-5 — REVISAR**: A obrigação de REQ-5 pode exigir revisão de testes para garantir que a verificação de membros ativos continue funcionando corretamente.
  - Conferência proposta pelo modelo: Testar se a verificação de membros ativos continua funcionando corretamente após a implementação da mudança.
- **REQ-6 — REVISAR**: A obrigação de REQ-6 pode exigir revisão de testes, pois a nova versão permite exportações com mais de 100 requisitos sem armazenar as relações descartadas.
  - Conferência proposta pelo modelo: Testar se a versão publicada continua registrando todas as informações solicitadas em REQ-6 mesmo quando mais de 100 requisitos são exportados.

### controle-negativos

- **REQ-9 — REVISAR**: A obrigação de recuperação de senha pode exigir revisão de testes para garantir que o limite de 15 minutos para o token de uso único não seja violado.
  - Conferência proposta pelo modelo: Testar a validade do token após 15 minutos para garantir que a recuperação de senha funcione conforme esperado.
- **REQ-10 — SEM_IMPACTO_IDENTIFICADO**: O trecho do alvo não menciona exportações de documentos ou requisitos selecionados, então não há vínculo direto com as mudanças no PRINCIPAL.
  - Conferência proposta pelo modelo: Confirmar manualmente o vínculo com a parte alterada, usando as versões e a evidência do alvo.

## Estado deixado no ambiente

- Publicada v1: limite 100, ID `62a088ba-8fa8-45b9-a3a6-a083d168727e`, hash `882c6453c4b37edb8fdb88f62787076b7b2b7c477aba8a2c048479fea8999cdd` preservado.
- Candidata v2: limite 1000 e bloqueio a partir de 1001, em RASCUNHO, ID `e1457a25-0c11-4fd4-9ed5-55ecea0fdf8b`.
- O texto de descarte foi retirado da candidata após os testes. Seus snapshots e análises permanecem nas evidências.

Para repetir a análise da candidata atual, na raiz do repositório:

```sh
docker compose exec -T agente-ia python -m athena_ia impacto --requisito 5d59d1b9-5346-4c0d-9a18-8a01c998afb4 --saida /resultados/impacto-req12.json
```

A geração de sugestões não altera nenhum requisito. Aprovar/publicar continua sendo decisão humana. Os três cenários com mutações já foram executados; o comando acima apenas analisa o limite 1000 que ficou salvo.
