**ENTREGA 1 - DESCOBERTA DO PROBLEMA E DA OPORTUNIDADE**

***ATHENA***

Gestão, manutenção e rastreabilidade de requisitos de software

Lavras - MG

2026

**1. Identificação do grupo**

Nome provisório do projeto/empreendimento: ATHENA.

| **Integrante**                 | **Responsabilidade proposta**                                                                                                                                                                                                                 |
| ------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Ana Clara Rocha Gomes          | Responsável pelo design da solução e prototipação das interfaces, contribuindo para a definição da experiência do usuário e para a representação visual das funcionalidades previstas.                                                        |
| Bárbara Oliveira Fonseca       | Responsável pela estruturação e organização dos dados, além de atuar no apoio à estruturação da LLM, contribuindo para a preparação das informações que serão utilizadas pelos recursos de inteligência artificial do sistema.                |
| Fernando Chaves Scarabeli      | Responsável pela estruturação do MVP base e pelo desenvolvimento do back-end, incluindo a implementação das principais regras de negócio, serviços e integrações necessárias para o funcionamento inicial da solução.                         |
| Jhennifer Hellen Campos Silva  | Responsável pelo desenvolvimento do front-end, implementando as interfaces e fluxos de interação definidos para o sistema.                                                                                                                    |
| José Vítor Machado de Oliveira | Responsável pelo levantamento e organização dos requisitos, estruturação das entrevistas com potenciais usuários, documentação das etapas do projeto e gestão do produto, acompanhando a priorização das funcionalidades e a evolução do MVP. |

Link do repositório: [https://github.com/JoseVitorMachado/Athena](https://github.com/JoseVitorMachado/Athena)

O ATHENA é uma proposta de plataforma voltada à gestão e à manutenção de requisitos de software. Nesta primeira entrega, porém, o foco não é apresentar a solução em detalhes, mas delimitar e investigar o problema que motiva a oportunidade de negócio, conforme solicitado na atividade de Empreendedorismo.

# 2. Três problemas inicialmente considerados

A partir do contexto de Engenharia de Requisitos e das dificuldades descritas no documento de concepção do projeto, foram estruturados três problemas candidatos relacionados à manutenção de requisitos. Eles representam recortes distintos de uma mesma área e ajudam a justificar a escolha do problema prioritário.

**Problema candidato 1 -**Dificuldade de estudantes universitários para montar a grade de horários sem conflitos

**Breve descrição:**Estudantes universitários podem enfrentar dificuldade para organizar as disciplinas que pretendem cursar em cada semestre, principalmente quando precisam comparar horários, turmas disponíveis, pré-requisitos e possíveis conflitos. Em muitos casos, esse planejamento exige consulta manual a diferentes informações e várias tentativas até chegar a uma combinação viável.

**Público relacionado:** Estudantes de graduação, especialmente aqueles que precisam montar ou ajustar sua grade de disciplinas a cada período letivo.

**Motivo pelo qual parece relevante:** A dificuldade de planejamento pode gerar perda de tempo, conflitos de horário, escolhas inadequadas de turmas e maior esforço no período de matrícula. Além disso, trata-se de um problema com público acessível para investigação dentro do ambiente universitário.

**Problema candidato 2 -**Dificuldade de pequenos negócios de manutenção e assistência técnica para acompanhar ordens de serviço

**Breve descrição:** Pequenos negócios que realizam manutenção, reparos ou assistência técnica podem controlar solicitações, orçamentos, aprovações, andamento dos serviços e comunicação com clientes por meio de planilhas, anotações ou aplicativos de mensagens. Quando essas informações ficam dispersas, torna-se mais difícil acompanhar o histórico e o estado de cada atendimento.

**Público relacionado:** Pequenos negócios de manutenção e assistência técnica, seus funcionários e clientes que acompanham a execução dos serviços.

**Motivo pelo qual parece relevante:**A falta de organização pode causar perda de informações, atrasos na atualização do cliente, dificuldade para localizar históricos e maior esforço para acompanhar vários serviços simultaneamente. O problema também pode ser investigado diretamente com pequenos prestadores e empresas locais.

**Problema candidato 3 -** Dificuldade para identificar os impactos de mudanças em requisitos de software

**Breve descrição:**Em projetos de software, a alteração de um requisito pode afetar outros requisitos, regras de negócio, critérios de aceitação, funcionalidades, tarefas e cenários de teste. Quando essas relações não estão claramente registradas, a equipe pode ter dificuldade para identificar de forma completa quais elementos precisam ser revisados após uma mudança.

**Público relacionado:** analistas de requisitos, product Owners,  desenvolvedores, profissionais de QA/testes, gestores de projeto e demais integrantes de equipes de desenvolvimento de software.

**Motivo pelo qual parece relevante:** A identificação incompleta dos impactos pode gerar retrabalho, inconsistências, atualizações parciais e descoberta tardia de problemas. Além disso, a literatura já reunida pelo grupo indica que análise de impacto, rastreabilidade e manutenção de requisitos são temas relevantes na Engenharia de Software.

Após comparar os três problemas candidatos, o grupo selecionou para investigação o problema relacionado à **mudança e à manutenção de requisitos de software.**Esse problema foi escolhido por estar alinhado à área de Sistemas de Informação, permitir acesso a profissionais e equipes de desenvolvimento para validação e apresentar potencial de investigação sobre rastreabilidade, análise de impacto e redução de retrabalho. A partir dele, o projeto ATHENA poderá ser discutido posteriormente como uma possível resposta ao problema, caso as evidências coletadas confirmem as hipóteses iniciais.

# 3. Problema escolhido

## 3.1 Descrição objetiva

Em projetos de software, requisitos não existem de forma isolada. Histórias de usuário, regras de negócio, critérios de aceitação, requisitos não funcionais, funcionalidades e cenários de teste podem manter relações entre si. Quando um requisito é modificado, a alteração pode repercutir sobre outros itens relacionados. O problema escolhido consiste na dificuldade de identificar, de forma completa e rastreável, quais requisitos e artefatos precisam ser revisados quando uma mudança ocorre.

As relações entre esses itens frequentemente permanecem implícitas ou distribuídas entre documentos, ferramentas e conversas. Por isso, a equipe pode depender da memória e da disponibilidade de pessoas que conhecem o projeto para descobrir os efeitos de uma alteração. Quando essa análise é incompleta, parte da especificação pode ser atualizada enquanto outros elementos permanecem desatualizados ou inconsistentes.

## 3.2 Contexto em que ocorre

O problema ocorre principalmente durante a evolução e a manutenção de projetos de software. Mesmo quando uma especificação inicial está definida, requisitos podem mudar em razão de novas necessidades, esclarecimentos do cliente, decisões de negócio, restrições técnicas ou descobertas realizadas durante o desenvolvimento. Nesses momentos, a equipe precisa compreender não apenas o requisito alterado, mas também os elementos relacionados a ele.

Um exemplo apresentado na concepção do ATHENA é a alteração de um requisito de autenticação. Uma mudança desse tipo pode exigir revisão de critérios de aceitação, regras de negócio, cenários de teste e funcionalidades relacionadas. Se as relações não estiverem registradas ou forem difíceis de localizar, a alteração pode ser aplicada apenas ao requisito principal, deixando outros artefatos desatualizados.

## 3.3 Frequência ou recorrência esperada

A frequência real ainda não foi validada com usuários e constitui uma das hipóteses que será investigada na Entrega 2. A expectativa inicial é que o problema seja recorrente em projetos nos quais requisitos mudam ao longo do desenvolvimento, várias pessoas participam da especificação e implementação, e as informações estão distribuídas entre diferentes ferramentas.

Nesta etapa, portanto, não se afirma que o problema ocorre com determinada frequência. A pesquisa com potenciais usuários deverá verificar quando ele acontece, com que frequência e em quais tipos de projeto sua intensidade é maior.

## 3.4 Consequências

A partir da fundamentação do projeto, as principais consequências esperadas são:

- retrabalho causado pela descoberta tardia de impactos;
- requisitos relacionados que permanecem desatualizados;
- inconsistências entre requisitos, regras, testes e funcionalidades;
- maior tempo gasto em consultas manuais e reconstrução de contexto;
- dependência do conhecimento de pessoas específicas da equipe;
- dificuldade para transferir conhecimento quando integrantes mudam ou não estão disponíveis;
- aumento do esforço necessário para revisar alterações e manter a especificação coerente.

## 3.5 Pessoas e organizações potencialmente afetadas

O problema pode afetar profissionais e organizações envolvidos na especificação, desenvolvimento e manutenção de software. Entre os principais perfis estão:

- analistas de requisitos;
- Product Owners;
- desenvolvedores;
- profissionais de qualidade e testes;
- gestores de projeto;
- equipes de desenvolvimento de software que trabalham com requisitos em evolução.

# 4. Público-alvo preliminar

## 4.1 Quem provavelmente possui o problema

O público preliminar é formado por profissionais que participam de projetos de software e precisam criar, consultar, alterar, implementar, testar ou acompanhar requisitos. A hipótese é que a dor seja mais perceptível em equipes nas quais requisitos mudam ao longo do tempo e a informação do projeto está distribuída entre diferentes artefatos ou ferramentas.

## 4.2 Características relevantes

- atua em equipe de desenvolvimento ou manutenção de software;
- trabalha com histórias de usuário, requisitos funcionais ou não funcionais, regras de negócio ou critérios de aceitação;
- participa de projetos nos quais requisitos sofrem alterações durante o ciclo de desenvolvimento;
- consulta mais de uma ferramenta ou fonte para compreender o contexto de uma mudança;
- precisa comunicar ou validar mudanças com outros membros da equipe.

## 4.3 Contexto de utilização

O contexto principal são projetos de software em andamento, especialmente durante atividades de manutenção de requisitos, revisão de escopo, refinamento, implementação, testes e análise de mudanças. O problema pode ser mais relevante em equipes com múltiplos papéis, grande quantidade de artefatos e uso de ferramentas diferentes para documentação, código e gestão do trabalho.

## 4.4 Quem é usuário

Os usuários potenciais são as pessoas que precisam consultar ou tomar decisões sobre requisitos e seus impactos. Inicialmente, consideram-se Product Owners, analistas de requisitos, desenvolvedores, profissionais de QA/testes e gestores de projeto.

## 4.5 Quem poderia ser cliente ou pagador

A hipótese preliminar é que o cliente ou pagador seja uma organização que desenvolva ou mantenha software e possua interesse em reduzir retrabalho, melhorar rastreabilidade e facilitar a manutenção de requisitos. Essa hipótese ainda não está validada. As entrevistas da Entrega 2 deverão ajudar a compreender quem reconhece maior valor no problema e, futuramente, quem poderia justificar investimento em uma solução.

# 5. Evidências secundárias

A concepção atual do ATHENA está fundamentada em quatro referências acadêmicas que abordam aspectos complementares do problema: análise de impacto, rastreabilidade, qualidade de requisitos e uso de modelos de linguagem no desenvolvimento de software. Essas fontes sustentam a relevância técnica do problema, embora ainda seja necessário validá-lo diretamente com potenciais usuários.

## 5.1 Análise de impacto de mudanças

Arora et al. (2015) investigam a análise de impacto de mudanças em requisitos escritos em linguagem natural. O estudo mostra que uma alteração pode exigir revisão de outros requisitos semanticamente relacionados, inclusive quando os vínculos entre eles não estão explicitamente registrados. Essa evidência reforça a hipótese de que identificar manualmente todos os elementos afetados por uma mudança pode ser difícil.

## 5.2 Rastreabilidade entre artefatos

Rubasinghe, Meedeniya e Perera (2018) discutem a gestão de rastreabilidade e a análise de impacto entre artefatos de software no contexto de desenvolvimento contínuo. A pesquisa reforça a importância de considerar requisitos, documentos e outros artefatos de maneira conectada quando uma mudança acontece.

## 5.3 Qualidade e anomalias em requisitos

Pereira, Costa e Parreira (2024) analisam ferramentas voltadas à identificação de anomalias em requisitos. A discussão inclui problemas como ambiguidades, duplicidades, inconsistências e lacunas de informação, indicando que a manutenção da qualidade da documentação é uma preocupação relevante no processo de evolução de requisitos.

## 5.4 Uso de inteligência artificial com supervisão humana

Tabarsi et al. (2026) analisam a incorporação de modelos de linguagem ao desenvolvimento de software. A referência destaca que possíveis ganhos dependem de verificação crítica das saídas geradas. Para o problema investigado, isso sustenta a possibilidade de apoio automatizado no futuro, mas não substitui a necessidade de decisão humana sobre impactos e relações entre requisitos.

## 5.5 Limitação das evidências disponíveis nesta versão

No conteúdo fornecido ao grupo para elaboração deste documento, as evidências secundárias identificadas são principalmente artigos científicos. Não foram fornecidos dados estatísticos de mercado, relatórios institucionais ou levantamentos quantitativos específicos sobre a frequência do problema em empresas. Por isso, esta versão não inventa números ou percentuais. Caso o grupo deseje fortalecer a entrega, poderá complementar posteriormente com dados públicos, relatórios ou outras fontes confiáveis, mantendo clara a origem de cada evidência.

# 6. Soluções atuais e alternativas existentes

O problema não decorre da ausência total de ferramentas. Equipes de software já utilizam diferentes plataformas para documentação, acompanhamento de tarefas, versionamento de código e comunicação. O desafio identificado é que cada ferramenta tende a atender uma parte do trabalho, enquanto as relações entre requisitos e os impactos de mudanças podem continuar distribuídos ou implícitos.

| **Alternativa atual**             | **Uso principal**                                         | **Limitação relacionada ao problema investigado**                                                                  |
| --------------------------------- | --------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------ |
| Google Drive / Google Docs        | Documentação e compartilhamento de informações            | Pode concentrar documentos, mas as relações entre requisitos, código, tarefas e testes podem continuar implícitas. |
| Jira                              | Gestão de trabalho, issues e acompanhamento de atividades | Pode registrar histórias e tarefas, mas nem sempre reúne todo o contexto documental e as relações entre artefatos. |
| GitHub                            | Código, issues, pull requests e versionamento             | É central para evolução técnica, mas não é necessariamente a fonte principal da especificação de requisitos.       |
| OpenProject                       | Gestão de projetos e acompanhamento de atividades         | Pode apoiar o trabalho, porém o contexto de requisitos pode permanecer distribuído em outras fontes.               |
| Documentos, planilhas e conversas | Registro manual e comunicação                             | São flexíveis, mas aumentam a dependência de organização manual, memória e recuperação de contexto.                |

As ferramentas citadas acima devem ser tratadas como alternativas atuais e concorrentes indiretos, pois o documento de concepção do ATHENA afirma explicitamente que a proposta não pretende substituí-las. O material fornecido não apresenta uma lista de concorrentes diretos especializados em gestão e rastreabilidade de requisitos. Portanto, para manter a fidelidade às fontes, esta versão registra o mapeamento de concorrentes diretos como uma atividade ainda necessária para a etapa seguinte, em vez de inventar empresas ou produtos.

## 6.1 Como o problema é resolvido atualmente

Com base no problema descrito, as equipes podem resolver a análise de mudanças por meio de consulta manual a documentos, busca em ferramentas distintas, comunicação direta com integrantes que conhecem o projeto, revisão de tarefas e artefatos relacionados e manutenção manual de vínculos. A Entrega 2 deverá verificar quais dessas práticas realmente são utilizadas pelos entrevistados e quais exigem maior esforço.

# 7. Hipóteses iniciais

As hipóteses abaixo representam suposições que orientam a investigação. Elas não devem ser tratadas como fatos confirmados. O objetivo da próxima etapa será confrontá-las com evidências coletadas junto ao público-alvo.

| **Hipótese**                     | **Formulação inicial**                                                                                                                                                                                                                                          |
| -------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Hipótese de problema**         | Equipes de desenvolvimento de software enfrentam dificuldade para identificar todos os requisitos e artefatos que podem ser afetados quando um requisito é alterado.                                                                                            |
| **Hipótese de cliente**          | Equipes e organizações que desenvolvem ou mantêm software podem reconhecer valor em melhorar o processo de manutenção, rastreabilidade e análise de mudanças em requisitos.                                                                                     |
| **Hipótese de necessidade**      | Profissionais envolvidos em requisitos precisam recuperar contexto, visualizar relações e identificar possíveis impactos de mudanças com menos dependência de consultas manuais e memória individual.                                                           |
| **Hipótese preliminar de valor** | Uma abordagem que torne relações entre requisitos mais visíveis e apoie a identificação dos possíveis impactos de mudanças pode reduzir esforço de análise, retrabalho e risco de alterações incompletas, mantendo a decisão final sob responsabilidade humana. |

# 8. Plano para a Entrega 2 - Validação do problema

A Entrega 2 deverá verificar se as pessoas que imaginamos como público realmente vivenciam o problema e o consideram relevante. O plano abaixo prioriza entrevistas semiestruturadas, permitindo que o grupo siga um roteiro comum e, ao mesmo tempo, explore situações reais relatadas pelos participantes.

## 8.1 Quem será entrevistado

Serão priorizadas pessoas que já tenham participado de projetos reais de desenvolvimento ou manutenção de software e que tenham contato com requisitos. Os perfis prioritários são:

- Product Owners;
- analistas de requisitos;
- desenvolvedores;
- profissionais de QA/testes;
- gestores ou líderes de projetos de software.

A participação de diferentes perfis permitirá comparar a percepção de quem define requisitos, de quem implementa mudanças e de quem verifica seus efeitos.

## 8.2 Como os participantes serão encontrados

Como estratégia de recrutamento, o grupo poderá utilizar sua rede acadêmica e profissional, buscando pessoas em projetos da universidade, empresas juniores, grupos de desenvolvimento, contatos profissionais e redes como LinkedIn. O critério principal será a experiência real com projetos de software e não apenas a proximidade pessoal com os integrantes.

## 8.3 Quantidade pretendida

O trabalho exige pelo menos 8 interações válidas com representantes do público-alvo e recomenda que ao menos 5 sejam entrevistas semiestruturadas. Como meta operacional, o grupo pretende realizar entre 8 e 10 interações válidas, garantindo pelo menos 5 entrevistas semiestruturadas. Questionários poderão ser utilizados apenas como complemento, não como substituição da análise qualitativa das entrevistas.

## 8.4 Perguntas iniciais

1. Qual é o seu papel ou principal atuação em projetos de software?
2. Como os requisitos costumam ser registrados ou documentados nos projetos dos quais você participa?
3. Quais ferramentas vocês utilizam para trabalhar com requisitos e informações do projeto?
4. Pense na última vez em que um requisito mudou durante um projeto. O que aconteceu?
5. Quando um requisito muda, como vocês descobrem quais outras partes do projeto podem ser afetadas?
6. Existe algum registro das relações entre requisitos, funcionalidades, regras de negócio, testes ou tarefas? Como isso é feito?
7. Já aconteceu de uma mudança em um requisito afetar outra parte do projeto e a equipe perceber isso somente depois? O que aconteceu?
8. Quais consequências esse tipo de situação costuma trazer?
9. Com que frequência problemas relacionados a mudanças de requisitos acontecem?
10. Quanto tempo ou esforço normalmente é necessário para analisar os impactos de uma mudança?
11. As informações necessárias para compreender um requisito ficam concentradas em uma ferramenta ou distribuídas entre diferentes fontes?
12. O processo depende do conhecimento de pessoas específicas da equipe? O que acontece quando essas pessoas não estão disponíveis?
13. O que funciona bem nas ferramentas e processos atuais?
14. O que funciona mal ou exige mais esforço?
15. Você já procurou ou testou outra solução para melhorar esse processo? Por quê?
16. Se pudesse melhorar uma única coisa no processo de gestão e manutenção de requisitos, o que melhoraria?

Durante as entrevistas, deve-se priorizar exemplos concretos e perguntas de aprofundamento, como “quando isso aconteceu?”, “o que vocês fizeram?”, “quanto esforço foi necessário?” e “qual foi a consequência?”. A intenção é evitar perguntas indutivas e descobrir o comportamento atual antes de apresentar qualquer solução.

## 8.5 Registro e análise das entrevistas

Para cada interação, o grupo deverá registrar o perfil do participante, as principais situações relatadas, ferramentas utilizadas, frequência percebida, consequências, dificuldades, padrões e divergências. Depois, as respostas deverão ser comparadas com as hipóteses iniciais, classificando-as como confirmadas, enfraquecidas ou rejeitadas.

Também deverá ser registrado se as evidências apontam para prosseguir com o problema atual, prosseguir com ajustes ou pivotar a investigação. Respostas individuais não devem ser tratadas como confirmação automática; a análise deve buscar padrões entre participantes.

# 10. Síntese da oportunidade investigada

A oportunidade investigada parte de um problema de manutenção de requisitos: quando um requisito muda, a equipe pode ter dificuldade para identificar todas as partes relacionadas que precisam ser revisadas. Esse desafio é agravado quando informações estão espalhadas em diferentes ferramentas, relações não são registradas e o entendimento depende de pessoas específicas.

A literatura utilizada na concepção do projeto sustenta que análise de impacto, rastreabilidade e qualidade da documentação são temas relevantes em Engenharia de Requisitos. Entretanto, a existência de literatura não substitui a validação com usuários. Por isso, a próxima etapa deverá verificar se o problema aparece de forma recorrente na prática, quais perfis são mais afetados, como as equipes resolvem a situação hoje e qual intensidade de esforço ou consequência está associada a ela.

Somente após essa validação o grupo deverá consolidar a proposta de valor e decidir quais funcionalidades realmente precisam compor o MVP. Essa abordagem mantém o foco da Entrega 1 na compreensão do problema e evita assumir antecipadamente que a solução inicialmente imaginada é necessariamente a melhor resposta.

# Referências

ARORA, C.; SABETZADEH, M.; GOKNIL, A.; BRIAND, L. C.; ZIMMER, F. Change impact analysis for Natural Language requirements: an NLP approach. 2015 IEEE 23rd International Requirements Engineering Conference, 2015, p. 6-15.

RUBASINGHE, I. D.; MEEDENIYA, D. A.; PERERA, I. Traceability Management with Impact Analysis in DevOps based Software Development. 2018 International Conference on Advances in Computing, Communications and Informatics, 2018, p. 1956-1962.

PEREIRA, F. R.; COSTA, H. A. X.; PARREIRA, P. A. A Comparative Study of Tools for Anomaly Detection in Software Requirements. Brazilian Symposium on Software Quality, 2024, p. 11-21.

TABARSI, B. et al. LLMs’ reshaping of people, processes, products, and society in software development: a qualitative exploration with early adopters. Empirical Software Engineering, 2026.
