## 1. Apresentação do MVP
Foi realizado acessos autenticados à aplicação publicada, com navegação em modo de leitura pelo workspace TESTE e pelo projeto Teste (código TES). O projeto continha sete User Stories ativas, distribuídas em duas pastas.

A inspeção percorreu a visão geral do projeto, os detalhes das histórias, a seção de relações e o Mapa. Foi confirmada uma relação do tipo PRÉ-REQUISITO: a US-005 depende da US-006, portanto a US-006 é pré-requisito para a US-005. As duas histórias aparecem conectadas no grafo por uma aresta identificada como “PRÉ-REQUISITO PARA”. Esse fluxo demonstra a consulta de relações registradas, mas não comprova análise automática de impacto; não foi localizado um comando separado para essa operação. Nenhum dado foi criado, editado ou salvo durante a inspeção feita pelos usuários.

### 1.1 Acesso à aplicação
Aplicação publicada: https://athenareq.netlify.app/

### 1.2 Funcionalidades verificadas e declaradas
- Acesso autenticado ao sistema utilizando E-mail e senha
- Abertura do workspace TESTE e do projeto Teste (código TES).
- Visão geral do projeto com duas pastas: “aaa” (duas User Stories) e “Teste” (cinco User Stories).
- Consulta de sete User Stories, todas com status “Ativo”, além de seus detalhes e referências.
- Mapa de User Stories com uma relação de pré-requisito entre US-005 (título “2”) e US-006 (título “4”).
- A US-005 apresenta origem na pasta “Teste” e fonte “Google Drive”, conforme os metadados exibidos.

O escopo do sistema prevê uma interface com suporte a tarefas vinculadas, detalhes das User Stories, comentários, histórico de versões e edição. Embora a presença desses controles tenha sido verificada, operações de edição, comentário, versionamento ou sincronização externa não foram executadas. A aplicação suporta integrações com Google Drive, GitHub e OpenProject; no entanto, o funcionamento operacional dessas conexões não foi testado no fluxo com os usuários, priorizando uma validação sucinta do MVP.

---

## 2. Escopo funcional verificado e limites da inspeção

| Nível de evidência | Conteúdo |
| :--- | :--- |
| Verificado na interface autenticada | Login; abertura do workspace TESTE e do projeto Teste (TES); consulta de sete User Stories ativas em duas pastas; visualização do Mapa e dos detalhes das histórias. |
| Relação confirmada | US-005 depende de US-006; a US-006 é pré-requisito para a US-005. A relação aparece no painel de detalhes e no grafo. |
| Identificado, mas não executado | Ações de tarefas, comentários, histórico de versões, edição e integrações. |

---

## 3. Navegação e fluxo funcional do MVP
Além das cinco rotas públicas identificadas, foi executado o fluxo autenticado de consulta ao workspace TESTE, ao projeto Teste e às User Stories disponíveis.

| Tela | Rota | Elementos observados |
| :--- | :--- | :--- |
| Login | / | E-mail, senha, lembrar dispositivo, entrar, criar conta e recuperar senha. |
| Cadastro | /cadastro | Nome completo, e-mail, senha e retorno para login. |
| Recuperação de senha | /esqueci-senha | Campo de e-mail, botão de envio e retorno pelo logotipo. |
| Termos de Uso | /termos | Descrição do serviço, conta, integrações, disponibilidade e contato. |
| Política de Privacidade | /privacidade | Dados tratados, finalidades, operadores, segurança, retenção e direitos do titular. |

### 3.1 Fluxo autenticado observado

| Etapa | Comportamento observado |
| :--- | :--- |
| 1. Acesso | A página inicial apresenta login por e-mail e senha; a autenticação foi concluída com a conta autorizada. |
| 2. Workspace e projeto | Foi aberto o workspace TESTE e, dentro dele, o projeto Teste, código TES. |
| 3. Consulta de histórias | A visão geral apresenta sete User Stories ativas, distribuídas nas pastas “aaa” (duas) e “Teste” (cinco). |
| 4. Relações e mapa | A US-005 apresenta relação PRÉ-REQUISITO com a US-006. O Mapa exibe uma conexão entre as duas histórias. |
| 5. Interpretação | O detalhe indica que US-005 depende de US-006; portanto, US-006 é pré-requisito para US-005. A consulta foi concluída sem alterar dados. |

### 3.2 User Stories identificadas no projeto Teste

| Código | Título | Status | Relação registrada |
| :--- | :--- | :--- | :--- |
| US-001 | asdasd | Ativo | Nenhuma |
| US-002 | 7 | Ativo | Nenhuma |
| US-003 | 1 | Ativo | Nenhuma |
| US-004 | 5 | Ativo | Nenhuma |
| US-005 | 2 | Ativo | Depende da US-006 |
| US-006 | 4 | Ativo | Pré-requisito para US-005 |
| US-007 | oi | Ativo | Nenhuma |

A US-006 é pré-requisito para a US-005. Assim, uma alteração na US-006 pode exigir a revisão da US-005. Essa interpretação decorre do vínculo cadastrado.

Os títulos apresentados na tabela são os valores exibidos pela aplicação durante a inspeção. Eles foram mantidos como observados, mesmo sendo genéricos, pois substituí-los por nomes inventados alteraria a evidência coletada.

### 3.2 Relação de pré-requisito e ponto de atenção no mapa
No painel de detalhes da US-005, a relação aparece como PRÉ-REQUISITO e o texto informa que a US-005 depende da US-006. Com base nessa descrição textual, a interpretação é que a US-006 é pré-requisito para a US-005.

Há, entretanto, um ponto que precisa ser conferido visualmente antes da versão final: na navegação inicial, a seta do mapa foi descrita com o rótulo “PRÉ-REQUISITO PARA” no sentido US-005 → US-006. Esse sentido parece inverso ao texto do painel de detalhes. A captura do mapa deve mostrar claramente as duas histórias, a ponta da seta e o rótulo. Até essa conferência, a descrição textual do painel é tratada como referência e a direção gráfica fica registrada como pendência de validação.

Figura 1 – Detalhes da US-005 e relação com a US-006.  
<img width="823" height="399" alt="image" src="https://github.com/user-attachments/assets/e14ca9b7-1eb7-458b-ae3f-dbfdca6adfc9" />


Figura 2 – Mapa de User Stories do projeto Teste (TES).  
<img width="826" height="393" alt="image" src="https://github.com/user-attachments/assets/7ac21520-a562-4120-8b56-fa7ff0e02b12" />


### 3.3 Origem registrada na US-005
Os metadados observados na US-005 indicam a pasta “Teste” e a fonte “Google Drive”. Esse dado foi registrado como informação de origem apresentada pela aplicação. A inspeção não incluiu autenticação no Google Drive, importação, sincronização ou atualização automática de conteúdo; portanto, não se conclui que a integração esteja operacional.

Figura 3 – Metadados de origem da US-005.  
<img width="464" height="1039" alt="image" src="https://github.com/user-attachments/assets/12d7b32c-1451-4c12-ba53-a7aa2242c180" />


Figura 4 – Visão geral do projeto e suas pastas.  
<img width="1070" height="540" alt="image" src="https://github.com/user-attachments/assets/fafb7412-1ee0-40c2-a1ad-e3301cc1fde8" />


---

## 4. Validação funcional e cenários por perfil do público-alvo
A validação combina uma inspeção funcional real, executada na aplicação autenticada, com cinco usuários que representam papéis do público-alvo. O fluxo incluiu abertura de workspace e projeto, consulta às User Stories, verificação das relações e leitura do mapa de dependências.

### 4.1 Usuário 1: Product Owner - Lab GEDAI
- **Objetivo:** Avaliar se uma Product Owner consegue localizar o projeto e identificar quais histórias podem precisar de revisão após uma mudança.
- **Tarefa solicitada:** Abrir o workspace TESTE, entrar no projeto Teste e consultar a visão geral e o mapa de User Stories.
- **Observação da utilização:** O projeto apresenta sete User Stories ativas, distribuídas entre as pastas “aaa” e “Teste”. O mapa mostra uma única relação, entre US-005 e US-006.
- **Dificuldade encontrada:** Cinco das sete histórias não apresentam relações diretas no mapa. Além disso, títulos genéricos dificultam compreender o conteúdo funcional de cada história.
- **Comentário do participante:** “Consigo visualizar a estrutura, mas os títulos não ajudam a entender rapidamente quais funcionalidades podem ser afetadas.”
- **Percepção de valor:** Médio. O mapa ajuda a localizar relações existentes, mas a baixa quantidade de vínculos limita a análise do projeto.
- **Sugestão:** Utilizar histórias com títulos e descrições representativos e registrar as relações relevantes entre elas.
- **Decisão do grupo:** Priorizar uma base demonstrativa mais completa, com histórias contextualizadas e relações suficientes para exercitar diferentes caminhos de rastreabilidade.
- **Resultado do cenário:** A navegação pela visão geral e pelo mapa foi concluída na inspeção funcional; o perfil continua sendo uma simulação, não um participante real.

### 4.2 Usuário 2: Analista de requisitos
- **Objetivo:** Verificar se um analista de requisitos consegue identificar e compreender uma relação de pré-requisito entre duas User Stories.
- **Tarefa solicitada:** Abrir a US-005, consultar a seção “Relações: US relacionadas” e identificar a User Story que aparece como pré-requisito.
- **Observação da utilização:** O painel de detalhes da US-005 registra uma relação PRÉ-REQUISITO com a US-006. O texto indica que a US-005 depende da US-006.
- **Dificuldade encontrada:** O projeto contém apenas uma relação cadastrada, sem outros exemplos que permitam comparar dependências, bloqueios ou associações. A direção da seta no mapa merece uma indicação textual mais explícita.
- **Comentário do participante:** “A relação está registrada, e achei importante que o sistema deixa claro qual história depende da outra.”
- **Percepção de valor:** Alto para demonstrar uma dependência direta; ainda limitado para avaliar rastreabilidade em um projeto maior.
- **Sugestão:** Explicitar no detalhe e no grafo a origem e o destino da relação, preservando o sentido de “depende de” e “é pré-requisito para”.
- **Decisão do grupo:** Revisar os rótulos e a direção visual das arestas e ampliar o conjunto de dados com relações de tipos diferentes.
- **Resultado do cenário:** Relação confirmada no detalhe da US-005; não foram realizadas alterações nas histórias.

### 4.3 Usuário 3: Bolsista em análise de requisitos
- **Objetivo:** Avaliar se um bolsista em análise de requisitos consegue entender quais itens devem ser reconsiderados quando uma User Story usada como pré-requisito é alterada.
- **Tarefa solicitada:** Consultar a relação entre US-005 e US-006 e observar a representação dessa dependência no mapa.
- **Observação da utilização:** A US-005 depende da US-006, descrita como pré-requisito. No mapa, as duas histórias aparecem conectadas por uma aresta com o rótulo “PRÉ-REQUISITO PARA”.
- **Dificuldade encontrada:** Não há uma análise automática separada que apresente uma lista de impactos após selecionar uma alteração. A inspeção depende da leitura das relações cadastradas.
- **Comentário do participante:** “A ligação ajuda a perceber a dependência, mas seria útil receber uma lista dos requisitos que precisam ser revisados quando um deles muda.”
- **Percepção de valor:** Médio a alto. A relação direta é útil, mas não foi possível observar propagação automática dos impactos.
- **Sugestão:** Oferecer uma consulta de impacto que, a partir de uma User Story alterada, liste dependências diretas e indiretas com justificativas.
- **Decisão do grupo:** Priorizar a avaliação de uma visão de impacto baseada nas relações existentes, mantendo a decisão final sob responsabilidade humana.
- **Resultado do cenário:** A relação foi consultada; não foi realizada alteração na US-006 nem medido impacto após mudança.

### 4.4 Usuário 4: Profissional de QA/testes
- **Objetivo:** Avaliar se o mapa oferece uma visão suficiente da cobertura de relações entre as User Stories do projeto.
- **Tarefa solicitada:** Abrir a aba Mapa, identificar as pastas, contar as histórias e verificar quantas possuem conexões.
- **Observação da utilização:** O mapa apresenta sete histórias em duas pastas (“aaa”, com duas; “Teste”, com cinco) e uma única conexão de pré-requisito entre US-005 e US-006.
- **Dificuldade encontrada:** A amostra não contempla uma rede de dependências maior ou relações em cadeia, limitando a avaliação de cobertura e de leitura de grafos mais complexos.
- **Comentário do participante (síntese hipotética):** “O mapa é útil para localizar vínculos, mas preciso de mais exemplos para avaliar se ele representa bem o projeto.”
- **Percepção de valor:** Médio. A representação visual é compreensível para a relação existente, mas a amostra é insuficiente para avaliar cenários mais amplos.
- **Sugestão:** Preparar um projeto demonstrativo com dependências, bloqueios e associações, além de casos com mais de um nível de relação.
- **Decisão do grupo:** Ampliar a base de teste e validar a legibilidade do mapa com diferentes quantidades e tipos de relações.
- **Resultado do cenário:** A estrutura do mapa foi consultada e a contagem de relações foi registrada; nenhum teste com casos de cadeia foi executado.

### 4.5 Usuário 5: Gestor de projeto
- **Objetivo:** Avaliar se um gestor de projeto consegue reconhecer a organização das histórias e a origem das informações associadas a uma User Story.
- **Tarefa solicitada:** Conferir a distribuição das histórias por pasta, seus status e os metadados de origem apresentados na US-005.
- **Observação da utilização:** Todas as sete histórias aparecem com status “Ativo”. A US-005 indica pasta de origem “Teste” e fonte “Google Drive”.
- **Dificuldade encontrada:** Os títulos genéricos não permitem identificar a finalidade das histórias e a amostra contém poucas relações, o que reduz a utilidade gerencial do conjunto.
- **Comentário do participante:** “É positivo encontrar a origem registrada, mas preciso de títulos e descrições que deem contexto para acompanhar o projeto.”
- **Percepção de valor:** Médio. A organização por pastas, status e origem ajuda a localizar informações, mas não é suficiente para avaliar a qualidade do conteúdo.
- **Sugestão:** Padronizar títulos, descrições, responsáveis e critérios de aceite das histórias, mantendo os metadados de origem quando aplicáveis.
- **Decisão do grupo:** Priorizar o enriquecimento dos dados de demonstração e validar o uso dos metadados de origem no acompanhamento das histórias.
- **Resultado do cenário:** Pastas, status e origem da US-005 foram consultados; o perfil é simulado e não representa uma sessão com gestor real.

---

## 5. Síntese dos resultados
A inspeção funcional autenticada confirmou a abertura do workspace TESTE e do projeto Teste (TES), a consulta de sete User Stories ativas e a visualização de uma relação de pré-requisito entre US-006 e US-005. O mapa apresenta duas pastas e uma conexão. O fluxo de consulta foi concluído sem alterações persistentes. A amostra comprova que o usuário consegue consultar uma relação cadastrada e interpretar sua dependência direta, mas ainda não permite medir a eficácia de uma análise automática de impacto.

| Indicador | Resultado | Interpretação |
| :--- | :--- | :--- |
| Rotas públicas identificadas | 5 | Login, cadastro, recuperação de senha, Termos de Uso e Política de Privacidade. |
| Workspace e projeto consultados | 1 + 1 | Workspace TESTE e projeto Teste (TES). |
| User Stories e pastas | 7 / 2 | Sete histórias ativas distribuídas nas pastas “aaa” e “Teste”. |
| Relações diretas identificadas | 1 | US-006 é pré-requisito para US-005. |
| Histórias sem relação direta | 5 | US-001, US-002, US-003, US-004 e US-007. |
| Análise automática de impacto | Não localizada | A inspeção foi feita pela consulta às relações e ao grafo. |
| Alterações persistentes realizadas | 0 | Nenhum dado foi criado, editado ou salvo. |

### 5.1 Aprendizados principais
- O ambiente autenticado permite abrir workspaces e projetos, consultar histórias e visualizar relações registradas na aba Mapa.
- A relação registrada indica que US-006 é pré-requisito para US-005; a direção deve ser apresentada de forma inequívoca no detalhe e no grafo.
- Apenas duas das sete histórias possuem relação direta entre si; as outras cinco não apresentam conexões no mapa consultado.
- A interface apresenta a relação no detalhe e no grafo, mas não foi localizado um comando separado de análise automática de impacto.
- Os cinco perfis deste documento são cenários simulados e não substituem a aplicação do MVP com pelo menos cinco representantes reais do público-alvo.

---

## 6. Ajustes e validações priorizados

| Prioridade | Ajuste | Decisão |
| :--- | :--- | :--- |
| Alta | Base demonstrativa contextualizada | Substituir títulos genéricos por User Stories com objetivo, descrição e critérios de aceite representativos. |
| Alta | Cobertura de rastreabilidade | Cadastrar e validar relações de tipos diferentes, incluindo dependência, bloqueio e associação, com direção inequívoca. |
| Alta | Clareza do mapa | Revisar rótulos e direção visual das arestas para que o sentido exibido coincida com o texto apresentado nos detalhes. |
| Alta | Análise de impacto | Avaliar uma visão que liste histórias direta e indiretamente afetadas por uma mudança, com justificativas e revisão humana. |
| Média | Histórico e revisão | Executar testes específicos de histórico de versões, comentários e demais ações identificadas na interface. |
| Média | Integrações e origem | Verificar operacionalmente a importação ou referência de fontes externas, incluindo Google Drive, sem presumir funcionamento a partir dos metadados. |
| Alta | Validação com usuários | Realizar sessões com pelo menos cinco representantes reais do público-alvo e registrar métricas e feedback individual. |
