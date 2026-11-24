## 1. Parte A — Proposta de valor

### 1.1 Segmento prioritário
Equipes de desenvolvimento e manutenção de software que trabalham com requisitos em evolução e precisam coordenar mudanças entre diferentes artefatos e papéis. O ponto de entrada proposto são equipes que já documentam requisitos, mas ainda dependem de buscas, referências manuais e conhecimento tácito para descobrir relações e impactos.

### 1.2 Problema prioritário
Quando um requisito é alterado, a equipe pode não identificar todos os requisitos, regras, tarefas, protótipos ou testes que precisam ser reconsiderados. Como as relações nem sempre estão explícitas ou atualizadas, a análise depende de consultas manuais em várias fontes e da disponibilidade de pessoas que conhecem o projeto.

### 1.3 Necessidade central
Encontrar e compreender as relações entre requisitos, preservar o histórico das alterações e organizar a revisão dos itens potencialmente afetados, sem introduzir um fluxo de trabalho difícil de aprender ou retirar o controle dos responsáveis humanos.

### 1.4 Proposta de valor
O ATHENA ajuda equipes de software a organizar requisitos, visualizar as relações entre eles e acompanhar mudanças com histórico e revisão controlada, facilitando a identificação do que pode precisar ser atualizado sem depender exclusivamente de buscas manuais e da memória da equipe.

### 1.5 Benefício principal
Tornar a análise de mudanças mais rastreável e organizada: em vez de reconstruir todo o contexto de forma dispersa, a equipe pode consultar relações registradas, examinar versões e encaminhar os itens pertinentes para revisão. A redução efetiva de retrabalho ou de tempo ainda é uma hipótese a ser testada com o MVP; não é um resultado já medido.

### 1.6 Diferenciais propostos
- Relações explícitas entre requisitos e visualização em mapa, com tipos definidos para dependência, bloqueio e associação.
- Histórico de versões e publicação explícita, preservando o conteúdo publicado enquanto uma nova versão é elaborada ou revisada.
- Fluxo de submissão, decisão humana e publicação no Livro de Requisitos, com acesso limitado aos membros autorizados.
- Assistência opcional por agente externo via MCP, com recuperação de contexto e sugestões que apresentam evidências, sem converter automaticamente sugestões em relações oficiais.
- Posicionamento complementar a ferramentas existentes; o baixo atrito de adoção e a portabilidade dos dados devem orientar as decisões de produto.

### 1.7 Justificativa baseada nas evidências
A proposta prioriza a rastreabilidade e a análise de impacto porque a entrega 2 registrou dificuldades com qualidade e manutenção dos requisitos em todos os sete respondentes, dificuldades relacionadas à ausência de especialistas também em todos os sete, cinco relatos de impactos percebidos tardiamente e esforço médio de 3,9/5 para identificar impactos. Esses resultados justificam testar o valor da organização de relações, do histórico e do fluxo de revisão. Eles não comprovam, por si só, demanda comercial nem ganhos quantitativos do produto.

---

## 2. Parte B — Business Model Canvas

O Canvas a seguir consolida nove blocos do modelo de negócio. Os elementos que ainda não foram testados são formulados como hipóteses de trabalho. A proposta comercial parte de um MVP gratuito para fins acadêmicos e não pressupõe receitas durante a etapa de desenvolvimento.

<img width="1038" height="520" alt="image" src="https://github.com/user-attachments/assets/dcde2556-be73-4342-85bd-47bf4f197366" />


### 2.1 Hipótese inicial de monetização
Como o MVP será desenvolvido e utilizado no contexto acadêmico, não se propõe cobrar pelo protótipo. Para uma eventual continuidade comercial, a hipótese a investigar é um plano freemium ou assinatura por equipe/projeto, pois o benefício é utilizado coletivamente ao longo da manutenção do software. Antes de escolher o modelo, será necessário investigar quem controla o orçamento, quais funcionalidades justificariam pagamento, alternativas disponíveis, custos de operação e sensibilidade a preço. Não se atribui preço nesta etapa.

---

## 3. Parte C — Personal Business Model Canvas

| Dimensão | Aplicação ao time ATHENA |
| :--- | :--- |
| Competências | Responsabilidades propostas no grupo: design e prototipação; organização de dados e apoio à estruturação da LLM; back-end e regras de negócio; front-end; levantamento de requisitos, entrevistas, documentação e gestão do produto. |
| Recursos disponíveis | Repositório GitHub do ATHENA; Entregas 1 e 2; Livro de Arquitetura; resultados do questionário e registros de interações; ferramentas de desenvolvimento e ambiente acadêmico. |
| Conhecimentos | Conhecimentos e frentes de aprendizagem relacionados a Engenharia de Requisitos, design de interface, desenvolvimento web, modelagem de dados, integração de sistemas e assistência por IA. A profundidade individual deve ser confirmada conforme as tarefas reais. |
| Rede de contatos | Rede acadêmica e profissional usada para alcançar participantes; integrantes do contexto de requisitos do GEDAI; docente e ambiente da UFLA. Esses contatos apoiam pesquisa e feedback, sem serem tratados como clientes ou parceiros comerciais confirmados. |
| Limitações | A implementação ainda não começou; a amostra de validação é pequena e exploratória; disposição a pagar e canal comercial não foram testados; a experiência técnica específica e a disponibilidade semanal de cada integrante não estão quantificadas no material. |
| Apoios | Orientação da disciplina; feedback de participantes; ferramentas e documentação técnica; serviços externos configuráveis para a demonstração de LLM. O acesso, os custos e as condições de cada apoio externo precisam ser confirmados. |
| Benefícios esperados | Aprender a transformar evidências de problema em produto; construir e demonstrar um MVP; exercitar colaboração, desenvolvimento e gestão de produto; avaliar a utilidade de uma abordagem de rastreabilidade; obter base para decidir se a proposta merece continuidade. |
| Esforços necessários | Concluir a fatia funcional principal; integrar componentes e regras de acesso; controlar escopo e dependências; testar com usuários; registrar erros e decisões; avaliar MCP/RAG; documentar execução, limitações, riscos e contribuições individuais. |

---

## 4. Parte D — Visão do produto de software

### 4.1 Identificação e descrição
Nome provisório: ATHENA.  
O ATHENA é uma plataforma web de gestão, rastreabilidade e revisão de requisitos de software. Ela organiza documentos e requisitos identificáveis, registra suas relações e versões, permite visualizar dependências e encaminha alterações para revisão antes da publicação de uma versão no Livro de Requisitos. A assistência por IA ocorre por meio de agente externo conectado por MCP; a plataforma recupera contexto autorizado e fornece evidências para sugestões, mas não decide nem publica automaticamente em nome da equipe.

| Elemento | Definição proposta |
| :--- | :--- |
| Usuário principal | Analista de requisitos ou Product Owner que precisa revisar requisitos e coordenar uma alteração. |
| Usuários complementares | Desenvolvedores, profissionais de QA/testes, líderes técnicos e revisores autorizados. |
| Objetivo principal | Permitir que a equipe consulte relações e histórico, identifique os itens que podem precisar de revisão e controle a atualização da especificação. |
| Resultado esperado | Requisitos estruturados, relações consultáveis, histórico preservado e fluxo de revisão/publicação rastreável. |
| Limite de acesso | O Livro de Requisitos inicial é restrito aos membros autorizados do projeto; compartilhar o endereço não concede acesso. |

### 4.2 Jornada principal do usuário
A jornada central prioriza a rastreabilidade e a manutenção controlada. A integração MCP/RAG é um ramo complementar para a demonstração acadêmica, e não um pré-requisito para cada operação manual de requisito.

| Etapa | Ação e valor esperado |
| :--- | :--- |
| 1. Entrar | Autenticar-se e abrir um workspace/projeto para o qual possui acesso. |
| 2. Registrar | Criar ou editar um requisito com identificador, conteúdo e organização por épico. |
| 3. Relacionar | Registrar relações válidas entre requisitos e visualizar o mapa de rastreabilidade. |
| 4. Alterar e avaliar | Editar uma candidata, consultar histórico e examinar quais itens relacionados podem demandar revisão. |
| 5. Revisar | Submeter a candidata, receber decisão humana e corrigir ou aprovar conforme o fluxo. |
| 6. Publicar e consultar | Publicar explicitamente a versão aprovada e permitir consulta no Livro apenas a membros autorizados. |
| 7. Apoio opcional por IA | Solicitar ao agente MCP uma consulta/sugestão com contexto recuperado e referências; a pessoa avalia antes de registrar qualquer mudança oficial. |

### 4.3 Wireframes iniciais
Os wireframes abaixo são esboços funcionais, não protótipos validados. Foram construídos para tornar visíveis os principais componentes da jornada: lista de requisitos, status e versões; detalhe do requisito, relações registradas, histórico e ação de submissão para revisão. A interface final deverá ser testada com usuários e alinhada às regras de permissão da arquitetura.

<img width="724" height="534" alt="image" src="https://github.com/user-attachments/assets/ba9e81a1-a269-4405-99f0-05b108174066" />
<img width="950" height="526" alt="image" src="https://github.com/user-attachments/assets/5e849161-1f54-4057-8a7c-9aabd32fde8f" />
<img width="946" height="545" alt="image" src="https://github.com/user-attachments/assets/33bee16c-db27-47bc-9da7-90b81a6ba529" />
<img width="941" height="488" alt="image" src="https://github.com/user-attachments/assets/d1d593d6-ff90-4fe7-8ff6-26d458600902" />
<img width="949" height="501" alt="image" src="https://github.com/user-attachments/assets/8d8ac0a2-8bdb-4396-b183-23aa35a0a06e" />
<img width="942" height="499" alt="image" src="https://github.com/user-attachments/assets/7f77212f-06e8-4a63-a01d-af64e4f04db3" />


### 4.4 Funcionalidades candidatas
- Gerenciamento de workspaces, projetos, membros e níveis de acesso (papéis e permissões).
- Criação e edição de requisitos estruturados por épico, contendo código identificador único, origem, responsável, status e histórico de versionamento.
- Mapeamento de relacionamentos entre requisitos com visualização gráfica da matriz de rastreabilidade.
- Sistema de comentários, notificações internas e acompanhamento de alterações relevantes.
- Fluxo de submissão de versões para revisão, decisão humana e publicação formal das versões aprovadas no Livro de Requisitos restrito.
- Ingestão e recuperação de contexto autorizado via MCP/RAG para análise de impacto assistida por IA, fornecendo sugestões fundamentadas em evidências sujeitas à validação humana.
- Funcionalidades futuras/extensíveis: conectores externos adicionais, sincronização bidirecional, exportação avançada de relatórios e gestão de planos comerciais.

---

## 5. Parte E — Priorização das funcionalidades

| Prioridade | Funcionalidade | Delimitação | Justificativa |
| :--- | :--- | :--- | :--- |
| Essencial para o MVP | Acesso, projetos e papéis mínimos | Restringir dados ao projeto e possibilitar uso por uma equipe-piloto. | Núcleo de funcionamento e segurança. |
| Essencial para o MVP | Cadastro/edição de requisitos, organização por épico, identificador, responsável e histórico | Criar requisitos de teste e acompanhar versões. | Base para rastreabilidade e manutenção. |
| Essencial para o MVP | Relações DEPENDE_DE, BLOQUEIA e RELACIONADO_A; mapa de rastreabilidade | Criar, consultar e validar relações entre requisitos do mesmo projeto. | Ataca diretamente a dificuldade validada. |
| Essencial para o MVP | Revisão e publicação explícitas; Livro restrito | Fluxo básico rascunho → submissão → decisão humana → publicação; consultar versão publicada. | Mantém consistência e histórico da especificação. |
| Essencial para o MVP | Comentários e notificações internas | Apoiar coordenação da revisão e sinalização de mudanças. | Previsto no Livro de Arquitetura para o MVP. |
| Essencial acadêmico | Demonstração MCP/RAG com consulta a dados reais e LLM externo | Executar perguntas com/sem recuperação, registrar referências, latência e estimativa de custo; avaliar mecanismos de resiliência. | Necessidade técnica acadêmica, com risco de integração; não substitui o caminho manual. |
| Importante, pode esperar | Conector funcional e amplo com Google Docs/Drive ou OpenProject | Pode começar por importação explícita ou amostra limitada, se houver capacidade e exigência acadêmica. | A arquitetura prevê serviço Integrações; a extensão do conector precisa ser confirmada com as restrições dos cursos. |
| Importante, pode esperar | Templates avançados, indicadores de cobertura ampliados, exportação sofisticada | Aprimorar padronização e portabilidade após o fluxo principal. | Não é necessário para o primeiro teste de rastreabilidade. |
| Futuro | Publicação pública do Livro, edição em tempo real, sincronização bidirecional com mesclagem automática, conectores adicionais | Considerar após validação e maturidade técnica. | Explicitamente tratado como evolução na arquitetura. |
| Fora do escopo do MVP acadêmico | Cobrança real, gestão completa de planos e faturamento; aprovar automaticamente sugestões de IA; aplicativos móveis nativos | Não implementar nesta etapa. | Não é necessário para testar a hipótese principal e aumenta risco de escopo. |

---

## 6. Parte F — Escopo do MVP

### 6.1 Hipótese que será testada
Se uma equipe de software registrar requisitos e suas relações em um espaço comum, consultar um mapa de rastreabilidade e acompanhar histórico e revisão, então seus integrantes conseguirão identificar de forma mais clara quais itens relacionados devem ser reconsiderados após uma mudança, sem depender exclusivamente de buscas manuais e da memória individual. O MVP testa a utilidade percebida e a capacidade de completar a tarefa; não assume antecipadamente redução de custo ou retrabalho.

### 6.2 Fluxo principal de ponta a ponta

| Passo | Resultado mínimo demonstrável |
| :--- | :--- |
| Acesso | Usuário autorizado abre o projeto-piloto. |
| Requisito | Cadastra três ou mais requisitos de exemplo, organizados e identificados. |
| Relações | Registra dependências e consulta o mapa. |
| Mudança | Edita uma candidata e consulta histórico/itens relacionados. |
| Revisão | Envia para revisão; revisor aprova ou rejeita com decisão registrada. |
| Livro | Versão aprovada é publicada explicitamente e fica acessível a membros autorizados. |

Fluxo complementar da demonstração acadêmica: um agente externo conectado via MCP consulta contexto real autorizado; o pipeline RAG recupera fontes e a chain chama um LLM externo configurável. A resposta mostra evidências e limitações; nenhuma relação oficial ou publicação é criada automaticamente. Essa demonstração deverá ser avaliada à parte do fluxo central.

### 6.3 O que será implementado
- Uma aplicação web de demonstração com acesso por projeto e papéis mínimos necessários para editar, revisar, aprovar e publicar.
- Cadastro, consulta e edição de requisitos com identificador, organização por épico, versão e histórico.
- Criação e consulta de relações pelos três tipos definidos na arquitetura e mapa visual com limites adequados ao conjunto de teste.
- Fluxo controlado de submissão, decisão humana, comentários/notificações internas e publicação explícita no Livro privado.
- Cliente de demonstração MCP/Python integrado a recuperação de contexto e LLM externo, conforme viabilidade e exigências acadêmicas; perguntas comparativas, mecanismos de resiliência, latência e custo serão registrados.

### 6.4 O que não será implementado nesta versão
- Livro público, acesso por link sem autenticação ou publicação pública de documentos.
- Edição simultânea em tempo real, sincronização bidirecional com mesclagem automática e conjunto amplo de conectores externos.
- Aplicação móvel nativa, cobrança real, assinatura comercial, administração financeira e plano de suporte comercial.
- Decisões automáticas de aprovação, publicação ou confirmação de relações sugeridas por IA.

### 6.5 Por que esse escopo é suficiente
O fluxo permite observar a hipótese principal: se relações explícitas, histórico e revisão facilitam localizar elementos relacionados e encaminhar uma alteração. Não é necessário substituir toda a documentação nem conectar todos os provedores para testar essa hipótese. A parte MCP/RAG será demonstrada separadamente devido ao planejamento acadêmico, mantendo o caminho manual sempre disponível.

### 6.6 Evidências que indicarão sucesso

| Dimensão | Evidência a coletar |
| :--- | :--- |
| Conclusão de tarefa | Testar com pelo menos cinco representantes do público-alvo, sempre que viável. Registrar quem conclui o fluxo de criar/relacionar/revisar/publicar sem ajuda crítica. |
| Identificação de impactos | Solicitar que cada participante altere um requisito de um conjunto de demonstração e identifique quais outros itens precisam ser analisados. Registrar omissões, erros e tempo. |
| Percepção de utilidade | Perguntar qual parte foi útil, em que situação utilizaria, o que faltou e o que deveria ser removido; registrar intenção de reutilização sem induzir respostas. |
| Robustez funcional mínima | Verificar permissões, tentativas de acesso indevido, preservação do conteúdo publicado, rejeição/novo envio e tratamento de estados inválidos. |
| Demonstração IA | Comparar pelo menos cinco perguntas com e sem recuperação, registrar referências recuperadas, avaliação humana da relevância, falhas induzidas, resiliência, latência e estimativa de custo por operação. |

---

## 7. Parte G — Planejamento técnico mínimo

| Item | Planejamento proposto |
| :--- | :--- |
| Tipo de aplicação | Plataforma web com duas interfaces: Gestão e Livro de Requisitos. |
| Front-end | Angular e TypeScript |
| Serviços de domínio | Seis serviços planejados: Projetos e acesso; Requisitos; Rastreabilidade; Revisões; Integrações; Análise de Impacto/MCP. Os cinco primeiros usam Java 17 e Spring Boot; Análise de Impacto/MCP usa Python. |
| Entrada e composição | Spring Cloud Gateway e dois BFFs, com APIs REST/HATEOAS para as interfaces. O agente acessa o endpoint MCP pelo Gateway. |
| Persistência | Seis instâncias PostgreSQL independentes, uma por domínio, com pgvector na instância de Análise de Impacto/MCP. BFFs sem banco próprio. |
| Comunicação assíncrona | RabbitMQ; outbox e CQRS; SAGA orquestrada na submissão, envolvendo Requisitos, Rastreabilidade e Revisões, com compensações explícitas. |
| IA | Embeddings locais para indexação/recuperação; LLM remoto configurável invocado pelo cliente externo Python com LangChain. Provedor, credenciais e custo efetivo ainda precisam ser definidos e medidos; a aceitação acadêmica do cliente permanece pendente. |
| Execução inicial | O Livro de Arquitetura prevê ambiente local com Docker Compose e volumes persistentes. Hospedagem pública do MVP não está definida; Kubernetes consta no desenho de operação e deve ser priorizado conforme as exigências acadêmicas e a capacidade da equipe. |
| Integrações externas | MCP/LLM externo para a demonstração; conectores adicionais de documentação/gestão são evoluções. A extensão do serviço Integrações no MVP deve ser compatibilizada com os requisitos técnicos da disciplina de Sistemas Distribuídos. |

### 7.1 Riscos técnicos e respostas previstas

| Risco | Consequência possível | Mitigação proposta |
| :--- | :--- | :--- |
| Superdimensionamento | Seis serviços, persistência separada, Gateway/BFFs, mensageria e IA aumentam o esforço de integração a partir de uma base ainda não implementada. | Construir cedo uma fatia vertical funcional; priorizar tarefas críticas; não adicionar conectores ou refinamentos antes do fluxo principal. |
| Autorização e exposição de dados | Gestão, Livro e agente MCP precisam respeitar permissões; uma resposta gerada não deve revelar conteúdo sem autorização. | Aplicar autorização no serviço de domínio, limitar contexto por projeto, testar acessos negativos e exigir confirmação humana para decisões/publicação. |
| Consistência e processamento distribuído | Mensagens duplicadas/atrasadas, projeções defasadas e compensações podem deixar estados intermediários. | Usar idempotência, estado persistido, logs, testes de falha e não declarar sucesso/desfazimento sem confirmação. |
| Qualidade da recuperação/LLM | Sugestões podem omitir contexto, usar fontes desatualizadas ou gerar informação incorreta; latência e custo não estão medidos. | Manter operação manual; apresentar referências; comparar perguntas com/sem RAG; registrar falhas, latência e custo; não autoaplicar sugestões. |
| Dependências externas | Disponibilidade do provedor de LLM e compatibilidade de clientes MCP/troca de tokens não foram comprovadas. | Usar provedor configurável, tratar timeout/fallback e validar integração antes de depender dela no fluxo principal. |
| Prazo e validação de usuários | A implementação não iniciou e a janela até a Entrega 4 é curta. | Fechar escopo, acompanhar marcos diários, preparar conjunto de dados de teste e agendar participantes em paralelo ao desenvolvimento. |

---

## 8. Plano de desenvolvimento

| Período | Atividade | Frentes responsáveis (proposta) | Saída verificável |
| :--- | :--- | :--- | :--- |
| 26–27/10 | Fechar escopo, hipótese, wireframes, critérios de sucesso, backlog e contratos mínimos. | José Vítor coordena requisitos/validação; Ana Clara wireframes; equipe revisa riscos. | Escopo versionado e tarefas no GitHub. |
| 28–30/10 | Preparar ambiente, autenticação/projeto e cadastro/edição de requisitos com persistência. | Fernando lidera back-end; Jhennifer front-end; Bárbara apoia dados; José Vítor valida critérios. | Criar, consultar e editar requisito em ambiente integrado. |
| 31/10–02/11 | Implementar identificadores, histórico, relações e mapa de rastreabilidade. | Back-end e front-end em paralelo; Ana Clara revisa legibilidade. | Demonstrar relações entre itens e inspeção do mapa. |
| 03–05/11 | Concluir submissão, decisão humana, comentários/notificações e publicação no Livro restrito. | Fernando/Jhennifer com definição de regras por José Vítor; Ana Clara revisa fluxos. | Fluxo manual central funcional de ponta a ponta. |
| 06–07/11 | Integrar MCP/RAG com contexto real autorizado; preparar conjunto de perguntas, resiliência e medições. | Bárbara apoia dados/LLM; Fernando integra serviços; equipe valida acesso e evidências. | Demonstração IA registrada, se compatível com as dependências técnicas. |
| 08–09/11 | Testes integrados, correção de falhas críticas, sessões com usuários e documentação de resultados. | José Vítor coordena testes; todos corrigem falhas de suas frentes. | Registros de tarefas, erros, percepção de valor e decisões. |
| 10/11 | Preparar demonstração e pacote de entrega. | Todos os integrantes, com responsabilidades registradas. | MVP demonstrável, instruções, capturas e evidências. |

---

## 9. Plano de validação do MVP

### 9.1 Participantes e método
Planeja-se testar o MVP com pelo menos cinco representantes do público-alvo, sempre que viável, conforme a orientação da disciplina. Priorizar pessoas que participem de projetos reais de software e trabalhem com requisitos ou com suas consequências em desenvolvimento e QA. Os participantes da Entrega 2 podem ser convidados novamente para comparação.

### 9.2 Roteiro da sessão
- Explicar que o teste avalia o sistema, não a pessoa; pedir consentimento e evitar coletar dados pessoais desnecessários.
- Apresentar um projeto de demonstração com requisitos fictícios, mas plausíveis, e relações previamente definidas.
- Solicitar que o participante encontre um requisito, identifique itens relacionados após uma mudança e explique quais revisaria.
- Solicitar que ele faça ou acompanhe a alteração, consulte histórico, submeta para revisão e verifique a versão publicada no Livro.
- Observar sem instrução excessiva; anotar conclusão, tempo aproximado, erro, dúvida e ajuda solicitada.
- Ao final, perguntar o que o sistema faz, qual parte foi valiosa, o que foi difícil, em que situação utilizaria e o que removeria ou acrescentaria.
- Registrar síntese por participante, decisões de produto e alterações priorizadas; não inventar comentários nem resultados.

### 9.3 Instrumentos e métricas

| Métrica | Como registrar | Por que importa |
| :--- | :--- | :--- |
| Conclusão da tarefa | Concluiu ou não cada etapa; onde precisou de ajuda. | Sinaliza se o fluxo central é compreensível. |
| Tempo e esforço | Tempo por etapa, dúvidas, erros e solicitações de ajuda. | Ajuda a comparar pontos de atrito entre sessões. |
| Rastreabilidade percebida | Itens relacionados identificados e omissões relevantes. | Observa se as relações ajudam a descobrir possíveis impactos. |
| Percepção de valor | Resposta aberta sobre utilidade, situação de uso e intenção de reutilização. | Explora valor percebido sem induzir resposta favorável. |
| Segurança e consistência | Tentativas de acesso negado, versão publicada preservada e estado após rejeição. | Verifica riscos críticos do fluxo. |
| Avaliação IA | Cinco perguntas com e sem RAG, relevância de fontes, limitações, falhas, latência e custo estimado. | Atende às validações planejadas na arquitetura, sem assumir qualidade antecipadamente. |

### 9.4 Como os resultados serão usados
Após os testes, devemos sintetizar padrões, divergências e problemas observados; decidir o que corrigir, remover ou deixar para depois; e registrar a relação entre evidência e mudança no GitHub. As decisões não devem ser tomadas apenas pela preferência dos integrantes ou por uma resposta positiva isolada. Como o MVP ainda não existe, esta seção descreve o plano; resultados serão adicionados depois da execução.

---

## 10. Decisões pendentes e questões para as próximas etapas

| Tema | Questão pendente |
| :--- | :--- |
| Validação comercial | Quem dentro da organização controla orçamento e é o comprador mais provável? |
| Monetização | Assinatura por equipe/projeto, freemium ou outro modelo? Quais benefícios justificariam pagamento e a que preço? |
| Segmento inicial de adoção | Equipes pequenas/médias, empresas juniores, laboratórios ou outro perfil? A opção de trabalho atual é equipes de desenvolvimento/manutenção. |
| Integrações | Qual é a menor integração viável que satisfaz os objetivos de arquitetura sem colocar em risco o fluxo principal? |
| Escopo técnico acadêmico | Validar com os docentes responsáveis quais componentes e demonstrações têm que estar funcionais até cada data, especialmente a integração completa MCP/RAG e o plano de execução. |
| Ambiente e custos | Escolher provedor de LLM, definir execução local/hospedada, medir latência/custo e verificar recursos disponíveis. |
| Critérios de sucesso | Confirmar os limiares propostos e registrar a razão das decisões antes de observar os resultados, para evitar ajustar a régua depois do teste. |

---

## 11. Conclusão
As evidências da Entrega 2 justificam prosseguir com ajustes e concentrar o ATHENA em rastreabilidade, histórico de mudanças e análise de impacto apoiada por relações consultáveis. A proposta de valor é coerente com o problema observado e com a arquitetura planejada, desde que a inteligência artificial permaneça complementar e submetida a supervisão humana.

O modelo de negócio ainda é uma hipótese, sobretudo quanto a quem pagará, qual modelo de receita será aceito e quais canais conseguirão atrair equipes. O MVP proposto busca testar primeiro o fluxo de registrar requisitos, consultar relações, acompanhar uma alteração e conduzir revisão/publicação. A demonstração MCP/RAG é mantida como atividade acadêmica paralela e deverá gerar métricas e evidências próprias. Nenhum resultado de execução ou de validação do MVP é declarado nesta entrega.
