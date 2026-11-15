**UNIVERSIDADE FEDERAL DE LAVRAS**

Empreendedorismo em Sistemas de Informação

**Entrega 2 – Validação do problema, clientes e mercado**

***ATHENA***

Documento de validação do problema, público-alvo e mercado

Ana Clara Rocha Gomes

Bárbara Oliveira Fonseca

Fernando Chaves Scarabeli

Jhennifer Hellen Campos Silva

José Vítor Machado de Oliveira

Lavras - MG

2026

**1. Objetivo da validação**

Esta etapa busca verificar se o problema que motivou o ATHENA ocorre em contextos reais de desenvolvimento de software, se é percebido como relevante pelos profissionais envolvidos e como é enfrentado atualmente. A investigação concentra-se nas dificuldades para rastrear relações entre requisitos e outros artefatos, compreender os efeitos de uma alteração e manter a documentação consistente ao longo da evolução de um sistema.

O ATHENA parte da hipótese de que mudanças em requisitos podem afetar histórias relacionadas, regras de negócio, protótipos, implementação, testes e documentação. Quando essas relações não estão explícitas, a equipe precisa reconstruí-las manualmente ou depender da memória de pessoas experientes, aumentando o risco de retrabalho, atraso e inconsistência. A pesquisa procura avaliar essa hipótese a partir de experiências relatadas pelos participantes, sem tratar a opinião favorável a uma ferramenta específica como prova suficiente de demanda.

# 2. Contexto da pesquisa e perfil dos participantes

A pesquisa foi realizada por meio de entrevistas semiestruturadas e também um formulário “Pesquisa sobre Gestão e Manutenção de Requisitos de Software”. O público consultado incluiu o time de requisitos do GEDAI, organização da qual participam alguns integrantes do grupo. Esse contexto aproxima a investigação de pessoas que vivenciam atividades de levantamento, documentação, revisão e manutenção de requisitos em projetos de software.

Também participou dessa pesquisa o professor Paulo Afonso, cuja participação ampliou a diversidade dos pontos de vista considerados na pesquisa. Sua resposta é analisada como uma contribuição individual e não como validação automática de todas as hipóteses do produto.

Foram obtidas sete respostas válidas no arquivo de respostas analisado, e também interações adicionais com as entrevistas. Os participantes declararam experiências que variam de dois meses a três anos em projetos de desenvolvimento de software. A amostra é exploratória e contextualizada: *oferece indícios relevantes sobre as práticas e dificuldades do grupo consultado, mas não representa estatisticamente todo o mercado de desenvolvimento de software.*

| **Participante**          | **Experiência declarada** | **Contexto de atuação indicado nas respostas**                    |
| ------------------------- | ------------------------- | ----------------------------------------------------------------- |
| Thomas Anderson Lopes     | 3 anos                    | Histórias de usuário; Google Docs, OpenProject e LLMs.            |
| Karyna Morais Lins        | 2 meses                   | Documentação em texto livre e tabelas; Google Docs e OpenProject. |
| Caio Teixeira Ladeira     | 1 ano                     | Histórias de usuário; Google Docs, Drive e Figma.                 |
| Thaís Giovanna Lopes      | 2 anos                    | Épicos e critérios de aceitação; ferramentas Google.              |
| Gabriel Soares Carvalho   | 1 ano e 3 meses           | Histórias de usuário; OpenProject e Google Docs.                  |
| Ezequiel Dominguez Santos | 1 ano                     | Documentos estruturados no Google Docs.                           |
| Paulo Afonso              | 2 anos                    | Histórias de usuário; Google Docs e planilhas.                    |

*Nota metodológica: os nomes e tempos de experiência acima reproduzem as informações declaradas no questionário.*

# 3. Método de coleta e análise

A coleta de dados para a validação do problema da ATHENA foi realizada por meio de questionários individuais e reuniões de levantamento de informações com pessoas que possuem experiência ou contato com atividades relacionadas à Engenharia de Requisitos. A combinação desses instrumentos teve como objetivo compreender as dificuldades enfrentadas na documentação, manutenção, rastreabilidade e análise de impacto de requisitos de software, além de identificar práticas utilizadas atualmente e oportunidades de melhoria.

### 3.1. Aplicação dos questionários

Foi aplicado um questionário composto por perguntas abertas e questões estruturadas, incluindo uma escala de esforço de 1 a 5. O instrumento investigou a experiência dos participantes em projetos de software, as formas de documentação utilizadas, as ferramentas adotadas, os procedimentos para identificar impactos de mudanças, a existência de relações entre requisitos e outros artefatos, a ocorrência de impactos percebidos tardiamente e o esforço necessário para compreender as consequências de uma alteração.

Também foram investigadas a dependência de pessoas com conhecimento especializado, a localização das informações, a qualidade dos requisitos, a atualização da documentação, as limitações das ferramentas atuais e as melhorias consideradas desejáveis.

### 3.2. Reuniões de levantamento e entrevistas

Além dos questionários, foram consideradas reuniões e conversas voltadas à compreensão das necessidades relacionadas ao problema investigado. Esses encontros tiveram como finalidade aprofundar aspectos do processo de Engenharia de Requisitos, compreender dificuldades práticas e levantar possibilidades para a concepção da solução.

**Interação 1: **Foi realizada uma reunião de levantamento e refinamento de ideias com Thomas, registrada em ata. O encontro teve como objetivo discutir a estrutura e as funcionalidades inicialmente consideradas para o sistema. Entre os assuntos abordados estavam a organização dos requisitos por épicos, a representação de dependências entre requisitos e módulos, a utilização do princípio INVEST na escrita de histórias de usuário, a definição de status, os papéis e as permissões de acesso e o controle de versões. Também foram discutidas a criação de um Livro de Requisitos para consulta, a possibilidade de disponibilizar um manual do usuário e a integração com Inteligência Artificial por meio de um servidor MCP. A reunião contribuiu para transformar necessidades gerais de organização e manutenção de requisitos em possibilidades concretas de funcionalidades para a ATHENA.

**Interação 2: **Paulo Afonso participou da coleta de dados por meio de uma entrevista e também pelo questionário. Além da estrutura prevista, foi possível compreender melhor suas dores como engenheiro de requisitos. Ao longo da conversa, foi possível analisar que ele possui uma contribuição relevante no contexto acadêmico da investigação sobre Engenharia de Requisitos, destacando-se pela pertinência do artigo *A Comparative Study of Tools for Anomaly Detection in Software Requirements*, de Pereira, Costa e Parreira (2024).

**Interação 3:** As respostas da Thaís foram utilizadas para examinar sua experiência com documentação e manutenção de requisitos, considerando os problemas relatados na estrutura. Sua contribuição foi analisada em conjunto com as demais respostas para identificar dificuldades relacionadas à compreensão das mudanças, à atualização das informações e à identificação de possíveis impactos. 

**Interação 4:** As respostas do Caio foram analisadas com foco nas práticas utilizadas para acompanhar alterações e compreender suas consequências no projeto. O material foi comparado aos relatos dos demais participantes para identificar semelhanças e diferenças nos procedimentos adotados, especialmente quanto à localização das informações, à comunicação entre integrantes da equipe e à necessidade de verificar manualmente relações entre artefatos.

**Interação 5:** As respostas da Karyna foram incorporadas à análise para compreender as dificuldades enfrentadas por pessoas com diferentes níveis de experiência no contexto de requisitos. Seu relato foi considerado na identificação de obstáculos de aprendizagem, compreensão da documentação e utilização das ferramentas disponíveis, sempre respeitando o conteúdo efetivamente informado pela participante.

**3.3. Análise dos dados**

Os dados foram analisados por meio de uma abordagem qualitativa, com agrupamento dos relatos por temas recorrentes. A análise procurou identificar padrões entre as experiências dos participantes, preservando divergências e situações particulares. Os principais temas considerados foram:

1. **Qualidade dos requisitos:** requisitos desatualizados, contraditórios, duplicados ou pouco claros.
2. **Rastreabilidade:** dificuldade de localizar relações entre requisitos e outros artefatos.
3. **Análise de impacto:** necessidade de investigar quais partes do projeto podem ser afetadas por uma mudança.
4. **Versionamento:** dificuldade de acompanhar alterações e manter o histórico atualizado.
5. **Dependência de conhecimento especializado:** necessidade de consultar pessoas que conhecem o contexto do projeto.
6. **Ferramentas e comunicação:** limitações dos recursos utilizados e dificuldades para manter as informações sincronizadas entre os integrantes da equipe.
7. **Esforço operacional:** trabalho necessário para esclarecer dúvidas, reconstruir dependências e verificar consequências de alterações.

Nas questões categóricas, foram contabilizadas as ocorrências entre os sete respondentes. A escala de esforço foi analisada numericamente, utilizando média e mediana para resumir a percepção dos participantes. As respostas foram interpretadas considerando o tamanho do conjunto analisado, sem generalizar os resultados para todas as equipes de desenvolvimento de software.

# 4. Hipóteses testadas

| **ID** | **Hipótese de validação**                                                                                                                   |
| ------ | ------------------------------------------------------------------------------------------------------------------------------------------- |
| H1     | Mudanças em requisitos podem afetar outras partes do projeto e exigir alterações adicionais.                                                |
| H2     | A identificação de impactos depende, em grande parte, de conhecimento humano e verificações manuais.                                        |
| H3     | As relações entre requisitos, funcionalidades, regras de negócio, protótipos, testes e tarefas são incompletas ou difíceis de consultar.    |
| H4     | A descoberta tardia de impactos pode provocar retrabalho, atrasos, dúvidas ou remodelagem.                                                  |
| H5     | As ferramentas utilizadas atualmente ajudam na documentação, mas não resolvem integralmente rastreabilidade e análise de impacto.           |
| H6     | A dependência de especialistas prejudica a continuidade e a autonomia da equipe.                                                            |
| H7     | Existe oportunidade para uma solução de apoio à manutenção de requisitos, desde que sua adoção seja simples e compatível com o fluxo atual. |
| H8     | Recursos de Inteligência Artificial podem agregar valor à escrita, manutenção ou análise de impacto dos requisitos.                         |

# 5. Síntese dos resultados obtidos através das entrevistas e dos questionários

## 5.1. Indicadores observados

| **Indicador observado**                                                                                          | **Resultado nas 7 respostas** | **Interpretação**                                                                                                   |
| ---------------------------------------------------------------------------------------------------------------- | ----------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| Participantes que já encontraram requisitos desatualizados, contraditórios, duplicados ou pouco claros           | 7 de 7                        | Problemas de qualidade e manutenção documental foram relatados por toda a amostra.                                  |
| Participantes que afirmaram que a ausência de uma pessoa experiente dificulta compreender requisitos ou impactos | 7 de 7                        | Há forte dependência do conhecimento acumulado por indivíduos.                                                      |
| Participantes que relataram impactos percebidos somente depois da mudança                                        | 5 de 7                        | A maioria relatou ocorrências concretas; dois não relataram ocorrência anterior, embora um deles reconheça o risco. |
| Esforço para identificar impactos (escala de 1 a 5)                                                              | Média: 3,9; mediana: 4        | A percepção tende a esforço moderado a considerável.                                                                |
| Participantes que indicaram rastreabilidade incompleta ou dificuldade de relacionar artefatos                    | 7 de 7, em diferentes formas  | Mesmo quando há referências ou mapeamentos, a cobertura é percebida como insuficiente ou difícil de usar.           |

## 5.2. Situações reais relatadas

1- Uma alteração em história de usuário exigiu mudanças também no protótipo e no frontend.

2- Uma mudança demandou conversa com a equipe de desenvolvimento para esclarecer dúvidas, atrasando o trabalho.

3- Foram relatadas revisões intensas para compensar falhas, atualização de design com pouco prazo, atrasos e aumento de retrabalho.

4- Um participante descreveu a necessidade de remodelar partes do sistema após um impacto ser identificado posteriormente.

5- Também foi relatada a necessidade de avisar toda a equipe quando um requisito muda, mostrando que a comunicação da alteração é parte do problema.

*Nem todos os participantes relataram que já sofreram um impacto tardio; um afirmou que isso ainda não ocorreu, mas reconheceu que a complexidade crescente torna difícil manter todas as relações apenas na memória.*

# 5.3. Frequência e intensidade

Os relatos de diferentes participantes descrevem mudanças em histórias, protótipos, frontend e outras partes do sistema, além de revisões manuais recorrentes. Quanto à intensidade, a escala de esforço para descobrir impactos teve média de 3,9 em 5, com respostas entre 3 e 5. Esse resultado sugere que, para os participantes, a tarefa não costuma ser trivial.

A intensidade também aparece nas consequências qualitativas citadas: retrabalho, atrasos, revisão extensa, remodelagem e dependência de pessoas com conhecimento especializado.

# 6. Comportamento atual e ferramentas utilizadas

O comportamento predominante consiste em revisar documentos e histórias, consultar integrantes da equipe, usar o conhecimento prévio do sistema e verificar manualmente protótipos, implementação, tarefas ou produto final. Em alguns casos, as relações são registradas como referências a outras histórias ou por meio de mapeamentos numerados; contudo, os participantes indicam que tais mecanismos não permitem visualizar todos os impactos com facilidade.

| **Ferramenta/prática**                      | **Uso relatado**                                                                              | **Limitação identificada nas respostas**                                                                                           |
| ------------------------------------------- | --------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| Google Docs / Planilhas / Drive             | Documentação, edição colaborativa, comentários e organização das informações.                 | Não identifica automaticamente todos os impactos; controle de versão e conformidade com templates podem exigir trabalho adicional. |
| OpenProject                                 | Gestão de atividades e acompanhamento de cards.                                               | Concluir cards não garante, por si só, que todas as dependências de requisitos foram identificadas.                                |
| Figma / protótipos                          | Representação visual de telas e alterações de design.                                         | A relação entre mudança no requisito e atualização de protótipos pode depender de comunicação e revisão manual.                    |
| Referências entre histórias                 | Indicação textual de histórias relacionadas e, em um caso, mapeamento numerado de requisitos. | Cobertura incompleta, propensão a erro e ausência de visualização clara das relações.                                              |
| Revisões, reuniões e conhecimento da equipe | Conferência de alterações e esclarecimento de dúvidas.                                        | Consome tempo e depende da disponibilidade e experiência de pessoas específicas.                                                   |

Os entrevistados também destacaram aspectos positivos das práticas atuais: facilidade de edição, colaboração em nuvem, comentários, templates, padrões de escrita, histórico de alterações e familiaridade com as ferramentas. Isso indica que a oportunidade não é simplesmente substituir as ferramentas existentes, mas complementar seus pontos fortes com rastreabilidade, sinalização de mudanças e análise de impacto.

# 7. Síntese individual das respostas

| **Participante**          | **Síntese individual**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| ------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Thomas Anderson Lopes     | Relatou impactos em design, desenvolvimento e testes; controle manual das dependências; dificuldade de manter consistência e atualização simultânea. Pesquisou GitBook e MkDocs, mas a adoção não avançou por falta de aderência completa e pelo custo de aprender outro fluxo. Destacou o versionamento.                                                                                                                                                                                                 |
| Karyna Morais Lins        | Relatou atrasos causados por esclarecimentos posteriores, ausência de registro específico de relações e dependência de pessoas experientes. Apontou dependências pouco claras e necessidade de sinalização mais explícita das mudanças.                                                                                                                                                                                                                                                                   |
| Caio Teixeira Ladeira     | Relatou mudança em história, protótipo e frontend; revisão com membros da equipe; esforço 4/5; verificação manual e dificuldade para atualizar vários documentos de uma vez. Indicou interesse em integração com IA.                                                                                                                                                                                                                                                                                      |
| Thaís Giovanna Lopes      | Relatou dificuldade em identificar efeitos decorrentes de uma mudança, dependência da memória e de especialistas, atrasos e retrabalho. Destacou rastreabilidade, adaptação rápida e organização das tarefas como pontos de melhoria.                                                                                                                                                                                                                                                                     |
| Gabriel Soares Carvalho   | Relatou dependência do conhecimento prévio, relações entre histórias insuficientes e necessidade de remodelagem após impacto tardio. Apontou comunicação e distribuição das tarefas como desafios.                                                                                                                                                                                                                                                                                                        |
| Ezequiel Dominguez Santos | Relatou uso de documentos estruturados, requisitos numerados, versionamento, histórico, comentários e notificações. Não relatou impacto tardio já ocorrido, mas indicou esforço 4/5 e dificuldade de relacionar claramente requisitos em diferentes partes do software.                                                                                                                                                                                                                                   |
| Paulo Afonso              | Relatou controle de versão feito no próprio documento, rastreamento apoiado no conhecimento geral do projeto e referências textuais a histórias relacionadas, consideradas pouco eficientes e propensas a erro. Destacou dificuldade crescente conforme aumentam o tamanho e a complexidade do sistema, além de problemas de versionamento, rastreabilidade e conformidade com templates. Também observou que algumas ferramentas exigem conhecimento especializado ou dificultam a exportação dos dados. |

# 8. Principais achados e padrões identificados

## 8.1. A rastreabilidade

O padrão mais consistente não é a ausência absoluta de documentação, mas a dificuldade de compreender as relações entre os elementos já documentados. Os participantes utilizam histórias, documentos, cards, protótipos e referências, porém ainda precisam reconstruir manualmente o caminho entre uma mudança e seus possíveis efeitos.

## 8.2. O conhecimento está concentrado em pessoas

Todos os participantes afirmaram que a ausência de alguém com conhecimento profundo dificulta entender requisitos ou consequências de mudanças. Isso indica risco de continuidade: o processo fica menos previsível quando especialistas estão indisponíveis, e o conhecimento tácito não está completamente representado nos artefatos.

## 8.3. Os impactos tardios geram custo operacional

Cinco dos sete participantes relataram já ter percebido impactos depois da alteração. As consequências descritas incluem retrabalho, atrasos, dúvidas, atualização de design com pouco prazo e remodelagem.

## 8.4. Versionamento e consistência são requisitos importantes

Versionar histórias foi mencionado como prática existente ou necessidade explícita. O desafio não se limita a guardar versões: é preciso compreender o que mudou, por que mudou e quais requisitos ou artefatos relacionados devem ser revisados.

## 8.5. A comunicação da mudança também importa

A mudança pode estar registrada, mas não ser percebida por quem precisa agir sobre ela. Comentários isolados não garantem que os responsáveis consultem o documento. Uma solução de valor deve considerar sinalização, responsáveis e acompanhamento das consequências.

## 8.6. Facilidade de adoção

A experiência relatada com alternativas mostra que recursos interessantes não bastam se a ferramenta exigir reaprender um fluxo, consumir tempo de configuração ou dificultar a exportação dos dados. O ATHENA deve reduzir o esforço de análise sem criar uma nova carga de trabalho desproporcional.

## 8.7. IA é uma possibilidade, não a hipótese central já comprovada

Um participante citou integração com IA como melhoria desejada, enquanto outros enfatizaram dependências, versionamento, comunicação e consistência. Assim, a pesquisa sustenta com mais força a necessidade de rastreabilidade e análise de impacto do que a necessidade específica de IA. A IA pode ser avaliada como mecanismo de apoio, mantendo validação humana.

# 9. Hipóteses: situação após a pesquisa

| **Hipótese**                                              | **Avaliação**           | **Base empírica**                                                                                                                                                                       |
| --------------------------------------------------------- | ----------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| H1 — Mudanças afetam outras partes                        | Sustentada              | Há relatos de alterações em design, frontend, testes, documentos e tarefas; 5/7 relataram impactos percebidos tardiamente.                                                              |
| H2 — Análise depende de trabalho manual e conhecimento    | Fortemente sustentada   | Todos indicaram dependência da disponibilidade de pessoas experientes; revisão manual e memória aparecem em diversas respostas.                                                         |
| H3 — Rastreabilidade é incompleta                         | Fortemente sustentada   | Todos relataram alguma limitação de relacionamento, visualização ou consulta de dependências.                                                                                           |
| H4 — Impactos tardios causam retrabalho/atrasos           | Sustentada              | Relatos de atrasos, retrabalho, revisão extensa, dúvidas e remodelagem; não houve experiência igual em todos os casos.                                                                  |
| H5 — Ferramentas atuais não resolvem tudo                 | Sustentada              | Ferramentas facilitam edição e gestão, mas não garantem análise completa de impacto.                                                                                                    |
| H6 — Dependência de especialistas prejudica a equipe      | Fortemente sustentada   | 7/7 responderam que a ausência de uma pessoa experiente dificulta compreender requisitos ou consequências.                                                                              |
| H7 — Há oportunidade para uma solução simples e integrada | Parcialmente sustentada | As dificuldades são claras, mas a pesquisa não mede disposição de pagamento nem valida diretamente a adoção de um novo produto. A barreira de aprendizagem foi explicitamente relatada. |
| H8 — IA agrega valor                                      | Ainda inconclusiva      | A IA foi citada por um participante; as respostas não demonstram que seja a necessidade prioritária da maioria.                                                                         |

# 10. Definição revisada do problema

Equipes de desenvolvimento de software têm dificuldade para identificar, rastrear e comunicar todas as consequências de uma mudança em requisitos porque as relações entre histórias, regras de negócio, protótipos, tarefas, testes e outros artefatos são registradas de maneira parcial ou ficam dependentes do conhecimento de pessoas experientes. A análise acaba exigindo revisões manuais e comunicação para isto; quando algum impacto passa despercebido, a equipe pode enfrentar retrabalho, atrasos, dúvidas e inconsistências.

A formulação delimita o problema em três dimensões interligadas: (1) rastreabilidade insuficiente; (2) identificação manual dos impactos; e (3) comunicação e acompanhamento das ações decorrentes da mudança.

# 11. Público-alvo revisado

O público-alvo prioritário do ATHENA são equipes de desenvolvimento de software que mantêm requisitos em evolução e precisam coordenar alterações entre diferentes artefatos e profissionais. O problema tende a ser especialmente relevante quando o sistema cresce, as dependências aumentam ou o conhecimento fica concentrado em poucos especialistas.

- Analistas de requisitos e profissionais de Engenharia de Requisitos.
- Product Owners e responsáveis por organizar e priorizar requisitos.
- Desenvolvedores que precisam entender o contexto e os efeitos das alterações.
- Profissionais de testes/QA responsáveis por revisar cenários afetados.
- Designers de produto que precisam atualizar protótipos conforme requisitos mudam.
- Líderes técnicos e responsáveis por projetos com muitas dependências.

O questionário oferece evidências mais diretas sobre pessoas envolvidas em documentação e gestão de requisitos. A relevância para outros perfis, como QA e designers, é uma inferência coerente com os impactos citados em testes e protótipos, mas não foi medida separadamente na amostra.

# 12. Personas representativas

| **Persona**                  | **Objetivos e contexto**                                                                      | **Dores e necessidades**                                                                         |
| ---------------------------- | --------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------ |
| Analista de requisitos / PO  | Documenta histórias, organiza critérios de aceitação, negocia mudanças e mantém consistência. | Visualizar dependências, identificar impactos e comunicar mudanças sem revisar tudo manualmente. |
| Desenvolvedor                | Implementa histórias e precisa compreender requisitos relacionados e efeitos técnicos.        | Receber contexto claro sobre alterações e evitar retrabalho por informação incompleta.           |
| Profissional de testes / QA  | Confere se a implementação continua atendendo aos requisitos após mudanças.                   | Saber quais cenários e testes devem ser revisados quando um requisito muda.                      |
| Líder técnico / especialista | Conhece a arquitetura e ajuda a equipe a entender dependências complexas.                     | Reduzir a concentração de conhecimento e tornar as relações do sistema consultáveis pela equipe. |

# 13. Análise de concorrentes e alternativas

A análise considera tanto concorrentes diretos quanto alternativas que hoje resolvem parte da necessidade. As respostas dos participantes citam explicitamente Google Docs, OpenProject, Jira, GitBook e MkDocs. O foco comparativo é a gestão de requisitos, e não apenas a documentação ou o gerenciamento geral de tarefas.

| **Alternativa**           | **Solução e público**                                                                                         | **Funcionalidades / acesso**                                                                                                | **Receita e preço**                                                                        | **Pontos fortes**                                                           | **Limitações e oportunidade para ATHENA**                                                                                                                                                             |
| ------------------------- | ------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Google Docs               | Editor de documentos colaborativo, usado por equipes de diferentes áreas.                                     | Edição simultânea, comentários, histórico e compartilhamento via web.                                                       | Modelo gratuito e planos pagos no ecossistema Google; preço depende do plano/região.       | Familiaridade, baixo atrito de adoção, colaboração e facilidade de escrita. | Não oferece, por si só, um mapa completo de dependências nem análise de impacto entre requisitos.                                                                                                     |
| OpenProject               | Plataforma de gestão de projetos e trabalho para equipes.                                                     | Gestão de tarefas, planejamento, acompanhamento e relações entre itens; acesso web e opções de implantação conforme edição. | Há opções comunitárias e comerciais; valores dependem da edição e do plano vigente.        | Organização de atividades e acompanhamento do trabalho.                     | A experiência relatada aponta que a análise de impactos de requisitos ainda pode depender de revisão manual e conhecimento do projeto.                                                                |
| Jira                      | Gestão de trabalho e desenvolvimento de software para equipes técnicas.                                       | Itens de trabalho, fluxos, filtros, relatórios e integrações; acesso web.                                                   | Possui modalidade gratuita e planos pagos; preço varia por plano e número de usuários.     | Ecossistema amplo e flexibilidade de configuração.                          | Um participante relatou que a tentativa de uso não funcionou bem no contexto da equipe. Oportunidade: experiência mais específica para rastreabilidade de requisitos e menor esforço de configuração. |
| GitBook                   | Plataforma de documentação e base de conhecimento para equipes e produtos.                                    | Documentação estruturada, publicação e colaboração via web.                                                                 | Modelo comercial com planos e condições variáveis; verificar tabela vigente do fornecedor. | Organização e apresentação de documentação.                                 | Foi pesquisado por um participante, mas não atendeu integralmente às necessidades e exigia adaptação de fluxo. Não equivale a uma ferramenta especializada em análise de impacto de requisitos.       |
| MkDocs                    | Gerador de sites de documentação a partir de arquivos Markdown, frequentemente usado em documentação técnica. | Documentação versionável e publicação de site; normalmente integrado a repositório e fluxo técnico.                         | Software de código aberto; custos indiretos podem existir para hospedagem e manutenção.    | Documentação organizada, versionável e controlável junto ao código.         | Não fornece, por si só, rastreabilidade semântica entre requisitos e artefatos nem análise de impacto automatizada.                                                                                   |
| Planilhas / Google Sheets | Alternativa usada para tabelas, mapeamentos e controles auxiliares.                                           | Tabelas, filtros, fórmulas e compartilhamento via web.                                                                      | Versão gratuita e opções em planos Google; valores variam.                                 | Flexibilidade e facilidade para montar controles simples.                   | Relações complexas podem ficar difíceis de manter, navegar e atualizar sem erro.                                                                                                                      |

## 13.1. Oportunidade competitiva percebida

A oportunidade do ATHENA não deve ser apresentada como a criação de mais um editor de documentos ou gerenciador de tarefas. A diferenciação proposta é apoiar a manutenção de requisitos com relações explícitas entre artefatos, visualização de dependências, histórico de alterações e indicação dos elementos que podem precisar de revisão. A solução deve conviver com os processos existentes sempre que possível, reduzir a necessidade de migração e manter a pessoa responsável no controle das decisões.

# 14. Implicações para a proposta de valor do ATHENA

- Tornar visíveis as relações entre requisitos e artefatos relacionados, sem depender exclusivamente da memória do analista.
- Ajudar a identificar possíveis impactos quando um requisito muda, apresentando sugestões para revisão humana.
- Manter histórico de alterações e facilitar a comparação entre versões.
- Sinalizar mudanças e apoiar a identificação de responsáveis pelas revisões necessárias.
- Apoiar consistência de templates e padrões de escrita.
- Priorizar uma experiência simples, com baixo custo de aprendizagem e opções de exportação dos dados.

A IA pode apoiar a busca de relações, a identificação de inconsistências e a geração de sugestões, mas não deve ser tratada como substituta da validação humana. Essa decisão de produto decorre do conjunto das respostas: o problema de rastreabilidade foi recorrente, enquanto o interesse explícito por IA apareceu de forma pontual.

# 15. Evidências das interações e materiais utilizados

As evidências analisadas incluem o arquivo exportado das respostas ao formulário “Pesquisa sobre Gestão e Manutenção de Requisitos de Software”, com sete registros individuais e carimbos de data/hora entre 28 de setembro e 13 de outubro de 2026, além da ata da reunião de 28/09/2026 com Thomas.

O público consultado incluiu integrantes do contexto de requisitos do GEDAI, do qual participam alguns membros do grupo, e Paulo Afonso, professor que também respondeu ao questionário. A reunião registrada com Thomas complementa as respostas do formulário com discussões sobre organização por épicos, dependências entre requisitos e módulos, padrão INVEST, status, papéis e permissões, Livro de Requisitos, histórico de versões e possibilidades de integração com IA via MCP.

[Pesquisa sobre Gestão e Manutenção de Requisitos de Software (respostas)](https://docs.google.com/spreadsheets/d/1ZZtp-OhVChrewfMgSwZ99YX9jIcf9ldHqAHk208w6NA/edit?usp=sharing)

A ata de 28/09/2026 registra discussões sobre organização por épicos, dependências entre requisitos e módulos, padrão INVEST, status, papéis e permissões, Livro de Requisitos, histórico de versões e possível integração com IA via MCP. Esses tópicos são apresentados como conteúdo efetivamente registrado na ata, não como transcrição literal de uma entrevista.

[ATA-Reunião_Thomas_Requisitos](https://drive.google.com/file/d/1gyM161uSiEfG-Xce-LrYMuJPp_PYTb0-/view?usp=sharing)

# 16. Decisão do grupo

***DECISÃO: PROSSEGUIR COM AJUSTES.***

A decisão é fundamentada na convergência dos relatos sobre rastreabilidade incompleta, dependência de especialistas, esforço para descobrir impactos e dificuldades de comunicação e versionamento. Sete de sete participantes relataram dificuldades relacionadas à qualidade ou manutenção dos requisitos e à ausência de especialistas; cinco de sete relataram impactos percebidos tardiamente. A média de esforço de 3,9/5 reforça que a identificação dos efeitos de uma mudança não é considerada simples.

Os ajustes devem concentrar a proposta de valor em rastreabilidade e análise de impacto, com histórico de versões e sinalização de mudanças, em vez de priorizar IA como diferencial isolado. A facilidade de adoção deve ser tratada como requisito central, pois a pesquisa mostrou que aprender um novo fluxo e adaptar ferramentas pode impedir a adoção mesmo quando existem recursos úteis.

# 17. Conclusão

A dificuldade central está em reconhecer e acompanhar as relações entre requisitos e os demais elementos do projeto quando uma mudança ocorre. Os participantes descrevem processos manuais, dependência de especialistas, rastreabilidade parcial e consequências como atrasos, retrabalho, dúvidas e remodelagem.

Os dados sustentam a continuidade do ATHENA com foco em tornar as dependências consultáveis, apoiar a análise de impacto, preservar o histórico das alterações e facilitar a comunicação das revisões necessárias. A IA pode ser um recurso complementar, sujeito a validação e supervisão humana. Como a amostra é pequena e foi coletada por questionário, as conclusões devem ser compreendidas como evidência exploratória do contexto estudado, e não como estimativa estatística de todo o mercado.

# Anexo A — Roteiro de questionário

- Há quanto tempo você participa de projetos de desenvolvimento de software?
- Como os requisitos costumam ser registrados ou documentados nos projetos dos quais você participa?
- Quais ferramentas vocês utilizam para trabalhar com requisitos e informações do projeto?
- Pense na última vez em que um requisito mudou durante um projeto. O que aconteceu?
- Quando um requisito muda, como vocês descobrem quais outras partes do projeto podem ser afetadas?
- Normalmente existe algum registro das relações entre requisitos, funcionalidades, regras de negócio, testes ou tarefas?
- Já aconteceu de uma mudança em um requisito afetar outra parte do projeto e a equipe perceber isso somente depois? Quais foram as consequências?
- Quanto esforço normalmente é necessário para descobrir os impactos de uma alteração em um requisito? (escala de 1 a 5)
- Quando uma pessoa que conhece profundamente o projeto não está disponível, isso dificulta entender os requisitos ou as consequências de uma mudança?
- As informações necessárias para compreender um requisito costumam estar concentradas em uma ferramenta ou distribuídas?
- Você já encontrou requisitos desatualizados, contraditórios, duplicados ou pouco claros?
- Como a equipe verifica se todas as partes afetadas por uma mudança de requisito foram atualizadas?
- Qual é hoje a maior dificuldade ao trabalhar com mudanças em requisitos?
- O que funciona bem nas ferramentas e processos atuais?
- O que essas ferramentas ou processos não resolvem bem?
- Você já procurou ou utilizou outra ferramenta para melhorar a gestão ou manutenção de requisitos?
- Se pudesse melhorar apenas uma coisa no processo de gestão e manutenção de requisitos, o que melhoraria?
- Existe alguma dificuldade relacionada a mudanças de requisitos que não foi abordada e que considera importante?

**Anexo B - Entrevista semiestruturada**

**Introdução e Alinhamento**

"Olá, obrigado pelo seu tempo. Estamos fazendo uma pesquisa sobre como as equipes lidam com mudanças em requisitos de software. Não tem resposta certa ou errada, queremos apenas entender a sua experiência real no dia a dia. 

**Contexto e Rotina**

1. Conta um pouco sobre o seu papel atual e como é a sua rotina no acompanhamento ou desenvolvimento de requisitos de software.\
   \

2. Quando um requisito ou regra de negócio é definido, onde e como vocês documentam isso hoje?

**Exploração do Problema**

3. Pense na última vez em que um requisito importante precisou mudar no meio do projeto. O que aconteceu?\
   \

4. Como vocês fizeram para descobrir quais outras partes do sistema (protótipos, testes, código, documentação) seriam afetadas por essa mudança?\
   \

5. O que foi mais difícil ou frustrante nesse processo de mapear os impactos?

**Consequências e Esforço**

6. Já aconteceu de uma mudança ser feita e a equipe só perceber os impactos (ou esquecer de atualizar algo) muito tempo depois? Quais foram as consequências (atrasos, retrabalho, bugs)?\
   \

7. Se a pessoa que mais conhece a arquitetura ou o projeto faltasse hoje, o quanto a equipe sofreria para entender o que precisa ser alterado após uma mudança de requisito?\
   \

8. Com que frequência vocês passam por esse tipo de problema na equipe?

**Ferramentas e Alternativas**

9. Quais ferramentas vocês usam hoje para tentar manter esse controle de mudanças e impactos?\
   \

10. O que funciona muito bem nesse processo/ferramenta atual?\
    \

11. O que não funciona ou gera muito trabalho manual?\
    \

12. Vocês já tentaram buscar outra solução, ferramenta ou método para resolver esse problema de rastreabilidade?

**Encerramento**

13. Se você tivesse uma "varinha mágica" e pudesse melhorar apenas uma coisa na forma como sua equipe lida com mudanças de requisitos hoje, o que seria?\
    \

14. Há algo sobre esse assunto que eu não perguntei e você acha importante compartilhar?
