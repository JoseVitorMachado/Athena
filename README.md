Inteligência e estratégia para prever o impacto das mudanças em requisitos.

<img width="1920" height="1080" alt="Apresentação projeto ATHENA" src="https://github.com/user-attachments/assets/35d84113-8416-4ba6-ac1d-442b18c37deb" />

## Nome da Startup

**ATHENA**

A ATHENA é uma proposta de plataforma voltada à **gestão inteligente de requisitos de software**, com foco na automação da rastreabilidade e na análise de impacto de mudanças em requisitos.

O objetivo é apoiar equipes de software na compreensão das relações existentes entre requisitos, regras de negócio, critérios de aceitação, testes, documentos e demais artefatos associados ao desenvolvimento de um sistema.

## Integrantes do Grupo

| Integrante | GitHub |
|---|---|
| Ana Clara Rocha Gomes | @clararochag |
| Bárbara Oliveira Fonseca | @babifonsec |
| Fernando Chaves Scarabeli | @FernandoScarabeli |
| Jhennifer Hellen Campos Silva | @jhennifer-silva |
| José Vítor Machado de Oliveira | JoseVitorMachado |

**Disciplina:** Sistemas Distribuídos  
**Professor:** André de Lima Salgado

---

## Problema e motivação

<img width="1027" height="793" alt="image" src="https://github.com/user-attachments/assets/37e3ed94-e7de-431b-a018-8488b298b654" />

Requisitos de software não permanecem estáticos durante todo o desenvolvimento de um sistema. Eles evoluem conforme surgem novas necessidades, alterações de negócio, decisões de projeto e mudanças no próprio produto.

Essa evolução cria uma rede de dependências entre diferentes artefatos do projeto, como histórias de usuário, regras de negócio, critérios de aceitação, testes, documentos, tarefas, código e requisitos relacionados.

O problema surge quando essas relações não estão explícitas ou não são mantidas de forma organizada. Uma alteração aparentemente simples em um requisito pode exigir a revisão de diversos outros elementos do projeto.

Por exemplo, uma mudança em um requisito relacionado à autenticação pode exigir a revisão de critérios de aceitação, regras de negócio, testes e outros requisitos relacionados.

Quando a rastreabilidade entre esses elementos não está clara, os efeitos de uma mudança podem não ser percebidos imediatamente. Como consequência, podem surgir inconsistências, retrabalho e atrasos durante o desenvolvimento.

Outro ponto importante é que as informações de um projeto normalmente ficam distribuídas em diferentes ferramentas e documentos. Requisitos podem estar em uma ferramenta de gestão, código em um repositório, documentos em serviços de armazenamento e decisões importantes podem permanecer apenas no conhecimento de determinados integrantes da equipe.

Nesse cenário, parte do contexto do projeto pode se perder, principalmente quando:

1. as informações estão espalhadas em fontes diferentes;
2. parte do conhecimento depende de pessoas específicas;
3. mudanças são revisadas de forma incompleta;
4. inconsistências são percebidas apenas posteriormente, gerando retrabalho.


A ATHENA surge a partir desse problema: **tornar visíveis as relações entre os elementos de um projeto para que a equipe consiga compreender melhor o impacto provocado por uma mudança em um requisito**.

---

## O contexto de um projeto se perde quando as relações não estão visíveis

Um dos principais problemas identificados é a dificuldade de preservar o contexto completo de um projeto quando requisitos, documentos, regras, testes e decisões permanecem separados.

A ausência de relações explícitas entre esses elementos gera quatro situações principais:

### Informações ficam em fontes diferentes

Os dados necessários para compreender um requisito podem estar distribuídos em diferentes ferramentas, documentos e ambientes.

### Parte do conhecimento depende de pessoas específicas

Quando determinadas relações ou decisões não estão documentadas, o entendimento do sistema passa a depender da memória dos integrantes que participaram dessas decisões.

### Mudanças podem ser revisadas de forma incompleta

Sem uma visão clara das dependências, uma equipe pode modificar um requisito sem perceber que outros artefatos também precisam ser revisados.

### Inconsistências aparecem posteriormente

Quando um impacto não é identificado no momento da alteração, o problema pode surgir apenas durante a implementação, os testes ou etapas posteriores, aumentando a possibilidade de retrabalho.

A ATHENA busca reduzir esse problema tornando as relações entre os elementos do projeto mais visíveis e acessíveis.

---

## Qual é a ideia?

A proposta da ATHENA é desenvolver uma ferramenta para **automação de rastreabilidade e análise de impacto em requisitos de software**.

A plataforma deverá reunir informações sobre os requisitos e suas relações, permitindo que a equipe visualize de maneira mais clara quais elementos podem ser afetados quando uma alteração acontece.

Em vez de depender apenas da memória individual dos membros da equipe, a solução busca registrar essas relações de maneira estruturada, tornando o conhecimento sobre os requisitos mais acessível e compartilhado.

A ideia central pode ser resumida da seguinte forma:

> Quando um requisito muda, a ATHENA ajuda a equipe a descobrir o que precisa ser revisado.

A ferramenta não pretende substituir a análise realizada pelos profissionais responsáveis pelo projeto. Seu papel é apoiar esse processo, apresentando relações, possíveis impactos e informações relevantes para a tomada de decisão.

---

## Esboço da solução
<img width="1255" height="688" alt="image" src="https://github.com/user-attachments/assets/f598176b-c898-43d0-8ec7-52d095c97862" />


A ATHENA deverá apoiar o fluxo de análise de requisitos por meio de cinco ações principais.

### 1. Registrar ou importar requisitos e documentos

A plataforma permitirá centralizar requisitos e documentos relevantes para o projeto, criando uma base de informações que poderá ser utilizada nas análises posteriores.

### 2. Relacionar requisitos, critérios e regras

Os diferentes elementos do projeto poderão ser relacionados entre si, permitindo representar dependências e conexões existentes entre requisitos, critérios de aceitação, regras de negócio e outros artefatos.

### 3. Detectar uma mudança em um requisito

Quando um requisito for alterado, o sistema deverá identificar essa mudança e utilizar as relações existentes para iniciar a análise de impacto.

### 4. Sugerir possíveis impactos com justificativa

A ATHENA deverá apresentar os elementos que podem ser afetados pela alteração e fornecer uma justificativa para as sugestões apresentadas.

O objetivo não é determinar automaticamente o que deve ser feito, mas oferecer informações que ajudem a equipe a compreender quais pontos precisam ser revisados.

### 5. Registrar revisão humana e decisão

As sugestões apresentadas pelo sistema deverão passar pela análise dos profissionais responsáveis pelo projeto.

A decisão tomada pela equipe poderá ser registrada, mantendo um histórico das revisões e das decisões realizadas.

Dessa forma, a ATHENA atua como uma ferramenta de apoio: **o sistema auxilia na identificação e análise dos impactos, enquanto a decisão final permanece com a equipe responsável pelo projeto**.

---

## Fundamentação do problema e da solução

A proposta apresentada para a ATHENA é sustentada por quatro trabalhos relacionados à análise de impacto, rastreabilidade, identificação de anomalias em requisitos e uso de modelos de linguagem no desenvolvimento de software.

### Arora et al. (2015)

O trabalho **“Change impact analysis for natural language requirements: an NLP approach”** aborda a análise de impacto de mudanças em requisitos escritos em linguagem natural.

A relação com a ATHENA está na necessidade de identificar quais elementos podem ser afetados quando um requisito é alterado. Essa ideia sustenta uma das principais funções propostas para a plataforma: ajudar a equipe a compreender as consequências de uma mudança antes que seus efeitos apareçam em etapas posteriores do desenvolvimento.

### Rubasinghe, Meedeniya e Perera (2018)

O trabalho **“Traceability management with impact analysis in DevOps based software development”** relaciona a gestão de rastreabilidade com a análise de impacto em ambientes de desenvolvimento de software baseados em DevOps.

Essa referência está diretamente ligada ao problema tratado pela ATHENA: requisitos e demais artefatos de um projeto possuem relações importantes que precisam ser identificadas e mantidas para que uma alteração possa ser analisada de forma adequada.

### Pereira, Costa e Parreira Júnior (2024)

O estudo **“A comparative study of tools for anomaly detection in software requirements”** trata da detecção de anomalias em requisitos de software.

Esse trabalho contribui para a proposta de apoiar equipes na identificação de problemas presentes na documentação de requisitos, ampliando a capacidade de revisão e reduzindo a possibilidade de inconsistências permanecerem sem identificação durante a evolução do projeto.

### Tabarsi et al. (2026)

O trabalho **“LLMs’ reshaping of people, processes, products, and society in software development”** discute o uso de modelos de linguagem de grande escala no contexto do desenvolvimento de software.

Na proposta da ATHENA, recursos baseados em inteligência artificial podem ser utilizados como apoio à análise dos requisitos e dos possíveis impactos de mudanças. Entretanto, a revisão humana permanece como parte essencial do processo.


Por isso, a plataforma adota como princípio que **o sistema apoia a análise, mas a decisão final continua com as pessoas responsáveis pelo projeto**.

| Referência | Relação com a ATHENA |
|---|---|
| Arora et al. (2015) | Análise de impacto em requisitos escritos em linguagem natural |
| Rubasinghe, Meedeniya e Perera (2018) | Rastreabilidade e análise de impacto em ambientes de desenvolvimento |
| Pereira, Costa e Parreira Júnior (2024) | Detecção de anomalias em documentação de requisitos |
| Tabarsi et al. (2026) | Uso de LLMs no desenvolvimento de software com participação e revisão humana |

Em conjunto, esses trabalhos fundamentam a proposta de uma plataforma capaz de relacionar requisitos, apoiar a análise de mudanças, identificar possíveis problemas e fornecer informações para auxiliar a equipe durante a tomada de decisão.


---

## Impacto social esperado

O impacto esperado da ATHENA está relacionado à ampliação do acesso ao conhecimento sobre os requisitos de um projeto.

Ao tornar relações e dependências mais visíveis, a plataforma busca diminuir a dependência do conhecimento individual e facilitar a compreensão do projeto por diferentes membros de uma equipe.

### Quem é beneficiado?

A proposta pode beneficiar principalmente:

- analistas de requisitos;
- Product Owners;
- desenvolvedores;
- profissionais de QA;
- gestores;
- equipes acadêmicas envolvidas no desenvolvimento de software.

### De que forma?

Os principais benefícios esperados são:

- diminuição de pontos cegos na documentação;
- redução de retrabalho causado por alterações incompletas;
- apoio a uma tomada de decisão mais fundamentada e transparente;
- menor dependência da memória individual de membros específicos da equipe;
- maior visibilidade das relações existentes entre requisitos e outros artefatos;
- maior facilidade para identificar quais elementos precisam ser revisados após uma mudança.

### Como medir o impacto?

O impacto da solução poderá ser acompanhado por indicadores como:

- **Tempo para identificar impactos:** tempo necessário para localizar os elementos que precisam ser revisados após uma alteração.
- **Cobertura de rastreabilidade:** proporção de requisitos e artefatos que possuem relações registradas no sistema.
- **Sugestões consideradas úteis pela equipe:** quantidade ou percentual de sugestões que efetivamente contribuíram para o processo de revisão.
- **Taxa de assertividade da IA:** percentual de sugestões de impacto e de anomalias aceitas pela equipe durante a revisão humana.

Essas métricas permitem avaliar se a ferramenta está contribuindo para tornar o processo de manutenção de requisitos mais claro, rastreável e útil para os profissionais envolvidos.

---

## Referências

1. ARORA, C. et al. **Change impact analysis for natural language requirements: an NLP approach.** IEEE RE, 2015.

2. RUBASINGHE, I.; MEEDENIYA, D.; PERERA, I. **Traceability management with impact analysis in DevOps based software development.** ICACCI, 2018.

3. PEREIRA, G.; COSTA, H.; PARREIRA JÚNIOR, P. **A comparative study of tools for anomaly detection in software requirements.** SBQS, 2024.

4. TABARSI, A. et al. **LLMs’ reshaping of people, processes, products, and society in software development.** *Empirical Software Engineering*, 2026.
