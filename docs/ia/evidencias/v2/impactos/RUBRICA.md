# Revisão humana pendente

Leia cada JSON (pergunta, resposta, gabarito, versões, fontes, estado e métricas) antes de preencher o CSV.
Não atribua notas apenas por palavras-chave ou por uma citação existir. A coluna `esperado` é um roteiro
autorado para estes dados fictícios, não uma verdade universal. Divergências justificadas são permitidas.

| Dimensão | 0 | 1 | 2 |
|---|---|---|---|
| Correção | Errada/contraditória ou indisponível | Parcial, omite parte relevante | Responde corretamente ao pedido |
| Fundamentação | Fatos inventados/sem apoio ou indisponível | Apoio parcial, referências pouco precisas | Afirmações sustentadas nas fontes; sem contexto, admite essa limitação |
| Ausência | Inventa o que falta ou indisponível | Reconhece só parte das lacunas | Distingue o documentado do ausente e publicada de candidata |

Para impacto, correção considera o delta, efeito plausível e verificação concreta; fundamentação exige
evidência do alvo e das duas versões. Relação no mapa não basta. Ausência considera alvos sem efeito
demonstrável e informação insuficiente. Uma falha de geração deve ser identificada como tal nas observações,
sem tratá-la como uma resposta semântica correta. Rejeitar controles negativos não prova cobertura global.

Preencha as três notas (0, 1 ou 2), observações, nome do revisor e data ISO (YYYY-MM-DD).
Deixe a linha inteiramente sem notas enquanto estiver pendente. Não preencha nome de outra pessoa.
Use `python -m athena_ia consolidar-revisao --arquivo <pasta>/revisao-humana.csv`
para validar o preenchimento e obter médias separadas por modo. Esse comando não altera os JSONs originais.
Análise assistida pelo Codex, se houver, é um documento separado e não substitui esta revisão humana.
